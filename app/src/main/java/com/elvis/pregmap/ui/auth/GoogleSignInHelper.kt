package com.elvis.pregmap.ui.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

enum class SignInResult {
    NewUser,
    ExistingUser,
    AccessDenied // New result for access control
}

class GoogleSignInHelper(private val context: Context) {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    // Google Sign-In Client with proper configuration
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("609695671732-lnov42uf90bj4fa726l50hc49aea7ocj.apps.googleusercontent.com")
        .requestEmail()
        .requestProfile()
        .build()
    
    val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
    
    /**
     * Get sign-in intent that will show account picker
     */
    fun getSignInIntent() = googleSignInClient.signInIntent
    
    /**
     * Sign out to ensure account picker is shown
     */
    suspend fun signOutToShowAccountPicker() {
        try {
            // Sign out from Google to ensure account picker is shown
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            // Ignore sign out errors as we just want to show account picker
        }
    }
    
    /**
     * Sign in with Google and implement access control
     * This method now checks if the user exists in Firestore before allowing access
     */
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<SignInResult> {
        return try {
            // Authenticate with Firebase first
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            
            if (authResult.user != null) {
                val userId = authResult.user!!.uid
                val userEmail = account.email ?: ""
                
                // Check if user document exists in Firestore by email
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .await()
                
                if (userQuery.documents.isNotEmpty()) {
                    // User exists, check if they are a Google user
                    val userDoc = userQuery.documents[0]
                    val signInMethod = userDoc.getString("signInMethod") ?: ""
                    
                    if (signInMethod == "google") {
                        // ✅ Access granted: User exists and is a Google user
                        // Update last sign in time
                        firestore.collection("users").document(userDoc.id)
                            .update("lastSignInAt", Date())
                            .await()
                        
                        Result.success(SignInResult.ExistingUser)
                    } else {
                        // ❌ Access denied: User exists but not with Google
                        // Sign out from Firebase since access is denied
                        auth.signOut()
                        Result.success(SignInResult.AccessDenied)
                    }
                } else {
                    // ❌ Access denied: User doesn't exist in Firestore
                    // Sign out from Firebase since access is denied
                    auth.signOut()
                    Result.success(SignInResult.AccessDenied)
                }
            } else {
                Result.failure(Exception("Firebase authentication failed"))
            }
        } catch (e: Exception) {
            // Ensure we sign out on any error
            try {
                auth.signOut()
            } catch (signOutError: Exception) {
                // Ignore sign out errors
            }
            Result.failure(e)
        }
    }
    
    /**
     * Sign in with Google for sign-up (creates new user)
     * This method is used during the sign-up process
     */
    suspend fun signUpWithGoogle(account: GoogleSignInAccount): Result<SignInResult> {
        return try {
            // Authenticate with Firebase first
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            
            if (authResult.user != null) {
                val userId = authResult.user!!.uid
                val userEmail = account.email ?: ""
                
                // Check if user already exists
                val userQuery = firestore.collection("users")
                    .whereEqualTo("email", userEmail)
                    .get()
                    .await()
                
                if (userQuery.documents.isNotEmpty()) {
                    // User already exists, sign out and return existing user
                    auth.signOut()
                    Result.success(SignInResult.ExistingUser)
                } else {
                    // New user, create account
                    val userData = extractUserDataFromGoogleAccount(account)
                    firestore.collection("users").document(userId)
                        .set(userData.toMap())
                        .await()
                    
                    Result.success(SignInResult.NewUser)
                }
            } else {
                Result.failure(Exception("Firebase authentication failed"))
            }
        } catch (e: Exception) {
            // Ensure we sign out on any error
            try {
                auth.signOut()
            } catch (signOutError: Exception) {
                // Ignore sign out errors
            }
            Result.failure(e)
        }
    }
    
    /**
     * Extract user data from Google account
     */
    private fun extractUserDataFromGoogleAccount(account: GoogleSignInAccount): UserData {
        val fullName = account.displayName ?: ""
        val email = account.email ?: ""
        val photoUrl = account.photoUrl?.toString() ?: ""
        
        // Parse full name into components (First Name, Middle Name, Last Name)
        val nameParts = fullName.split(" ").filter { it.isNotBlank() }
        val firstName = if (nameParts.isNotEmpty()) nameParts[0] else ""
        val lastName = if (nameParts.size > 1) nameParts.last() else ""
        val middleName = if (nameParts.size > 2) nameParts.drop(1).dropLast(1).joinToString(" ") else ""
        
        return UserData.createGoogleUser(
            firstName = firstName,
            middleName = middleName,
            lastName = lastName,
            fullName = fullName,
            email = email,
            photoUrl = photoUrl,
            googleId = account.id ?: ""
        )
    }
    
    /**
     * Save user data to Firestore
     */
    private suspend fun saveUserToFirestore(userId: String, userData: UserData) {
        try {
            // Check if user already exists
            val userDoc = firestore.collection("users").document(userId).get().await()
            
            if (userDoc.exists()) {
                // Update existing user with last sign in time
                firestore.collection("users").document(userId)
                    .update("lastSignInAt", Date())
                    .await()
            } else {
                // Create new user document
                firestore.collection("users").document(userId)
                    .set(userData.toMap())
                    .await()
            }
        } catch (e: Exception) {
            throw Exception("Failed to save user data: ${e.message}")
        }
    }
    
    /**
     * Sign out from Google
     */
    suspend fun signOut() {
        try {
            // Sign out from Firebase
            auth.signOut()
            
            // Sign out from Google
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            throw Exception("Failed to sign out: ${e.message}")
        }
    }
    
    /**
     * Get current user data from Firestore
     */
    suspend fun getCurrentUserData(): UserData? {
        val currentUser = auth.currentUser ?: return null
        
        return try {
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            if (userDoc.exists()) {
                UserData(
                    firstName = userDoc.getString("firstName") ?: "",
                    middleName = userDoc.getString("middleName") ?: "",
                    lastName = userDoc.getString("lastName") ?: "",
                    fullName = userDoc.getString("fullName") ?: "",
                    email = userDoc.getString("email") ?: "",
                    photoUrl = userDoc.getString("photoUrl") ?: "",
                    googleId = userDoc.getString("googleId") ?: "",
                    createdAt = userDoc.getDate("createdAt") ?: Date(),
                    lastSignInAt = userDoc.getDate("lastSignInAt") ?: Date(),
                    signInMethod = userDoc.getString("signInMethod") ?: "google"
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
} 