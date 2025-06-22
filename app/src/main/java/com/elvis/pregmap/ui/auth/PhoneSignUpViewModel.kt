package com.elvis.pregmap.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PhoneSignUpUiState(
    val firstName: String = "",
    val middleName: String = "",
    val surname: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val verificationCode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val verificationId: String? = null,
    val resendToken: PhoneAuthProvider.ForceResendingToken? = null,
    val isCodeSent: Boolean = false,
    val isVerified: Boolean = false,
    val resendTimerSeconds: Int = 0,
    val canResendCode: Boolean = true
)

class PhoneSignUpViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PhoneSignUpUiState())
    val uiState: StateFlow<PhoneSignUpUiState> = _uiState.asStateFlow()
    
    private val phoneAuthHelper = PhoneAuthHelper()

    fun onFirstNameChange(firstName: String) {
        _uiState.value = _uiState.value.copy(firstName = firstName)
    }

    fun onMiddleNameChange(middleName: String) {
        _uiState.value = _uiState.value.copy(middleName = middleName)
    }

    fun onSurnameChange(surname: String) {
        _uiState.value = _uiState.value.copy(surname = surname)
    }

    fun onPhoneNumberChange(phoneNumber: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = phoneNumber)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }
    
    fun onVerificationCodeChange(code: String) {
        _uiState.value = _uiState.value.copy(verificationCode = code)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    private fun startResendTimer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                canResendCode = false,
                resendTimerSeconds = 60
            )
            
            for (i in 60 downTo 1) {
                delay(1000)
                _uiState.value = _uiState.value.copy(resendTimerSeconds = i)
            }
            
            _uiState.value = _uiState.value.copy(
                canResendCode = true,
                resendTimerSeconds = 0
            )
        }
    }
    
    fun sendVerificationCode() {
        val currentState = _uiState.value
        val fullPhoneNumber = "+254${currentState.phoneNumber}"
        
        if (currentState.phoneNumber.length < 9) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a valid phone number"
            )
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            phoneAuthHelper.sendVerificationCode(
                phoneNumber = fullPhoneNumber,
                onCodeSent = { verificationId, resendToken ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verificationId = verificationId,
                        resendToken = resendToken,
                        isCodeSent = true,
                        successMessage = "Verification code sent to $fullPhoneNumber"
                    )
                    // Start the resend timer after code is sent
                    startResendTimer()
                },
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error
                    )
                }
            )
        }
    }
    
    fun verifyCode() {
        val currentState = _uiState.value
        
        if (currentState.verificationCode.length != 6) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter the 6-digit verification code"
            )
            return
        }
        
        if (currentState.verificationId == null) {
            _uiState.value = currentState.copy(
                errorMessage = "No verification ID found. Please send code again."
            )
            return
        }
        
        if (currentState.password != currentState.confirmPassword) {
            _uiState.value = currentState.copy(
                errorMessage = "Passwords do not match"
            )
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            val userData = UserData.createPhoneUser(
                firstName = currentState.firstName,
                middleName = currentState.middleName,
                lastName = currentState.surname,
                phoneNumber = "+254${currentState.phoneNumber}"
            )
            
            phoneAuthHelper.verifyCodeAndSignIn(
                verificationId = currentState.verificationId!!,
                code = currentState.verificationCode,
                userData = userData,
                password = currentState.password,
                onSuccess = { verifiedUserData ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isVerified = true,
                        successMessage = "Phone number verified successfully! Welcome ${verifiedUserData.fullName}"
                    )
                },
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error
                    )
                }
            )
        }
    }
    
    fun resendCode() {
        val currentState = _uiState.value
        val fullPhoneNumber = "+254${currentState.phoneNumber}"
        
        if (!currentState.canResendCode) {
            return
        }
        
        if (currentState.resendToken == null) {
            _uiState.value = currentState.copy(
                errorMessage = "Cannot resend code. Please try sending a new code."
            )
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            phoneAuthHelper.resendCode(
                phoneNumber = fullPhoneNumber,
                token = currentState.resendToken!!,
                onCodeSent = { verificationId, resendToken ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        verificationId = verificationId,
                        resendToken = resendToken,
                        successMessage = "New verification code sent to $fullPhoneNumber"
                    )
                    // Start the resend timer after code is resent
                    startResendTimer()
                },
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error
                    )
                }
            )
        }
    }
} 