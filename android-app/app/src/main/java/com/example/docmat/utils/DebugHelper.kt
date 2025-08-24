package com.example.docmat.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug helper for Firebase Storage issues
 * This can be called from any Screen to test storage functionality
 */
@Singleton  
class DebugHelper @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val storageTest: FirebaseStorageTest
) {
    
    companion object {
        private const val TAG = "DebugHelper"
    }
    
    /**
     * Run comprehensive Firebase Storage debugging
     * Call this from any ViewModel or Screen to test storage functionality
     */
    suspend fun runStorageDebug(context: Context) {
        withContext(Dispatchers.IO) {
            Log.i(TAG, "🐛 =================================")
            Log.i(TAG, "🐛 STARTING FIREBASE STORAGE DEBUG")
            Log.i(TAG, "🐛 =================================")
            
            // Step 1: Basic Info
            debugBasicInfo()
            
            // Step 2: Run comprehensive tests
            storageTest.runAllTests(context)
            
            Log.i(TAG, "🐛 =================================")
            Log.i(TAG, "🐛 FIREBASE STORAGE DEBUG COMPLETE")
            Log.i(TAG, "🐛 =================================")
        }
    }
    
    /**
     * Debug basic Firebase configuration info
     */
    private fun debugBasicInfo() {
        try {
            Log.i(TAG, "📱 App Context: ${storage.app.name}")
            Log.i(TAG, "🔧 Storage Bucket: ${storage.app.options.storageBucket}")
            Log.i(TAG, "🏗️ Project ID: ${storage.app.options.projectId}")
            Log.i(TAG, "🌐 Storage URL: gs://${storage.app.options.storageBucket}")
            
            val user = auth.currentUser
            if (user != null) {
                Log.i(TAG, "👤 User: ${user.uid}")
                Log.i(TAG, "📧 Email: ${user.email}")
                Log.i(TAG, "✅ User Authenticated: true")
            } else {
                Log.w(TAG, "❌ User Authenticated: false")
            }
            
            Log.i(TAG, "⏱️ Max Upload Retry: ${storage.maxUploadRetryTimeMillis}ms")
            Log.i(TAG, "⏱️ Max Download Retry: ${storage.maxDownloadRetryTimeMillis}ms")
            Log.i(TAG, "⏱️ Max Operation Retry: ${storage.maxOperationRetryTimeMillis}ms")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting basic info", e)
        }
    }
    
    /**
     * Quick test to verify if a user can upload to their folder
     */
    suspend fun quickUploadTest(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "🚀 Running quick upload test...")
                val result = storageTest.testUploadDownloadCycle(context)
                
                if (result.success) {
                    Log.i(TAG, "✅ Quick upload test PASSED: ${result.message}")
                    true
                } else {
                    Log.e(TAG, "❌ Quick upload test FAILED: ${result.message}")
                    false
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 Quick upload test crashed", e)
                false
            }
        }
    }
    
    /**
     * Test if Firebase Storage is properly configured
     */
    fun isStorageConfigured(): Boolean {
        return try {
            val bucket = storage.app.options.storageBucket
            val configured = !bucket.isNullOrBlank()
            
            Log.d(TAG, "Storage configured: $configured (bucket: $bucket)")
            configured
        } catch (e: Exception) {
            Log.e(TAG, "Error checking storage configuration", e)
            false
        }
    }
    
    /**
     * Log current user authentication status  
     */
    fun logAuthStatus() {
        val user = auth.currentUser
        if (user != null) {
            Log.d(TAG, "✅ User authenticated: ${user.uid} (${user.email})")
        } else {
            Log.w(TAG, "❌ User not authenticated")
        }
    }
    
    /**
     * Check if storage rules allow access
     */
    suspend fun testStorageRulesAccess(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val result = storageTest.testStorageRules()
                Log.d(TAG, "Storage rules test: ${result.message}")
                result.success
            } catch (e: Exception) {
                Log.e(TAG, "Storage rules test failed", e)
                false
            }
        }
    }
}

/**
 * Extension function for easy debugging from ViewModels
 */
suspend fun Context.debugFirebaseStorage(debugHelper: DebugHelper) {
    debugHelper.runStorageDebug(this)
}
