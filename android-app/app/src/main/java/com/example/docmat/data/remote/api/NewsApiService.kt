package com.example.docmat.data.remote.api

import com.example.docmat.data.remote.dto.NewsDetailResponse
import com.example.docmat.data.remote.dto.NewsListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {
    @GET("api/news")
    suspend fun getNewsList(): NewsListResponse
    
    @GET("api/news/{id}")
    suspend fun getNewsDetail(@Path("id") id: Int): NewsDetailResponse
    
    @GET("api/news/search")
    suspend fun searchNews(@Query("keyword") keyword: String): NewsListResponse
    
    @GET("api/news/{id}/related")
    suspend fun getRelatedNews(@Path("id") id: Int): NewsListResponse
}
