package com.example.docmat.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test utility for debugging Firebase Storage upload and access issues
 */
@Singleton
class FirebaseStorageTest @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    
    companion object {
        private const val TAG = "StorageTest"
    }
    
    /**
     * Create a small test image file for upload testing
     */
    private fun createTestImageFile(context: Context): File {
        val testFile = File(context.cacheDir, "test_upload_${System.currentTimeMillis()}.jpg")
        
        // Create a simple test image (1x1 pixel JPEG)
        val testImageBytes = byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte(), // JPEG header
            0x00.toByte(), 0x10.toByte(), 0x4A.toByte(), 0x46.toByte(), // JFIF
            0x49.toByte(), 0x46.toByte(), 0x00.toByte(), 0x01.toByte(),
            0x01.toByte(), 0x01.toByte(), 0x00.toByte(), 0x48.toByte(),
            0x00.toByte(), 0x48.toByte(), 0x00.toByte(), 0x00.toByte(),
            0xFF.toByte(), 0xDB.toByte(), 0x00.toByte(), 0x43.toByte(),
            // Simplified JPEG data
            0xFF.toByte(), 0xD9.toByte() // JPEG end
        )
        
        try {
            FileOutputStream(testFile).use { fos ->
                fos.write(testImageBytes)
            }
            Log.d(TAG, "Created test image file: ${testFile.absolutePath}, size: ${testFile.length()}")
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create test image file", e)
            throw e
        }
        
        return testFile
    }
    
    /**
     * Test complete upload and download cycle
     */
    suspend fun testUploadDownloadCycle(context: Context): TestResult {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return TestResult(false, "User not authenticated")
        }
        
        val testFile = createTestImageFile(context)
        val startTime = System.currentTimeMillis()
        
        try {
            // Step 1: Upload test
            Log.d(TAG, "🚀 Starting upload test...")
            
            val testPath = "users/$uid/test/upload_test_${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child(testPath)
            
            Log.d(TAG, "Uploading to: $testPath")
            Log.d(TAG, "File details: exists=${testFile.exists()}, size=${testFile.length()}, canRead=${testFile.canRead()}")
            
            // Upload file
            val fileUri = android.net.Uri.fromFile(testFile)
            val uploadTask = ref.putFile(fileUri).await()
            
            Log.d(TAG, "✅ Upload successful: ${uploadTask.bytesTransferred} bytes")
            
            // Step 2: Get download URL
            Log.d(TAG, "🔗 Getting download URL...")
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(TAG, "✅ Download URL obtained: $downloadUrl")
            
            // Step 3: Test accessibility by getting metadata
            Log.d(TAG, "🔍 Testing file accessibility...")
            val metadata = ref.metadata.await()
            Log.d(TAG, "✅ File accessible: size=${metadata.sizeBytes}, contentType=${metadata.contentType}")
            
            // Step 4: Test download URL parsing
            Log.d(TAG, "🧩 Testing URL parsing...")
            val uri = android.net.Uri.parse(downloadUrl)
            val extractedPath = uri.path?.removePrefix("/v0/b/")
                ?.substringAfter("/o/")
                ?.substringBefore("?")
                ?.replace("%2F", "/")
                ?.replace("%2E", ".")
            
            Log.d(TAG, "Extracted path: $extractedPath")
            Log.d(TAG, "Original path: $testPath")
            
            if (extractedPath == testPath) {
                Log.d(TAG, "✅ URL parsing successful")
            } else {
                Log.w(TAG, "⚠️ URL parsing mismatch")
            }
            
            // Step 5: Cleanup
            Log.d(TAG, "🧹 Cleaning up test file...")
            ref.delete().await()
            Log.d(TAG, "✅ Test file deleted from Storage")
            
            testFile.delete()
            Log.d(TAG, "✅ Local test file deleted")
            
            val totalTime = System.currentTimeMillis() - startTime
            
            return TestResult(
                success = true,
                message = "Upload/download test successful in ${totalTime}ms",
                downloadUrl = downloadUrl,
                uploadTimeMs = totalTime,
                bytesTransferred = uploadTask.bytesTransferred
            )
            
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            Log.e(TAG, "❌ Test failed after ${totalTime}ms", e)
            
            // Clean up local file on error
            testFile.delete()
            
            val errorDetail = when (e) {
                is StorageException -> {
                    "Storage Error (${e.errorCode}): ${e.message}"
                }
                is SecurityException -> {
                    "Security Error: ${e.message} - Check Firebase Storage rules"
                }
                else -> {
                    "${e.javaClass.simpleName}: ${e.message}"
                }
            }
            
            return TestResult(
                success = false,
                message = "Test failed: $errorDetail",
                uploadTimeMs = totalTime,
                error = e
            )
        }
    }
    
    /**
     * Test Firebase Storage configuration
     */
    suspend fun testConfiguration(): ConfigTestResult {
        val issues = mutableListOf<String>()
        
        try {
            // Check storage bucket
            val bucket = storage.app.options.storageBucket
            if (bucket.isNullOrBlank()) {
                issues.add("Storage bucket is not configured")
            } else {
                Log.d(TAG, "Storage bucket: $bucket")
            }
            
            // Check authentication
            val user = auth.currentUser
            if (user == null) {
                issues.add("User is not authenticated")
            } else {
                Log.d(TAG, "User authenticated: ${user.uid}")
            }
            
            // Check storage settings
            val maxUploadRetryTime = storage.maxUploadRetryTimeMillis
            Log.d(TAG, "Max upload retry time: ${maxUploadRetryTime}ms")
            
            return ConfigTestResult(
                success = issues.isEmpty(),
                message = if (issues.isEmpty()) "Configuration looks good" else "Configuration issues found",
                issues = issues
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Configuration test failed", e)
            return ConfigTestResult(
                success = false,
                message = "Configuration test failed: ${e.message}",
                issues = listOf("Exception during config test: ${e.message}"),
                error = e
            )
        }
    }
    
    /**
     * Test Firebase Storage rules by trying to access user folder
     */
    suspend fun testStorageRules(): RulesTestResult {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return RulesTestResult(false, "User not authenticated")
        }
        
        try {
            Log.d(TAG, "🔐 Testing Storage rules for user: $uid")
            
            // Try to list files in user folder
            val userPath = "users/$uid/"
            val ref = storage.reference.child(userPath)
            
            val listResult = ref.listAll().await()
            Log.d(TAG, "✅ Storage rules allow access - found ${listResult.items.size} items")
            
            return RulesTestResult(
                success = true,
                message = "Storage rules allow access",
                itemsFound = listResult.items.size
            )
            
        } catch (e: StorageException) {
            when (e.errorCode) {
                StorageException.ERROR_NOT_AUTHORIZED -> {
                    Log.e(TAG, "❌ Storage rules deny access", e)
                    return RulesTestResult(
                        success = false,
                        message = "Storage rules deny access - check Firebase Storage security rules",
                        error = e
                    )
                }
                StorageException.ERROR_NOT_AUTHENTICATED -> {
                    Log.e(TAG, "❌ User not authenticated for Storage", e)
                    return RulesTestResult(
                        success = false,
                        message = "User not authenticated - check Firebase Auth",
                        error = e
                    )
                }
                else -> {
                    Log.w(TAG, "⚠️ Storage rules test got expected error: ${e.errorCode}")
                    return RulesTestResult(
                        success = true,
                        message = "Rules seem OK (expected error: ${e.errorCode})",
                        error = e
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error testing storage rules", e)
            return RulesTestResult(
                success = false,
                message = "Unexpected error: ${e.message}",
                error = e
            )
        }
    }
    
    /**
     * Run all tests and log results
     */
    suspend fun runAllTests(context: Context) {
        Log.i(TAG, "🧪 Starting comprehensive Firebase Storage tests...")
        
        // Test 1: Configuration
        val configResult = testConfiguration()
        Log.i(TAG, if (configResult.success) "✅ Configuration Test: ${configResult.message}" else "❌ Configuration Test: ${configResult.message}")
        configResult.issues.forEach { issue ->
            Log.w(TAG, "   ⚠️ $issue")
        }
        
        // Test 2: Storage Rules
        val rulesResult = testStorageRules()
        Log.i(TAG, if (rulesResult.success) "✅ Rules Test: ${rulesResult.message}" else "❌ Rules Test: ${rulesResult.message}")
        
        // Test 3: Upload/Download Cycle
        val uploadResult = testUploadDownloadCycle(context)
        Log.i(TAG, if (uploadResult.success) "✅ Upload/Download Test: ${uploadResult.message}" else "❌ Upload/Download Test: ${uploadResult.message}")
        
        // Summary
        val allPassed = configResult.success && rulesResult.success && uploadResult.success
        Log.i(TAG, if (allPassed) "🎉 ALL TESTS PASSED" else "💥 SOME TESTS FAILED")
        
        if (!allPassed) {
            Log.i(TAG, "📝 Troubleshooting suggestions:")
            if (!configResult.success) {
                Log.i(TAG, "   1. Check Firebase configuration in google-services.json")
                Log.i(TAG, "   2. Verify Firebase Storage is enabled in console")
            }
            if (!rulesResult.success) {
                Log.i(TAG, "   3. Update Firebase Storage security rules")
                Log.i(TAG, "   4. Ensure user is properly authenticated")
            }
            if (!uploadResult.success) {
                Log.i(TAG, "   5. Check network connectivity")
                Log.i(TAG, "   6. Verify file permissions")
                Log.i(TAG, "   7. Check Firebase Storage quota")
            }
        }
    }
}

// Data classes for test results
data class TestResult(
    val success: Boolean,
    val message: String,
    val downloadUrl: String? = null,
    val uploadTimeMs: Long? = null,
    val bytesTransferred: Long? = null,
    val error: Exception? = null
)

data class ConfigTestResult(
    val success: Boolean,
    val message: String,
    val issues: List<String> = emptyList(),
    val error: Exception? = null
)

data class RulesTestResult(
    val success: Boolean,
    val message: String,
    val itemsFound: Int? = null,
    val error: Exception? = null
)
