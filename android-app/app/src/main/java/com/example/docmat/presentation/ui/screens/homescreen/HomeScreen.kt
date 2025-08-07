package com.example.docmat.presentation.ui.screens.homescreen

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.docmat.utils.GalleryPermissionHelper
import com.example.docmat.R
import com.example.docmat.presentation.ui.component.NewsCard
import com.example.docmat.utils.UCropHelper
import com.yalantis.ucrop.UCrop


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: (Uri) -> Unit,
) {
    var showScanOptions by remember { mutableStateOf(false) }
    val news by viewModel.news.collectAsState()
    val context = LocalContext.current

    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                UCropHelper.handleCropResult(result.data)?.let { croppedUri ->
                    onNavigateToGallery(croppedUri)
                }
            }

            UCrop.RESULT_ERROR -> {
                UCropHelper.handleCropError(result.data)?.let { error ->
                    Log.e("UCrop", "Crop error: ${error.message}")
                    // Show error message to user
                }
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                // Launch uCrop after selecting image
                UCropHelper.startCrop(
                    context = context,
                    sourceUri = it,
                    launcher = uCropLauncher
                )
            }
        }
    )


    // Permission launcher for gallery
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val hasPermission = GalleryPermissionHelper.hasGalleryPermission(context)
        if (hasPermission) {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    // Optimize button colors - remember to avoid recreation
    val scanButtonColors = buttonColors(
        containerColor = colorScheme.primary,
        contentColor = colorScheme.onSecondary
    )

    // Remember navigation callbacks to prevent recomposition
    val onCameraClick = remember(onNavigateToCamera) {
        {
            showScanOptions = false
            onNavigateToCamera()
        }
    }

    val onGalleryClick = remember {
        {
            showScanOptions = false
            if (GalleryPermissionHelper.hasGalleryPermission(context)) {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                val permissions = GalleryPermissionHelper.getRequiredPermissions()
                permissionLauncher.launch(permissions)
            }
        }
    }

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
                ScanSection(
                    onScanClick = { showScanOptions = true },
                    buttonColors = scanButtonColors
                )
            }

            item {
                Text(
                    "Top News",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }

            items(
                items = news,
                key = { it.id } // Add key for better performance
            ) { newsItem ->
                NewsCard(news = newsItem) {
                    println("Clicked on: ${newsItem.title}")
                }
            }
        }
    }

    // Scan Options Bottom Sheet
    if (showScanOptions) {
        ScanOptionsBottomSheet(
            onDismiss = { showScanOptions = false },
            onCameraClick = onCameraClick,
            onGalleryClick = onGalleryClick
        )
    }
}

@Composable
private fun ScanSection(
    onScanClick: () -> Unit,
    buttonColors: ButtonColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorScheme.onSecondaryContainer)
            .padding(16.dp)
    ) {
        Text(
            text = "Analisa Tomat anda",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large,
            colors = buttonColors
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScanOptionsBottomSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Pilih Metode Scan",
                style = MaterialTheme.typography.titleLarge
            )

            FilledTonalButton(
                onClick = onCameraClick,
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
                onClick = onGalleryClick,
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
