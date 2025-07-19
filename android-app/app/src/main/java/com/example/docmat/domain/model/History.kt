package com.example.docmat.domain.model

class History(
    val id: String,
    val scanResult: String,
    val timestamp: String,
    val description: String? = null,
    val imageUrl: String? = null
)