package com.example.docmat.presentation.ui.screens.auth.register

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class RegisterUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun registerUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState(isLoading = true, errorMessage = null)

            try {
                Log.d("RegisterViewModel", "Starting registration for email: $email")

                val result = auth.createUserWithEmailAndPassword(email, password).await()

                Log.d("RegisterViewModel", "Registration successful for email: ${result.user?.uid}")

                // Update user profile with name
                result.user?.let { user ->
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }

                    user.updateProfile(profileUpdates).await()
                    Log.d("RegisterViewModel", "User profile updated for email: ${user.uid}")
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )

            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Registration failed for email: $email", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(e)
                )
            }
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        Log.d("RegisterViewModel", "Error: ${exception.message}")

        return when (exception) {
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah terdaftar"
                    "ERROR_INVALID_EMAIL" -> "Format email tidak valid"
                    "ERROR_WEAK_PASSWORD" -> "Password terlalu lemah. Minimal 6 karakter"
                    "ERROR_USER_DISABLED" -> "Akun telah dinonaktifkan"
                    "ERROR_TOO_MANY_REQUESTS" -> "Terlalu banyak percobaan. Coba lagi nanti"
                    "ERROR_OPERATION_NOT_ALLOWED" -> "Operasi tidak diizinkan"
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Koneksi internet bermasalah"
                    else -> "Registrasi gagal: ${exception.message}"
                }
            }

            is FirebaseException -> {
                when {
                    exception.message?.contains("API key not valid") == true ->
                        "Konfigurasi aplikasi bermasalah. Hubungi developer"

                    exception.message?.contains("network") == true ->
                        "Koneksi internet bermasalah"

                    else -> "Registrasi gagal. Silakan coba lagi"
                }
            }

            else -> "Registrasi gagal. Silakan coba lagi"
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}