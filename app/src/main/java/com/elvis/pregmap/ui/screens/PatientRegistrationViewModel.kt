package com.elvis.pregmap.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt
import com.google.firebase.firestore.SetOptions

// Singleton to store patient ID across screens
object PatientDataStore {
    var verifiedPatientId: String? = null
        set(value) {
            println("🔍 PatientDataStore.verifiedPatientId being set to: $value")
            field = value
        }
        get() {
            println("🔍 PatientDataStore.verifiedPatientId being accessed: $field")
            return field
        }
}

// Represents the state of the registration process
sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val patientId: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class PatientRegistrationViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    companion object {
        var verifiedPatientId: String? = null
    }

    // Add a method to manually set patient ID for testing
    fun setPatientIdForTesting(patientId: String) {
        PatientDataStore.verifiedPatientId = patientId
        println("🧪 Test: Patient ID manually set to: $patientId")
    }

    fun verifyPatientDetails(phoneNumber: String, accessCode: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            println("🔍 Starting patient verification...")
            println("🔍 Phone Number: $phoneNumber")
            println("🔍 Access Code: $accessCode")
            
            try {
                val patientsCollection = db.collection("patients")
                
                // Ensure phone number has +254 prefix
                val formattedPhoneNumber = if (phoneNumber.startsWith("+254")) {
                    phoneNumber
                } else if (phoneNumber.startsWith("254")) {
                    "+$phoneNumber"
                } else if (phoneNumber.startsWith("0")) {
                    "+254${phoneNumber.substring(1)}"
                } else {
                    "+254$phoneNumber"
                }
                
                println("🔍 Searching for phone number: $formattedPhoneNumber")
                println("🔍 Collection: patients")
                println("🔍 Field path: contactInfo.phone")
                
                // Query for patient by phone number in contactInfo.phone field
                val querySnapshot = patientsCollection
                    .whereEqualTo("contactInfo.phone", formattedPhoneNumber)
                    .get()
                    .await()

                println("🔍 Query result size: ${querySnapshot.size()}")
                
                if (querySnapshot.isEmpty) {
                    // Let's also try without the +254 prefix to see if that's the issue
                    val alternativePhone = if (formattedPhoneNumber.startsWith("+254")) {
                        formattedPhoneNumber.substring(4)
                    } else {
                        formattedPhoneNumber
                    }
                    
                    println("🔍 Trying alternative format: $alternativePhone")
                    val alternativeQuery = patientsCollection
                        .whereEqualTo("contactInfo.phone", alternativePhone)
                        .get()
                        .await()
                    
                    println("🔍 Alternative query result size: ${alternativeQuery.size()}")
                    
                    if (alternativeQuery.isEmpty) {
                        _registrationState.value = RegistrationState.Error("No patient record found with this phone number. Searched for: $formattedPhoneNumber and $alternativePhone")
                        return@launch
                    } else {
                        // Use the alternative query results
                        var patientFound = false
                        for (document in alternativeQuery.documents) {
                            val plainAccessCode = document.getString("accessCode")
                            val hashedAccessCode = document.getString("accessCodeHash")
                            
                            val isMatch = (plainAccessCode != null && plainAccessCode == accessCode) ||
                                          (hashedAccessCode != null && BCrypt.checkpw(accessCode, hashedAccessCode))

                            if (isMatch) {
                                println("✅ Patient found with alternative phone format!")
                                println("✅ Document ID: ${document.id}")
                                
                                // Store patient ID in user's document
                                val userId = auth.currentUser?.uid
                                if (userId != null) {
                                    db.collection("users").document(userId)
                                        .set(mapOf("patientId" to document.id), SetOptions.merge())
                                        .await()
                                    println("✅ Patient ID stored in user document: ${document.id}")
                                }
                                
                                _registrationState.value = RegistrationState.Success(document.id)
                                PatientDataStore.verifiedPatientId = document.id
                                println("✅ Patient ID stored in PatientDataStore: ${PatientDataStore.verifiedPatientId}")
                                patientFound = true
                                break 
                            }
                        }

                        if (!patientFound) {
                            _registrationState.value = RegistrationState.Error("Invalid access code.")
                        }
                        return@launch
                    }
                }

                var patientFound = false
                for (document in querySnapshot.documents) {
                    println("🔍 Checking document: ${document.id}")
                    val plainAccessCode = document.getString("accessCode")
                    val hashedAccessCode = document.getString("accessCodeHash")
                    
                    val isMatch = (plainAccessCode != null && plainAccessCode == accessCode) ||
                                  (hashedAccessCode != null && BCrypt.checkpw(accessCode, hashedAccessCode))

                    if (isMatch) {
                        // We found the correct patient
                        println("✅ Patient found with primary phone format!")
                        println("✅ Document ID: ${document.id}")
                        
                        // Store patient ID in user's document
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            db.collection("users").document(userId)
                                .set(mapOf("patientId" to document.id), SetOptions.merge())
                                .await()
                            println("✅ Patient ID stored in user document: ${document.id}")
                        }
                        
                        _registrationState.value = RegistrationState.Success(document.id)
                        PatientDataStore.verifiedPatientId = document.id
                        println("✅ Patient ID stored in PatientDataStore: ${PatientDataStore.verifiedPatientId}")
                        patientFound = true
                        break 
                    }
                }

                if (!patientFound) {
                    _registrationState.value = RegistrationState.Error("Invalid access code.")
                }

            } catch (e: Exception) {
                println("❌ Error in verifyPatientDetails: ${e.message}")
                _registrationState.value = RegistrationState.Error("An error occurred: ${e.message}")
            }
        }
    }
} 