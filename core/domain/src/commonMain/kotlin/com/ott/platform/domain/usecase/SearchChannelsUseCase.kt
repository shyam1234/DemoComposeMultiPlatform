package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository

class SearchChannelsUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(query: String, limit: Int = 40): OttResult<List<Channel>> {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return OttResult.Success(emptyList())
        }
        return catalogRepository.search(trimmed, limit)
    }
}
