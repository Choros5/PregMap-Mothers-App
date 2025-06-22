package com.elvis.pregmap.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.mindrot.jbcrypt.BCrypt

// Represents the state of the PIN validation/creation
sealed class PinState {
    object Idle : PinState()
    object Loading : PinState()
    object Success : PinState()
    data class Error(val message: String) : PinState()
}

class PinViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val _pinState = MutableStateFlow<PinState>(PinState.Idle)
    val pinState: StateFlow<PinState> = _pinState

    fun savePin(pin: String) {
        viewModelScope.launch {
            _pinState.value = PinState.Loading
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _pinState.value = PinState.Error("User not logged in.")
                    return@launch
                }
                
                // Hash the PIN before storing
                val pinHash = BCrypt.hashpw(pin, BCrypt.gensalt())
                
                db.collection("users").document(userId)
                    .set(mapOf("pinHash" to pinHash))
                    .await()
                
                _pinState.value = PinState.Success
            } catch (e: Exception) {
                _pinState.value = PinState.Error("Failed to save PIN: ${e.message}")
            }
        }
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            _pinState.value = PinState.Loading
            try {
                val userId = auth.currentUser?.uid
                if (userId == null) {
                    _pinState.value = PinState.Error("User not logged in.")
                    return@launch
                }

                val userDoc = db.collection("users").document(userId).get().await()
                val storedHash = userDoc.getString("pinHash")

                if (storedHash == null) {
                    _pinState.value = PinState.Error("No PIN has been set for this account.")
                    return@launch
                }

                if (BCrypt.checkpw(pin, storedHash)) {
                    _pinState.value = PinState.Success
                } else {
                    _pinState.value = PinState.Error("Invalid PIN.")
                }
            } catch (e: Exception) {
                _pinState.value = PinState.Error("An error occurred: ${e.message}")
            }
        }
    }
} 