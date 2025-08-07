package com.example.docmat.domain.model

import com.google.firebase.Timestamp
import java.util.*

/**
 * Domain model untuk item history analisis
 * Digunakan untuk menyimpan hasil prediksi di Firestore
 */
data class HistoryItem(
    val id: String = "",
    val userId: String = "",
    val predictId: String = "",
    val diseaseId: String = "",
    val diseaseName: String = "",
    val confidence: Double = 0.0,
    val confidenceStr: String = "",
    val symptoms: String = "",
    val causes: String = "",
    val solutions: String = "",
    val imageUrl: String = "",
    val localImageUri: String = "", // For offline display
    val timestamp: Timestamp = Timestamp.now(),
    val modelVersion: String = "",
    val createdAt: Timestamp = Timestamp.now()
) {
    
    /**
     * Convert to PredictionResult for reusing existing UI
     */
    fun toPredictionResult(): PredictionResult {
        return PredictionResult(
            predictId = predictId,
            diseaseId = diseaseId,
            diseaseName = diseaseName,
            confidence = confidence,
            confidenceStr = confidenceStr,
            symptoms = symptoms,
            causes = causes,
            solutions = solutions,
            imageUrl = imageUrl,
            timestamp = Date(timestamp.seconds * 1000),
            modelVersion = modelVersion
        )
    }
    
    /**
     * Get formatted date for display
     */
    val formattedDate: String
        get() {
            val date = Date(timestamp.seconds * 1000)
            return java.text.SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(date)
        }
    
    /**
     * Get formatted time for display
     */
    val formattedTime: String
        get() {
            val date = Date(timestamp.seconds * 1000)
            return java.text.SimpleDateFormat("HH:mm", Locale("id", "ID")).format(date)
        }
    
    /**
     * Check if prediction is healthy
     */
    val isHealthy: Boolean
        get() = diseaseName.contains("healthy", ignoreCase = true)
    
    companion object {
        /**
         * Create HistoryItem from PredictionResult
         */
        fun fromPredictionResult(
            predictionResult: PredictionResult,
            userId: String,
            localImageUri: String
        ): HistoryItem {
            return HistoryItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                predictId = predictionResult.predictId,
                diseaseId = predictionResult.diseaseId,
                diseaseName = predictionResult.diseaseName,
                confidence = predictionResult.confidence,
                confidenceStr = predictionResult.confidenceStr,
                symptoms = predictionResult.symptoms,
                causes = predictionResult.causes,
                solutions = predictionResult.solutions,
                imageUrl = predictionResult.imageUrl,
                localImageUri = localImageUri,
                timestamp = Timestamp(predictionResult.timestamp),
                modelVersion = predictionResult.modelVersion,
                createdAt = Timestamp.now()
            )
        }
    }
}
