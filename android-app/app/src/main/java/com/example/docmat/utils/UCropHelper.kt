package com.example.docmat.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import com.example.docmat.R
import com.yalantis.ucrop.UCrop
import java.io.File

object UCropHelper {

    fun startCrop(
        context: Context,
        sourceUri: Uri,
        launcher: ManagedActivityResultLauncher<Intent, ActivityResult>,
        aspectRatioX: Float = 4f,
        aspectRatioY: Float = 3f,
        maxResultSize: Int = 1536
    ) {
        val destinationUri = createDestinationUri(context)

        val uCropIntent = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(aspectRatioX, aspectRatioY)
            .withMaxResultSize(maxResultSize, maxResultSize)
            .withOptions(createCropOptions(context))
            .getIntent(context)

        launcher.launch(uCropIntent)
    }

    private fun createDestinationUri(context: Context): Uri {
        val file = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        return Uri.fromFile(file)
    }

    // Create crop options
    private fun createCropOptions(context: Context): UCrop.Options {
        return UCrop.Options().apply {
            // Compression - Higher quality untuk better analysis
            setCompressionQuality(95)
            setCompressionFormat(Bitmap.CompressFormat.JPEG)

            // UI Customization
            setToolbarTitle("Potong Gambar Tomat")
            setStatusBarColor(ContextCompat.getColor(context, android.R.color.black))
            setToolbarColor(ContextCompat.getColor(context, android.R.color.black))
            setToolbarWidgetColor(ContextCompat.getColor(context, android.R.color.white))
            setActiveControlsWidgetColor(ContextCompat.getColor(context, R.color.teal_200))

            // Crop Features - Optimized for tomato images
            setFreeStyleCropEnabled(false) // Force aspect ratio untuk consistency
            setHideBottomControls(false)
            setShowCropFrame(true)
            setShowCropGrid(true)
            
            // Size constraints untuk prevent extreme zoom
            setMaxScaleMultiplier(3.0f) // Limit zoom to 3x
            setImageToCropBoundsAnimDuration(300)
            
            // Better dimmed layer
            setDimmedLayerColor(ContextCompat.getColor(context, android.R.color.black))
            setCircleDimmedLayer(false)
            
            // Root view background
            setRootViewBackgroundColor(ContextCompat.getColor(context, android.R.color.black))
        }
    }

    fun handleCropResult(data: Intent?): Uri? {
        return data?.let { UCrop.getOutput(it) }
    }

    fun handleCropError(data: Intent?): Throwable? {
        return data?.let { UCrop.getError(it) }
    }
}