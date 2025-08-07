package com.example.docmat.data.repository

import com.example.docmat.domain.model.HistoryItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_PREDICTIONS = "predictions"
    }
    
    /**
     * Save prediction to user's history
     */
    suspend fun saveToHistory(historyItem: HistoryItem): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(
                IllegalStateException("User not authenticated")
            )
            
            val document = firestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_PREDICTIONS)
                .document(historyItem.id)
            
            document.set(historyItem.copy(userId = userId)).await()
            Result.success(historyItem.id)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
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
                        document.toObject(HistoryItem::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
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
}
