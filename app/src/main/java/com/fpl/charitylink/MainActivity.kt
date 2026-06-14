package com.fpl.charitylink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpl.charitylink.ui.theme.CharityLinkTheme
import com.fpl.charitylink.ui.navigation.CharityLinkNavHost
import com.fpl.charitylink.viewmodel.AuthViewModel
import androidx.compose.foundation.layout.systemBarsPadding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CharityLinkTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding(), // ← add this — handles both top and bottom

                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    LaunchedEffect(Unit) {
                        authViewModel.syncUserProfile()
                    }
                    CharityLinkNavHost(authViewModel = authViewModel)
                }
            }
        }
    }
}


