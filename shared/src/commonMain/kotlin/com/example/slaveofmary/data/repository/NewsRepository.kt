package com.example.slaveofmary.data.repository

import com.example.slaveofmary.data.entity.NewsItem
import com.example.slaveofmary.remote.ApiService
import kotlinx.datetime.Instant

class NewsRepository(private val apiService: ApiService) {

    suspend fun fetchNews(): List<NewsItem>? {

        val response = apiService.getNewsFeed() ?: return null

        return response.map { item ->
            val parsedTimestamp = try {
                item.publish_date?.let { Instant.parse(it).toEpochMilliseconds() }
            } catch (e: Exception) {
                null
            }

            NewsItem(
                title = item.title ?: "",
                content = item.content ?: "",
                timestamp = parsedTimestamp,
                isVisible = item.is_visible ?: true,
                actionLink = item.action_link,
                imageUrl = item.image_url
            )
        }
    }
}