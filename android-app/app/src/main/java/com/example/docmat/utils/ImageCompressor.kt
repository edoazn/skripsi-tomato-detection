package com.example.docmat.utils

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import androidx.annotation.WorkerThread
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import android.util.Base64
import kotlin.math.roundToInt

object ImageCompressor {

    // ✅ Atur sesuai kebutuhan
    private const val MAX_UPLOAD_BYTES = 2 * 1024 * 1024
    private const val START_QUALITY = 90
    private const val MIN_QUALITY = 50
    private const val QUALITY_STEP = 5
    private const val MAX_DIMENSION = 1600
    private const val SCALE_STEP = 0.85f

    @WorkerThread
    fun prepareImage(
        context: Context,
        uri: Uri,
        maxBytes: Int = MAX_UPLOAD_BYTES
    ): File {
        // 1) Baca bounds untuk hitung inSampleSize (hemat RAM)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        // Guard: kalau gagal detect ukuran, tetap coba decode normal
        val (srcW, srcH) = if (bounds.outWidth > 0 && bounds.outHeight > 0)
            bounds.outWidth to bounds.outHeight
        else 2000 to 2000

        val inSample = computeInSampleSize(srcW, srcH, MAX_DIMENSION, MAX_DIMENSION)

        val opts = BitmapFactory.Options().apply {
            inSampleSize = inSample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        // 2) Decode bitmap terdownsample
        var bmp = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: error("Gagal membaca gambar")

        // 3) Rotasi sesuai EXIF
        val rotation = readExifRotation(context, uri)
        if (rotation != 0) {
            val m = Matrix().apply { postRotate(rotation.toFloat()) }
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        }

        // 4) Kalau ada alpha → flatten ke putih agar JPEG tidak artefact transparansi
        if (bmp.hasAlpha()) {
            bmp = bmp.copy(Bitmap.Config.ARGB_8888, true).also { withAlpha ->
                val canvas = Canvas(withAlpha)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(bmp, 0f, 0f, null)
            }
        }

        // 5) Kompres adaptif sampai < maxBytes
        var quality = START_QUALITY
        var current = bmp
        var scaledOnce = false

        val tempFile = File(
            context.cacheDir,
            "upload_${System.currentTimeMillis()}.jpg"
        )

        while (true) {
            val baos = ByteArrayOutputStream()
            current.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            val data = baos.toByteArray()

            if (data.size <= maxBytes || (quality <= MIN_QUALITY && scaledOnce)) {
                // Tulis ke file lalu selesai
                FileOutputStream(tempFile).use { it.write(data) }
                baos.close()
                if (current != bmp) current.recycle()
                if (!bmp.isRecycled) bmp.recycle()
                return tempFile
            }

            // Turunkan quality dulu
            if (quality > MIN_QUALITY) {
                quality -= QUALITY_STEP
                baos.close()
                continue
            }

            // Quality sudah mentok → kecilkan dimensi, lalu ulangi
            val newW = (current.width * SCALE_STEP).roundToInt().coerceAtLeast(640)
            val newH = (current.height * SCALE_STEP).roundToInt().coerceAtLeast(640)
            if (newW == current.width || newH == current.height) {
                // Tidak bisa dikecilkan lagi secara bermakna → tulis apa adanya
                FileOutputStream(tempFile).use { it.write(data) }
                baos.close()
                if (current != bmp) current.recycle()
                if (!bmp.isRecycled) bmp.recycle()
                return tempFile
            }
            val resized = Bitmap.createScaledBitmap(current, newW, newH, true)
            if (current != bmp) current.recycle()
            current = resized
            quality = START_QUALITY
            scaledOnce = true
            baos.close()
        }
    }

    private fun computeInSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight &&
                (halfWidth / inSampleSize) >= reqWidth
            ) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    /**
     * Convert image file to Base64 string for Firestore storage
     * Compresses image to fit within Firestore document limit (~800KB)
     */
    @WorkerThread
    fun fileToBase64(
        imageFile: File,
        maxSizeKB: Int = 800 // Leave room for other document fields
    ): String? {
        return try {
            val maxBytes = maxSizeKB * 1024
            
            // Read and decode image
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                ?: return null
            
            // Compress to fit size limit
            var quality = 90
            var compressedData: ByteArray
            
            do {
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                compressedData = baos.toByteArray()
                baos.close()
                
                if (compressedData.size <= maxBytes) break
                quality -= 10
                
            } while (quality > 10)
            
            bitmap.recycle()
            
            // Convert to Base64
            val base64 = Base64.encodeToString(compressedData, Base64.DEFAULT)
            android.util.Log.d("ImageCompressor", "Base64 encoded: ${compressedData.size} bytes -> ${base64.length} chars")
            
            base64
            
        } catch (e: Exception) {
            android.util.Log.e("ImageCompressor", "Failed to convert file to Base64", e)
            null
        }
    }
    
    /**
     * Convert Base64 string back to bitmap for display
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            if (base64String.isBlank()) return null
            
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            
        } catch (e: Exception) {
            android.util.Log.e("ImageCompressor", "Failed to convert Base64 to bitmap", e)
            null
        }
    }

    private fun readExifRotation(context: Context, uri: Uri): Int {
        return try {
            val exif = if (uri.scheme == "content" || uri.scheme == "file") {
                context.contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
            } else null
            when (exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (_: Exception) {
            0
        }
    }
}
