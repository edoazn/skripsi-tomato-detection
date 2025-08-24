package com.example.docmat.data.remote.mapper

import com.example.docmat.data.remote.dto.PredictionResponse
import com.example.docmat.domain.model.DiseaseType
import com.example.docmat.domain.model.PredictionResult

/**
 * Mapper functions untuk konversi antara DTO dan Domain models
 */

/**
 * Convert PredictionResponse DTO to PredictionResult domain model
 */
fun PredictionResponse.toDomain(): PredictionResult {
    // Parse timestamp string to Date
    val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
    val timestampDate = try {
        dateFormat.parse(this.timestamp) ?: java.util.Date()
    } catch (e: Exception) {
        java.util.Date() // fallback to current date
    }
    
    return PredictionResult(
        predictId = this.predictId,
        diseaseId = this.data.diseaseId,
        diseaseName = this.data.namaPenyakit, // mapped to diseaseName
        confidence = this.data.confidence,
        confidenceStr = this.data.confidenceStr,
        symptoms = this.data.gejala.joinToString("\n• ", "• "), // convert List<String> to String
        causes = this.data.penyebab, // mapped to causes
        solutions = this.data.solusi.joinToString("\n• ", "• "), // convert List<String> to String
        imageUrl = this.data.imageUrl,
        timestamp = timestampDate, // now Date type
        modelVersion = this.modelVersion
    )
}


/**
 * Convert PredictionResult to History domain model for storage
 */
fun PredictionResult.toHistory(): com.example.docmat.domain.model.History {
    return com.example.docmat.domain.model.History(
        id = this.predictId,
        scanResult = "${this.diseaseName} (${this.confidenceStr})",
        timestamp = this.formattedTimestamp,
        description = this.causes, // updated field name
        imageUrl = this.imageUrl
    )
}
