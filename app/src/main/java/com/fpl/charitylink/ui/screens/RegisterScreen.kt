package com.fpl.charitylink.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpl.charitylink.viewmodel.AuthState
import com.fpl.charitylink.viewmodel.AuthViewModel
import com.fpl.charitylink.ui.theme.*

@Composable
fun RegisterScreen(
    onRegister: () -> Unit,
    onBackToLogin: () -> Unit,
    role: String = "donor",             // ← new
    authViewModel: AuthViewModel = viewModel()
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    var showFacebookComingSoon by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState is AuthState.Loading
    val errorMessage = (authState as? AuthState.Error)?.message

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            onRegister()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
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
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
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
                    Text(text = "Create your account", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Join our community of impactful donors", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(20.dp))

                    // Error message
                    if (errorMessage != null) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Password mismatch warning
                    if (confirmPassword.isNotBlank() && password != confirmPassword) {
                        Text(text = "Passwords do not match", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(text = "Full Name", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(value = fullName, onValueChange = { fullName = it }, singleLine = true, placeholder = { Text("John Doe") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next), colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceContainerLow, unfocusedContainerColor = SurfaceContainerLow, focusedIndicatorColor = MaterialTheme.colorScheme.primary, unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Email", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(value = email, onValueChange = { email = it }, singleLine = true, placeholder = { Text("example_fpl@gmail.com") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next), colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceContainerLow, unfocusedContainerColor = SurfaceContainerLow, focusedIndicatorColor = MaterialTheme.colorScheme.primary, unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Password", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(value = password, onValueChange = { password = it }, singleLine = true, placeholder = { Text("••••••••") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next), visualTransformation = PasswordVisualTransformation(), colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceContainerLow, unfocusedContainerColor = SurfaceContainerLow, focusedIndicatorColor = MaterialTheme.colorScheme.primary, unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Confirm Password", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(value = confirmPassword, onValueChange = { confirmPassword = it }, singleLine = true, placeholder = { Text("••••••••") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done), visualTransformation = PasswordVisualTransformation(), colors = TextFieldDefaults.colors(focusedContainerColor = SurfaceContainerLow, unfocusedContainerColor = SurfaceContainerLow, focusedIndicatorColor = MaterialTheme.colorScheme.primary, unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceVariant))

                    Spacer(modifier = Modifier.height(20.dp))
                    val isFormValid = fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && password == confirmPassword
                    Button(
                        onClick = { authViewModel.register(email, password, fullName, role) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isFormValid && !isLoading,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Text(text = "Register", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = "  or  ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { authViewModel.signInWithGoogle(context, role) },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(text = "Continue with Google", style = MaterialTheme.typography.labelLarge)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { showFacebookComingSoon = true },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(text = "Continue with Facebook", style = MaterialTheme.typography.labelLarge)
                    }
                    if (showFacebookComingSoon) {
                        LaunchedEffect(Unit) {
                            android.widget.Toast.makeText(
                                context,
                                "Facebook login is coming soon",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            showFacebookComingSoon = false
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBackToLogin) {
                Text(text = "Already have an account? Log in", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "By creating an account, you agree to our Terms of Service and Privacy Policy.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp), textAlign = TextAlign.Center)
        }
    }
}