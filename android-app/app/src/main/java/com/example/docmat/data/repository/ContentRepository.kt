package com.example.docmat.data.repository

import com.example.docmat.data.remote.api.ContentApiService
import com.example.docmat.data.remote.mapper.toContent
import com.example.docmat.data.remote.mapper.toContentList
import com.example.docmat.domain.model.Content
import com.example.docmat.domain.model.ContentStats
import com.example.docmat.domain.model.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepository @Inject constructor(
    private val contentApiService: ContentApiService
) {

    // Cache untuk different content types
    private var cachedAllContent: List<Content>? = null
    private var cachedBerita: List<Content>? = null
    private var cachedTips: List<Content>? = null
    private var cachedStats: ContentStats? = null

    /**
     * Get all content (untuk tab "Semua")
     */
    suspend fun getAllContent(): List<Content> {
        return withContext(Dispatchers.IO) {
            cachedAllContent?.let { return@withContext it }
            try {
                val response = contentApiService.getContentList()
                val contentList = response.data.toContentList()
                cachedAllContent = contentList
                contentList
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Get content by type (untuk tab "Berita" atau "Tips")
     */
    suspend fun getContentByType(type: ContentType): List<Content> {
        return withContext(Dispatchers.IO) {
            // Check cache first
            when (type) {
                ContentType.BERITA -> cachedBerita?.let { return@withContext it }
                ContentType.TIP -> cachedTips?.let { return@withContext it }
            }

            try {
                val response = contentApiService.getContentList(type = type.value)
                val contentList = response.data.toContentList()

                // Update cache
                when (type) {
                    ContentType.BERITA -> cachedBerita = contentList
                    ContentType.TIP -> cachedTips = contentList
                }

                contentList
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Get content by type and category (untuk badge filters)
     */
    suspend fun getContentByTypeAndCategory(type: ContentType, category: String): List<Content> {
        return withContext(Dispatchers.IO) {
            try {
                val response = contentApiService.getContentList(
                    type = type.value,
                    category = category
                )
                response.data.toContentList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Search content (untuk search bar)
     */
    suspend fun searchContent(
        query: String,
        type: ContentType? = null,
        category: String? = null
    ): List<Content> {
        return withContext(Dispatchers.IO) {
            try {
                val response = contentApiService.searchContent(
                    query = query,
                    type = type?.value,
                    category = category
                )
                response.data.toContentList()
            } catch (e: Exception) {
                // Fallback to local search in cached data
                val searchableContent = cachedAllContent ?: emptyList()
                searchableContent.filter { content ->
                    val matchesQuery = content.title.contains(query, ignoreCase = true) ||
                            content.description.contains(query, ignoreCase = true) ||
                            content.content?.contains(query, ignoreCase = true) == true

                    val matchesType = type?.let { content.type == it } ?: true
                    val matchesCategory = category?.let { content.category == it } ?: true

                    matchesQuery && matchesType && matchesCategory
                }
            }
        }
    }

    /**
     * Get content detail
     */
    suspend fun getContentDetail(id: Int, type: ContentType): Content {
        return withContext(Dispatchers.IO) {
            try {
                val response = contentApiService.getContentDetail(
                    contentType = type.value,
                    id = id
                )
                response.data.toContent()
            } catch (e: Exception) {
                Content(
                    id = id,
                    title = "Data tidak tersedia",
                    description = "Data tidak tersedia karena API error.",
                    type = type,
                    category = null,
                    imageUrl = null,
                    source = "",
                    publishedAt = null,
                    date = null,
                    content = ""
                )
            }
        }
    }

    /**
     * Get content statistics
     */
    suspend fun getContentStats(): ContentStats {
        return withContext(Dispatchers.IO) {
            cachedStats?.let { return@withContext it }
            try {
                // Ambil semua content dari API
                val allContent = cachedAllContent ?: getAllContent()
                val beritaCount = allContent.count { it.type == ContentType.BERITA }
                val tipsCount = allContent.count { it.type == ContentType.TIP }
                val totalCount = allContent.size
                val tipsByCategory = allContent
                    .filter { it.type == ContentType.TIP }
                    .groupBy { it.category ?: "" }
                    .mapValues { it.value.size }
                    .filterKeys { it.isNotEmpty() }
                val stats = ContentStats(
                    totalBerita = beritaCount,
                    totalTips = tipsCount,
                    totalAll = totalCount,
                    tipsByCategory = tipsByCategory
                )
                cachedStats = stats
                stats
            } catch (e: Exception) {
                ContentStats(0, 0, 0, emptyMap())
            }
        }
    }

    /**
     * Refresh all cached data
     */
    suspend fun refreshContent(): List<Content> {
        return withContext(Dispatchers.IO) {
            // Clear all caches
            cachedAllContent = null
            cachedBerita = null
            cachedTips = null
            cachedStats = null

            // Reload fresh data
            getAllContent()
        }
    }
}
