package com.fpl.charitylink.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fpl.charitylink.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.fpl.charitylink.data.repository.OrganizationRepository
import com.fpl.charitylink.data.repository.UserRepository
import com.fpl.charitylink.viewmodel.AuthViewModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ── ViewModel ──────────────────────────────────────────────
sealed class EditProfileState {
    object Idle : EditProfileState()
    object Loading : EditProfileState()
    object Success : EditProfileState()
    data class Error(val message: String) : EditProfileState()
}

class EditProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val organizationRepository = OrganizationRepository()
    private val storage = FirebaseStorage.getInstance()

    private val _state = MutableStateFlow<EditProfileState>(EditProfileState.Idle)
    val state: StateFlow<EditProfileState> = _state

    private val _organizationName = MutableStateFlow<String?>(null)
    val organizationName: StateFlow<String?> = _organizationName

    private val _organizationLogoUrl = MutableStateFlow<String?>(null)
    val organizationLogoUrl: StateFlow<String?> = _organizationLogoUrl

    // Associations have a separate Organization doc whose logoUrl is what actually
    // renders on their profile/cards — load it so we can prefill the name + photo too.
    fun loadOrganizationIfAssociation(uid: String, role: String) {
        if (role != "association") return
        viewModelScope.launch {
            val org = organizationRepository.getOrganization(uid)
            _organizationName.value = org?.name
            _organizationLogoUrl.value = org?.logoUrl
        }
    }

    fun saveProfile(uid: String, role: String, fullName: String, selectedPhotoUri: Uri?) {
        if (fullName.isBlank()) {
            _state.value = EditProfileState.Error("Name cannot be empty")
            return
        }
        viewModelScope.launch {
            _state.value = EditProfileState.Loading
            try {
                val photoUrl = selectedPhotoUri?.let { uploadProfilePhoto(uid, it) }

                if (role == "association") {
                    val updates = mutableMapOf<String, Any>("name" to fullName)
                    if (photoUrl != null) updates["logoUrl"] = photoUrl
                    organizationRepository.updateOrganization(uid, updates)
                } else {
                    val updates = mutableMapOf<String, Any>("fullName" to fullName)
                    if (photoUrl != null) updates["photoUrl"] = photoUrl
                    userRepository.updateUser(uid, updates)
                }
                _state.value = EditProfileState.Success
            } catch (e: Exception) {
                _state.value = EditProfileState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun deletePhoto(uid: String, role: String) {
        viewModelScope.launch {
            _state.value = EditProfileState.Loading
            try {
                try {
                    storage.reference.child("avatars/$uid/profile.jpg").delete().await()
                } catch (_: Exception) {
                    // Nothing to delete (e.g. user never uploaded one) — not a failure case.
                }
                if (role == "association") {
                    organizationRepository.updateOrganization(uid, mapOf("logoUrl" to ""))
                } else {
                    userRepository.updateUser(uid, mapOf("photoUrl" to ""))
                }
                _state.value = EditProfileState.Success
            } catch (e: Exception) {
                _state.value = EditProfileState.Error(e.message ?: "Failed to remove photo")
            }
        }
    }

    private suspend fun uploadProfilePhoto(uid: String, uri: Uri): String {
        // Path matches the existing storage.rules entry for avatars/{userId}/{fileName},
        // so this works for both donor and association accounts without a rules change.
        val ref = storage.reference.child("avatars/$uid/profile.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    fun resetState() { _state.value = EditProfileState.Idle }
}

// ── Screen ─────────────────────────────────────────────────
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    editProfileViewModel: EditProfileViewModel = viewModel()
) {
    val firebaseUser = authViewModel.currentUser
    val cachedUser by authViewModel.cachedUser.collectAsState()
    val role by authViewModel.cachedRole.collectAsState()
    val isAssociation = role == "association"
    val organizationName by editProfileViewModel.organizationName.collectAsState()
    val organizationLogoUrl by editProfileViewModel.organizationLogoUrl.collectAsState()

    LaunchedEffect(firebaseUser?.uid, role) {
        firebaseUser?.uid?.let { editProfileViewModel.loadOrganizationIfAssociation(it, role) }
    }

    val initialName = if (isAssociation) {
        organizationName ?: ""
    } else {
        cachedUser["fullName"]?.ifBlank { null } ?: firebaseUser?.displayName ?: ""
    }
    val email = cachedUser["email"]?.ifBlank { null }
        ?: firebaseUser?.email ?: ""
    val photoUrl = if (isAssociation) {
        organizationLogoUrl?.ifBlank { null }
    } else {
        cachedUser["photoUrl"]?.ifBlank { null } ?: firebaseUser?.photoUrl?.toString()
    }

    var fullName by rememberSaveable(initialName) { mutableStateOf(initialName) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var photoRemoved by remember { mutableStateOf(false) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            photoRemoved = false
        }
    }

    val editState by editProfileViewModel.state.collectAsState()
    val isLoading = editState is EditProfileState.Loading
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on success or error
    LaunchedEffect(editState) {
        when (editState) {
            is EditProfileState.Success -> {
                authViewModel.syncUserProfile()
                snackbarHostState.showSnackbar("Profile updated successfully!")
                editProfileViewModel.resetState()
                onBack()
            }
            is EditProfileState.Error -> {
                snackbarHostState.showSnackbar((editState as EditProfileState.Error).message)
                editProfileViewModel.resetState()
            }
            else -> Unit
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove photo?") },
            text = { Text(if (isAssociation) "This will remove your organization's logo." else "This will remove your profile photo.") },
            confirmButton = {
                TextButton(onClick = {
                    showRemoveDialog = false
                    selectedPhotoUri = null
                    photoRemoved = true
                    firebaseUser?.uid?.let { editProfileViewModel.deletePhoto(it, role) }
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showRemoveDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAssociation) "Edit Organization Profile" else "Edit Profile",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with camera button
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable(enabled = !isLoading) {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val imageToShow = if (photoRemoved) null else (selectedPhotoUri ?: photoUrl)
                    if (imageToShow != null) {
                        AsyncImage(
                            model = imageToShow,
                            contentDescription = if (isAssociation) "Organization logo" else "Profile photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = fullName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Change photo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = !isLoading
                ) {
                    Text(if (isAssociation) "Change Logo" else "Change Photo")
                }
                val hasPhotoToRemove = !photoRemoved && (selectedPhotoUri != null || photoUrl != null)
                if (hasPhotoToRemove) {
                    TextButton(
                        onClick = { showRemoveDialog = true },
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Name field (org name for associations, full name for donors)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isAssociation) "Organization Name" else "Full Name",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        placeholder = { Text(if (isAssociation) "Enter your organization name" else "Enter your full name") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Email field (read only)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.email),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = stringResource(R.string.email_cannot_change),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save button
            Button(
                onClick = {
                    firebaseUser?.uid?.let {
                        editProfileViewModel.saveProfile(it, role, fullName, selectedPhotoUri)
                    }
                },
                enabled = fullName.isNotBlank() && (fullName != initialName || selectedPhotoUri != null) && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(text = stringResource(R.string.save_changes), style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cancel button
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(text = "Cancel", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
