package com.example.storyhub.view

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyhub.data.model.UserModel
import com.example.storyhub.data.repository.StoryRepository
import com.example.storyhub.data.response.ListStoryItem
import com.example.storyhub.data.result.ResultState
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(private val storyRepository: StoryRepository) : ViewModel() {

    val storyDetail = storyRepository.storyDetail

    fun userLogin(email: String, password: String): LiveData<ResultState<String?>> {
        return storyRepository.userLogin(email, password)
    }

    fun userRegister(name: String, email: String, password: String) = storyRepository.userRegister(name, email, password)

    fun getRetrieveStories(token: String): LiveData<PagingData<ListStoryItem>> = storyRepository.getRetrieveStories(token).cachedIn(viewModelScope)

    fun fetchStoryDetail(authToken: String, storyId: String) = storyRepository.fetchStoryDetail(authToken, storyId)

    fun submitImage(authToken: String, imageFile: File, imageDescription: String) = storyRepository.submitImage(authToken, imageFile, imageDescription)


    fun storeUserSession(user: UserModel) {
        viewModelScope.launch {
            storyRepository.storeUserSession(user)
        }
    }

    fun retrieveUserSession(): LiveData<UserModel> {
        return storyRepository.retrieveUserSession().asLiveData()
    }

    fun userLogout() {
        viewModelScope.launch {
            storyRepository.userLogout()
        }
    }
    // MutableLiveData untuk stories dengan lokasi
    private val _storiesWithLocation = MutableLiveData<List<ListStoryItem>>()
    val storiesWithLocation: LiveData<List<ListStoryItem>> get() = _storiesWithLocation

    // Fetch stories dengan lokasi
    fun fetchStoriesWithLocation(token: String) {
        viewModelScope.launch {
            try {
                val response = storyRepository.getStoriesWithLocation(token)
                if (response.isNullOrEmpty()) {
                    Log.e("MainViewModel", "No stories with location found")
                    _storiesWithLocation.postValue(emptyList())
                } else {
                    _storiesWithLocation.postValue(response)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching stories with location: ${e.message}")
                _storiesWithLocation.postValue(emptyList()) // Clear the list if an error occurs
            }
        }
    }
}
