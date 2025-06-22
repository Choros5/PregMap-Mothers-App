package com.elvis.pregmap.ui.auth

import android.content.Context
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.tasks.await

class AppCheckHelper(private val context: Context) {
    
    private val firebaseAppCheck: FirebaseAppCheck = FirebaseAppCheck.getInstance()
    private var isInitialized = false
    
    /**
     * Initialize App Check with Play Integrity
     */
    fun initializeAppCheck() {
        try {
            // Install the Play Integrity App Check provider
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            isInitialized = true
        } catch (e: Exception) {
            // Log error but don't crash the app
            println("App Check initialization failed: ${e.message}")
        }
    }
    
    /**
     * Get App Check token
     */
    suspend fun getAppCheckToken(): String? {
        return try {
            if (!isInitialized) {
                return null
            }
            val appCheckToken = firebaseAppCheck.getAppCheckToken(false).await()
            appCheckToken.token
        } catch (e: Exception) {
            println("Failed to get App Check token: ${e.message}")
            null
        }
    }
    
    /**
     * Check if App Check is properly configured
     */
    fun isAppCheckEnabled(): Boolean {
        return isInitialized
    }
} 