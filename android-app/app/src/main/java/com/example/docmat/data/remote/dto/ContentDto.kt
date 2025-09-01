package com.example.docmat.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response wrapper untuk unified content API
 */
data class ContentListResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<ContentItemDto>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("filters_applied")
    val filtersApplied: ContentFiltersDto? = null
)

data class ContentSearchResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<ContentItemDto>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("search_query")
    val searchQuery: String,
    @SerializedName("filters_applied")
    val filtersApplied: ContentFiltersDto? = null,
    @SerializedName("message")
    val message: String? = null
)

data class ContentDetailResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: ContentDetailDto
)

data class ContentStatsResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: ContentStatsDto
)

/**
 * Content item DTO untuk list responses
 */
data class ContentItemDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("type")
    val type: String, // "berita" atau "tip"
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("publishedAt")
    val publishedAt: String? = null
)

/**
 * Content detail DTO untuk detail responses
 */
data class ContentDetailDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("content")
    val content: String? = null,
    @SerializedName("type")
    val type: String, // "berita" atau "tip"
    @SerializedName("category")
    val category: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("source")
    val source: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("publishedAt")
    val publishedAt: String? = null,
    @SerializedName("url")
    val url: String? = null
)

/**
 * Filters DTO untuk tracking applied filters
 */
data class ContentFiltersDto(
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("category")
    val category: String? = null
)

/**
 * Statistics DTO untuk content stats
 */
data class ContentStatsDto(
    @SerializedName("total_content")
    val totalContent: Int,
    @SerializedName("berita_count")
    val beritaCount: Int,
    @SerializedName("tip_count")
    val tipCount: Int,
    @SerializedName("categories")
    val categories: List<String>,
    @SerializedName("tips_by_category")
    val tipsByCategory: Map<String, Int>? = null
)
