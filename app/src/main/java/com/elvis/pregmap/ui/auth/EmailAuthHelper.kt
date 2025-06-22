package com.elvis.pregmap.ui.auth

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class EmailAuthHelper(private val context: Context) {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val appCheckHelper = AppCheckHelper(context)
    
    /**
     * Create user account with email and password
     */
    suspend fun createUserWithEmail(
        email: String,
        password: String,
        userData: UserData
    ): Result<FirebaseUser> {
        return try {
            // Get App Check token for better deliverability
            val appCheckToken = appCheckHelper.getAppCheckToken()
            
            // Create user with Firebase Auth
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            
            if (authResult.user != null) {
                // Send email verification with App Check context
                authResult.user!!.sendEmailVerification().await()
                
                // Save user data to Firestore with App Check
                saveUserToFirestore(authResult.user!!.uid, userData, appCheckToken)
                
                Result.success(authResult.user!!)
            } else {
                Result.failure(Exception("Failed to create user account"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if email is already registered
     */
    suspend fun checkEmailExists(email: String): Boolean {
        return try {
            val appCheckToken = appCheckHelper.getAppCheckToken()
            val query = firestore.collection("users")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()
            
            !query.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Verify email verification status
     */
    suspend fun checkEmailVerification(): Boolean {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Reload user to get latest verification status
                currentUser.reload().await()
                currentUser.isEmailVerified
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Resend verification email
     */
    suspend fun resendVerificationEmail(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Get App Check token for better deliverability
                val appCheckToken = appCheckHelper.getAppCheckToken()
                
                currentUser.sendEmailVerification().await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("No user logged in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save user data to Firestore with App Check
     */
    private suspend fun saveUserToFirestore(userId: String, userData: UserData, appCheckToken: String? = null) {
        try {
            // Add App Check metadata to user data
            val userDataWithAppCheck = userData.toMap().toMutableMap()
            if (appCheckToken != null) {
                userDataWithAppCheck["appCheckToken"] = appCheckToken
                userDataWithAppCheck["appCheckEnabled"] = true
            }
            
            firestore.collection("users").document(userId)
                .set(userDataWithAppCheck)
                .await()
        } catch (e: Exception) {
            throw Exception("Failed to save user data: ${e.message}")
        }
    }
    
    /**
     * Get current user
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
    
    /**
     * Sign out user
     */
    suspend fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            throw Exception("Failed to sign out: ${e.message}")
        }
    }
    
    /**
     * Check if App Check is enabled
     */
    fun isAppCheckEnabled(): Boolean {
        return appCheckHelper.isAppCheckEnabled()
    }
    
    /**
     * Sign in with email and password with access control
     * Implements the Email Login Access Control system:
     * 1. Firebase Email Authentication
     * 2. Firestore Validation (email must match, signInMethod must be "email")
     * 3. Grant access if valid, sign out and show error if invalid
     */
    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            // Step 1: Firebase Email Authentication
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            
            if (authResult.user != null) {
                // Step 2: Firestore Validation
                val userDoc = firestore.collection("users").document(authResult.user!!.uid).get().await()
                
                if (userDoc.exists()) {
                    val firestoreEmail = userDoc.getString("email")
                    val signInMethod = userDoc.getString("signInMethod")
                    
                    // Check if email matches and signInMethod is "email"
                    if (firestoreEmail == email && signInMethod == "email") {
                        // ✅ Allowed: Grant access to the system
                        Result.success(authResult.user!!)
                    } else {
                        // ❌ Denied: Sign out immediately and show error
                        auth.signOut()
                        val errorMessage = if (signInMethod != "email") {
                            "This account was not created with email sign-in. Please use the appropriate sign-in method."
                        } else {
                            "Email validation failed. Please sign up first."
                        }
                        Result.failure(Exception(errorMessage))
                    }
                } else {
                    // ❌ Denied: User doesn't exist in Firestore
                    auth.signOut()
                    Result.failure(Exception("Account not found. Please sign up first."))
                }
            } else {
                Result.failure(Exception("Failed to authenticate with Firebase"))
            }
        } catch (e: Exception) {
            // If any error occurs, ensure user is signed out
            try {
                auth.signOut()
            } catch (signOutError: Exception) {
                // Ignore sign out errors
            }
            Result.failure(e)
        }
    }
} 