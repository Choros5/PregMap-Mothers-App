# Security Features - PregMap

## Back Button Protection

This document outlines the security features implemented to prevent users from accidentally logging out of the PregMap application.

### Features Implemented

#### 1. Double-Tap Back Button to Exit
- When a user is logged in and on the main screen, pressing the back button once shows a toast message: "Press back again to exit"
- The user must press the back button twice within 2 seconds to actually exit the app
- This prevents accidental logout by back button press

#### 2. Sign Out Confirmation Dialog
- When users tap the "Sign Out" button in the navigation drawer, a confirmation dialog appears
- Users must explicitly confirm their intention to sign out
- This prevents accidental sign outs through the UI

#### 3. Authentication State Monitoring
- The app continuously monitors the user's authentication state
- If a logged-in user somehow navigates to authentication screens, they are automatically redirected to the main screen
- If a non-authenticated user somehow reaches the main screen, they are redirected to the welcome screen

#### 4. Navigation Stack Protection
- When users sign out, the entire navigation stack is cleared using `popUpTo(0) { inclusive = true }`
- This prevents users from navigating back to the main screen after signing out
- The app starts fresh from the authentication flow

#### 5. Splash Screen Authentication Check
- The splash screen checks if a user is already logged in
- Logged-in users are automatically taken to the main screen
- Non-authenticated users are taken to the welcome screen

### Technical Implementation

#### MainActivity.kt
- `MainScreenWithBackHandler`: Wraps the main screen with custom back button handling
- `OnBackPressedCallback`: Custom callback that intercepts back button presses
- Authentication state monitoring with `LaunchedEffect`

#### MainScreen.kt
- Sign out confirmation dialog using `AlertDialog`
- Proper navigation stack management during sign out

### User Experience

1. **Logged-in users** cannot accidentally log out by pressing the back button
2. **Sign out is intentional** - requires confirmation through a dialog
3. **App state is consistent** - authentication state and navigation are always in sync
4. **Clear feedback** - users get visual feedback when trying to exit the app

### Security Benefits

- Prevents accidental data loss from unexpected logouts
- Ensures users remain authenticated during their session
- Provides clear, intentional logout process
- Maintains app security by preventing unauthorized access to main features 