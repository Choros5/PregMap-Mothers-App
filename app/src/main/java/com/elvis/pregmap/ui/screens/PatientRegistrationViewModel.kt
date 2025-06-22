package com.elvis.pregmap.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt

// Represents the state of the registration process
sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val patientId: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class PatientRegistrationViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    fun verifyPatientDetails(idNumber: String, phoneNumber: String, accessCode: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            try {
                val patientsCollection = db.collection("patients")
                // Query for patient by phone number first
                val querySnapshot = patientsCollection
                    .whereEqualTo("phone", phoneNumber)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _registrationState.value = RegistrationState.Error("No patient record found with this phone number.")
                    return@launch
                }

                var patientFound = false
                for (document in querySnapshot.documents) {
                    val plainAccessCode = document.getString("accessCode")
                    val hashedAccessCode = document.getString("accessCodeHash")
                    
                    val isMatch = (plainAccessCode != null && plainAccessCode == accessCode) ||
                                  (hashedAccessCode != null && BCrypt.checkpw(accessCode, hashedAccessCode))

                    if (isMatch) {
                        // We found the correct patient
                        _registrationState.value = RegistrationState.Success(document.id)
                        patientFound = true
                        break 
                    }
                }

                if (!patientFound) {
                    _registrationState.value = RegistrationState.Error("Invalid access code.")
                }

            } catch (e: Exception) {
                _registrationState.value = RegistrationState.Error("An error occurred: ${e.message}")
            }
        }
    }
} 