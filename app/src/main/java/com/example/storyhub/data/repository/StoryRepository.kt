package com.example.storyhub.data.repository

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.storyhub.data.api.ApiConfig
import com.example.storyhub.data.api.ApiService
import com.example.storyhub.data.model.UserModel
import com.example.storyhub.data.paging.StoryPagingSource
import com.example.storyhub.data.preference.UserPreferences
import com.example.storyhub.data.preference.userPreferencesDataStore
import com.example.storyhub.data.response.*
import com.example.storyhub.data.result.ResultState
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.io.File

open class StoryRepository constructor(
    private val apiService: ApiService,
    private val preferences: UserPreferences
) {

    private val _storiesWithLocation = MutableLiveData<List<ListStoryItem>>()
    val storiesWithLocation: LiveData<List<ListStoryItem>> get() = _storiesWithLocation

    private val _storyDetail = MutableLiveData<ResultState<Story>>()
    val storyDetail: LiveData<ResultState<Story>> get() = _storyDetail

    fun userLogin(email: String, password: String) = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.login(email, password)
            emit(ResultState.Success(response.loginResult?.token)) // Allow token to be nullable
        } catch (e: HttpException) {
            val errorMessage = parseError(e.response())
            emit(ResultState.Error(Throwable(errorMessage)))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }

    fun userRegister(name: String, email: String, password: String) = liveData {
        emit(ResultState.Loading)
        try {
            val response = apiService.register(name, email, password)
            emit(ResultState.Success(response.message)) // Allow message to be nullable
        } catch (e: HttpException) {
            val errorMessage = parseError(e.response())
            emit(ResultState.Error(Throwable(errorMessage)))
        } catch (e: Exception) {
            emit(ResultState.Error(e))
        }
    }


    fun getRetrieveStories(token: String): LiveData<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = { StoryPagingSource(apiService, "Bearer $token") }
        ).liveData
    }

    fun fetchStoryDetail(token: String, id: String) {
        _storyDetail.value = ResultState.Loading
        apiService.detailStory("Bearer $token", id).enqueue(object : Callback<DetailStoryResponse> {
            override fun onResponse(
                call: Call<DetailStoryResponse>,
                response: Response<DetailStoryResponse>
            ) {
                if (response.isSuccessful) {
                    val story = response.body()?.story
                    if (story != null) {
                        _storyDetail.value = ResultState.Success(story)
                    } else {
                        _storyDetail.value = ResultState.Error(Throwable("Story not found"))
                    }
                } else {
                    val errorMessage = parseError(response)
                    _storyDetail.value = ResultState.Error(Throwable(errorMessage))
                }
            }

            override fun onFailure(call: Call<DetailStoryResponse>, t: Throwable) {
                _storyDetail.value = ResultState.Error(t)
            }
        })
    }

    fun submitImage(token: String, imageFile: File, description: String) = liveData {
        emit(ResultState.Loading)
        val descriptionBody = description.toRequestBody("text/plain".toMediaType())
        val imageBody = imageFile.asRequestBody("image/jpeg".toMediaType())
        val imagePart = MultipartBody.Part.createFormData("photo", imageFile.name, imageBody)
        try {
            val response = apiService.addStory("Bearer $token", imagePart, descriptionBody)
            emit(ResultState.Success("Story uploaded successfully"))
        } catch (e: HttpException) {
            val errorMessage = parseError(e.response())
            emit(ResultState.Error(Throwable(errorMessage)))
        }
    }

    suspend fun getStoriesWithLocation(token: String): List<ListStoryItem>? {
        return try {
            val response = apiService.getStoriesWithLocation("Bearer $token", 1)
            if (response.isSuccessful) {
                response.body()?.listStory?.filterNotNull()
            } else {
                Log.e("StoryRepository", "Error: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("StoryRepository", "Error: ${e.message}")
            null
        }
    }


    fun retrieveUserSession(): Flow<UserModel> = preferences.getUserSession()

    suspend fun storeUserSession(user: UserModel) {
        preferences.saveUserSession(user)
    }

    suspend fun userLogout() {
        preferences.clearUserSession()
    }

    private fun parseError(response: Response<*>?): String {
        return response?.errorBody()?.string() ?: "Unknown error occurred"
    }

    private fun logError(message: String?) {
        Log.e(TAG, "Error: ${message.orEmpty()}")

    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, context: Context): StoryRepository {
            val preferences = UserPreferences.apply {
                init(context.userPreferencesDataStore)
            }
            return instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, preferences).also { instance = it }
            }
        }
    }
}