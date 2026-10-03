package com.makarios.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.makarios.app.util.UsernameValidator
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

/**
 * CommunityRepository
 *
 * Manages Cloud Firestore operations for:
 * - Public Community Declarations (publishing, live streaming, Amens)
 * - User Profiles (custom unique usernames, Friend Codes)
 * - Friend Discovery (search by @username, email, or Friend Code) and Connections
 */
object CommunityRepository {

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    var currentProfile by mutableStateOf<UserProfile?>(null)
        private set

    val publicDeclarations = mutableStateListOf<PublicAffirmation>()
    val friendsList = mutableStateListOf<UserProfile>()

    private var declarationsListener: ListenerRegistration? = null
    private var friendsListener: ListenerRegistration? = null

    init {
        // Observe auth state changes to load user profile and friends
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null && !user.isAnonymous) {
                loadOrCreateUserProfile(user.uid, user.email, user.displayName)
                listenToFriends(user.uid)
            } else {
                currentProfile = null
                friendsListener?.remove()
                friendsList.clear()
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // User Profile & Friend Code Management
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generates a distinct 6-character Friend Code (e.g. MK-7A4B).
     */
    fun generateFriendCode(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // Exclude confusing chars 0/O, 1/I
        val randomPart = (1..4).map { chars[Random.nextInt(chars.length)] }.joinToString("")
        return "MK-$randomPart"
    }

    /**
     * Loads existing profile or creates a default initial profile with a verified username.
     */
    fun loadOrCreateUserProfile(
        userId: String,
        email: String?,
        displayName: String?,
        onComplete: ((UserProfile) -> Unit)? = null
    ) {
        val userDoc = firestore.collection("users").document(userId)
        userDoc.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                if (profile != null) {
                    currentProfile = profile
                    onComplete?.invoke(profile)
                    return@addOnSuccessListener
                }
            }

            // Create initial profile
            val cleanEmail = email.orEmpty().trim().lowercase()
            val baseName = displayName?.ifBlank { null }
                ?: cleanEmail.substringBefore('@').ifBlank { "member" }

            var candidateUsername = UsernameValidator.normalize(baseName)
            if (UsernameValidator.validate(candidateUsername) !is UsernameValidator.ValidationResult.Valid) {
                candidateUsername = "member_${Random.nextInt(1000, 9999)}"
            }

            val newProfile = UserProfile(
                userId = userId,
                displayName = displayName?.ifBlank { null } ?: baseName.replaceFirstChar { it.uppercase() },
                username = candidateUsername,
                email = cleanEmail,
                friendCode = generateFriendCode(),
                createdAt = System.currentTimeMillis()
            )

            userDoc.set(newProfile).addOnSuccessListener {
                currentProfile = newProfile
                onComplete?.invoke(newProfile)
            }
        }.addOnFailureListener {
            // Local fallback if offline
            currentProfile = UserProfile(
                userId = userId,
                displayName = displayName ?: "Friend",
                username = "member",
                email = email.orEmpty(),
                friendCode = generateFriendCode()
            )
        }
    }

    /**
     * Updates the user's username after verifying formatting, reserved names, and uniqueness.
     */
    fun updateUsername(
        newUsername: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = UsernameValidator.validate(newUsername)
        if (validation is UsernameValidator.ValidationResult.Invalid) {
            onError(validation.message)
            return
        }

        val normalized = UsernameValidator.normalize(newUsername)
        val uid = auth.currentUser?.uid ?: run {
            onError("You must be signed in to set a username")
            return
        }

        // Check if username is already claimed by someone else
        firestore.collection("users")
            .whereEqualTo("username", normalized)
            .get()
            .addOnSuccessListener { query ->
                val takenByOther = query.documents.any { it.id != uid }
                if (takenByOther) {
                    onError("The username @$normalized is already taken")
                    return@addOnSuccessListener
                }

                // Update in Firestore
                firestore.collection("users").document(uid)
                    .update("username", normalized)
                    .addOnSuccessListener {
                        currentProfile = currentProfile?.copy(username = normalized)
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onError(e.localizedMessage ?: "Failed to update username")
                    }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Unable to check username availability")
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public Community Declarations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Publishes a declaration to the public community collection.
     */
    fun publishDeclaration(
        declaration: String,
        scriptureText: String,
        reference: String,
        category: String,
        imageUrl: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val user = auth.currentUser
        val profile = currentProfile

        val declarationId = UUID.randomUUID().toString()
        val publicAffirmation = PublicAffirmation(
            id = declarationId,
            authorId = user?.uid ?: "guest",
            authorName = profile?.displayName ?: user?.displayName ?: "Friend",
            authorUsername = profile?.username.orEmpty(),
            declaration = declaration.trim(),
            scriptureText = scriptureText.trim(),
            reference = reference.trim(),
            category = category,
            imageUrl = imageUrl,
            amenCount = 0,
            amenedBy = emptyList(),
            saveCount = 0,
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("public_declarations")
            .document(declarationId)
            .set(publicAffirmation)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to share declaration with community")
            }
    }

    /**
     * Starts live listening to the public declarations stream, optionally filtered by category.
     */
    fun startListeningToPublicDeclarations(category: String = "All") {
        declarationsListener?.remove()

        var query: Query = firestore.collection("public_declarations")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)

        if (category != "All") {
            query = query.whereEqualTo("category", category)
        }

        declarationsListener = query.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                return@addSnapshotListener
            }

            val items = snapshot.documents.mapNotNull { it.toObject(PublicAffirmation::class.java) }
            publicDeclarations.clear()
            publicDeclarations.addAll(items)
        }
    }

    fun stopListeningToPublicDeclarations() {
        declarationsListener?.remove()
        declarationsListener = null
    }

    /**
     * Toggles the "Amen" status on a public declaration.
     */
    fun toggleAmen(declarationId: String, currentAmenedBy: List<String>) {
        val uid = auth.currentUser?.uid ?: return
        val isAlreadyAmened = currentAmenedBy.contains(uid)
        val docRef = firestore.collection("public_declarations").document(declarationId)

        if (isAlreadyAmened) {
            docRef.update(
                "amenedBy", FieldValue.arrayRemove(uid),
                "amenCount", FieldValue.increment(-1)
            )
        } else {
            docRef.update(
                "amenedBy", FieldValue.arrayUnion(uid),
                "amenCount", FieldValue.increment(1)
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Friends: Search by Code, Username, or Email
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Searches for other users by:
     * - Friend Code (e.g. MK-7A4B)
     * - @username (e.g. @precious or precious)
     * - Email address
     */
    fun searchUsers(
        rawQuery: String,
        onResult: (List<UserProfile>) -> Unit,
        onError: (String) -> Unit
    ) {
        val query = rawQuery.trim()
        if (query.isBlank()) {
            onResult(emptyList())
            return
        }

        val myUid = auth.currentUser?.uid.orEmpty()

        // 1. Friend Code match (e.g. MK-XXXX)
        if (query.startsWith("MK-", ignoreCase = true) || (query.length == 7 && query[2] == '-')) {
            val code = query.uppercase(Locale.ROOT)
            firestore.collection("users")
                .whereEqualTo("friendCode", code)
                .get()
                .addOnSuccessListener { snapshot ->
                    val users = snapshot.documents
                        .mapNotNull { it.toObject(UserProfile::class.java) }
                        .filter { it.userId != myUid }
                    onResult(users)
                }
                .addOnFailureListener { e -> onError(e.localizedMessage ?: "Search failed") }
            return
        }

        // 2. Email match
        if (query.contains("@") && query.contains(".")) {
            val email = query.lowercase(Locale.ROOT)
            firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { snapshot ->
                    val users = snapshot.documents
                        .mapNotNull { it.toObject(UserProfile::class.java) }
                        .filter { it.userId != myUid }
                    onResult(users)
                }
                .addOnFailureListener { e -> onError(e.localizedMessage ?: "Search failed") }
            return
        }

        // 3. Username match
        val username = UsernameValidator.normalize(query)
        firestore.collection("users")
            .whereGreaterThanOrEqualTo("username", username)
            .whereLessThanOrEqualTo("username", username + "\uf8ff")
            .limit(15)
            .get()
            .addOnSuccessListener { snapshot ->
                val users = snapshot.documents
                    .mapNotNull { it.toObject(UserProfile::class.java) }
                    .filter { it.userId != myUid }
                onResult(users)
            }
            .addOnFailureListener { e -> onError(e.localizedMessage ?: "Search failed") }
    }

    /**
     * Adds a friend to the user's circle.
     */
    fun addFriend(
        targetUser: UserProfile,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val myUid = auth.currentUser?.uid ?: run {
            onError("Sign in to connect with friends")
            return
        }

        // Add to current user's friends subcollection
        firestore.collection("users").document(myUid)
            .collection("friends")
            .document(targetUser.userId)
            .set(targetUser)
            .addOnSuccessListener {
                // Also add reverse connection
                currentProfile?.let { me ->
                    firestore.collection("users").document(targetUser.userId)
                        .collection("friends")
                        .document(myUid)
                        .set(me)
                }
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to add friend")
            }
    }

    private fun listenToFriends(userId: String) {
        friendsListener?.remove()
        friendsListener = firestore.collection("users").document(userId)
            .collection("friends")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val friends = snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
                    friendsList.clear()
                    friendsList.addAll(friends)
                }
            }
    }
}
