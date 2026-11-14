package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository

class GetHomeFeedUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(limitPerShelf: Int = 24): OttResult<HomeFeed> {
        require(limitPerShelf > 0) { "limitPerShelf must be > 0" }
        return catalogRepository.getHomeFeed(limitPerShelf)
    }
}
