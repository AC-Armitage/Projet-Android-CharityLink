package com.fpl.charitylink

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fpl.charitylink.data.local.UserPreferences
import com.fpl.charitylink.ui.navigation.CharityLinkNavHost
import com.fpl.charitylink.ui.theme.CharityLinkTheme
import com.fpl.charitylink.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved locale BEFORE super.onCreate so the layout inflates correctly
        lifecycleScope.launch {
            val prefs = UserPreferences(applicationContext)
            val language = prefs.language.first()
            applyLocale(language)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CharityLinkTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .systemBarsPadding()
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

    companion object {
        fun applyLocale(language: String) {
            val tag = when (language) {
                "French" -> "fr"
                "Arabic" -> "ar"
                else -> "en"
            }
            // API 33+: per-app language API (no activity recreate needed)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
            } else {
                // API 28–32: set via AppCompatDelegate (triggers recreate automatically)
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
            }
        }
    }
}
