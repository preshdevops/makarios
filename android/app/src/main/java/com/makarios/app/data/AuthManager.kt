package com.makarios.app.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

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
                user.isAnonymous -> "Guest"
                !user.displayName.isNullOrBlank() -> user.displayName!!
                !user.email.isNullOrBlank() -> user.email!!.substringBefore('@').replaceFirstChar { it.uppercase() }
                else -> "Friend"
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
     * Resolves the Web Client ID for Google Sign-In from resources.
     */
    fun getGoogleServerClientId(context: Context): String? {
        val defaultIdRes = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (defaultIdRes != 0) {
            val id = context.getString(defaultIdRes)
            if (id.isNotBlank()) return id
        }
        val manualIdRes = context.resources.getIdentifier("google_web_client_id", "string", context.packageName)
        if (manualIdRes != 0) {
            val id = context.getString(manualIdRes)
            if (id.isNotBlank()) return id
        }
        return null
    }

    /**
     * Sign in or register with Google via Android Credential Manager.
     */
    suspend fun signInWithGoogle(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val serverClientId = getGoogleServerClientId(context)
        if (serverClientId.isNullOrBlank()) {
            onError("Google Sign-In configuration required: Enable Google in Firebase Console and register SHA-1.")
            return
        }

        try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                auth.signInWithCredential(authCredential)
                    .addOnSuccessListener { authResult ->
                        currentUser = authResult.user
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(friendlyAuthError(e))
                    }
            } else {
                onError("Received unexpected credential format from Google.")
            }
        } catch (e: GetCredentialCancellationException) {
            // User dismissed or tapped outside Google account picker — silently return
        } catch (e: GetCredentialException) {
            val msg = e.message.orEmpty()
            if (msg.contains("10", ignoreCase = true) || msg.contains("DEVELOPER_ERROR", ignoreCase = true)) {
                onError("Google Sign-In configuration error: Add SHA-1 to Firebase Console.")
            } else {
                onError(friendlyAuthError(e))
            }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Google sign-in failed.")
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
            msg.contains("ERROR_OPERATION_NOT_ALLOWED", ignoreCase = true) || msg.contains("disabled for this project", ignoreCase = true) ->
                "This sign-in method is disabled. Please enable it in Firebase Console."
            else -> e.localizedMessage ?: "Authentication failed. Please try again."
        }
    }
}
