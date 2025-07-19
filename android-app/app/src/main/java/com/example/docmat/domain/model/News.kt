package com.example.docmat.domain.model

class News (
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String? = null,
    val date: String,
    val author: String? = null
) {
}