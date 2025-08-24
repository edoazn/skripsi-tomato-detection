package com.example.docmat.data.remote.mapper

import com.example.docmat.data.remote.dto.NewsDetailDto
import com.example.docmat.data.remote.dto.NewsItemDto
import com.example.docmat.domain.model.News

fun NewsItemDto.toNews(): News {
    return News(
        id = this.id,
        title = this.title,
        description = this.description,
        imageUrl = this.imageUrl,
        source = this.source
    )
}

fun NewsDetailDto.toNews(): News {
    return News(
        id = this.id,
        title = this.title,
        description = this.description,
        content = this.content,
        url = this.url,
        imageUrl = this.imageUrl,
        publishedAt = this.publishedAt,
        source = this.source,
        category = this.category
    )
}
