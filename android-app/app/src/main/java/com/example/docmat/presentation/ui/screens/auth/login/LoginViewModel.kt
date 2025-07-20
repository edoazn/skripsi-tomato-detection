package com.example.docmat.presentation.ui.screens.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)


class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true, errorMessage = null)

            try {
                Log.d("LoginViewModel", "Starting login for: $email")

                val result = auth.signInWithEmailAndPassword(email, password).await()

                Log.d("LoginViewModel", "Login successful for: ${result.user?.uid}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login failed", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(e)
                )
            }
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        Log.d("LoginViewModel", "Error: ${exception.message}")

        return when (exception) {
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Format email tidak valid"
                    "ERROR_USER_NOT_FOUND" -> "Email tidak terdaftar"
                    "ERROR_WRONG_PASSWORD" -> "Password salah"
                    "ERROR_USER_DISABLED" -> "Akun telah dinonaktifkan"
                    "ERROR_TOO_MANY_REQUESTS" -> "Terlalu banyak percobaan. Coba lagi nanti"
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Koneksi internet bermasalah"
                    else -> "Login gagal: ${exception.message}"
                }
            }

            is FirebaseException -> {
                when {
                    exception.message?.contains("API key not valid") == true ->
                        "Konfigurasi aplikasi bermasalah. Hubungi developer"

                    exception.message?.contains("network") == true ->
                        "Koneksi internet bermasalah"

                    else -> "Login gagal. Silakan coba lagi"
                }
            }

            else -> "Login gagal. Silakan coba lagi"
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

