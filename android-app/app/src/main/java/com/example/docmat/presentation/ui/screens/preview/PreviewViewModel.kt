package com.example.docmat.presentation.ui.screens.preview

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmat.data.remote.api.TomatoApiService
import com.example.docmat.data.remote.mapper.toDomain
import com.example.docmat.data.repository.HistoryRepository
import com.example.docmat.domain.model.HistoryItem
import com.example.docmat.domain.model.PredictionResult
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.provider.MediaStore
import java.io.ByteArrayOutputStream

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val apiService: TomatoApiService,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()

    /**
     * Analyze image using ML API
     */
    fun analyzeImage(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    loadingMessage = "Memproses gambar..."
                )

                // Convert Uri to File for upload
                val imageFile = uriToFile(imageUri, context)
                
                _uiState.value = _uiState.value.copy(
                    loadingMessage = "Mengunggah ke server..."
                )

                // Create multipart body with proper MIME type
                val mimeType = getMimeType(imageFile) ?: "image/jpeg"
                val requestFile = imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

                _uiState.value = _uiState.value.copy(
                    loadingMessage = "Menganalisis dengan AI..."
                )

                // Get Firebase ID token for authentication
                val user = FirebaseAuth.getInstance().currentUser
                val idToken = user?.getIdToken(false)?.result?.token
                
                if (idToken == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Authentication failed. Please login again."
                    )
                    return@launch
                }

                // Make API call with Bearer token
                val bearerToken = "Bearer $idToken"
                val response = apiService.predictDisease(body, bearerToken)

                if (response.isSuccessful) {
                    val predictionResponse = response.body()
                    if (predictionResponse != null) {
                        val predictionResult = predictionResponse.toDomain()
                        
                        // Save to history
                        saveToHistory(predictionResult, imageUri.toString())
                        
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            predictionResult = predictionResult,
                            isSuccess = true,
                            loadingMessage = "Analisis selesai!"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Response kosong dari server"
                        )
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "Format gambar tidak valid. Gunakan JPEG, PNG, atau JPG."
                        413 -> "Ukuran gambar terlalu besar. Maksimal 5MB."
                        422 -> "Gambar tidak dapat diproses. Pastikan gambar jelas."
                        500 -> "Server sedang bermasalah. Coba lagi nanti."
                        else -> "Error ${response.code()}: ${response.message()}"
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }

                // Clean up temp file
                imageFile.delete()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = when {
                        e.message?.contains("timeout") == true -> "Koneksi timeout. Periksa internet Anda."
                        e.message?.contains("network") == true -> "Tidak ada koneksi internet."
                        e.message?.contains("host") == true -> "Server tidak dapat dijangkau."
                        else -> "Terjadi kesalahan: ${e.message}"
                    }
                )
            }
        }
    }

    /**
     * Reset states untuk analisis baru
     */
    fun resetState() {
        _uiState.value = PreviewUiState()
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Save prediction result to history
     */
    private fun saveToHistory(predictionResult: PredictionResult, localImageUri: String) {
        viewModelScope.launch {
            try {
                val user = FirebaseAuth.getInstance().currentUser
                val userId = user?.uid ?: return@launch
                
                val historyItem = HistoryItem.fromPredictionResult(
                    predictionResult = predictionResult,
                    userId = userId,
                    localImageUri = localImageUri
                )
                
                historyRepository.saveToHistory(historyItem)
                    .onFailure { error ->
                        // Log error but don't show to user (non-critical)
                        android.util.Log.e("PreviewViewModel", "Failed to save to history: ${error.message}")
                    }
            } catch (e: Exception) {
                android.util.Log.e("PreviewViewModel", "Exception saving to history: ${e.message}")
            }
        }
    }

    /**
     * Convert Uri to File untuk upload dengan proper image processing
     */
    private fun uriToFile(uri: Uri, context: Context): File {
        val tempFile = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        
        try {
            // Load bitmap from Uri
            val bitmap = when {
                uri.scheme == "content" -> {
                    // Handle content:// URIs (gallery and camera)
                    loadBitmapFromContentUri(uri, context)
                }
                uri.scheme == "file" -> {
                    // Handle file:// URIs
                    BitmapFactory.decodeFile(uri.path)
                }
                else -> {
                    // Fallback: try as input stream
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                }
            }
            
            if (bitmap != null) {
                // Apply EXIF rotation if needed
                val rotatedBitmap = rotateImageIfRequired(uri, bitmap, context)
                
                // Compress and save as JPEG with proper quality
                FileOutputStream(tempFile).use { outputStream ->
                    val quality = if (rotatedBitmap.byteCount > 1024 * 1024 * 2) 80 else 90 // 2MB threshold
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                }
                
                // Clean up bitmap
                if (rotatedBitmap != bitmap) {
                    rotatedBitmap.recycle()
                }
                bitmap.recycle()
            } else {
                // Fallback: direct copy if bitmap fails
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        } catch (e: Exception) {
            // Final fallback: direct stream copy
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (fallbackException: Exception) {
                throw IllegalStateException("Failed to process image: ${fallbackException.message}", fallbackException)
            }
        }
        
        return tempFile
    }
    
    /**
     * Load bitmap from content URI with proper handling
     */
    private fun loadBitmapFromContentUri(uri: Uri, context: Context): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Rotate image based on EXIF data
     */
    private fun rotateImageIfRequired(uri: Uri, bitmap: Bitmap, context: Context): Bitmap {
        return try {
            val exif = when {
                uri.scheme == "content" -> {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        ExifInterface(inputStream)
                    }
                }
                uri.scheme == "file" && uri.path != null -> {
                    ExifInterface(uri.path!!)
                }
                else -> null
            }
            
            val orientation = exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            ) ?: ExifInterface.ORIENTATION_NORMAL
            
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap // Return original if rotation fails
        }
    }
    
    /**
     * Rotate bitmap by degrees
     */
    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    /**
     * Get proper MIME type for file
     */
    private fun getMimeType(file: File): String? {
        val extension = file.extension.lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            else -> "image/jpeg" // Default to JPEG
        }
    }
}

/**
 * UI State untuk PreviewScreen
 */
data class PreviewUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val predictionResult: PredictionResult? = null,
    val error: String? = null,
    val loadingMessage: String = "Memproses..."
)
