package com.elvis.pregmap.ui.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

// Data class to hold the state for the sign-up UI
data class SignUpUiState(
    val firstName: String = "",
    val middleName: String = "",
    val surname: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isEmailSent: Boolean = false,
    val isEmailVerified: Boolean = false,
    val currentStep: Int = 1,
    val appCheckEnabled: Boolean = false
)

// ViewModel to hold the logic and state for the Email Sign-Up flow
class EmailSignUpViewModel(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState = _uiState.asStateFlow()
    
    private val emailAuthHelper = EmailAuthHelper(context)

    fun onFirstNameChange(newValue: String) {
        _uiState.update { it.copy(firstName = newValue) }
    }

    fun onMiddleNameChange(newValue: String) {
        _uiState.update { it.copy(middleName = newValue) }
    }

    fun onSurnameChange(newValue: String) {
        _uiState.update { it.copy(surname = newValue) }
    }

    fun onEmailChange(newValue: String) {
        _uiState.update { it.copy(email = newValue) }
    }

    fun onPasswordChange(newValue: String) {
        _uiState.update { it.copy(password = newValue) }
    }

    fun onConfirmPasswordChange(newValue: String) {
        _uiState.update { it.copy(confirmPassword = newValue) }
    }

    /**
     * Create user account with email and password
     */
    fun createAccount() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                
                // Check if App Check is enabled
                val appCheckEnabled = emailAuthHelper.isAppCheckEnabled()
                _uiState.update { it.copy(appCheckEnabled = appCheckEnabled) }
                
                // Validate input
                val validationResult = validateInput()
                if (!validationResult.isValid) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = validationResult.errorMessage
                        ) 
                    }
                    return@launch
                }
                
                // Check if email already exists
                val emailExists = emailAuthHelper.checkEmailExists(uiState.value.email)
                if (emailExists) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = "An account with this email already exists. Please use a different email or try signing in."
                        ) 
                    }
                    return@launch
                }
                
                // Create user data
                val userData = UserData.createEmailUser(
                    firstName = uiState.value.firstName,
                    middleName = uiState.value.middleName,
                    lastName = uiState.value.surname,
                    email = uiState.value.email
                )
                
                // Create account with Firebase
                val result = emailAuthHelper.createUserWithEmail(
                    email = uiState.value.email,
                    password = uiState.value.password,
                    userData = userData
                )
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isEmailSent = true,
                            currentStep = 4 // Move to verification step
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to create account"
                        ) 
                    }
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "An error occurred: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Check email verification status
     */
    fun checkEmailVerification() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                
                val isVerified = emailAuthHelper.checkEmailVerification()
                
                if (isVerified) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isEmailVerified = true,
                            currentStep = 5 // Move to success step
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Email not verified yet. Please check your email and click the verification link."
                        ) 
                    }
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to check verification status: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Resend verification email
     */
    fun resendVerificationEmail() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                
                val result = emailAuthHelper.resendVerificationEmail()
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            errorMessage = "Verification email sent! Please check your inbox."
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = result.exceptionOrNull()?.message ?: "Failed to resend verification email"
                        ) 
                    }
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Failed to resend verification email: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Reset state
     */
    fun resetState() {
        _uiState.value = SignUpUiState()
    }
    
    /**
     * Validate input fields
     */
    private fun validateInput(): ValidationResult {
        val state = uiState.value
        
        if (state.firstName.isBlank()) {
            return ValidationResult(false, "First name is required")
        }
        
        if (state.surname.isBlank()) {
            return ValidationResult(false, "Surname is required")
        }
        
        if (state.email.isBlank()) {
            return ValidationResult(false, "Email is required")
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            return ValidationResult(false, "Please enter a valid email address")
        }
        
        if (state.password.length < 8) {
            return ValidationResult(false, "Password must be at least 8 characters long")
        }
        
        if (state.password != state.confirmPassword) {
            return ValidationResult(false, "Passwords do not match")
        }
        
        return ValidationResult(true, null)
    }
    
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
} 