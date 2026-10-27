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
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber

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

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    var userReloader: (suspend () -> Boolean)? = null

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
                !user.displayName.isNullOrBlank() -> user.displayName ?: "Friend"
                !user.email.isNullOrBlank() -> (user.email ?: "Friend").substringBefore('@').replaceFirstChar { it.uppercase() }
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
        _isEmailVerified.value = currentUser?.isEmailVerified == true

        try {
            auth.addAuthStateListener { firebaseAuth ->
                currentUser = firebaseAuth.currentUser
                _isEmailVerified.value = firebaseAuth.currentUser?.isEmailVerified == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Reload the current user and drop cached token to fetch fresh emailVerified claim.
     * Throws FirebaseAuthInvalidUserException if account deleted remotely.
     */
    suspend fun reloadUser(): Boolean {
        userReloader?.let {
            val res = it.invoke()
            _isEmailVerified.value = res
            return res
        }
        val user = auth.currentUser ?: run {
            _isEmailVerified.value = false
            return false
        }
        return try {
            user.reload().await()
            user.getIdToken(true).await()
            currentUser = auth.currentUser
            val verified = currentUser?.isEmailVerified == true
            _isEmailVerified.value = verified
            verified
        } catch (e: FirebaseAuthInvalidUserException) {
            signOut()
            _isEmailVerified.value = false
            throw e
        } catch (e: Exception) {
            val verified = currentUser?.isEmailVerified == true
            _isEmailVerified.value = verified
            verified
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
            onError(friendlyAuthError(e))
        }
    }

    /**
     * Register a new account with email and password.
     * Optionally takes a preferred display name.
     */
    fun signUpWithEmail(
        name: String? = null,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.length < 8) {
            onError("Use 8 or more characters. A phrase you can remember works well.")
            return
        }

        try {
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    currentUser = user
                    if (user != null) {
                        if (!name.isNullOrBlank()) {
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(name.trim())
                                .build()
                            user.updateProfile(profileUpdates)
                        }
                        // Send verification email in background without blocking flow
                        user.sendEmailVerification()
                    }
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onError(friendlyAuthError(e))
                }
        } catch (e: Exception) {
            onError(friendlyAuthError(e))
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
            onError("Please enter both email and password.")
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
            onError(friendlyAuthError(e))
        }
    }

    /**
     * Send email verification to the current logged in user.
     */
    fun sendEmailVerification(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = currentUser
        if (user == null) {
            onError("Please sign in first.")
            return
        }

        try {
            user.sendEmailVerification()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(friendlyAuthError(e)) }
        } catch (e: Exception) {
            onError(friendlyAuthError(e))
        }
    }

    /**
     * Delete the current user account (Google Play compliance).
     */
    fun deleteAccount(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = currentUser
        if (user == null) {
            onError("No active account found.")
            return
        }

        try {
            user.delete()
                .addOnSuccessListener {
                    currentUser = null
                    signOut()
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onError(friendlyAuthError(e))
                }
        } catch (e: Exception) {
            onError(friendlyAuthError(e))
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
            onError("Please enter your email address.")
            return
        }

        try {
            auth.sendPasswordResetEmail(email.trim())
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(friendlyAuthError(e)) }
        } catch (e: Exception) {
            onError(friendlyAuthError(e))
        }
    }

    /**
     * Resolves the Web Client ID for Google Sign-In from resources, with constant fallback.
     */
    fun getGoogleServerClientId(context: Context): String {
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
        return "10687276915-3m2l9utkihjc6okfpbomh4cqmv4ngov0.apps.googleusercontent.com"
    }

    private fun Context.findActivity(): android.app.Activity? {
        var ctx = this
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Sign in or register with Google via Android Credential Manager.
     */
    suspend fun signInWithGoogle(
        context: Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val serverClientId = getGoogleServerClientId(context)
        Timber.tag("AuthManager").d("Initiating Google Sign-In with serverClientId=$serverClientId")

        val activity = context.findActivity()
        if (activity == null) {
            Timber.tag("AuthManager").e("Failed to find Activity context for CredentialManager. Context=$context")
            onError("Cannot launch Google Sign-In (no active window).")
            return
        }

        try {
            val credentialManager = CredentialManager.create(activity)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Timber.tag("AuthManager").d("Requesting credentials from CredentialManager...")
            val result = credentialManager.getCredential(
                request = request,
                context = activity
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Timber.tag("AuthManager").d("Received Google ID Token. Authenticating with Firebase...")
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).await()
                currentUser = authResult.user
                Timber.tag("AuthManager").i("Firebase Google auth succeeded for user: ${authResult.user?.uid}")
                onSuccess()
            } else {
                Timber.tag("AuthManager").w("Unexpected credential type: ${credential::class.java.name}")
                onError("Could not complete sign in with Google (unsupported credential type).")
            }
        } catch (e: GetCredentialCancellationException) {
            Timber.tag("AuthManager").d("Google sign-in was cancelled by user")
            onCancel()
        } catch (e: GetCredentialException) {
            Timber.tag("AuthManager").e(e, "Google sign-in GetCredentialException: ${e.message}")
            val msg = e.message.orEmpty()
            if (msg.contains("10", ignoreCase = true) || msg.contains("DEVELOPER_ERROR", ignoreCase = true)) {
                onError("Google Sign-In configuration error (10: DEVELOPER_ERROR). Check SHA-1/SHA-256 and Google provider in Firebase Console.")
            } else if (msg.contains("16", ignoreCase = true) || msg.contains("Cannot find a matching credential", ignoreCase = true)) {
                onError("No Google accounts found on this device.")
            } else {
                onError("Google sign-in error (${e.javaClass.simpleName}): ${e.localizedMessage ?: e.message}")
            }
        } catch (e: Exception) {
            Timber.tag("AuthManager").e(e, "Google sign-in exception: ${e.message}")
            onError(friendlyAuthError(e))
        }
    }

    /**
     * Sign out.
     */
    fun signOut() {
        try {
            auth.signOut()
            currentUser = null
            _isEmailVerified.value = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun friendlyAuthError(e: Exception): String {
        val msg = e.message.orEmpty()
        return when {
            msg.contains("The email address is already in use", ignoreCase = true) ||
            msg.contains("email-already-in-use", ignoreCase = true) ->
                "That email already has an account. Sign in instead."
            msg.contains("The email address is badly formatted", ignoreCase = true) ||
            msg.contains("invalid-email", ignoreCase = true) ->
                "Please enter a valid email address."
            msg.contains("Password should be at least", ignoreCase = true) ||
            msg.contains("weak-password", ignoreCase = true) ->
                "Use 8 or more characters. A phrase you can remember works well."
            msg.contains("There is no user record", ignoreCase = true) ||
            msg.contains("invalid-credential", ignoreCase = true) ||
            msg.contains("wrong-password", ignoreCase = true) ||
            msg.contains("user-not-found", ignoreCase = true) ->
                "That email and password do not match."
            msg.contains("network error", ignoreCase = true) ||
            msg.contains("offline", ignoreCase = true) ||
            msg.contains("network", ignoreCase = true) ->
                "You are offline. You can continue as a guest."
            msg.contains("ERROR_OPERATION_NOT_ALLOWED", ignoreCase = true) ||
            msg.contains("disabled for this project", ignoreCase = true) ->
                "Google Sign-In is disabled in Firebase Console. Enable it under Authentication > Sign-in method."
            msg.contains("firebase", ignoreCase = true) ->
                "Service is currently unavailable (${e.javaClass.simpleName}: ${e.message ?: "unknown"})."
            else -> "Authentication failed (${e.javaClass.simpleName}: ${e.message ?: "unknown"})."
        }
    }
}

