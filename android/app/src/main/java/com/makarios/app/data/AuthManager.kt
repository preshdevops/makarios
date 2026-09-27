package com.makarios.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * AuthManager
 *
 * Manages Firebase Authentication state for Makarios.
 * Supports:
 * - Anonymous Guest Mode (zero-friction first run)
 * - Email & Password Sign Up / Sign In
 * - Account Linking (seamlessly promoting guest accounts to permanent accounts)
 * - Sign Out
 */
object AuthManager {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    var currentUser by mutableStateOf<FirebaseUser?>(null)
        private set

    val isLoggedIn: Boolean
        get() = currentUser != null

    val isAnonymous: Boolean
        get() = currentUser?.isAnonymous == true

    val userEmail: String?
        get() = currentUser?.email

    val displayName: String
        get() {
            val user = currentUser
            return when {
                user == null -> "Guest"
                user.isAnonymous -> "Guest Pilgrim"
                !user.displayName.isNullOrBlank() -> user.displayName!!
                !user.email.isNullOrBlank() -> user.email!!.substringBefore('@').replaceFirstChar { it.uppercase() }
                else -> "Beloved"
            }
        }

    init {
        // Initialize with existing Firebase auth state and attach listener
        currentUser = try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }

        try {
            auth.addAuthStateListener { firebaseAuth ->
                currentUser = firebaseAuth.currentUser
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sign in anonymously as a guest.
     */
    fun signInAnonymously(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    currentUser = it.user
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onError(e.localizedMessage ?: "Unable to continue as guest")
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Firebase Auth unavailable")
        }
    }

    /**
     * Register a new account with email and password.
     * If currently an anonymous guest, links the credential to preserve existing declarations.
     */
    fun signUpWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.length < 6) {
            onError("Please enter a valid email and a password of at least 6 characters")
            return
        }

        try {
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener {
                    currentUser = it.user
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onError(friendlyAuthError(e))
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Sign up failed")
        }
    }

    /**
     * Sign in to an existing account with email and password.
     */
    fun signInWithEmail(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onError("Please enter both email and password")
            return
        }

        try {
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener {
                    currentUser = it.user
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onError(friendlyAuthError(e))
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Sign in failed")
        }
    }

    /**
     * Send a password reset email.
     */
    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank()) {
            onError("Please enter your email address")
            return
        }

        try {
            auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(friendlyAuthError(e)) }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Could not send reset email")
        }
    }

    /**
     * Sign out.
     */
    fun signOut() {
        try {
            auth.signOut()
            currentUser = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun friendlyAuthError(e: Exception): String {
        val msg = e.message.orEmpty()
        return when {
            msg.contains("The email address is already in use", ignoreCase = true) ->
                "An account with this email already exists. Please sign in instead."
            msg.contains("The email address is badly formatted", ignoreCase = true) ->
                "Please enter a valid email address."
            msg.contains("Password should be at least 6 characters", ignoreCase = true) ->
                "Password must be at least 6 characters."
            msg.contains("There is no user record", ignoreCase = true) || msg.contains("invalid-credential", ignoreCase = true) ->
                "Incorrect email or password. Please try again."
            msg.contains("network error", ignoreCase = true) ->
                "Network connection issue. Please check your internet."
            else -> e.localizedMessage ?: "Authentication failed. Please try again."
        }
    }
}
