package com.example.docmat.presentation.ui.screens.history

import com.example.docmat.presentation.ui.component.HistoryItemCard
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.docmat.domain.model.HistoryItem
import com.example.docmat.presentation.ui.component.EmptyHistoryView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateToDetail: (HistoryItem, String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Riwayat",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.historyItems.isEmpty() -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.isEmpty -> {
                    // Empty state
                    EmptyHistoryView()
                }

                else -> {
                    // Content
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {

                        // History items
                        items(
                            items = uiState.historyItems,
                            key = { it.id }
                        ) { historyItem ->
                            HistoryItemCard(
                                historyItem = historyItem,
                                onClick = {
                                    // Use bestImageUrl for cross-device compatibility
                                    onNavigateToDetail(historyItem, historyItem.bestImageUrl)
                                },
                                onDelete = {
                                    viewModel.deleteHistoryItem(historyItem.id)
                                }
                            )
                        }
                    }
                }
            }

            // Error snackbar
            uiState.error?.let { error ->
                LaunchedEffect(error) {
                    // Show error message
                }
            }
        }
    }
}

