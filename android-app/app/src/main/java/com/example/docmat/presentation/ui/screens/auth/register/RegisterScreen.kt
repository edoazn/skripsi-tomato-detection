package com.example.docmat.presentation.ui.screens.auth.register

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.docmat.R
import com.example.docmat.presentation.ui.component.EmailTextField
import com.example.docmat.presentation.ui.component.NameTextField
import com.example.docmat.presentation.ui.component.PasswordTextField

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel()
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmPasswordError by rememberSaveable { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle register success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Pendaftaran berhasil. Silakan login.")
            onRegisterSuccess()
        }
    }

    // Handle error
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                withDismissAction = true
            )
            viewModel.clearError()
        }
    }


    // logic validation most be in custom text field
    fun validateInputs(): Boolean {
        var isValid = true

        if (name.isBlank()) {
            nameError = "Nama tidak boleh kosong"
            isValid = false
        } else if (name.length < 3) {
            nameError = "Nama minimal 3 karakter"
            isValid = false
        } else {
            nameError = null
        }

        if (email.isBlank()) {
            emailError = "Email tidak boleh kosong"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Format email tidak valid"
            isValid = false
        } else {
            emailError = null
        }

        if (password.isBlank()) {
            passwordError = "Password tidak boleh kosong"
            isValid = false
        }

        if (confirmPassword.isBlank()) {
            confirmPasswordError = "Konfirmasi password tidak boleh kosong"
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = "Password dan konfirmasi password tidak sama"
            isValid = false
        } else {
            confirmPasswordError = null
        }

        return isValid

    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Daftar Akun",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Buat akun baru untuk memulai",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Image Section
            Image(
                painter = painterResource(id = R.drawable.dimasjid_layo),
                contentDescription = "register image",
                modifier = Modifier
                    .size(180.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form Section
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                NameTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    errorMessage = nameError,
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                EmailTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError != null,
                    errorMessage = emailError,
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPasswordError != null,
                    errorMessage = confirmPasswordError,
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Register Button
                Button(
                    onClick = {
                        if (validateInputs()) {
                            viewModel.registerUser(name, email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = "Daftar",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login Navigation
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sudah punya akun? Masuk",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}