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
        aspectRatioX: Float = 1f,
        aspectRatioY: Float = 1f,
        maxResultSize: Int = 1024
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
            // Compression
            setCompressionQuality(90)
            setCompressionFormat(Bitmap.CompressFormat.JPEG)

            // UI Customization
            setToolbarTitle("Crop Tomato Image")
            setStatusBarColor(ContextCompat.getColor(context, android.R.color.black))
            setToolbarColor(ContextCompat.getColor(context, android.R.color.black))
            setToolbarWidgetColor(ContextCompat.getColor(context, android.R.color.white))
            setActiveControlsWidgetColor(ContextCompat.getColor(context, R.color.teal_200))

            // Crop Features
            setFreeStyleCropEnabled(true)
            setHideBottomControls(false)
            setShowCropFrame(true)
            setShowCropGrid(true)

            // Circle crop for better tomato detection (optional)
            setCircleDimmedLayer(false)
        }
    }

    fun handleCropResult(data: Intent?): Uri? {
        return data?.let { UCrop.getOutput(it) }
    }

    fun handleCropError(data: Intent?): Throwable? {
        return data?.let { UCrop.getError(it) }
    }
}