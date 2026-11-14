package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.Channel
import com.ott.platform.domain.model.ContentId
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository

class GetChannelDetailsUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(id: ContentId): OttResult<Channel> =
        catalogRepository.getChannel(id)
}
