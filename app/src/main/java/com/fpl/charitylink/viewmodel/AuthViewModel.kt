package com.fpl.charitylink.viewmodel

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.fpl.charitylink.data.local.UserPreferences
import com.fpl.charitylink.data.model.User
import com.fpl.charitylink.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.fpl.charitylink.data.model.Organization
import com.fpl.charitylink.data.repository.OrganizationRepository

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser, val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userRepository = UserRepository()
    private val userPrefs = UserPreferences(application)
    private val organizationRepository = OrganizationRepository()
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Expose cached role as StateFlow
    val cachedRole: StateFlow<String> = userPrefs.role
        .stateIn(viewModelScope, SharingStarted.Eagerly, "donor")

    // Expose cached user data as StateFlow
    val cachedUser: StateFlow<Map<String, String>> = userPrefs.userData
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // Expose cached language as StateFlow
    val cachedLanguage: StateFlow<String> = userPrefs.language
        .stateIn(viewModelScope, SharingStarted.Eagerly, "English")

    val currentUser: FirebaseUser? get() = auth.currentUser

    // --- Register ---
    fun register(email: String, password: String, fullName: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user!!

                val newUser = User(uid = user.uid, fullName = fullName, email = email, role = role)
                userRepository.saveUser(newUser)

                if (role == "association") {
                    val org = Organization(
                        uid = user.uid,
                        name = fullName,
                        email = email,
                        verified = false,
                        createdAt = System.currentTimeMillis()
                    )
                    organizationRepository.saveOrganization(org)
                }

                userPrefs.saveUser(user.uid, fullName, email, role, user.photoUrl?.toString())
                _authState.value = AuthState.Success(user, role)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    // --- Login ---
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user!!
                val firestoreUser = userRepository.syncUser(user.uid)
                val role = firestoreUser?.role ?: "donor"
                userPrefs.saveUser(
                    uid = user.uid,
                    fullName = firestoreUser?.fullName ?: user.displayName ?: "",
                    email = user.email ?: "",
                    role = role,
                    photoUrl = user.photoUrl?.toString()
                )
                _authState.value = AuthState.Success(user, role)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    // --- Google Sign-In ---
    fun signInWithGoogle(context: Context, role: String = "donor") {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(context, request)
                val googleIdToken = GoogleIdTokenCredential.createFrom(result.credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user!!

                val doc = db.collection("users").document(user.uid).get().await()
                val existingRole = if (doc.exists()) {
                    doc.getString("role") ?: role
                } else {
                    val newUser = User(
                        uid = user.uid,
                        fullName = user.displayName ?: "",
                        email = user.email ?: "",
                        role = role
                    )
                    userRepository.saveUser(newUser)
                    if (role == "association") {
                        val org = Organization(
                            uid = user.uid,
                            name = user.displayName ?: "",
                            email = user.email ?: "",
                            verified = false,
                            createdAt = System.currentTimeMillis()
                        )
                        organizationRepository.saveOrganization(org)
                    }
                    role
                }
                userPrefs.saveUser(
                    uid = user.uid,
                    fullName = user.displayName ?: "",
                    email = user.email ?: "",
                    role = existingRole,
                    photoUrl = user.photoUrl?.toString()
                )
                _authState.value = AuthState.Success(user, existingRole)
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    // --- Sync profile from Firestore (call on app start) ---
    fun syncUserProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val user = userRepository.syncUser(uid) ?: return@launch
            userPrefs.saveUser(
                uid = user.uid,
                fullName = user.fullName,
                email = user.email,
                role = user.role,
                photoUrl = user.photoUrl
            )
        }
    }

    // --- Fetch role (used in NavHost) ---
    fun fetchCurrentUserRole(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val cached = cachedRole.value
            if (cached.isNotEmpty()) {
                onResult(cached)
                return@launch
            }
            val role = userRepository.syncUser(uid)?.role ?: "donor"
            onResult(role)
        }
    }

    // --- Save language preference ---
    fun saveLanguage(language: String) {
        viewModelScope.launch {
            userPrefs.saveLanguage(language)
        }
    }

    // --- Upload profile photo and persist URL ---
    fun updatePhotoUrl(
        imageUri: Uri,
        onResult: (success: Boolean, error: String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: run {
            onResult(false, "Not logged in")
            return
        }
        viewModelScope.launch {
            try {
                val storageRef = FirebaseStorage.getInstance()
                    .reference
                    .child("profile_photos/$uid.jpg")
                // Upload bytes
                storageRef.putFile(imageUri).await()
                // Get public download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()
                // Persist to Firestore
                userRepository.updateUser(uid, mapOf("photoUrl" to downloadUrl))
                // Sync local cache (read current snapshot from the StateFlow)
                val current = cachedUser.value
                userPrefs.saveUser(
                    uid = uid,
                    fullName = current["fullName"] ?: "",
                    email = current["email"] ?: "",
                    role = current["role"] ?: "",
                    photoUrl = downloadUrl
                )
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.message ?: "Upload failed")
            }
        }
    }

    // --- Logout ---
    fun logout() {
        viewModelScope.launch {
            userPrefs.clear()
            auth.signOut()
            _authState.value = AuthState.Idle
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    companion object {
        const val WEB_CLIENT_ID = "643180925766-okslpbei5thamd72ak30enos88skn9la.apps.googleusercontent.com"
    }
}
