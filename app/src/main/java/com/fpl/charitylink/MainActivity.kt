package com.fpl.charitylink

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "English") ?: "English"
        val locale = languageToLocale(language)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sync DataStore -> SharedPreferences on startup so attachBaseContext
        // always has the latest value on next recreate.
        lifecycleScope.launch {
            val prefs = UserPreferences(applicationContext)
            val language = prefs.language.first()
            persistLocale(language)
        }

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
        fun languageToLocale(language: String): Locale = when (language) {
            "French" -> Locale.FRENCH
            "Arabic" -> Locale("ar")
            else -> Locale.ENGLISH
        }

        /**
         * Write language to SharedPreferences synchronously (commit, not apply)
         * so the value is guaranteed to be there when recreate() triggers
         * attachBaseContext on the new instance.
         */
        fun applyLocale(context: Context, language: String) {
            context.getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("language", language)
                .commit() // synchronous — must be committed before recreate()
        }
    }

    private fun persistLocale(language: String) {
        getSharedPreferences("locale_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("language", language)
            .commit()
    }
}
