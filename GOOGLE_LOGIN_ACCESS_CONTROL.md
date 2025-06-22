# Google Login Access Control Implementation

## Overview
This implementation adds access control to Google login in the PregMap Android app. When a user tries to log in with Google, the app checks if their email exists in the Firestore users collection and verifies the sign-in method.

## 🔍 Authentication Guard Flow

### When a user tries to log in with Google:

1. **Firebase Authentication**: User is authenticated with Firebase using Google credentials
2. **Firestore Check**: App queries the Firestore "users" collection to find a document with the user's email
3. **Sign-in Method Verification**: If found, checks if the `signInMethod` field equals "google"

### ✅ Access Granted
- **Condition**: Email exists in users collection AND `signInMethod` is "google"
- **Action**: User is granted access to the app
- **Navigation**: User is taken to the main app screen

### ❌ Access Denied
- **Condition**: Email is not found in users collection OR `signInMethod` is not "google"
- **Action**: User is shown an access denied message
- **Options**: User can either sign up or try a different account

## Implementation Details

### 1. Updated GoogleSignInHelper.kt
- Added new `SignInResult.AccessDenied` enum value
- Modified `signInWithGoogle()` method to implement access control
- Added `signUpWithGoogle()` method for the sign-up process
- Implemented email-based user lookup in Firestore

### 2. Created GoogleLoginScreen.kt
- New screen specifically for Google login with access control
- Shows access denied message when user doesn't exist
- Provides options to sign up or try different account
- Handles navigation to main app on successful login

### 3. Updated Navigation
- Added `google_login` route to MainActivity
- Updated AuthSelectionScreen to route to appropriate Google screen based on context
- Integrated with existing navigation flow

### 4. Updated GoogleSignUpScreen.kt
- Now uses `signUpWithGoogle()` method instead of `signInWithGoogle()`
- Maintains existing sign-up functionality

## Key Features

### Access Control Logic
```kotlin
// Check if user exists by email
val userQuery = firestore.collection("users")
    .whereEqualTo("email", userEmail)
    .get()
    .await()

if (userQuery.documents.isNotEmpty()) {
    val userDoc = userQuery.documents[0]
    val signInMethod = userDoc.getString("signInMethod") ?: ""
    
    if (signInMethod == "google") {
        // ✅ Access granted
        Result.success(SignInResult.ExistingUser)
    } else {
        // ❌ Access denied - wrong sign-in method
        Result.success(SignInResult.AccessDenied)
    }
} else {
    // ❌ Access denied - user doesn't exist
    Result.success(SignInResult.AccessDenied)
}
```

### User Experience
- **Clear messaging**: Users understand why access was denied
- **Easy recovery**: Direct link to sign-up process
- **Account switching**: Option to try different Google account
- **Seamless flow**: Smooth navigation between login and sign-up

### Security Features
- **Automatic sign-out**: Firebase authentication is cleared on access denial
- **Method verification**: Ensures users can only access with their original sign-in method
- **Email validation**: Uses email as the primary identifier for user lookup

## Usage

### For Login Flow:
1. User selects "Login" from welcome screen
2. User chooses "Continue with Google"
3. Google account picker appears
4. App checks user existence and sign-in method
5. Access granted or denied with appropriate messaging

### For Sign-up Flow:
1. User selects "Sign Up" from welcome screen
2. User chooses "Continue with Google"
3. Google account picker appears
4. New user account is created in Firestore
5. User is directed to login

## Error Handling
- **Network errors**: Proper error messages for connectivity issues
- **Firebase errors**: Graceful handling of authentication failures
- **User cancellation**: Clear messaging when user cancels Google sign-in
- **Invalid accounts**: Access denied for unregistered or wrong-method accounts

## Testing Scenarios
1. **New user login attempt**: Should show access denied and sign-up option
2. **Existing Google user login**: Should grant access and navigate to main app
3. **Email user trying Google login**: Should show access denied
4. **Phone user trying Google login**: Should show access denied
5. **Network failure**: Should show appropriate error message
6. **User cancellation**: Should show cancellation message 