package com.elvis.pregmap.ui.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class PhoneAuthHelper {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    
    /**
     * Custom exception for phone authentication success without Firebase user
     */
    class PhoneAuthSuccessException(message: String) : Exception(message)
    
    /**
     * Hash password using SHA-256
     */
    private fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: NoSuchAlgorithmException) {
            // Fallback to simple hash if SHA-256 is not available
            password.hashCode().toString()
        }
    }
    
    /**
     * Check if a phone number already exists in the database
     */
    suspend fun isPhoneNumberExists(phoneNumber: String): Boolean {
        return try {
            val querySnapshot = firestore.collection("users")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .await()
            
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            // If there's an error checking, assume it doesn't exist to allow the flow to continue
            false
        }
    }
    
    /**
     * Sign in with phone number and password with access control
     * Implements the Phone Login Access Control system:
     * 1. Firestore Validation (phone number must exist, signInMethod must be "phone", password must match)
     * 2. Create Firebase authentication session
     * 3. Grant access if valid, show error if invalid
     */
    suspend fun signInWithPhoneAndPassword(
        phoneNumber: String,
        password: String
    ): Result<com.google.firebase.auth.FirebaseUser> {
        return try {
            // Format phone number: remove leading 0 and add +254
            val formattedPhone = formatPhoneNumber(phoneNumber)
            
            // Step 1: Firestore Validation
            val userDoc = firestore.collection("users")
                .whereEqualTo("phoneNumber", formattedPhone)
                .whereEqualTo("signInMethod", "phone")
                .limit(1)
                .get()
                .await()
            
            if (!userDoc.isEmpty) {
                val userData = userDoc.documents[0]
                val storedHashedPassword = userData.getString("password")
                val userId = userData.id
                val userEmail = userData.getString("email")
                
                // Hash the provided password for comparison
                val hashedPassword = hashPassword(password)
                
                // Check if password matches
                if (storedHashedPassword == hashedPassword) {
                    // ✅ Allowed: Update last sign in time
                    firestore.collection("users").document(userId)
                        .update("lastSignInAt", java.util.Date())
                        .await()
                    
                    // Try to get existing Firebase user
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        return Result.success(currentUser)
                    }
                    
                    // If no current user, try to sign in with email if available
                    if (userEmail != null && userEmail.isNotEmpty()) {
                        try {
                            val authResult = auth.signInWithEmailAndPassword(userEmail, password).await()
                            if (authResult.user != null) {
                                return Result.success(authResult.user!!)
                            }
                        } catch (e: Exception) {
                            // Email/password auth failed, continue with phone auth
                        }
                    }
                    
                    // Try to create a Firebase session using anonymous authentication
                    try {
                        val authResult = auth.signInAnonymously().await()
                        if (authResult.user != null) {
                            // Update the anonymous user's metadata to link it to the phone user
                            firestore.collection("users").document(authResult.user!!.uid)
                                .set(mapOf(
                                    "phoneNumber" to formattedPhone,
                                    "signInMethod" to "phone",
                                    "originalUserId" to userId,
                                    "lastSignInAt" to java.util.Date(),
                                    "isAnonymous" to true
                                ))
                                .await()
                            
                            return Result.success(authResult.user!!)
                        }
                    } catch (e: Exception) {
                        // Anonymous auth failed
                    }
                    
                    // If all else fails, throw an exception indicating success but no Firebase user
                    throw PhoneAuthSuccessException("Phone authentication successful")
                } else {
                    // ❌ Denied: Wrong password
                    Result.failure(Exception("Invalid phone number or password"))
                }
            } else {
                // ❌ Denied: Phone number not found in Firestore
                Result.failure(Exception("You are not registered. Please sign up first."))
            }
        } catch (e: PhoneAuthSuccessException) {
            // This is a success case, but we need to return a FirebaseUser
            // Try to create an anonymous session one more time
            try {
                val authResult = auth.signInAnonymously().await()
                if (authResult.user != null) {
                    Result.success(authResult.user!!)
                } else {
                    Result.failure(Exception("Authentication successful but unable to create Firebase session"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Authentication successful but unable to create Firebase session"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Format phone number: remove leading 0 and add +254
     */
    private fun formatPhoneNumber(phoneNumber: String): String {
        val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
        return if (cleanPhone.startsWith("0")) {
            "+254${cleanPhone.substring(1)}"
        } else if (cleanPhone.startsWith("254")) {
            "+$cleanPhone"
        } else {
            "+254$cleanPhone"
        }
    }

    /**
     * Send OTP for phone signup (for new users)
     */
    suspend fun sendVerificationCode(
        phoneNumber: String,
        onCodeSent: (String, PhoneAuthProvider.ForceResendingToken) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val formattedPhone = formatPhoneNumber(phoneNumber)
            
            // First check if phone number already exists
            if (isPhoneNumberExists(formattedPhone)) {
                onError("This phone number is already registered. Please use a different number or try logging in.")
                return
            }
            
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification completed (SMS received automatically)
                    // This is handled in the verification step
                }
                
                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    onError("Verification failed: ${e.message}")
                }
                
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId, token)
                }
            }
            
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbacks)
                .build()
            
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            onError("Failed to send verification code: ${e.message}")
        }
    }
    
    suspend fun verifyCodeAndSignIn(
        verificationId: String,
        code: String,
        userData: UserData,
        password: String,
        onSuccess: (UserData) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = auth.signInWithCredential(credential).await()
            
            result.user?.let { user ->
                // Double-check if phone number already exists before storing
                val formattedPhone = formatPhoneNumber(userData.phoneNumber)
                if (isPhoneNumberExists(formattedPhone)) {
                    onError("This phone number is already registered. Please use a different number or try logging in.")
                    return
                }
                
                // Hash the password before storing
                val hashedPassword = hashPassword(password)
                
                // Store user data in Firestore with hashed password
                val userDataMap = userData.copy(
                    uid = user.uid,
                    phoneNumber = formattedPhone,
                    authProvider = "phone",
                    isPhoneVerified = true,
                    phoneVerifiedAt = java.util.Date()
                ).toMap().toMutableMap()
                
                // Add hashed password to the user data
                userDataMap["password"] = hashedPassword
                
                firestore.collection("users")
                    .document(user.uid)
                    .set(userDataMap)
                    .await()
                
                onSuccess(userData.copy(
                    uid = user.uid,
                    phoneNumber = formattedPhone,
                    authProvider = "phone",
                    isPhoneVerified = true,
                    phoneVerifiedAt = java.util.Date()
                ))
            } ?: onError("Failed to get user data after verification")
            
        } catch (e: Exception) {
            onError("Verification failed: ${e.message}")
        }
    }
    
    suspend fun resendCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken,
        onCodeSent: (String, PhoneAuthProvider.ForceResendingToken) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val formattedPhone = formatPhoneNumber(phoneNumber)
            
            // Check if phone number already exists before resending
            if (isPhoneNumberExists(formattedPhone)) {
                onError("This phone number is already registered. Please use a different number or try logging in.")
                return
            }
            
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification completed
                }
                
                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    onError("Verification failed: ${e.message}")
                }
                
                override fun onCodeSent(
                    verificationId: String,
                    resendToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    onCodeSent(verificationId, resendToken)
                }
            }
            
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(formattedPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setCallbacks(callbacks)
                .setForceResendingToken(token)
                .build()
            
            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (e: Exception) {
            onError("Failed to resend verification code: ${e.message}")
        }
    }
} 