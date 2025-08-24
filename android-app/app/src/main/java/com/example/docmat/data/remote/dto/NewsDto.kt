package com.example.docmat.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NewsListResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<NewsItemDto>
)

data class NewsDetailResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: NewsDetailDto
)

data class NewsItemDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("source")
    val source: String? = null
)

data class NewsDetailDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("publishedAt")
    val publishedAt: String? = null,
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("category")
    val category: String? = null
)
