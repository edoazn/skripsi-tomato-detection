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
import javax.inject.Inject
import com.example.docmat.utils.ImageCompressor
import com.example.docmat.utils.FirebaseStorageTest
import com.example.docmat.utils.DebugHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@HiltViewModel
class PreviewViewModel @Inject constructor(
    private val apiService: TomatoApiService,
    private val historyRepository: HistoryRepository,

    ) : ViewModel() {

    private val _uiState = MutableStateFlow(PreviewUiState())
    val uiState: StateFlow<PreviewUiState> = _uiState.asStateFlow()
    private val PREDICT_PART_NAME = "file"


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

                val imageFile = withContext(Dispatchers.IO) {
                    ImageCompressor.prepareImage(context, imageUri)
                }

                val mimeType = resolveMime(context, imageUri, imageFile)
                val requestFile = imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData(
                    PREDICT_PART_NAME,
                    imageFile.name,
                    requestFile
                )

                _uiState.value = _uiState.value.copy(loadingMessage = "Menganalisis dengan AI...")

                // ambil token dengan await
                val idToken =
                    FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
                if (idToken.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Authentication failed. Please login again."
                    )
                    imageFile.delete(); return@launch
                }

                val response = withContext(Dispatchers.IO) {
                    apiService.predictDisease(
                        body,
                        "Bearer $idToken"
                    )
                }

                if (response.isSuccessful) {
                    val predictionResponse = response.body()
                    if (predictionResponse != null) {
                        val predictionResult = predictionResponse.toDomain()

                        // SIMPAN RIWAYAT dengan improved error handling
                        _uiState.value = _uiState.value.copy(loadingMessage = "Menyimpan hasil analisis...")
                        
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        android.util.Log.d("PreviewVM", "Preparing to save history for user: $uid")
                        android.util.Log.d("PreviewVM", "Image file details: path=${imageFile.absolutePath}, size=${imageFile.length()}, exists=${imageFile.exists()}")
                        
                        val historyItem = HistoryItem.fromPredictionResult(
                            predictionResult = predictionResult,
                            userId = uid,
                            localImageUri = imageUri.toString()
                        )
                        
                        android.util.Log.d("PreviewVM", "Created history item: disease=${historyItem.diseaseName}, confidence=${historyItem.confidence}, localUri=${historyItem.localImageUri}")
                        
                        // Try Base64 first (reliable cross-device solution)
                        android.util.Log.d("PreviewVM", "Starting saveToHistoryWithBase64Image...")
                        historyRepository.saveToHistoryWithBase64Image(
                            historyItem = historyItem,
                            localImageFile = imageFile
                        ).onSuccess { docId ->
                            android.util.Log.d("PreviewVM", "✅ History saved with Base64 image, ID: $docId")
                            imageFile.delete() // Delete file only if save successful
                        }.onFailure { e ->
                            android.util.Log.w("PreviewVM", "❌ Base64 save failed, trying Firebase Storage fallback: ${e.message}", e)
                            
                            // Fallback 1: Try Firebase Storage
                            android.util.Log.d("PreviewVM", "Attempting Firebase Storage fallback...")
                            historyRepository.saveToHistoryWithImage(
                                historyItem = historyItem,
                                localImageFile = imageFile
                            ).onSuccess { docId ->
                                android.util.Log.d("PreviewVM", "✅ History saved with Firebase Storage, ID: $docId")
                                imageFile.delete()
                            }.onFailure { storageError ->
                                android.util.Log.w("PreviewVM", "❌ Firebase Storage also failed, saving without image: ${storageError.message}")
                                
                                // Final Fallback: Save without image
                                historyRepository.saveToHistoryWithoutImage(historyItem)
                                    .onSuccess { docId ->
                                        android.util.Log.d("PreviewVM", "✅ History saved without image, ID: $docId")
                                    }
                                    .onFailure { finalError ->
                                        android.util.Log.e("PreviewVM", "❌ Complete save failure: ${finalError.message}", finalError)
                                    }
                                
                                imageFile.delete() // Clean up file anyway
                            }
                        }

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
                        imageFile.delete() // Hapus file jika response kosong
                    }
                } else {
                    val serverMsg = withContext(Dispatchers.IO) { response.errorBody()?.string() }
                        ?: "Unknown error"
                    val errorMessage = when (response.code()) {
                        400 -> "Format gambar tidak valid. Gunakan JPEG, PNG, atau JPG. ($serverMsg)"
                        413 -> "Ukuran gambar terlalu besar. Maksimal 2MB. ($serverMsg)"
                        422 -> "Gambar tidak dapat diproses. Pastikan gambar jelas. ($serverMsg)"
                        401, 403 -> "Token auth tidak valid/kadaluarsa. Silakan login ulang. ($serverMsg)"
                        else -> "Error ${response.code()}: ${response.message()} ($serverMsg)"
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage)
                    imageFile.delete() // Hapus file jika error
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = when {
                        e.message?.contains(
                            "timeout",
                            true
                        ) == true -> "Koneksi timeout. Periksa internet Anda."

                        e.message?.contains(
                            "network",
                            true
                        ) == true -> "Tidak ada koneksi internet."

                        e.message?.contains("host", true) == true -> "Server tidak dapat dijangkau."
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

    private fun resolveMime(context: Context, uri: Uri, file: File): String {
        return context.contentResolver.getType(uri)
            ?: when (file.extension.lowercase()) {
                "jpg", "jpeg" -> "image/jpeg"
                "png" -> "image/png"
                "webp" -> "image/webp"
                else -> "image/jpeg"
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
