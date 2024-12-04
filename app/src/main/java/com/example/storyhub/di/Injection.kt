package com.example.storyhub.di


import android.content.Context
import com.example.storyhub.data.api.ApiConfig
import com.example.storyhub.data.preference.UserPreferences
import com.example.storyhub.data.preference.userPreferencesDataStore
import com.example.storyhub.data.repository.StoryRepository


object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val dataStore = context.userPreferencesDataStore
        UserPreferences.init(dataStore)
        val apiService = ApiConfig.getApiService()
        return StoryRepository.getInstance(apiService, context)
    }
}



