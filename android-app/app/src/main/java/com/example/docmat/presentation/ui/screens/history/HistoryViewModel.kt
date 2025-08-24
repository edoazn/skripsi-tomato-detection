package com.example.docmat.presentation.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmat.data.repository.HistoryRepository
import com.example.docmat.domain.model.HistoryItem
import com.example.docmat.utils.FirebaseStorageDiagnostics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val diagnostics: FirebaseStorageDiagnostics
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

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
    
    /**
     * Debug function to test Firebase Storage URL accessibility
     * This helps troubleshoot cross-device image loading issues
     */
    fun debugStorageAccess() {
        viewModelScope.launch {
            val currentItems = _uiState.value.historyItems
            android.util.Log.d("HistoryViewModel", "Starting Storage URL accessibility test for ${currentItems.size} items")
            
            currentItems.forEach { item ->
                if (item.imageUrl.isNotBlank()) {
                    android.util.Log.d("HistoryViewModel", "Testing storage access for item ${item.id}: ${item.imageUrl}")
                    historyRepository.testStorageAccess(item.imageUrl)
                        .onSuccess { accessible ->
                            android.util.Log.d("HistoryViewModel", "Storage access test for ${item.id}: $accessible")
                        }
                        .onFailure { error ->
                            android.util.Log.e("HistoryViewModel", "Storage access test failed for ${item.id}", error)
                        }
                } else {
                    android.util.Log.w("HistoryViewModel", "Item ${item.id} has empty imageUrl - localUri: '${item.localImageUri}'")
                }
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
