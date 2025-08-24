package com.example.docmat.presentation.ui.screens.auth.register

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.docmat.R
import com.example.docmat.RegisterEvent
import com.example.docmat.presentation.ui.component.EmailTextField
import com.example.docmat.presentation.ui.component.GradientButton
import com.example.docmat.presentation.ui.component.NameTextField
import com.example.docmat.presentation.ui.component.PasswordTextField

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onEvent: (RegisterEvent) -> Unit,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val isFormValid by viewModel.isFormValid.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle register success
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            snackbarHostState.showSnackbar("Register berhasil")
            kotlinx.coroutines.delay(1000)
            onRegisterSuccess()
        }
    }

    // Handle error
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onEvent(RegisterEvent.ErrorShown)
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Buat akun baru untuk memulai",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Image(
                    painter = painterResource(id = R.drawable.register),
                    contentDescription = "register image",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(MaterialTheme.shapes.medium),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.padding(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // name input
                NameTextField(
                    value = state.name,
                    onValueChange = { onEvent(RegisterEvent.NameChanged(it)) },
                    isError = state.nameError != null,
                    errorMessage = state.nameError,
                    enabled = !state.isLoading
                )
                Spacer(Modifier.height(16.dp))
                // email input
                EmailTextField(
                    value = state.email,
                    onValueChange = { onEvent(RegisterEvent.EmailChanged(it)) },
                    isError = state.emailError != null,
                    errorMessage = state.emailError,
                    enabled = !state.isLoading
                )
                Spacer(Modifier.height(16.dp))

                // password input
                PasswordTextField(
                    value = state.password,
                    onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
                    isError = state.passwordError != null,
                    errorMessage = state.passwordError,
                    enabled = !state.isLoading,
                )
                Spacer(Modifier.height(16.dp))

                // confirm password
                PasswordTextField(
                    value = state.confirmPassword,
                    onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                    isError = state.confirmPasswordError != null,
                    errorMessage = state.confirmPasswordError,
                    enabled = !state.isLoading,
//                    placeholder = "Konfirmasi password"
                )
                Spacer(Modifier.height(32.dp))
                // register button
                GradientButton(
                    text = "Daftar",
                    onClick = { onEvent(RegisterEvent.Submit) },
                    enabled = isFormValid && !state.isLoading,
                    isLoading = state.isLoading
                )

                Spacer(Modifier.height(16.dp))

                // Navigate to Login
                TextButton(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(
                        text = "Sudah punya akun? Masuk",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}