package com.ott.platform.domain.repository

import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult

interface CatalogRepository {
    suspend fun getCategories(): OttResult<List<Category>>
    suspend fun getHomeFeed(limitPerShelf: Int = 24): OttResult<HomeFeed>
    suspend fun getChannelsByCategory(categoryId: String, limit: Int = 40): OttResult<List<Channel>>
    suspend fun search(query: String, limit: Int = 40): OttResult<List<Channel>>
    suspend fun getChannel(id: ContentId): OttResult<Channel>
}
