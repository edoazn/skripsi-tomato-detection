package com.example.docmat.presentation.ui.screens.auth.register

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.docmat.RegisterEvent
import com.example.docmat.RegisterState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    // helper
    val isFormValid: StateFlow<Boolean> = _state.map { s ->
        // Button aktif jika semua field terisi
        s.name.isNotBlank() &&
                s.email.isNotBlank() &&
                s.password.isNotBlank() &&
                s.confirmPassword.isNotBlank()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.NameChanged -> _state.update {
                it.copy(name = event.value, nameError = null)
            }

            is RegisterEvent.EmailChanged -> _state.update {
                it.copy(email = event.value, emailError = null)
            }

            is RegisterEvent.PasswordChanged -> _state.update {
                val passwordError = if (event.value.isNotBlank() && event.value.length < 6) {
                    "Password minimal 6 karakter"
                } else null

                it.copy(
                    password = event.value,
                    passwordError = passwordError
                )
            }

            is RegisterEvent.ConfirmPasswordChanged -> _state.update {
                val confirmError = if (event.value.isNotBlank() && event.value != it.password) {
                    "Konfirmasi password tidak sama"
                } else null

                it.copy(
                    confirmPassword = event.value,
                    confirmPasswordError = confirmError
                )
            }

            RegisterEvent.Submit -> register()
            RegisterEvent.ErrorShown -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun register() = viewModelScope.launch {
        val s = _state.value
        val nameErr = if (s.name.isBlank()) "Nama tidak boleh kosong"
        else if (s.name.length < 3) "Nama minimal 3 karakter" else null
        val emailErr =
            if (!Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) "Email tidak valid" else null
        val passErr = if (s.password.length < 6) "Password minimal 6 karakter" else null
        val confirmErr = if (s.password != s.confirmPassword) "Konfirmasi tidak sama" else null

        if (listOf(nameErr, emailErr, passErr, confirmErr).all { it == null }) {
            _state.update { it.copy(isLoading = true) }
            try {
                auth.createUserWithEmailAndPassword(s.email, s.password)
                    .await().user?.updateProfile(userProfileChangeRequest { displayName = s.name })
                    ?.await()
                auth.signOut()
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = getErrorMessage(e)) }
            }
        } else {
            // Tampilkan error validasi sebagai snackbar
            val validationError =
                listOfNotNull(nameErr, emailErr, passErr, confirmErr)
                    .firstOrNull() ?: "Mohon lengkapi form dengan benar."


            _state.update {
                it.copy(
                    nameError = nameErr,
                    emailError = emailErr,
                    passwordError = passErr,
                    confirmPasswordError = confirmErr,
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
                    else -> "Terjadi kesalahan: ${exception.message}"
                }
            }

            else -> "Terjadi kesalahan. Silakan coba lagi."
        }
    }
}