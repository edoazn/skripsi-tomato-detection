package com.example.docmat.data.remote.api

import com.example.docmat.data.remote.dto.PredictionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface untuk Tomato Disease Detection
 */
interface TomatoApiService {
    @Multipart
    @POST("predict")
    suspend fun predictDisease(
        @Part file: MultipartBody.Part,
        @Header("Authorization") authorization: String
    ): Response<PredictionResponse>
}
