package com.elvis.pregmap.ui.auth

import java.util.Date

data class UserData(
    val uid: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val photoUrl: String = "",
    val googleId: String = "",
    val createdAt: Date = Date(),
    val lastSignInAt: Date = Date(),
    val signInMethod: String = "email", // "google", "email", or "phone"
    val authProvider: String = "email", // "google", "email", or "phone"
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    val emailVerifiedAt: Date? = null,
    val phoneVerifiedAt: Date? = null
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "firstName" to firstName,
            "middleName" to middleName,
            "lastName" to lastName,
            "fullName" to fullName,
            "email" to email,
            "phoneNumber" to phoneNumber,
            "photoUrl" to photoUrl,
            "googleId" to googleId,
            "createdAt" to createdAt,
            "lastSignInAt" to lastSignInAt,
            "signInMethod" to signInMethod,
            "authProvider" to authProvider,
            "isEmailVerified" to isEmailVerified,
            "isPhoneVerified" to isPhoneVerified,
            "emailVerifiedAt" to (emailVerifiedAt ?: ""),
            "phoneVerifiedAt" to (phoneVerifiedAt ?: "")
        )
    }
    
    companion object {
        /**
         * Create UserData for email sign-up
         */
        fun createEmailUser(
            firstName: String,
            middleName: String,
            lastName: String,
            email: String
        ): UserData {
            val fullName = buildString {
                append(firstName.trim())
                if (middleName.isNotBlank()) {
                    append(" ${middleName.trim()}")
                }
                if (lastName.isNotBlank()) {
                    append(" ${lastName.trim()}")
                }
            }
            
            return UserData(
                firstName = firstName.trim(),
                middleName = middleName.trim(),
                lastName = lastName.trim(),
                fullName = fullName,
                email = email.trim(),
                signInMethod = "email",
                authProvider = "email",
                isEmailVerified = false
            )
        }
        
        /**
         * Create UserData for Google sign-in
         */
        fun createGoogleUser(
            firstName: String,
            middleName: String,
            lastName: String,
            fullName: String,
            email: String,
            photoUrl: String,
            googleId: String
        ): UserData {
            return UserData(
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                fullName = fullName,
                email = email,
                photoUrl = photoUrl,
                googleId = googleId,
                signInMethod = "google",
                authProvider = "google",
                isEmailVerified = true // Google accounts are pre-verified
            )
        }
        
        /**
         * Create UserData for phone sign-up
         */
        fun createPhoneUser(
            firstName: String,
            middleName: String,
            lastName: String,
            phoneNumber: String
        ): UserData {
            val fullName = buildString {
                append(firstName.trim())
                if (middleName.isNotBlank()) {
                    append(" ${middleName.trim()}")
                }
                if (lastName.isNotBlank()) {
                    append(" ${lastName.trim()}")
                }
            }
            
            return UserData(
                firstName = firstName.trim(),
                middleName = middleName.trim(),
                lastName = lastName.trim(),
                fullName = fullName,
                phoneNumber = phoneNumber.trim(),
                signInMethod = "phone",
                authProvider = "phone",
                isPhoneVerified = false
            )
        }
    }
} 