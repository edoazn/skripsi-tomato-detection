@file:Suppress("DEPRECATION", "NAME_SHADOWING")

package com.example.docmat.presentation.ui.screens.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onBackClick: () -> Unit,
    onImageCaptured: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    var isCapturing by remember { mutableStateOf(false) }

    // Use remember for executor to avoid recreation
    val cameraExecutor: ExecutorService = remember {
        Executors.newSingleThreadExecutor()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    // Check permission on launch
    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        )) {
            android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                hasPermission = true
            }

            else -> {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    // Cleanup resources when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            cameraProvider?.unbindAll()
        }
    }

    if (hasPermission) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    startCamera(
                        context = ctx,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewView,
                        onImageCaptureReady = { capture, provider ->
                            imageCapture = capture
                            cameraProvider = provider
                        }
                    )
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Back Button - Top Left
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Capture Button
            FloatingActionButton(
                onClick = {
                    if (!isCapturing) {
                        scope.launch {
                            isCapturing = true
                            takePhotoSuspend(
                                context = context,
                                imageCapture = imageCapture,
                                onImageCaptured = onImageCaptured,
                                onComplete = { isCapturing = false }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .size(80.dp),
                shape = CircleShape,
                containerColor = Color.White
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.Black,
                        strokeWidth = 4.dp
                    )
                } else {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Take Photo",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Black
                    )
                }
            }
        }
    } else {
        // Permission denied UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Izin kamera diperlukan",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = { permissionLauncher.launch(android.Manifest.permission.CAMERA) }
                ) {
                    Text("Berikan Izin")
                }
            }
        }
    }
}

private fun startCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    onImageCaptureReady: (ImageCapture, ProcessCameraProvider) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener({
        try {
            val cameraProvider = cameraProviderFuture.get()

            // Optimize camera configuration
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            val imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Unbind previous use cases before rebinding
            cameraProvider.unbindAll()

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            onImageCaptureReady(imageCapture, cameraProvider)

        } catch (exc: Exception) {
            Log.e("CameraScreen", "Use case binding failed", exc)
        }
    }, ContextCompat.getMainExecutor(context))
}

// Suspend function for better coroutine handling
private suspend fun takePhotoSuspend(
    context: Context,
    imageCapture: ImageCapture?,
    onImageCaptured: (Uri) -> Unit,
    onComplete: () -> Unit
) {
    val imageCapture = imageCapture ?: run {
        onComplete()
        return
    }

    withContext(Dispatchers.IO) {
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Docmat")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        onImageCaptured(uri)
                        Log.d("CameraScreen", "Photo saved: $uri")
                    }
                    onComplete()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
                    onComplete()
                }
            }
        )
    }
}