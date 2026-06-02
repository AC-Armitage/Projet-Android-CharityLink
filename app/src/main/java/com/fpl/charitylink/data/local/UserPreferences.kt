package com.fpl.charitylink.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(private val context: Context) {

    companion object {
        val KEY_UID = stringPreferencesKey("uid")
        val KEY_FULL_NAME = stringPreferencesKey("full_name")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_PHOTO_URL = stringPreferencesKey("photo_url")
    }

    // Save user to local cache
    suspend fun saveUser(
        uid: String,
        fullName: String,
        email: String,
        role: String,
        photoUrl: String? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_UID] = uid
            prefs[KEY_FULL_NAME] = fullName
            prefs[KEY_EMAIL] = email
            prefs[KEY_ROLE] = role
            prefs[KEY_PHOTO_URL] = photoUrl ?: ""
        }
    }

    // Get role as Flow (reactive)
    val role: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[KEY_ROLE] ?: "donor" }

    // Get full cached user as Flow
    val userData: Flow<Map<String, String>> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            mapOf(
                "uid" to (prefs[KEY_UID] ?: ""),
                "fullName" to (prefs[KEY_FULL_NAME] ?: ""),
                "email" to (prefs[KEY_EMAIL] ?: ""),
                "role" to (prefs[KEY_ROLE] ?: "donor"),
                "photoUrl" to (prefs[KEY_PHOTO_URL] ?: "")
            )
        }

    // Clear cache on logout
    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}