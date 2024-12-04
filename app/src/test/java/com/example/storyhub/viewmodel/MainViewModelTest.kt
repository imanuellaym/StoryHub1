package com.example.storyhub.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import com.example.storyhub.collectData
import com.example.storyhub.data.repository.StoryRepository
import com.example.storyhub.testutil.DataDummy
import com.example.storyhub.testutil.LiveDataTestUtil.getOrAwaitValue
import com.example.storyhub.view.MainViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import com.example.storyhub.testutil.MainDispatcherRule

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mainViewModel = MainViewModel(storyRepository)
    }

    @Test
    fun `verify stories are fetched successfully`() = runTest {
        val dummyStories = DataDummy.generateDummyStories()
        val expectedPagingData = PagingData.from(dummyStories)

        // Mocking storyRepository method
        `when`(storyRepository.getRetrieveStories("token"))
            .thenReturn(MutableLiveData(expectedPagingData))

        // Act
        val actualPagingData = mainViewModel.getRetrieveStories("token").getOrAwaitValue()
        val actualData = actualPagingData.collectData() // Convert PagingData to List

        // Assert
        assertNotNull(actualData)
        assertEquals(dummyStories.size, actualData.size)
    }


    @Test
    fun `verify no stories are returned when repository is empty`() = runTest {
        val dummyStories = DataDummy.generateEmptyStories()
        val expectedPagingData = PagingData.from(dummyStories)

        `when`(storyRepository.getRetrieveStories("token"))
            .thenReturn(MutableLiveData(expectedPagingData))

        val actualPagingData = mainViewModel.getRetrieveStories("token").getOrAwaitValue()
        val actualData = actualPagingData.collectData() // Convert to List

        // Assert
        assertNotNull(actualData)
        assertTrue(actualData.isEmpty())
    }

}
