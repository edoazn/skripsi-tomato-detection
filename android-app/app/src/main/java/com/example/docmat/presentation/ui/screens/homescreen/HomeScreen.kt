package com.example.docmat.presentation.ui.screens.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docmat.R
import com.example.docmat.presentation.ui.component.NewsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    var showScanOptions by remember { mutableStateOf(false) }
    val news by viewModel.news.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Docmat", fontWeight = FontWeight.Bold) },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // Header with Scan Button
                // TODO: Ubah background container menjadi lebih soft
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.onSecondaryContainer)
                        .padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Analisa Tomat anda",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Button(
                        onClick = { showScanOptions = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_camera),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Document")
                    }
                }
            }

            item {
                // News Section
                Text(
                    "Top News",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            items(news) { newsItem ->
                NewsCard(news = newsItem) {
                    // Handle news item click
                    println("Clicked on: ${newsItem.title}")
                }
            }
        }
    }

    // Scan Options Bottom Sheet
    if (showScanOptions) {
        ModalBottomSheet(
            onDismissRequest = { showScanOptions = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Pilih Metode Scan",
                    style = MaterialTheme.typography.titleLarge
                )

                FilledTonalButton(
                    onClick = {
                        showScanOptions = false
                        onNavigateToCamera()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_camera),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ambil Foto")
                }

                FilledTonalButton(
                    onClick = { showScanOptions = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_picture),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ambil dari Galeri")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Preview
@Composable
@Preview(showBackground = true, showSystemUi = true)
fun HomeScreenPreview() {
    HomeScreen(
        viewModel = HomeViewModel(),
        onNavigateToCamera = {},
        onNavigateToHistory = {},
        onNavigateToSettings = {}
    )
}
