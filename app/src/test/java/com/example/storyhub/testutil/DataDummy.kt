// DataDummy.kt
package com.example.storyhub.testutil

import com.example.storyhub.data.response.ListStoryItem

object DataDummy {
    fun generateDummyStories(): List<ListStoryItem> {
        return listOf(
            ListStoryItem(
                id = "1",
                name = "Story 1",
                description = "Description 1",
                photoUrl = "https://example.com/story1.jpg",
                createdAt = "2024-01-01",
                lat = 0.0,
                lon = 0.0
            ),
            ListStoryItem(
                id = "2",
                name = "Story 2",
                description = "Description 2",
                photoUrl = "https://example.com/story2.jpg",
                createdAt = "2024-01-02",
                lat = 1.0,
                lon = 1.0
            )
        )
    }

    fun generateEmptyStories(): List<ListStoryItem> {
        return emptyList()
    }
}
