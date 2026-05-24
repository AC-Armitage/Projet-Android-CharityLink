package com.fpl.charitylink.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: FirebaseUser, val role: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    val currentUser: FirebaseUser? get() = auth.currentUser

    // --- Register with role ---
    fun register(email: String, password: String, fullName: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user!!
                // Save user data to Firestore
                db.collection("users").document(user.uid).set(
                    mapOf(
                        "uid" to user.uid,
                        "fullName" to fullName,
                        "email" to email,
                        "role" to role,
                        "createdAt" to System.currentTimeMillis()
                    )
                ).await()
                _authState.value = AuthState.Success(user, role)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    // --- Login then fetch role ---
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user!!
                val role = fetchRole(user.uid)
                _authState.value = AuthState.Success(user, role)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    // --- Google Sign-In then fetch/create role ---
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

                // Check if user already exists in Firestore
                val doc = db.collection("users").document(user.uid).get().await()
                val existingRole = if (doc.exists()) {
                    doc.getString("role") ?: role
                } else {
                    // New Google user — save to Firestore
                    db.collection("users").document(user.uid).set(
                        mapOf(
                            "uid" to user.uid,
                            "fullName" to (user.displayName ?: ""),
                            "email" to (user.email ?: ""),
                            "role" to role,
                            "createdAt" to System.currentTimeMillis()
                        )
                    ).await()
                    role
                }
                _authState.value = AuthState.Success(user, existingRole)
            } catch (e: GetCredentialException) {
                _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    // --- Fetch role from Firestore ---
    private suspend fun fetchRole(uid: String): String {
        return try {
            val doc = db.collection("users").document(uid).get().await()
            doc.getString("role") ?: "donor"
        } catch (e: Exception) {
            "donor"
        }
    }

    // --- Fetch role for already logged in user ---
    fun fetchCurrentUserRole(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val role = fetchRole(uid)
            onResult(role)
        }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    companion object {
        const val WEB_CLIENT_ID = "643180925766-okslpbei5thamd72ak30enos88skn9la.apps.googleusercontent.com"
    }
}