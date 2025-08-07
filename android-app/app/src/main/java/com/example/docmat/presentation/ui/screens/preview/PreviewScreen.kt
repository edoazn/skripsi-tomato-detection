@file:Suppress("DEPRECATION")

package com.example.docmat.presentation.ui.screens.preview

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.outlinedButtonBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@Composable
fun PreviewScreen(
    imageUri: Uri,
    onBackClick: () -> Unit,
    onRetakePhoto: () -> Unit,
    onAnalyzePhoto: (Uri) -> Unit
) {
    var isAnalyzing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Create optimized image request
    val imageRequest = remember(imageUri) {
        ImageRequest.Builder(context)
            .data(imageUri)
            .crossfade(true)
            .build()
    }

    val painter = rememberAsyncImagePainter(imageRequest)

    // Fullscreen preview without scaffold
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Main Image
        Image(
            painter = painter,
            contentDescription = "Preview gambar",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay for better button visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Back Button - Top Left
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(
                    Color.Black.copy(alpha = 0.6f),
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.7f)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Info Text
            Text(
                text = "Pastikan gambar tomat terlihat jelas dan tidak blur",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Retake Button
                OutlinedButton(
                    onClick = onRetakePhoto,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    border = outlinedButtonBorder.copy(
                        brush = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = Color.White
                        ).brush
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ulangi")
                }

                // Analyze Button
                Button(
                    onClick = {
                        isAnalyzing = true
                        onAnalyzePhoto(imageUri)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menganalisis...")
                    } else {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analisis")
                    }
                }
            }
        }
    }
}
