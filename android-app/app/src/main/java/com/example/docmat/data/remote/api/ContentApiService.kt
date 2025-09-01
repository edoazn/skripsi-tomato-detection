package com.example.docmat.data.remote.api

import com.example.docmat.data.remote.dto.ContentDetailResponse
import com.example.docmat.data.remote.dto.ContentListResponse
import com.example.docmat.data.remote.dto.ContentSearchResponse
import com.example.docmat.data.remote.dto.ContentStatsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ContentApiService {
    
    /**
     * Get content list dengan filter dasar (untuk tab navigation)
     */
    @GET("api/content")
    suspend fun getContentList(
        @Query("type") type: String? = null,
        @Query("category") category: String? = null
    ): ContentListResponse
    
    /**
     * Search content dengan keyword (untuk search bar)
     */
    @GET("api/content/search")
    suspend fun searchContent(
        @Query("q") query: String,
        @Query("type") type: String? = null,
        @Query("category") category: String? = null
    ): ContentSearchResponse
    
    /**
     * Get content detail berdasarkan type dan ID
     */
    @GET("api/content/{type}/{id}")
    suspend fun getContentDetail(
        @Path("type") contentType: String,
        @Path("id") id: Int
    ): ContentDetailResponse
    
    /**
     * Get content statistics
     */
    @GET("api/content/stats")
    suspend fun getContentStats(): ContentStatsResponse
}
