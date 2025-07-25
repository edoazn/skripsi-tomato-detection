package com.example.docmat.presentation.ui.screens.auth.login

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
import com.example.docmat.presentation.ui.component.PasswordTextField

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle login success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            snackbarHostState.showSnackbar("Login berhasil")
            onLoginSuccess()
        }
    }

    // Show error message
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(
                message = error,
                withDismissAction = true
            )
            viewModel.clearError()
        }
    }


    fun validateInputs(): Boolean {
        var isValid = true

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
        } else if (password.length < 6) {
            passwordError = "Password minimal 6 karakter"
            isValid = false
        } else {
            passwordError = null
        }

        return isValid
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Text header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            {
                Text(
                    text = "Selamat Datang",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Text body
                Text(
                    text = "Silahkan login untuk melanjutkan",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Image
                Image(
                    painter = painterResource(id = R.drawable.login),
                    contentDescription = "Login Image",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.padding(16.dp))

                // Form Section
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
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

                    // Password TextField
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

                    // Login Button with loading indicator
                    Button(
                        onClick = {
                            if (validateInputs()) {
                                viewModel.loginUser(email, password)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = "Masuk",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Register Navigation
                    TextButton(
                        onClick = onNavigateToRegister,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isLoading
                    ) {
                        Text(
                            text = "Belum punya akun? Daftar",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

    }
}


