package com.ott.platform.data.iptv

import com.ott.platform.domain.model.Category
import com.ott.platform.domain.model.Channel
import com.ott.platform.logging.OttLogger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

class IptvCatalogRemoteDataSource(
    private val httpClient: HttpClient,
    private val logger: OttLogger,
) {
    suspend fun fetchCategories(): List<Category> {
        val dtos: List<CategoryDto> = httpClient.get(IptvOrgEndpoints.CATEGORIES_JSON).body()
        return dtos
            .filter { it.id != "xxx" }
            .map { it.toDomain() }
    }

    suspend fun fetchCategoryPlaylist(
        categoryId: String,
        categoryName: String,
        limit: Int,
    ): List<Channel> {
        val url = IptvOrgEndpoints.categoryPlaylist(categoryId)
        logger.d(TAG, "Fetching playlist $url")
        val body = httpClient.get(url).bodyAsText()
        return M3uPlaylistParser.parse(
            playlist = body,
            categoryId = categoryId,
            categoryName = categoryName,
            limit = limit,
        )
    }

    private companion object {
        const val TAG = "IptvRemote"
    }
}
