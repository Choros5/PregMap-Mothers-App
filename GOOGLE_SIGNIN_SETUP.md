# Google Sign-In Setup Guide for PregMap

This guide explains how the Google Sign-In implementation works and what has been set up.

## Features Implemented

### ✅ Google Sign-In Flow
- Single-tap Google Sign-In button with proper Google branding
- Automatic fetching of user data from Google account
- Firebase Authentication integration
- Firestore database integration for user data storage

### ✅ User Data Extraction
The implementation automatically fetches and stores:
- **Full Name** (parsed into First Name, Middle Name, Last Name)
- **Email Address**
- **Google Account Photo URL** (optional for avatar)
- **Google Account ID**
- **Sign-in timestamps**

### ✅ Firestore Database Structure
User data is stored in the `users` collection with the following structure:
```json
{
  "firstName": "John",
  "middleName": "Michael",
  "lastName": "Doe",
  "fullName": "John Michael Doe",
  "email": "john.doe@gmail.com",
  "photoUrl": "https://lh3.googleusercontent.com/...",
  "googleId": "123456789",
  "createdAt": "2024-01-01T00:00:00Z",
  "lastSignInAt": "2024-01-01T00:00:00Z",
  "signInMethod": "google"
}
```

## Technical Implementation

### 1. Dependencies Added
- `com.google.firebase:firebase-firestore` - For database operations
- `com.google.android.gms:play-services-auth:20.7.0` - For Google Sign-In

### 2. Key Components

#### GoogleSignInHelper.kt
- Centralized Google Sign-In logic
- Handles Firebase authentication
- Manages Firestore operations
- Provides user data extraction and storage

#### UserData.kt
- Data class representing user information
- Type-safe structure for Firestore operations
- Includes all required user fields

#### GoogleSignUpScreen.kt
- Clean UI implementation
- Proper error handling
- Loading states
- Navigation to main app after successful sign-in

### 3. Configuration
The Google Sign-In is configured with:
- **Client ID**: `609695671732-lnov42uf90bj4fa726l50hc49aea7ocj.apps.googleusercontent.com`
- **Scopes**: Email and Profile
- **Firebase Integration**: Automatic token exchange

## How It Works

1. **User taps "Continue with Google"**
2. **Google Sign-In dialog appears** (handled by Google Play Services)
3. **User selects their Google account**
4. **Google returns account information** including:
   - Display name
   - Email address
   - Profile photo URL
   - ID token for Firebase
5. **Firebase authentication** using the ID token
6. **User data extraction** and parsing of name components
7. **Firestore storage** - creates new user document or updates existing one
8. **Navigation to main app** on success

## Error Handling

The implementation includes comprehensive error handling for:
- Google Sign-In failures
- Firebase authentication errors
- Firestore database errors
- Network connectivity issues
- User cancellation

## Security Features

- **Secure token exchange** between Google and Firebase
- **User ID-based document storage** in Firestore
- **Proper sign-out functionality**
- **No sensitive data stored in plain text**

## Testing

To test the implementation:

1. **Build and run the app**
2. **Navigate to Google Sign-In screen**
3. **Tap "Continue with Google"**
4. **Select a Google account**
5. **Verify successful navigation to main screen**
6. **Check Firestore console** to see user data stored

## Troubleshooting

### Common Issues:

1. **"Google sign in failed"**
   - Check internet connectivity
   - Verify Google Play Services is installed and updated
   - Ensure the app is signed with the correct certificate

2. **"Authentication failed"**
   - Verify Firebase configuration in `google-services.json`
   - Check that the client ID matches your Firebase project
   - Ensure Google Sign-In is enabled in Firebase console

3. **"Failed to save user data"**
   - Check Firestore rules allow write operations
   - Verify Firestore is enabled in Firebase console
   - Check network connectivity

### Debug Steps:

1. **Check Firebase Console**:
   - Go to Authentication > Sign-in method
   - Ensure Google is enabled
   - Verify the OAuth client ID is correct

2. **Check Firestore Console**:
   - Go to Firestore Database
   - Look for the `users` collection
   - Verify documents are being created

3. **Check Logs**:
   - Use Android Studio's Logcat
   - Filter by your app package
   - Look for authentication and database errors

## Next Steps

The Google Sign-In implementation is now complete and ready for production use. You can:

1. **Customize the UI** to match your app's design
2. **Add additional user fields** to the UserData class
3. **Implement user profile management**
4. **Add sign-out functionality** to other screens
5. **Implement user data synchronization**

## Files Modified/Created

- ✅ `app/build.gradle.kts` - Added Firestore dependency
- ✅ `ui/auth/GoogleSignUpScreen.kt` - Complete rewrite with proper implementation
- ✅ `ui/auth/GoogleSignInHelper.kt` - New helper class for Google Sign-In logic
- ✅ `ui/auth/UserData.kt` - New data class for user information
- ✅ `ui/MainScreen.kt` - New main screen for testing navigation
- ✅ `MainActivity.kt` - Added main screen route

The implementation follows Google's best practices and Firebase documentation for a secure and reliable Google Sign-In experience. 