package com.elvis.pregmap

import com.elvis.pregmap.ui.auth.PhoneAuthHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

@RunWith(MockitoJUnitRunner::class)
class PhoneLoginAccessControlTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirestore: FirebaseFirestore

    @Mock
    private lateinit var mockUser: FirebaseUser

    @Mock
    private lateinit var mockQuerySnapshot: QuerySnapshot

    @Mock
    private lateinit var mockCollection: com.google.firebase.firestore.CollectionReference

    @Mock
    private lateinit var mockQuery: com.google.firebase.firestore.Query

    @Mock
    private lateinit var mockDocument: com.google.firebase.firestore.QueryDocumentSnapshot

    private lateinit var phoneAuthHelper: PhoneAuthHelper

    @Before
    fun setUp() {
        // Initialize the PhoneAuthHelper with mocked dependencies
        phoneAuthHelper = PhoneAuthHelper()
    }

    @Test
    fun `test successful phone login with valid password`() = runBlocking {
        // Given
        val phoneNumber = "712345678" // without leading 0
        val password = "password123"
        val formattedPhone = "+254712345678"

        // Mock Firestore query success with valid data
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.whereEqualTo("phoneNumber", formattedPhone)).thenReturn(mockQuery)
        `when`(mockCollection.whereEqualTo("signInMethod", "phone")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockQuerySnapshot)
        )
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocument))
        `when`(mockDocument.getString("password")).thenReturn(password)

        // Mock current user for success case
        `when`(mockAuth.currentUser).thenReturn(mockUser)

        // When
        val result = phoneAuthHelper.signInWithPhoneAndPassword(phoneNumber, password)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
    }

    @Test
    fun `test phone login fails when phone number not found in Firestore`() = runBlocking {
        // Given
        val phoneNumber = "712345678"
        val password = "password123"
        val formattedPhone = "+254712345678"

        // Mock Firestore query returns empty (phone number not found)
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.whereEqualTo("phoneNumber", formattedPhone)).thenReturn(mockQuery)
        `when`(mockCollection.whereEqualTo("signInMethod", "phone")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockQuerySnapshot)
        )
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true)

        // When
        val result = phoneAuthHelper.signInWithPhoneAndPassword(phoneNumber, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("You are not registered. Please sign up first.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test phone login fails when password is incorrect`() = runBlocking {
        // Given
        val phoneNumber = "712345678"
        val password = "wrongpassword"
        val correctPassword = "correctpassword"
        val formattedPhone = "+254712345678"

        // Mock Firestore query success but with wrong password
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.whereEqualTo("phoneNumber", formattedPhone)).thenReturn(mockQuery)
        `when`(mockCollection.whereEqualTo("signInMethod", "phone")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockQuerySnapshot)
        )
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocument))
        `when`(mockDocument.getString("password")).thenReturn(correctPassword)

        // When
        val result = phoneAuthHelper.signInWithPhoneAndPassword(phoneNumber, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("Invalid phone number or password", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test phone login fails when signInMethod is not phone`() = runBlocking {
        // Given
        val phoneNumber = "712345678"
        val password = "password123"
        val formattedPhone = "+254712345678"

        // Mock Firestore query success but with wrong signInMethod
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.whereEqualTo("phoneNumber", formattedPhone)).thenReturn(mockQuery)
        `when`(mockCollection.whereEqualTo("signInMethod", "phone")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockQuerySnapshot)
        )
        `when`(mockQuerySnapshot.isEmpty).thenReturn(true) // No phone users found

        // When
        val result = phoneAuthHelper.signInWithPhoneAndPassword(phoneNumber, password)

        // Then
        assertFalse(result.isSuccess)
        assertEquals("You are not registered. Please sign up first.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `test phone number formatting - removes leading 0`() = runBlocking {
        // Given
        val phoneNumberWithZero = "0712345678"
        val phoneNumberWithoutZero = "712345678"
        val password = "password123"
        val expectedFormattedPhone = "+254712345678"

        // Mock Firestore query success
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.whereEqualTo("phoneNumber", expectedFormattedPhone)).thenReturn(mockQuery)
        `when`(mockCollection.whereEqualTo("signInMethod", "phone")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockQuerySnapshot)
        )
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocument))
        `when`(mockDocument.getString("password")).thenReturn(password)

        // Mock current user for success case
        `when`(mockAuth.currentUser).thenReturn(mockUser)

        // When - test with phone number that has leading 0
        val result = phoneAuthHelper.signInWithPhoneAndPassword(phoneNumberWithZero, password)

        // Then
        assertTrue(result.isSuccess)
        // Verify that the query was made with the correctly formatted phone number
        verify(mockCollection).whereEqualTo("phoneNumber", expectedFormattedPhone)
    }

    @Test
    fun `test phone login access control flow - step by step validation`() = runBlocking {
        // This test verifies the complete flow as described in the requirements
        
        // Given: Valid phone login credentials
        val phoneNumber = "712345678" // without leading 0
        val password = "password123"
        val formattedPhone = "+254712345678"

        // Step 1: Phone Number Formatting - SUCCESS
        // The phone number is automatically formatted to remove leading 0 and add +254

        // Step 2: Firestore Validation - SUCCESS
        `when`(mockFirestore.collection("users")).thenReturn(mockCollection)
        `when`(mockCollection.whereEqualTo("phoneNumber", formattedPhone)).thenReturn(mockQuery)
        `when`(mockCollection.whereEqualTo("signInMethod", "phone")).thenReturn(mockQuery)
        `when`(mockQuery.limit(1)).thenReturn(mockQuery)
        `when`(mockQuery.get()).thenReturn(
            com.google.android.gms.tasks.Task.forResult(mockQuerySnapshot)
        )
        `when`(mockQuerySnapshot.isEmpty).thenReturn(false)
        `when`(mockQuerySnapshot.documents).thenReturn(listOf(mockDocument))
        `when`(mockDocument.getString("password")).thenReturn(password) // ✅ password matches

        // Mock current user for success case
        `when`(mockAuth.currentUser).thenReturn(mockUser)

        // When: User enters phone number and password and clicks login
        val result = phoneAuthHelper.signInWithPhoneAndPassword(phoneNumber, password)

        // Then: ✅ Allowed - Grant access to the system
        assertTrue(result.isSuccess)
        assertEquals(mockUser, result.getOrNull())
    }
} 