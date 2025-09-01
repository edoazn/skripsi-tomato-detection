package com.example.docmat.domain.model

/**
 * Domain model untuk unified content (news + tips)
 */
data class Content(
    val id: Int,
    val title: String,
    val description: String,
    val content: String? = null,
    val type: ContentType,
    val category: String? = null,
    val imageUrl: String? = null,
    val source: String? = null,
    val date: String? = null,
    val publishedAt: String? = null,
    val url: String? = null
)

/**
 * Enum untuk content types
 */
enum class ContentType(val value: String, val displayName: String) {
    BERITA("berita", "Berita"),
    TIP("tip", "Tips");
    
    companion object {
        fun fromString(value: String): ContentType {
            return values().find { it.value == value } ?: BERITA
        }
    }
}

/**
 * Enum untuk tip categories (badge filters)
 */
enum class TipCategory(val value: String, val displayName: String, val emoji: String, val colorHex: Long) {
    PENCEGAHAN("Pencegahan", "Pencegahan", "🛡️", 0xFF22C55E),
    PENGOBATAN("Pengobatan", "Pengobatan", "💊", 0xFFEAB308), 
    PERAWATAN("Perawatan", "Perawatan", "🌱", 0xFF3B82F6);
    
    val color: androidx.compose.ui.graphics.Color
        get() = androidx.compose.ui.graphics.Color(colorHex)
    
    companion object {
        fun fromString(value: String): TipCategory? {
            return values().find { it.value == value }
        }
        
        fun getAllCategories(): List<TipCategory> = values().toList()
    }
}

/**
 * Data class untuk content filters
 */
data class ContentFilters(
    val type: ContentType? = null,
    val category: String? = null
)

/**
 * Sealed class untuk different content loading states
 */
sealed class ContentLoadingState {
    object Loading : ContentLoadingState()
    data class Success(val content: List<Content>) : ContentLoadingState()
    data class Error(val message: String) : ContentLoadingState()
    object Empty : ContentLoadingState()
}
