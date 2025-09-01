package com.example.docmat.domain.model

/**
 * Represents different states of search operation
 */
sealed class SearchState {
    object Idle : SearchState()
    object Loading : SearchState()
    data class Success(val results: List<Content>, val query: String) : SearchState()
    data class Empty(val query: String) : SearchState()
    data class Error(val message: String) : SearchState()
}

/**
 * Content statistics from backend API
 */
data class ContentStats(
    val totalBerita: Int,
    val totalTips: Int,
    val totalAll: Int,
    val tipsByCategory: Map<String, Int>
)
