package com.example.docmat.data.remote.api

import com.example.docmat.data.remote.dto.HealthCheckResponse
import com.example.docmat.data.remote.dto.NewsResponse
import com.example.docmat.data.remote.dto.PredictionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface untuk Tomato Disease Detection
 */
interface TomatoApiService {

    /**
     * Predict tomato disease from uploaded image
     * 
     * @param file Image file to analyze (JPEG, PNG, JPG)
     * @param authorization Bearer token for authentication
     * @return Prediction response with disease classification, confidence, description and recommendation
     */
    @Multipart
    @POST("/predict")
    suspend fun predictDisease(
        @Part file: MultipartBody.Part,
        @Header("Authorization") authorization: String
    ): Response<PredictionResponse>

    /**
     * Get all news articles
     */
    @GET("api/news")
    suspend fun getAllNews(): Response<NewsResponse>

    /**
     * Search news articles by keyword
     * 
     * @param keyword Search term for filtering news by title or description
     */
    @GET("api/news/search")
    suspend fun searchNews(
        @Query("keyword") keyword: String
    ): Response<NewsResponse>

    /**
     * Get news article by ID
     * 
     * @param newsId ID of the news article
     */
    @GET("api/news/{news_id}")
    suspend fun getNewsById(
        @Path("news_id") newsId: Int
    ): Response<NewsResponse>

    /**
     * Get related news articles for a specific news item
     * 
     * @param newsId ID of the news article to find related articles for
     */
    @GET("api/news/{news_id}/related")
    suspend fun getRelatedNews(
        @Path("news_id") newsId: Int
    ): Response<NewsResponse>

    /**
     * Health check endpoint
     */
    @GET("api/health")
    suspend fun healthCheck(): Response<HealthCheckResponse>
}
