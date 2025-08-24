package com.example.docmat.data.repository

import com.example.docmat.domain.model.HistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import com.example.docmat.utils.ImageCompressor

@Singleton
class HistoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_PREDICTIONS = "predictions"
    }

    /**
     * Save history with image upload to Firebase Storage
     * If storage upload fails, save without image URL as fallback
     */
    suspend fun saveToHistoryWithImage(
        historyItem: HistoryItem,
        localImageFile: File
    ): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("User not authenticated")

        // Generate document ID first for consistent file naming
        val docRef = firestore.collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_PREDICTIONS)
            .document()
        val docId = docRef.id

        var downloadUrl: String? = null
        
        android.util.Log.d("HistoryRepository", "🔄 Starting saveToHistoryWithImage for docId: $docId")
        android.util.Log.d("HistoryRepository", "User: $uid, File: ${localImageFile.absolutePath}")
        
        try {
            // Upload image with retry mechanism
            var uploadAttempts = 0
            val maxRetries = 3
            
            while (downloadUrl == null && uploadAttempts < maxRetries) {
                uploadAttempts++
                
                try {
                    android.util.Log.d("HistoryRepository", "Upload attempt $uploadAttempts/$maxRetries")
                    
                    // Validate file exists and is readable
                    if (!localImageFile.exists()) {
                        throw IllegalStateException("Image file does not exist: ${localImageFile.absolutePath}")
                    }
                    
                    if (!localImageFile.canRead()) {
                        throw IllegalStateException("Cannot read image file: ${localImageFile.absolutePath}")
                    }
                    
                    val fileSize = localImageFile.length()
                    android.util.Log.d("HistoryRepository", "Uploading file: ${localImageFile.absolutePath}, size: $fileSize bytes")
                    
                    if (fileSize == 0L) {
                        throw IllegalStateException("Image file is empty: ${localImageFile.absolutePath}")
                    }
                    
                    // Try to upload image to Storage
                    val storagePath = "users/$uid/predictions/$docId.jpg"
                    val ref = storage.reference.child(storagePath)
                    val meta = storageMetadata { 
                        contentType = "image/jpeg"
                        setCustomMetadata("uploadedAt", System.currentTimeMillis().toString())
                        setCustomMetadata("originalSize", fileSize.toString())
                        setCustomMetadata("attempt", uploadAttempts.toString())
                    }
                    
                    android.util.Log.d("HistoryRepository", "Attempting to upload to: gs://${storage.app.options.storageBucket}/$storagePath")
                    
                    // Use putFile instead of putBytes for better reliability
                    val fileUri = android.net.Uri.fromFile(localImageFile)
                    val uploadTask = ref.putFile(fileUri, meta).await()
                    
                    android.util.Log.d("HistoryRepository", "Upload completed: ${uploadTask.bytesTransferred} bytes transferred")
                    
                    // Get download URL
                    val tempDownloadUrl = ref.downloadUrl.await().toString()
                    
                    // Verify the URL is accessible
                    if (tempDownloadUrl.isNullOrBlank()) {
                        throw IllegalStateException("Download URL is null or empty after successful upload")
                    }
                    
                    // Test URL accessibility
                    android.util.Log.d("HistoryRepository", "Testing download URL accessibility...")
                    val testResult = testStorageAccess(tempDownloadUrl)
                    if (testResult.isSuccess && testResult.getOrNull() == true) {
                        downloadUrl = tempDownloadUrl
                        android.util.Log.d("HistoryRepository", "Image uploaded successfully: $downloadUrl")
                    } else {
                        throw IllegalStateException("Download URL is not accessible after upload")
                    }
                    
                    // If we reach here, upload was successful
                    break
                    
                } catch (attemptError: Exception) {
                    android.util.Log.e("HistoryRepository", "Upload attempt $uploadAttempts failed", attemptError)
                    
                    if (uploadAttempts >= maxRetries) {
                        // Final attempt failed, log comprehensive error and break
                        android.util.Log.e("HistoryRepository", "All $maxRetries upload attempts failed")
                        break
                    } else {
                        // Wait before retry (exponential backoff)
                        val delayMs = 1000L * uploadAttempts
                        android.util.Log.d("HistoryRepository", "Retrying upload in ${delayMs}ms...")
                        kotlinx.coroutines.delay(delayMs)
                    }
                }
            }
            
        } catch (storageError: Exception) {
            android.util.Log.e("HistoryRepository", "Failed to upload image to Firebase Storage", storageError)
            android.util.Log.e("HistoryRepository", "Storage error details: ${storageError.javaClass.simpleName}: ${storageError.message}")
            
            // Continue without image URL - save history anyway
            downloadUrl = null
        }

        // Save data to Firestore (with or without imageUrl)
        val data = historyItem.copy(
            id = docId,
            userId = uid,
            imageUrl = downloadUrl ?: "", // Empty string if upload failed
            createdAt = com.google.firebase.Timestamp.now()
        )
        
        docRef.set(data).await()
        android.util.Log.d("HistoryRepository", "History saved to Firestore with ID: $docId")
        
        docId
    }
    
    /**
     * Save history with Base64 encoded image directly to Firestore
     * This method provides cross-device compatibility without Firebase Storage
     */
    suspend fun saveToHistoryWithBase64Image(
        historyItem: HistoryItem,
        localImageFile: File
    ): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("User not authenticated")

        android.util.Log.d("HistoryRepository", "🔄 Starting saveToHistoryWithBase64Image")
        android.util.Log.d("HistoryRepository", "File: ${localImageFile.absolutePath}, size: ${localImageFile.length()} bytes")

        // Generate document ID
        val docRef = firestore.collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_PREDICTIONS)
            .document()
        val docId = docRef.id

        // Convert image to Base64
        val base64Image = ImageCompressor.fileToBase64(localImageFile)
        if (base64Image == null) {
            android.util.Log.w("HistoryRepository", "Failed to convert image to Base64, falling back to no image")
        } else {
            android.util.Log.d("HistoryRepository", "✅ Image converted to Base64: ${base64Image.length} characters")
        }

        // Save data to Firestore with Base64 image
        val data = historyItem.copy(
            id = docId,
            userId = uid,
            imageBase64 = base64Image ?: "", // Base64 image or empty if conversion failed
            imageUrl = "", // Not using Firebase Storage
            createdAt = com.google.firebase.Timestamp.now()
        )
        
        docRef.set(data).await()
        android.util.Log.d("HistoryRepository", "✅ History with Base64 image saved to Firestore with ID: $docId")
        
        docId
    }

    /**
     * Save history without image upload (fallback method)
     */
    suspend fun saveToHistoryWithoutImage(
        historyItem: HistoryItem
    ): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("User not authenticated")

        // Generate document ID
        val docRef = firestore.collection(COLLECTION_USERS)
            .document(uid)
            .collection(COLLECTION_PREDICTIONS)
            .document()
        val docId = docRef.id

        // Save data to Firestore without image URL
        val data = historyItem.copy(
            id = docId,
            userId = uid,
            imageUrl = "", // No image URL
            createdAt = com.google.firebase.Timestamp.now()
        )
        
        docRef.set(data).await()
        android.util.Log.d("HistoryRepository", "History saved to Firestore without image, ID: $docId")
        
        docId
    }

    /**
     * Get user's prediction history as Flow for real-time updates
     */
    fun getHistoryFlow(): Flow<List<HistoryItem>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listenerRegistration = firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_PREDICTIONS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error gracefully
                    return@addSnapshotListener
                }

                val historyItems = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val historyItem = document.toObject(HistoryItem::class.java)?.copy(id = document.id)
                        
                        // Debug logging for each item
                        historyItem?.let { item ->
                            val base64Info = if (item.imageBase64.isNotBlank()) "base64=${item.imageBase64.length}chars" else "base64=empty"
                            android.util.Log.d(
                                "HistoryRepository",
                                "Loaded history item: id=${item.id}, imageUrl='${item.imageUrl}', localUri='${item.localImageUri}', $base64Info, diseaseName='${item.diseaseName}'"
                            )
                        }
                        
                        historyItem
                    } catch (e: Exception) {
                        android.util.Log.e("HistoryRepository", "Failed to deserialize history item: ${document.id}", e)
                        null
                    }
                } ?: emptyList()

                android.util.Log.d("HistoryRepository", "Retrieved ${historyItems.size} history items from Firestore")
                trySend(historyItems)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    /**
     * Get single history item by ID
     */
    suspend fun getHistoryItem(itemId: String): Result<HistoryItem?> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(
                IllegalStateException("User not authenticated")
            )

            val document = firestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PREDICTIONS)
                .document(itemId)
                .get()
                .await()

            val historyItem = document.toObject(HistoryItem::class.java)?.copy(id = document.id)
            Result.success(historyItem)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete history item
     */
    suspend fun deleteHistoryItem(itemId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(
                IllegalStateException("User not authenticated")
            )

            firestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PREDICTIONS)
                .document(itemId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get history count for statistics
     */
    suspend fun getHistoryCount(): Result<Int> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(
                IllegalStateException("User not authenticated")
            )

            val snapshot = firestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PREDICTIONS)
                .get()
                .await()

            Result.success(snapshot.size())

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search history by disease name
     */
    fun searchHistory(query: String): Flow<List<HistoryItem>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            return@callbackFlow
        }

        val listenerRegistration = firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(COLLECTION_PREDICTIONS)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                val historyItems = snapshot?.documents?.mapNotNull { document ->
                    try {
                        document.toObject(HistoryItem::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                }?.filter { item ->
                    item.diseaseName.contains(query, ignoreCase = true) ||
                            item.symptoms.contains(query, ignoreCase = true)
                } ?: emptyList()

                trySend(historyItems)
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }
    
    /**
     * Test if Firebase Storage URL is accessible
     * This method helps debug cross-device image loading issues
     */
    suspend fun testStorageAccess(imageUrl: String): Result<Boolean> {
        return try {
            if (imageUrl.isBlank()) {
                android.util.Log.w("HistoryRepository", "Empty imageUrl provided for storage access test")
                return Result.success(false)
            }
            
            // Extract storage path from URL
            val uri = android.net.Uri.parse(imageUrl)
            val path = uri.path?.removePrefix("/v0/b/")
                ?.substringAfter("/o/")
                ?.substringBefore("?")
                ?.replace("%2F", "/")
                ?.replace("%2E", ".")
            
            if (path.isNullOrBlank()) {
                android.util.Log.e("HistoryRepository", "Failed to extract path from URL: $imageUrl")
                return Result.success(false)
            }
            
            // Try to get metadata from Firebase Storage
            val ref = storage.reference.child(path)
            val metadata = ref.metadata.await()
            
            android.util.Log.d("HistoryRepository", "Storage access test successful: $path, size: ${metadata.sizeBytes}")
            Result.success(true)
            
        } catch (e: Exception) {
            android.util.Log.e("HistoryRepository", "Storage access test failed for URL: $imageUrl", e)
            Result.success(false)
        }
    }
}
