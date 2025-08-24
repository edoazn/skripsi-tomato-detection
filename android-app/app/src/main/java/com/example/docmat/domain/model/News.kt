package com.example.docmat.domain.model

data class News(
    val id: Int,
    val title: String,
    val description: String,
    val content: String? = null,
    val url: String? = null,
    val imageUrl: String? = null,
    val publishedAt: String? = null,
    val source: String? = null,
    val category: String? = null
)
