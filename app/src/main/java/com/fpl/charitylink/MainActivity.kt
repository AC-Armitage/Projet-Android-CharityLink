package com.fpl.charitylink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.fpl.charitylink.ui.theme.CharityLinkTheme
import com.fpl.charitylink.ui.navigation.CharityLinkNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CharityLinkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val authViewModel: AuthViewModel = viewModel()
                    // Sync profile in background on app start
                    LaunchedEffect(Unit) {
                        authViewModel.syncUserProfile()
                    }
                    CharityLinkNavHost(authViewModel = authViewModel)
                }
            }
        }
    }
}