package com.ott.platform.domain.usecase

import com.ott.platform.domain.model.AppMenuItem
import com.ott.platform.domain.model.OttResult
import com.ott.platform.domain.repository.CatalogRepository

class GetMenuUseCase(
    private val catalogRepository: CatalogRepository,
) {
    suspend operator fun invoke(): OttResult<List<AppMenuItem>> =
        catalogRepository.getMenu()
}
