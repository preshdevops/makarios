package com.makarios.app

import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.makarios.app.data.AuthManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Item B Automated Tests:
 * 1. Mock auth user with emailVerified = false.
 * 2. Call refresh logic with a mock that returns emailVerified = true on the second call.
 * 3. Assert the StateFlow emits true.
 * 4. Verify remote deletion throws FirebaseAuthInvalidUserException and resets verified state to false.
 */
class EmailVerificationTest {

    @Before
    fun setUp() {
        AuthManager.userReloader = null
    }

    @After
    fun tearDown() {
        AuthManager.userReloader = null
    }

    @Test
    fun emailVerificationStateFlowUpdatesOnReload() = runBlocking {
        var callCount = 0

        // Mock reloader returns false on call 1, true on call 2
        AuthManager.userReloader = {
            callCount++
            when (callCount) {
                1 -> false
                else -> true
            }
        }

        // Call 1: user is still unverified
        val firstResult = AuthManager.reloadUser()
        assertFalse("First reload must return false", firstResult)
        assertFalse("StateFlow must emit false initially", AuthManager.isEmailVerified.value)

        // Call 2: user clicked link, reloaded state becomes true
        val secondResult = AuthManager.reloadUser()
        assertTrue("Second reload must return true", secondResult)
        assertTrue("StateFlow must emit true after verification", AuthManager.isEmailVerified.value)
        assertEquals("StateFlow collect must equal true", true, AuthManager.isEmailVerified.first())
    }

    @Test
    fun remoteAccountDeletionThrowsInvalidUserExceptionAndResetsState() = runBlocking {
        AuthManager.userReloader = {
            throw FirebaseAuthInvalidUserException("ERROR_USER_NOT_FOUND", "User was deleted remotely")
        }

        try {
            AuthManager.reloadUser()
            fail("Expected FirebaseAuthInvalidUserException to be thrown")
        } catch (e: FirebaseAuthInvalidUserException) {
            assertTrue("Exception message should indicate deletion", e.message?.contains("User was deleted") == true)
        }

        assertFalse("StateFlow must reset to false on deleted account", AuthManager.isEmailVerified.value)
    }
}
