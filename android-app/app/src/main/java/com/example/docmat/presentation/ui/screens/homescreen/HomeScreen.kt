package com.example.docmat.presentation.ui.screens.homescreen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
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
import android.os.Build


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: (Uri) -> Unit,
    onNewsClick: (Int) -> Unit = {}
) {
    var showScanOptions by remember { mutableStateOf(false) }
    val news by viewModel.news.collectAsState()
    val context = LocalContext.current

    // UCrop launcher
    val uCropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                UCropHelper.handleCropResult(result.data)?.let { croppedUri ->
                    Log.d("UCrop", "Crop successful: $croppedUri")
                    onNavigateToGallery(croppedUri)
                }
            }

            UCrop.RESULT_ERROR -> {
                UCropHelper.handleCropError(result.data)?.let { error ->
                    Log.e("UCrop", "Crop error: ${error.message}")
                    // TODO: Show snackbar atau toast untuk crop error
                }
            }

            Activity.RESULT_CANCELED -> {
                Log.d("UCrop", "Crop cancelled by user")
                // User cancelled cropping, do nothing
            }
        }
    }

    // Gallery launcher dengan fallback untuk kompatibilitas
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ActivityResultContracts.PickVisualMedia()
        } else {
            ActivityResultContracts.GetContent()
        },
        onResult = { uri ->
            uri?.let {
                UCropHelper.startCrop(
                    context = context,
                    sourceUri = it,
                    launcher = uCropLauncher
                )
            }
        }
    )

    // ---- Modern Photo Picker ----
    val pickVisualMedia = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            UCropHelper.startCrop(
                context = context,
                sourceUri = it,
                launcher = uCropLauncher
            )
        }
    }

    // ---- Fallback (GetContent) ----
    val pickContent = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { /* provider mungkin tidak mendukung */
            }

            UCropHelper.startCrop(
                context = context,
                sourceUri = it,
                launcher = uCropLauncher
            )
        }
    }

    fun canResolvePickImages(): Boolean {
        val intent = Intent("android.provider.action.PICK_IMAGES").apply {
            type = "image/*"
        }
        val matches = context.packageManager.queryIntentActivities(
            intent, PackageManager.MATCH_DEFAULT_ONLY
        )
        return matches.isNotEmpty()
    }

    val onGalleryClick = remember {
        {
            showScanOptions = false

            val photoPickerAvailable =
                ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(context)
            val pickImagesResolvable = canResolvePickImages()

            if (photoPickerAvailable && pickImagesResolvable) {
                try {
                    pickVisualMedia.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                } catch (e: ActivityNotFoundException) {
                    // Sabuk pengaman terakhir → jatuh ke GetContent
                    Log.w("Picker", "PICK_IMAGES not found, fallback to GetContent", e)
                    pickContent.launch("image/*")
                }
            } else {
                pickContent.launch("image/*")
            }
        }
    }


    // Remember navigation callbacks to prevent recomposition
    val onCameraClick = remember(onNavigateToCamera) {
        {
            showScanOptions = false
            onNavigateToCamera()
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
                )
            }

            // Tips Section
            item {
                TipsSection()
            }

            // Recent History Preview
            item {
                RecentHistorySection()
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        "Berita Terkini",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(onClick = { }) {
                        Text(
                            "Lihat Semua",
                            color = colorScheme.primary
                        )
                    }
                }
            }

            items(
                items = news.take(3), // Show only top 3 news
                key = { it.id }
            ) { newsItem ->
                NewsCard(
                    news = newsItem,
                    onClick = { clickedNews ->
                        onNewsClick(clickedNews.id)
                    }
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(24.dp))
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
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            colorScheme.primaryContainer.copy(alpha = 0.3f)
//        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Analisa Tomat Anda",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = "Deteksi penyakit pada daun tomat dengan AI",
                fontSize = 14.sp,
                color = colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.large,
                colors = buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_camera),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mulai Analisa",
                    fontWeight = FontWeight.SemiBold
                )
            }
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

@Composable
private fun TipsSection() {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                "Tips Hari Ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = colorScheme.primary
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Pemeriksaan Rutin",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Periksa daun tomat setiap pagi untuk deteksi dini penyakit. Fokus pada daun yang menguning atau berbercak.",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentHistorySection() {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                "Riwayat Terkini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { /* Navigate to history */ }) {
                Text(
                    "Lihat Semua",
                    color = colorScheme.primary
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(3) { index ->
                RecentHistoryCard(
                    result = if (index == 0) "Sehat" else "Bercak Daun",
                    date = "2 jam lalu",
                    isHealthy = index == 0
                )
            }
        }
    }
}

@Composable
private fun RecentHistoryCard(
    result: String,
    date: String,
    isHealthy: Boolean
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHealthy)
                colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = if (isHealthy)
                        colorScheme.primary
                    else
                        colorScheme.error
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isHealthy) "✅" else "⚠️",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column {
                Text(
                    text = result,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
