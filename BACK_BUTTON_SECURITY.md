# Back Button Security Features

## Overview
The PregMap app implements several security features to prevent users from accidentally logging out by pressing the back button when they are already authenticated.

## Security Features Implemented

### 1. Double-Tap Back Button to Exit
- When a user is logged in and on the main screen, pressing the back button once shows a toast message: "Press back again to exit"
- The user must press the back button twice within 2 seconds to actually exit the app
- This prevents accidental app closure and potential data loss

### 2. Sign Out Confirmation Dialog
- The only way to sign out is through the dedicated "Sign Out (Secure)" button in the navigation drawer
- Clicking this button shows a confirmation dialog asking: "Are you sure you want to sign out? You will need to log in again to access your account."
- Users must explicitly confirm their intention to sign out
- This prevents accidental sign outs

### 3. User Education
- When users first access the main screen, they see a helpful toast message: "Tip: Use the menu to sign out. Back button requires double-tap to exit."
- This educates users about the proper way to sign out

### 4. Automatic Navigation Protection
- The app automatically redirects logged-in users to the main screen
- Prevents navigation to authentication screens when already authenticated
- Ensures users stay in the authenticated state

## Technical Implementation

### MainActivity.kt
- `MainScreenWithBackHandler` composable wraps the main screen with custom back button handling
- Uses `OnBackPressedCallback` to intercept back button presses
- Registers and unregisters callbacks properly to prevent memory leaks

### MainScreen.kt
- `MainScreen` composable includes a confirmation dialog for sign out
- Sign out button is clearly labeled as "Sign Out (Secure)"
- Dialog provides clear messaging about the consequences of signing out

## User Experience

### For Logged-In Users:
1. **Back Button**: Shows "Press back again to exit" message on first press
2. **Sign Out**: Must use the drawer menu → "Sign Out (Secure)" → Confirm in dialog
3. **Education**: Receives helpful tip about back button behavior

### For Non-Logged-In Users:
1. **Back Button**: Works normally for navigation
2. **No Restrictions**: Can navigate freely through authentication screens

## Testing
The implementation includes comprehensive tests in `BackButtonSecurityTest.kt` to verify:
- Back button behavior for logged-in users
- Sign out confirmation dialog functionality
- Proper button labeling and messaging
- Normal back navigation for non-authenticated users

## Security Benefits
- **Prevents Accidental Logout**: Users cannot accidentally sign out by pressing back
- **Clear Intent**: Sign out requires explicit user action and confirmation
- **Data Protection**: Prevents loss of unsaved data or session state
- **User Education**: Informs users about proper sign out procedures
- **Consistent Experience**: Maintains app state and user session integrity 