package com.example.docmat.domain.model


data class PredictionResult(
    val predictId: String,
    val diseaseId: String,
    val diseaseName: String, // changed from namaPenyakit
    val confidence: Double,
    val confidenceStr: String,
    val symptoms: String, // changed from gejala: List<String>
    val causes: String, // changed from penyebab
    val solutions: String, // changed from solusi: List<String> 
    val imageUrl: String,
    val timestamp: java.util.Date, // changed from String
    val modelVersion: String
) {
    
    /**
     * Get confidence percentage as formatted string
     */
    val confidencePercentage: String
        get() = "${(confidence * 100).toInt()}%"

    /**
     * Check if prediction is healthy
     */
    val isHealthy: Boolean
        get() = diseaseName.contains("healthy", ignoreCase = true)

    /**
     * Get formatted timestamp for display
     */
    val formattedTimestamp: String
        get() = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale("id", "ID")).format(timestamp)
}

/**
 * Enum untuk jenis penyakit tomat berdasarkan model ML
 */
enum class DiseaseType(
    val displayName: String,
    val severity: Severity,
    val color: String // Color hex for UI display
) {
    HEALTHY("Sehat", Severity.NONE, "#4CAF50"),
    BACTERIAL_SPOT("Bercak Bakteri", Severity.MEDIUM, "#FF9800"),
    EARLY_BLIGHT("Hawar Awal", Severity.HIGH, "#F44336"),
    LATE_BLIGHT("Hawar Akhir", Severity.CRITICAL, "#D32F2F"),
    LEAF_MOLD("Jamur Daun", Severity.MEDIUM, "#FF5722"),
    SEPTORIA_LEAF_SPOT("Bercak Septoria", Severity.MEDIUM, "#FF7043"),
    SPIDER_MITES("Tungau Laba-laba", Severity.MEDIUM, "#FF8A65"),
    TARGET_SPOT("Bercak Target", Severity.MEDIUM, "#FFAB91"),
    YELLOW_LEAF_CURL_VIRUS("Virus Keriting Kuning", Severity.HIGH, "#E57373"),
    MOSAIC_VIRUS("Virus Mosaik", Severity.HIGH, "#EF5350"),
    UNKNOWN("Tidak Diketahui", Severity.UNKNOWN, "#9E9E9E");

    companion object {
        /**
         * Convert prediction string from API to DiseaseType
         */
        fun fromPrediction(prediction: String): DiseaseType {
            return when {
                prediction.contains("healthy", ignoreCase = true) -> HEALTHY
                prediction.contains("bacterial", ignoreCase = true) -> BACTERIAL_SPOT
                prediction.contains("early", ignoreCase = true) -> EARLY_BLIGHT
                prediction.contains("late", ignoreCase = true) -> LATE_BLIGHT
                prediction.contains("leaf_mold", ignoreCase = true) -> LEAF_MOLD
                prediction.contains("septoria", ignoreCase = true) -> SEPTORIA_LEAF_SPOT
                prediction.contains("spider", ignoreCase = true) -> SPIDER_MITES
                prediction.contains("target", ignoreCase = true) -> TARGET_SPOT
                prediction.contains("yellow", ignoreCase = true) -> YELLOW_LEAF_CURL_VIRUS
                prediction.contains("mosaic", ignoreCase = true) -> MOSAIC_VIRUS
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Enum untuk tingkat keparahan penyakit
 */
enum class Severity(val displayName: String, val level: Int) {
    NONE("Tidak Ada", 0),
    LOW("Ringan", 1),
    MEDIUM("Sedang", 2),
    HIGH("Tinggi", 3),
    CRITICAL("Kritis", 4),
    UNKNOWN("Tidak Diketahui", -1)
}
