package com.example.docmat.presentation.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.docmat.domain.model.Content
import com.example.docmat.domain.model.ContentType
import com.example.docmat.presentation.ui.components.TypeBadge
import com.example.docmat.presentation.ui.components.CategoryBadge
import com.example.docmat.presentation.ui.components.BadgeSize
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentDetailScreen(
    contentId: Int,
    contentType: ContentType,
    onBackClick: () -> Unit,
    viewModel: ContentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    LaunchedEffect(contentId, contentType) {
        viewModel.getContentDetail(contentId, contentType)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        TopAppBar(
            title = { 
                Text(
                    text = when (contentType) {
                        ContentType.BERITA -> "Detail Berita"
                        ContentType.TIP -> "Detail Tips"
                    }
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                // Share button
                uiState.selectedContent?.let { content ->
                    IconButton(
                        onClick = {
                            // TODO: Implement share functionality
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
        
        // Content
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                uiState.isLoadingDetail -> {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    val errorMessage = uiState.error ?: "Unknown error"
                    ErrorMessage(
                        message = errorMessage,
                        onRetry = { viewModel.getContentDetail(contentId, contentType) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.selectedContent != null -> {
                    val selectedContent = uiState.selectedContent!!
                    ContentDetail(
                        content = selectedContent,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ContentDetail(
    content: Content,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            content.imageUrl?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = content.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } ?: run {
                // Fallback ketika tidak ada imageUrl
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = if (content.type == ContentType.BERITA) {
                                Color(0xFFE3F2FD)
                            } else {
                                Color(0xFFE8F5E8)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (content.type == ContentType.BERITA) "📰" else "💡",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "Gambar tidak tersedia",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Content Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Badges and Date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypeBadge(contentType = content.type)
                    
                    content.category?.let { category ->
                        CategoryBadge(
                            category = category,
                            contentType = content.type
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    content.getFormattedDate()?.let { date ->
                        Text(
                            text = date,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Title
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Source (jika ada)
                content.source?.let { source ->
                    Text(
                        text = "Sumber: $source",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Description
                Text(
                    text = content.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )
            }
        }
        
        // Full Content (jika ada)
        content.content?.let { fullContent ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = when (content.type) {
                            ContentType.BERITA -> "Artikel Lengkap"
                            ContentType.TIP -> "Tips Lengkap"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = fullContent,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }
        
        // External Link (jika ada)
        content.url?.let { url ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Baca artikel lengkap di sumber asli",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedButton(
                        onClick = {
                            // TODO: Open URL in browser
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Buka Link")
                    }
                }
            }
        }
        
        // Bottom spacing
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Extension function untuk format tanggal di detail screen
 */
private fun Content.getFormattedDate(): String? {
    return when (type) {
        ContentType.BERITA -> {
            publishedAt?.let { 
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    val date = inputFormat.parse(it)
                    date?.let { outputFormat.format(it) }
                } catch (e: Exception) {
                    null
                }
            }
        }
        ContentType.TIP -> {
            date?.let {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    val date = inputFormat.parse(it)
                    date?.let { outputFormat.format(it) }
                } catch (e: Exception) {
                    it // Return raw date jika parsing gagal
                }
            }
        }
    }
}
