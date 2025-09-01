package com.example.docmat.presentation.ui.screens.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmat.data.repository.ContentRepository
import com.example.docmat.domain.model.Content
import com.example.docmat.domain.model.ContentStats
import com.example.docmat.domain.model.ContentType
import com.example.docmat.domain.model.SearchState
import com.example.docmat.domain.model.TipCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentUiState(
    val isLoading: Boolean = false,
    val contentList: List<Content> = emptyList(),
    val selectedContent: Content? = null,
    val isLoadingDetail: Boolean = false,
    val error: String? = null,
    val stats: ContentStats? = null,
    
    // Tab navigation state
    val selectedTab: ContentTab = ContentTab.ALL,
    val selectedCategory: TipCategory? = null,
    
    // Search state
    val searchState: SearchState = SearchState.Idle,
    val searchQuery: String = "",
    val isSearchMode: Boolean = false
)

enum class ContentTab(val displayName: String, val contentType: ContentType?) {
    ALL("Semua", null),
    BERITA("Berita", ContentType.BERITA),
    TIPS("Tips", ContentType.TIP)
}

@OptIn(FlowPreview::class)
@HiltViewModel
class ContentViewModel @Inject constructor(
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentUiState())
    val uiState: StateFlow<ContentUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    
    init {
        // Load initial content
        loadContent()
        
        // Setup search debouncing
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) {
                        _uiState.value = _uiState.value.copy(
                            searchState = SearchState.Idle,
                            isSearchMode = false
                        )
                    } else if (query.length >= 2) {
                        performSearch(query)
                    }
                }
        }
    }
    
    /**
     * Load content berdasarkan selected tab
     */
    fun loadContent() {
        val currentTab = _uiState.value.selectedTab
        val currentCategory = _uiState.value.selectedCategory
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val contentList = when {
                    // Jika ada category filter dan tab adalah TIPS
                    currentCategory != null && currentTab == ContentTab.TIPS -> {
                        contentRepository.getContentByTypeAndCategory(
                            type = ContentType.TIP,
                            category = currentCategory.value
                        )
                    }
                    // Jika tab specific (BERITA atau TIPS)
                    currentTab.contentType != null -> {
                        contentRepository.getContentByType(currentTab.contentType)
                    }
                    // Tab ALL - semua content
                    else -> {
                        contentRepository.getAllContent()
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contentList = contentList,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    /**
     * Switch tab (Semua, Berita, Tips)
     */
    fun selectTab(tab: ContentTab) {
        _uiState.value = _uiState.value.copy(
            selectedTab = tab,
            selectedCategory = null, // Reset category filter when switching tabs
            isSearchMode = false,
            searchQuery = ""
        )
        _searchQuery.value = ""
        loadContent()
    }
    
    /**
     * Select category badge (hanya untuk Tips tab)
     */
    fun selectCategory(category: TipCategory?) {
        if (_uiState.value.selectedTab == ContentTab.TIPS) {
            _uiState.value = _uiState.value.copy(selectedCategory = category)
            loadContent()
        }
    }
    
    /**
     * Update search query
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            isSearchMode = query.isNotBlank()
        )
        _searchQuery.value = query
    }
    
    /**
     * Perform search dengan debouncing
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                searchState = SearchState.Loading
            )
            
            try {
                val results = contentRepository.searchContent(
                    query = query,
                    type = _uiState.value.selectedTab.contentType,
                    category = _uiState.value.selectedCategory?.value
                )
                
                _uiState.value = _uiState.value.copy(
                    searchState = if (results.isEmpty()) {
                        SearchState.Empty(query)
                    } else {
                        SearchState.Success(results, query)
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    searchState = SearchState.Error(e.message ?: "Search failed")
                )
            }
        }
    }
    
    /**
     * Clear search dan kembali ke browsing mode
     */
    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            isSearchMode = false,
            searchState = SearchState.Idle
        )
        _searchQuery.value = ""
    }
    
    /**
     * Get content detail
     */
    fun getContentDetail(id: Int, type: ContentType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingDetail = true, error = null)
            
            try {
                val contentDetail = contentRepository.getContentDetail(id, type)
                _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false,
                    selectedContent = contentDetail,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingDetail = false,
                    error = e.message ?: "Failed to load content detail"
                )
            }
        }
    }
    
    /**
     * Refresh content
     */
    fun refreshContent() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val contentList = contentRepository.refreshContent()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    contentList = contentList,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to refresh content"
                )
            }
        }
    }
    
    /**
     * Load content statistics
     */
    fun loadStats() {
        viewModelScope.launch {
            try {
                val stats = contentRepository.getContentStats()
                _uiState.value = _uiState.value.copy(stats = stats)
            } catch (e: Exception) {
                // Stats loading failure doesn't affect main UI
            }
        }
    }
    
}
