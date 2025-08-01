package com.example.docmat.presentation.ui.screens.auth.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmat.LoginEvent
import com.example.docmat.LoginState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    // form validate
    val isFormValid: StateFlow<Boolean> = _state.map { s ->
        s.email.isNotBlank() && s.password.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _state.update {
                val emailError = if (!Patterns.EMAIL_ADDRESS.matcher(event.value).matches()) {
                    "Format email tidak valid"
                } else null

                it.copy(email = event.value, emailError = emailError)
            }

            is LoginEvent.PasswordChanged -> _state.update {
                val passwordError = if (event.value.length < 6) {
                    "Password minimal 6 karakter"
                } else null

                it.copy(password = event.value, passwordError = passwordError)
            }

            LoginEvent.Submit -> login()
            LoginEvent.ErrorShown -> {
                _state.update {
                    it.copy(errorMessage = null)
                }

            }
        }
    }

    private fun login() = viewModelScope.launch {
        val s = _state.value
        val emailErr = if (s.email.isBlank()) "Email tidak boleh kosong"
        else if (!Patterns.EMAIL_ADDRESS.matcher(s.email)
                .matches()
        ) "Format email tidak valid" else null
        val passwordErr = if (s.password.isBlank()) "Password tidak boleh kosong"
        else if (s.password.length < 6) "Password minimal 6 karakter" else null

        if (listOf(emailErr, passwordErr).all { it == null }) {
            _state.update { it.copy(isLoading = true) }
            try {
                auth.signInWithEmailAndPassword(s.email, s.password).await()
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = getErrorMessage(e)) }
            }
        } else {
            // Tampilkan error validasi sebagai snackbar
            val validationError = listOfNotNull(emailErr, passwordErr).firstOrNull()
                ?: "Mohon lengkapi form dengan benar."
            _state.update {
                it.copy(
                    emailError = emailErr,
                    passwordError = passwordErr,
                    errorMessage = validationError
                )
            }
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthException -> {
                when (exception.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah digunakan oleh akun lain."
                    "ERROR_INVALID_EMAIL" -> "Format email tidak valid."
                    "ERROR_WEAK_PASSWORD" -> "Password terlalu lemah. Minimal 6 karakter."
                    "ERROR_USER_DISABLED" -> "Akun telah dinonaktifkan."
                    "ERROR_TOO_MANY_REQUESTS" -> "Terlalu banyak percobaan. Coba lagi nanti."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Koneksi internet bermasalah."
                    "ERROR_INVALID_CREDENTIAL" -> "Email atau password salah."
                    else -> "Terjadi kesalahan: ${exception.message}"
                }
            }

            else -> "Terjadi kesalahan. Silakan coba lagi."
        }
    }
}

