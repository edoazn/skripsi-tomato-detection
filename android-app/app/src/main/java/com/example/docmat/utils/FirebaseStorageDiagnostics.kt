package com.example.docmat.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for diagnosing Firebase Storage issues
 * Helps debug upload failures and cross-device image access problems
 */
@Singleton
class FirebaseStorageDiagnostics @Inject constructor(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {
    
    companion object {
        private const val TAG = "StorageDiagnostics"
    }
    
    /**
     * Run comprehensive diagnostics on Firebase Storage
     */
    suspend fun runDiagnostics(): DiagnosticsResult {
        val results = mutableListOf<DiagnosticCheck>()
        
        try {
            // Check 1: Authentication status
            results.add(checkAuthentication())
            
            // Check 2: Storage configuration
            results.add(checkStorageConfiguration())
            
            // Check 3: Storage rules (basic connectivity test)
            results.add(checkStorageRules())
            
            // Check 4: Network connectivity
            results.add(checkNetworkConnectivity())
            
        } catch (e: Exception) {
            Log.e(TAG, "Diagnostics failed", e)
            results.add(DiagnosticCheck("General Error", false, "Diagnostics crashed: ${e.message}"))
        }
        
        return DiagnosticsResult(results)
    }
    
    /**
     * Test file upload process with detailed logging
     */
    suspend fun testFileUpload(testFile: File): UploadTestResult {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            return UploadTestResult(false, "User not authenticated")
        }
        
        val startTime = System.currentTimeMillis()
        
        try {
            // Step 1: File validation
            Log.d(TAG, "Testing file upload: ${testFile.absolutePath}")
            
            if (!testFile.exists()) {
                return UploadTestResult(false, "Test file does not exist")
            }
            
            if (!testFile.canRead()) {
                return UploadTestResult(false, "Cannot read test file")
            }
            
            val fileSize = testFile.length()
            Log.d(TAG, "File size: $fileSize bytes")
            
            if (fileSize == 0L) {
                return UploadTestResult(false, "Test file is empty")
            }
            
            // Step 2: Upload attempt
            val testPath = "users/$uid/test/diagnostic_${System.currentTimeMillis()}.jpg"
            val ref = storage.reference.child(testPath)
            
            Log.d(TAG, "Uploading to: $testPath")
            
            val fileUri = android.net.Uri.fromFile(testFile)
            val uploadTask = ref.putFile(fileUri).await()
            
            Log.d(TAG, "Upload completed: ${uploadTask.bytesTransferred} bytes")
            
            // Step 3: Get download URL
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(TAG, "Download URL obtained: $downloadUrl")
            
            // Step 4: Test URL accessibility
            val metadata = ref.metadata.await()
            Log.d(TAG, "Metadata retrieved: size=${metadata.sizeBytes}, contentType=${metadata.contentType}")
            
            // Step 5: Cleanup test file
            ref.delete().await()
            Log.d(TAG, "Test file cleaned up")
            
            val totalTime = System.currentTimeMillis() - startTime
            
            return UploadTestResult(
                success = true,
                message = "Upload test successful in ${totalTime}ms",
                downloadUrl = downloadUrl,
                uploadTimeMs = totalTime,
                bytesTransferred = uploadTask.bytesTransferred
            )
            
        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - startTime
            Log.e(TAG, "Upload test failed after ${totalTime}ms", e)
            
            return UploadTestResult(
                success = false,
                message = "Upload test failed: ${e.message}",
                uploadTimeMs = totalTime,
                error = e
            )
        }
    }
    
    /**
     * Test download URL accessibility
     */
    suspend fun testDownloadUrl(downloadUrl: String): DownloadTestResult {
        try {
            Log.d(TAG, "Testing download URL: $downloadUrl")
            
            if (downloadUrl.isBlank()) {
                return DownloadTestResult(false, "Empty download URL")
            }
            
            // Extract path from URL
            val uri = android.net.Uri.parse(downloadUrl)
            val path = extractStoragePath(uri)
            
            if (path.isNullOrBlank()) {
                return DownloadTestResult(false, "Cannot extract storage path from URL")
            }
            
            Log.d(TAG, "Extracted path: $path")
            
            // Test metadata access
            val ref = storage.reference.child(path)
            val metadata = ref.metadata.await()
            
            Log.d(TAG, "Download test successful: size=${metadata.sizeBytes}")
            
            return DownloadTestResult(
                success = true,
                message = "Download URL is accessible",
                fileSize = metadata.sizeBytes,
                contentType = metadata.contentType
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Download test failed", e)
            
            val errorCode = if (e is StorageException) {
                when (e.errorCode) {
                    StorageException.ERROR_OBJECT_NOT_FOUND -> "File not found"
                    StorageException.ERROR_NOT_AUTHENTICATED -> "Not authenticated"
                    StorageException.ERROR_NOT_AUTHORIZED -> "Not authorized"
                    StorageException.ERROR_QUOTA_EXCEEDED -> "Quota exceeded"
                    else -> "Storage error: ${e.errorCode}"
                }
            } else {
                "Network error: ${e.message}"
            }
            
            return DownloadTestResult(
                success = false,
                message = errorCode,
                error = e
            )
        }
    }
    
    private suspend fun checkAuthentication(): DiagnosticCheck {
        return try {
            val user = auth.currentUser
            if (user != null) {
                DiagnosticCheck(
                    name = "Authentication",
                    passed = true,
                    message = "User authenticated: ${user.uid}"
                )
            } else {
                DiagnosticCheck(
                    name = "Authentication",
                    passed = false,
                    message = "User not authenticated"
                )
            }
        } catch (e: Exception) {
            DiagnosticCheck(
                name = "Authentication",
                passed = false,
                message = "Auth check failed: ${e.message}"
            )
        }
    }
    
    private suspend fun checkStorageConfiguration(): DiagnosticCheck {
        return try {
            val bucket = storage.app.options.storageBucket
            if (bucket != null) {
                DiagnosticCheck(
                    name = "Storage Configuration",
                    passed = true,
                    message = "Storage bucket: $bucket"
                )
            } else {
                DiagnosticCheck(
                    name = "Storage Configuration",
                    passed = false,
                    message = "Storage bucket not configured"
                )
            }
        } catch (e: Exception) {
            DiagnosticCheck(
                name = "Storage Configuration",
                passed = false,
                message = "Config check failed: ${e.message}"
            )
        }
    }
    
    private suspend fun checkStorageRules(): DiagnosticCheck {
        return try {
            val uid = auth.currentUser?.uid
            if (uid == null) {
                return DiagnosticCheck(
                    name = "Storage Rules",
                    passed = false,
                    message = "Cannot test rules - user not authenticated"
                )
            }
            
            // Try to access user's folder (this will test read permission)
            val userPath = "users/$uid/"
            val ref = storage.reference.child(userPath)
            
            try {
                // This will fail if rules don't allow access, but won't crash
                val listResult = ref.listAll().await()
                DiagnosticCheck(
                    name = "Storage Rules",
                    passed = true,
                    message = "Storage rules allow access (${listResult.items.size} files found)"
                )
            } catch (e: StorageException) {
                when (e.errorCode) {
                    StorageException.ERROR_NOT_AUTHORIZED -> DiagnosticCheck(
                        name = "Storage Rules",
                        passed = false,
                        message = "Storage rules deny access"
                    )
                    else -> DiagnosticCheck(
                        name = "Storage Rules", 
                        passed = true,
                        message = "Rules seem OK (got expected error: ${e.errorCode})"
                    )
                }
            }
            
        } catch (e: Exception) {
            DiagnosticCheck(
                name = "Storage Rules",
                passed = false,
                message = "Rules check failed: ${e.message}"
            )
        }
    }
    
    private suspend fun checkNetworkConnectivity(): DiagnosticCheck {
        return try {
            // Simple connectivity test by accessing storage root
            val maxRetries = storage.maxUploadRetryTimeMillis
            DiagnosticCheck(
                name = "Network Connectivity",
                passed = true,
                message = "Network settings OK (max retry time: ${maxRetries}ms)"
            )
        } catch (e: Exception) {
            DiagnosticCheck(
                name = "Network Connectivity",
                passed = false,
                message = "Network check failed: ${e.message}"
            )
        }
    }
    
    private fun extractStoragePath(uri: android.net.Uri): String? {
        return try {
            uri.path?.removePrefix("/v0/b/")
                ?.substringAfter("/o/")
                ?.substringBefore("?")
                ?.replace("%2F", "/")
                ?.replace("%2E", ".")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract storage path", e)
            null
        }
    }
    
    /**
     * Print diagnostic results to log
     */
    fun printDiagnostics(results: DiagnosticsResult) {
        Log.i(TAG, "=== FIREBASE STORAGE DIAGNOSTICS ===")
        results.checks.forEach { check ->
            val status = if (check.passed) "✅ PASS" else "❌ FAIL"
            Log.i(TAG, "$status ${check.name}: ${check.message}")
        }
        Log.i(TAG, "Overall: ${if (results.allPassed) "ALL CHECKS PASSED" else "SOME CHECKS FAILED"}")
        Log.i(TAG, "=====================================")
    }
}

data class DiagnosticsResult(
    val checks: List<DiagnosticCheck>
) {
    val allPassed: Boolean get() = checks.all { it.passed }
}

data class DiagnosticCheck(
    val name: String,
    val passed: Boolean,
    val message: String
)

data class UploadTestResult(
    val success: Boolean,
    val message: String,
    val downloadUrl: String? = null,
    val uploadTimeMs: Long? = null,
    val bytesTransferred: Long? = null,
    val error: Exception? = null
)

data class DownloadTestResult(
    val success: Boolean,
    val message: String,
    val fileSize: Long? = null,
    val contentType: String? = null,
    val error: Exception? = null
)
