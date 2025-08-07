package com.example.docmat.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Object untuk response prediction dari API
 */
data class PredictionResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("predict_id")
    val predictId: String,
    @SerializedName("timestamp")
    val timestamp: String,
    @SerializedName("model_version")
    val modelVersion: String,
    @SerializedName("data")
    val data: PredictionData
)

data class PredictionData(
    @SerializedName("disease_id")
    val diseaseId: String,
    @SerializedName("nama_penyakit")
    val namaPenyakit: String,
    @SerializedName("confidence")
    val confidence: Double,
    @SerializedName("confidence_str")
    val confidenceStr: String,
    @SerializedName("gejala")
    val gejala: List<String>,
    @SerializedName("penyebab")
    val penyebab: String,
    @SerializedName("solusi")
    val solusi: List<String>,
    @SerializedName("image_url")
    val imageUrl: String
)

/**
 * Data Transfer Object untuk response news dari API
 */
data class NewsResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("data")
    val data: List<NewsDto>
)

data class NewsDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("imageUrl")
    val imageUrl: String?,
    @SerializedName("publishedAt")
    val publishedAt: String,
    @SerializedName("source")
    val source: String,
    @SerializedName("content")
    val content: String
)

/**
 * Data Transfer Object untuk health check response
 */
data class HealthCheckResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("timestamp")
    val timestamp: String
)
