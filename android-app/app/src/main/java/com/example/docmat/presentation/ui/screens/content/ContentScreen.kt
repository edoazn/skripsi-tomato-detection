package com.example.docmat.presentation.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.docmat.domain.model.Content
import com.example.docmat.domain.model.ContentStats
import com.example.docmat.domain.model.SearchState
import com.example.docmat.domain.model.TipCategory
import com.example.docmat.presentation.ui.components.ContentCard
import com.example.docmat.presentation.ui.components.SearchResultCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentScreen(
    onContentClick: (Content) -> Unit,
    viewModel: ContentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(Unit) {
        viewModel.loadStats()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar
        SearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::updateSearchQuery,
            onClearQuery = {
                viewModel.clearSearch()
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        // Tab Navigation
        if (!uiState.isSearchMode) {
            TabNavigation(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab,
                stats = uiState.stats,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Category badges (hanya untuk Tips tab)
            if (uiState.selectedTab == ContentTab.TIPS) {
                CategoryBadges(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = viewModel::selectCategory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }
        }
        
        // Content List atau Search Results
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when {
                uiState.isSearchMode -> {
                    SearchResults(
                        searchState = uiState.searchState,
                        onContentClick = onContentClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                uiState.isLoading -> {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    val errorMessage = uiState.error ?: "Unknown error"
                    ErrorMessage(
                        message = errorMessage,
                        onRetry = viewModel::loadContent,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    ContentList(
                        contentList = uiState.contentList,
                        onContentClick = onContentClick,
                        onRefresh = viewModel::refreshContent,
                        isRefreshing = false,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = "Cari berita atau tips...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (query.isNotBlank()) {
            {
                IconButton(onClick = onClearQuery) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier
    )
}

@Composable
fun TabNavigation(
    selectedTab: ContentTab,
    onTabSelected: (ContentTab) -> Unit,
    stats: ContentStats?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(4.dp)
        ) {
            ContentTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val count = when (tab) {
                    ContentTab.ALL -> stats?.totalAll
                    ContentTab.BERITA -> stats?.totalBerita
                    ContentTab.TIPS -> stats?.totalTips
                }
                
                Surface(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        Color.Transparent
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tab.displayName,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        
                        if (count != null) {
                            Text(
                                text = count.toString(),
                                fontSize = 12.sp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadges(
    selectedCategory: TipCategory?,
    onCategorySelected: (TipCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // "Semua" badge untuk clear filter
        item {
            FilterChip(
                onClick = { onCategorySelected(null) },
                label = { Text("Semua") },
                selected = selectedCategory == null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        
        // Category badges
        items(TipCategory.entries.toTypedArray()) { category ->
            FilterChip(
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                selected = selectedCategory == category,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = category.color,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun ContentList(
    contentList: List<Content>,
    onContentClick: (Content) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    if (contentList.isEmpty()) {
        EmptyState(
            message = "Belum ada konten tersedia",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(contentList) { content ->
                ContentCard(
                    content = content,
                    onClick = { onContentClick(content) }
                )
            }
        }
    }
}

@Composable
fun SearchResults(
    searchState: SearchState,
    onContentClick: (Content) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when (searchState) {
            is SearchState.Idle -> {
                EmptyState(
                    message = "Masukkan kata kunci untuk mencari",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is SearchState.Loading -> {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is SearchState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Ditemukan ${searchState.results.size} hasil untuk \"${searchState.query}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    
                    items(searchState.results) { content ->
                        SearchResultCard(
                            content = content,
                            searchQuery = searchState.query,
                            onClick = { onContentClick(content) }
                        )
                    }
                }
            }
            is SearchState.Empty -> {
                EmptyState(
                    message = "Tidak ada hasil untuk \"${searchState.query}\"",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is SearchState.Error -> {
                ErrorMessage(
                    message = "Pencarian gagal: ${searchState.message}",
                    onRetry = null,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Memuat konten...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (onRetry != null) {
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Coba Lagi")
            }
        }
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
