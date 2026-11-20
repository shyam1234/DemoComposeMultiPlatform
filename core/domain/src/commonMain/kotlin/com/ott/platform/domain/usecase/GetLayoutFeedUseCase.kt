package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.HomeFeed
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository

class GetLayoutFeedUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(layoutId: String, limitPerShelf: Int = 24): OttResult<HomeFeed> {
        require(layoutId.isNotBlank()) { "layoutId must not be blank" }
        require(limitPerShelf > 0) { "limitPerShelf must be > 0" }
        return catalogRepository.getLayoutFeed(layoutId, limitPerShelf)
    }
}
