package com.example.docmat.presentation.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmat.data.repository.HistoryRepository
import com.example.docmat.domain.model.HistoryItem
import com.example.docmat.domain.model.PredictionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    init {
        loadHistory()
    }
    
    /**
     * Load user's prediction history
     */
    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            historyRepository.getHistoryFlow()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Gagal memuat riwayat"
                    )
                }
                .collect { historyItems ->
                    _uiState.value = _uiState.value.copy(
                        historyItems = historyItems,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    
    /**
     * Save prediction result to history
     */
    fun saveToHistory(predictionResult: PredictionResult, localImageUri: String) {
        viewModelScope.launch {
            val userId = "current_user" // Will be replaced with actual userId from auth
            val historyItem = HistoryItem.fromPredictionResult(
                predictionResult = predictionResult,
                userId = userId,
                localImageUri = localImageUri
            )
            
            historyRepository.saveToHistory(historyItem)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = "Gagal menyimpan ke riwayat: ${error.message}"
                    )
                }
        }
    }
    
    /**
     * Delete history item
     */
    fun deleteHistoryItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            historyRepository.deleteHistoryItem(itemId)
                .onSuccess {
                    // History will be automatically updated via Flow
                    clearError()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal menghapus item: ${error.message}"
                    )
                }
        }
    }
    
    /**
     * Search history
     */
    fun searchHistory(query: String) {
        _searchQuery.value = query
        
        if (query.isBlank()) {
            loadHistory()
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            historyRepository.searchHistory(query)
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Gagal mencari riwayat"
                    )
                }
                .collect { historyItems ->
                    _uiState.value = _uiState.value.copy(
                        historyItems = historyItems,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }
    
    /**
     * Clear search and reload all history
     */
    fun clearSearch() {
        _searchQuery.value = ""
        loadHistory()
    }
    
    /**
     * Refresh history
     */
    fun refresh() {
        loadHistory()
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Get history statistics
     */
    fun getHistoryStats() {
        viewModelScope.launch {
            historyRepository.getHistoryCount()
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(totalAnalysis = count)
                }
        }
    }
}

/**
 * UI State untuk History Screen
 */
data class HistoryUiState(
    val historyItems: List<HistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalAnalysis: Int = 0
) {
    val isEmpty: Boolean = historyItems.isEmpty() && !isLoading
    val hasError: Boolean = error != null
}
