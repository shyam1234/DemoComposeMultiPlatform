package com.ott.platform.data.iptv

import com.ott.platform.domain.model.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val description: String = "",
)

fun CategoryDto.toDomain(): Category = Category(
    id = id,
    name = name,
    description = description,
)
