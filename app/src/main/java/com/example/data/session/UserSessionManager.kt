package com.example.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tolet_user_session")

class UserSessionManager(private val context: Context) {

    companion object {
        val KEY_PHONE = stringPreferencesKey("phone")
        val KEY_NAME = stringPreferencesKey("name")
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_PASSWORD = stringPreferencesKey("password")
        val KEY_ROLE = stringPreferencesKey("role")
        val KEY_REMEMBER = booleanPreferencesKey("remember_me")
    }

    val currentUserFlow: Flow<User?> = context.dataStore.data.map { preferences ->
        val phone = preferences[KEY_PHONE]
        val name = preferences[KEY_NAME]
        if (!phone.isNullOrBlank() && !name.isNullOrBlank()) {
            User(
                phone = phone,
                name = name,
                email = preferences[KEY_EMAIL] ?: "",
                password = preferences[KEY_PASSWORD] ?: "",
                role = preferences[KEY_ROLE] ?: "user"
            )
        } else {
            null
        }
    }

    suspend fun saveUser(user: User, rememberMe: Boolean = true) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PHONE] = user.phone
            preferences[KEY_NAME] = user.name
            preferences[KEY_EMAIL] = user.email
            preferences[KEY_PASSWORD] = user.password
            preferences[KEY_ROLE] = user.role
            preferences[KEY_REMEMBER] = rememberMe
        }
    }

    suspend fun clearUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_PHONE)
            preferences.remove(KEY_NAME)
            preferences.remove(KEY_EMAIL)
            preferences.remove(KEY_PASSWORD)
            preferences.remove(KEY_ROLE)
        }
    }
}
