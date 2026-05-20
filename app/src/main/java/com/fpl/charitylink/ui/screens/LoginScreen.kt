package com.fpl.charitylink.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.fpl.charitylink.viewmodel.AuthState
import com.fpl.charitylink.viewmodel.AuthViewModel
import com.fpl.charitylink.ui.theme.*

@Composable
fun LoginScreen(
    onLogin: () -> Unit,
    onCreateAccount: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    // Google Sign-In launcher
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            authViewModel.handleGoogleSignInResult(account.idToken)
        } catch (e: ApiException) {
            // cancelled or failed — ViewModel stays in Idle
        }
    }

    // Navigate on success
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            onLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.size(220.dp).align(Alignment.TopEnd).background(PrimaryFixed.copy(alpha = 0.2f), CircleShape))
        Box(modifier = Modifier.size(220.dp).align(Alignment.BottomStart).background(TertiaryFixedDim.copy(alpha = 0.2f), CircleShape))

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "CharityLink", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(text = "Welcome back", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Continue your journey of making an impact today.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(20.dp))

                    // Error message
                    if (errorMessage != null) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(text = "Email", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = email, onValueChange = { email = it }, singleLine = true,
                        placeholder = { Text("example_fpl@gmail.com") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceContainerLow, unfocusedContainerColor = SurfaceContainerLow, focusedIndicatorColor = MaterialTheme.colorScheme.primary, unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Password", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = password, onValueChange = { password = it }, singleLine = true,
                        placeholder = { Text("••••••••") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        visualTransformation = PasswordVisualTransformation(),
                        trailingIcon = { Icon(imageVector = Icons.Outlined.Visibility, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceContainerLow, unfocusedContainerColor = SurfaceContainerLow, focusedIndicatorColor = MaterialTheme.colorScheme.primary, unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant)
                    )
                    TextButton(onClick = { }, modifier = Modifier.align(Alignment.End)) {
                        Text(text = "Forgot password?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Email login button
                    Button(
                        onClick = { authViewModel.login(email, password) },
                        enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text(text = "Log in", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Google Sign-In button
                    OutlinedButton(
                        onClick = {
                            val intent = authViewModel.getGoogleSignInIntent(context as Activity)
                            googleLauncher.launch(intent)
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(text = "Continue with Google", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onCreateAccount) {
                Text(text = "Don't have an account? Register", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}