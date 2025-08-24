package com.example.docmat.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.docmat.R
import com.example.docmat.domain.model.HistoryItem

@Composable
fun HistoryItemCard(
    historyItem: HistoryItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val ctx = LocalContext.current
    
    // Handle different image sources for cross-device compatibility
    val imageData: Any? = when {
        historyItem.imageBase64.isNotBlank() -> {
            // Convert Base64 to Bitmap for Coil
            com.example.docmat.utils.ImageCompressor.base64ToBitmap(historyItem.imageBase64)
        }
        historyItem.imageUrl.isNotBlank() -> {
            historyItem.imageUrl
        }
        historyItem.localImageUri.isNotBlank() -> {
            historyItem.localImageUri
        }
        else -> null
    }

    val request = remember(imageData, historyItem.id) {
        ImageRequest.Builder(ctx)
            .data(imageData)
            .crossfade(true)
            .allowHardware(false) // Better compatibility
            .diskCacheKey("history-${historyItem.id}")
            .memoryCacheKey("history-${historyItem.id}")
            .build()
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            AsyncImage(
                model = request,
                contentDescription = "Gambar analisis",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.tomato),
            )

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = historyItem.diseaseName.ifBlank { "Tidak diketahui" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // pastikan skala confidence benar
                val confPercent = when {
                    historyItem.confidence > 1.0 -> historyItem.confidence
                    else -> historyItem.confidence * 100.0
                }

                Text(
                    text = historyItem.confidenceStr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        confPercent >= 80 -> Color(0xFF4CAF50)
                        confPercent >= 60 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    },
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = historyItem.formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = historyItem.formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Delete
            IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Riwayat") },
            text = { Text("Apakah Anda yakin ingin menghapus riwayat ini?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") }
            }
        )
    }
}