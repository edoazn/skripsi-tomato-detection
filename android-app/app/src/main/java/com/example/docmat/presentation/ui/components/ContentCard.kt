package com.example.docmat.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.docmat.domain.model.Content
import com.example.docmat.domain.model.ContentType
import com.example.docmat.domain.model.TipCategory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentCard(
    content: Content,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
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
                        Text(
                            text = if (content.type == ContentType.BERITA) "📰" else "💡",
                            fontSize = 32.sp
                        )
                    }
                }
            }
            
            // Content Info
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Type and Category badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Type badge
                    TypeBadge(contentType = content.type)
                    
                    // Category badge (jika ada)
                    content.category?.let { category ->
                        CategoryBadge(
                            category = category,
                            contentType = content.type
                        )
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Date
                    content.getFormattedDate()?.let { date ->
                        Text(
                            text = date,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Title
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Description
                Text(
                    text = content.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Source (jika ada)
                content.source?.let { source ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sumber: $source",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    content: Content,
    searchQuery: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                content.imageUrl?.let { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = content.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    // Placeholder jika tidak ada image
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
                        Text(
                            text = if (content.type == ContentType.BERITA) "📰" else "💡",
                            fontSize = 24.sp
                        )
                    }
                }
            }
            
            // Content Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Type and Category
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TypeBadge(
                        contentType = content.type,
                        size = BadgeSize.Small
                    )
                    
                    content.category?.let { category ->
                        CategoryBadge(
                            category = category,
                            contentType = content.type,
                            size = BadgeSize.Small
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Title dengan highlight search query
                Text(
                    text = content.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Description
                Text(
                    text = content.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Date dan source
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    content.source?.let { source ->
                        Text(
                            text = source,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    content.getFormattedDate()?.let { date ->
                        Text(
                            text = date,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

enum class BadgeSize {
    Normal, Small
}

@Composable
fun TypeBadge(
    contentType: ContentType,
    size: BadgeSize = BadgeSize.Normal,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (contentType) {
        ContentType.BERITA -> Color(0xFF2196F3)
        ContentType.TIP -> Color(0xFF4CAF50)
    }
    
    val text = when (contentType) {
        ContentType.BERITA -> "Berita"
        ContentType.TIP -> "Tips"
    }
    
    val fontSize = when (size) {
        BadgeSize.Normal -> 11.sp
        BadgeSize.Small -> 10.sp
    }
    
    val padding = when (size) {
        BadgeSize.Normal -> PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        BadgeSize.Small -> PaddingValues(horizontal = 6.dp, vertical = 2.dp)
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun CategoryBadge(
    category: String,
    contentType: ContentType,
    size: BadgeSize = BadgeSize.Normal,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        contentType == ContentType.TIP -> {
            TipCategory.fromString(category)?.color ?: Color(0xFF9E9E9E)
        }
        else -> Color(0xFF9E9E9E)
    }
    
    val fontSize = when (size) {
        BadgeSize.Normal -> 11.sp
        BadgeSize.Small -> 10.sp
    }
    
    val padding = when (size) {
        BadgeSize.Normal -> PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        BadgeSize.Small -> PaddingValues(horizontal = 6.dp, vertical = 2.dp)
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = category,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(padding)
        )
    }
}

/**
 * Extension function untuk format tanggal
 */
private fun Content.getFormattedDate(): String? {
    return when (type) {
        ContentType.BERITA -> {
            publishedAt?.let { 
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
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
                    val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                    val date = inputFormat.parse(it)
                    date?.let { outputFormat.format(it) }
                } catch (e: Exception) {
                    it // Return raw date jika parsing gagal
                }
            }
        }
    }
}
