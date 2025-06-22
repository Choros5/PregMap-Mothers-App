package com.elvis.pregmap

import com.elvis.pregmap.ui.auth.EmailAuthHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import android.content.Context
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class EmailLoginAccessControlTest {

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockUser: FirebaseUser

    @Mock
    private lateinit var mockAuthResult: com.google.firebase.auth.AuthResult

    @Mock
    private lateinit var mockUserDoc: DocumentSnapshot

    @Mock
    private lateinit var mockCollection: com.google.firebase.firestore.CollectionReference

    @Mock
    private lateinit var mockDocument: com.google.firebase.firestore.DocumentReference

    private lateinit var emailAuthHelper: EmailAuthHelper

    @Before
    fun setUp() {
        // Initialize the EmailAuthHelper with mocked dependencies
        emailAuthHelper = EmailAuthHelper(mockContext)
    }

    @Test
    fun `test successful email login with valid credentials`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userId = "user123"

        // Mock Firebase Auth success
        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockAuthResult)
        )
        `when`(mockAuthResult.user).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)

        // Mock Firestore document exists with valid data
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.document(userId)).thenReturn(mockDocument)
        `when`(mockDocument.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockUserDoc)
        )
        `when`(mockUserDoc.exists()).thenReturn(true)
        `when`(mockUserDoc.getString("email")).thenReturn(email)
        `when`(mockUserDoc.getString("signInMethod")).thenReturn("email")

        // When
        val result = emailAuthHelper.signInWithEmailAndPassword(email, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
    }

    @Test
    fun `test email login fails when user does not exist in Firestore`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userId = "user123"

        // Mock Firebase Auth success
        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockAuthResult)
        )
        `when`(mockAuthResult.user).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)

        // Mock Firestore document does not exist
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.document(userId)).thenReturn(mockDocument)
        `when`(mockDocument.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockUserDoc)
        )
        `when`(mockUserDoc.exists()).thenReturn(false)

        // When
        val result = emailAuthHelper.signInWithEmailAndPassword(email, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Account not found. Please sign up first.", result.exceptionOrNull()?.message)
        verify(mockAuth).signOut()
    }

    @Test
    fun `test email login fails when signInMethod is not email`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userId = "user123"

        // Mock Firebase Auth success
        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockAuthResult)
        )
        `when`(mockAuthResult.user).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)

        // Mock Firestore document exists but with wrong signInMethod
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.document(userId)).thenReturn(mockDocument)
        `when`(mockDocument.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockUserDoc)
        )
        `when`(mockUserDoc.exists()).thenReturn(true)
        `when`(mockUserDoc.getString("email")).thenReturn(email)
        `when`(mockUserDoc.getString("signInMethod")).thenReturn("google")

        // When
        val result = emailAuthHelper.signInWithEmailAndPassword(email, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals(
            "This account was not created with email sign-in. Please use the appropriate sign-in method.",
            result.exceptionOrNull()?.message
        )
        verify(mockAuth).signOut()
    }

    @Test
    fun `test email login fails when email does not match`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val userId = "user123"
        val differentEmail = "different@example.com"

        // Mock Firebase Auth success
        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockAuthResult)
        )
        `when`(mockAuthResult.user).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)

        // Mock Firestore document exists but with different email
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.document(userId)).thenReturn(mockDocument)
        `when`(mockDocument.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockUserDoc)
        )
        `when`(mockUserDoc.exists()).thenReturn(true)
        `when`(mockUserDoc.getString("email")).thenReturn(differentEmail)
        `when`(mockUserDoc.getString("signInMethod")).thenReturn("email")

        // When
        val result = emailAuthHelper.signInWithEmailAndPassword(email, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Email validation failed. Please sign up first.", result.exceptionOrNull()?.message)
        verify(mockAuth).signOut()
    }

    @Test
    fun `test email login fails when Firebase Auth fails`() = runBlocking {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val authException = Exception("Invalid email or password")

        // Mock Firebase Auth failure
        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(
            com.google.android.gms.tasks.Task.forException(authException)
        )

        // When
        val result = emailAuthHelper.signInWithEmailAndPassword(email, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals(authException, result.exceptionOrNull())
        verify(mockAuth).signOut()
    }

    @Test
    fun `test email login access control flow - step by step validation`() = runBlocking {
        // This test verifies the complete flow as described in the requirements
        
        // Given: Valid email login credentials
        val email = "valid@example.com"
        val password = "validpassword"
        val userId = "validuser123"

        // Step 1: Firebase Email Authentication - SUCCESS
        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockAuthResult)
        )
        `when`(mockAuthResult.user).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn(userId)

        // Step 2: Firestore Validation - SUCCESS
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.document(userId)).thenReturn(mockDocument)
        `when`(mockDocument.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockUserDoc)
        )
        `when`(mockUserDoc.exists()).thenReturn(true)
        `when`(mockUserDoc.getString("email")).thenReturn(email) // ✅ email matches
        `when`(mockUserDoc.getString("signInMethod")).thenReturn("email") // ✅ signInMethod is "email"

        // When: User clicks Login
        val result = emailAuthHelper.signInWithEmailAndPassword(email, password)

        // Then: ✅ Allowed - Grant access to the system
        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
        verify(mockAuth, never()).signOut() // User should NOT be signed out
    }
} 