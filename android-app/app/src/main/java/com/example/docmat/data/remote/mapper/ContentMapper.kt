package com.example.docmat.data.remote.mapper

import com.example.docmat.data.remote.dto.ContentDetailDto
import com.example.docmat.data.remote.dto.ContentItemDto
import com.example.docmat.data.remote.dto.ContentStatsDto
import com.example.docmat.domain.model.Content
import com.example.docmat.domain.model.ContentStats
import com.example.docmat.domain.model.ContentType

/**
 * Extension function untuk convert ContentItemDto ke Content domain model
 */
fun ContentItemDto.toContent(): Content {
    return Content(
        id = this.id,
        title = this.title,
        description = this.description,
        type = ContentType.fromString(this.type),
        category = this.category,
        imageUrl = this.imageUrl,
        source = this.source,
        date = this.date,
        publishedAt = this.publishedAt
    )
}

/**
 * Extension function untuk convert ContentDetailDto ke Content domain model
 */
fun ContentDetailDto.toContent(): Content {
    return Content(
        id = this.id,
        title = this.title,
        description = this.description,
        content = this.content,
        type = ContentType.fromString(this.type),
        category = this.category,
        imageUrl = this.imageUrl,
        source = this.source,
        date = this.date,
        publishedAt = this.publishedAt,
        url = this.url
    )
}

/**
 * Extension function untuk convert ContentStatsDto ke ContentStats domain model
 */
fun ContentStatsDto.toContentStats(): ContentStats {
    return ContentStats(
        totalBerita = this.beritaCount,
        totalTips = this.tipCount,
        totalAll = this.totalContent,
        tipsByCategory = this.tipsByCategory ?: emptyMap()
    )
}

/**
 * Extension function untuk convert List<ContentItemDto> ke List<Content>
 */
fun List<ContentItemDto>.toContentList(): List<Content> {
    return this.map { it.toContent() }
}
