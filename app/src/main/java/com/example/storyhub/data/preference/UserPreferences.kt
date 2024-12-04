package com.example.storyhub.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.storyhub.data.model.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

object UserPreferences {

    private lateinit var dataStore: DataStore<Preferences>

    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_PASSWORD_KEY = stringPreferencesKey("user_password")
    private val USER_TOKEN_KEY = stringPreferencesKey("user_token")
    private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")

    fun init(dataStore: DataStore<Preferences>) {
        UserPreferences.dataStore = dataStore
    }


    suspend fun saveUserSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences.apply {
                this[USER_EMAIL_KEY] = user.email
                this[USER_PASSWORD_KEY] = user.password
                this[USER_TOKEN_KEY] = user.token
                this[IS_LOGGED_IN_KEY] = true
            }
        }
    }


    fun getUserSession(): Flow<UserModel> {
        return dataStore.data.map { preferences ->
            UserModel(
                email = preferences[USER_EMAIL_KEY] ?: "",
                password = preferences[USER_PASSWORD_KEY] ?: "",
                token = preferences[USER_TOKEN_KEY] ?: "",
                isLogin = preferences[IS_LOGGED_IN_KEY] ?: false
            )
        }
    }


    suspend fun clearUserSession() {
        dataStore.edit { it.clear() }
    }

}
