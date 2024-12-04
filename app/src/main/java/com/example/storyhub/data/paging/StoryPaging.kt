package com.example.storyhub.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storyhub.data.api.ApiService
import com.example.storyhub.data.response.ListStoryItem
import retrofit2.HttpException
import java.io.IOException

class StoryPagingSource(private val apiService: ApiService, private val token: String) : PagingSource<Int, ListStoryItem>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
        const val TAG = "StoryPagingSource"
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        val position = params.key ?: INITIAL_PAGE_INDEX
        return try {
            val responseData = apiService.getStories(token, position, params.loadSize)

            val stories = responseData.listStory?.filterNotNull() ?: emptyList()

            LoadResult.Page(
                data = stories,
                prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                nextKey = if (stories.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            Log.e(TAG, "IOException: ${exception.localizedMessage}")
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            Log.e(TAG, "HttpException: ${exception.localizedMessage}")
            LoadResult.Error(exception)
        } catch (exception: Exception) {
            Log.e(TAG, "Error: ${exception.localizedMessage}")
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
