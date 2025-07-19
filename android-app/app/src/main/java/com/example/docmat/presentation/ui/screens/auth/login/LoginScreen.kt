package com.example.docmat.presentation.ui.screens.auth.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.docmat.R
import com.example.docmat.presentation.ui.component.EmailTextField
import com.example.docmat.presentation.ui.component.PasswordTextField

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // Tampil halaman login berserta register

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Text header
        Text(
            text = "Login Screen",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Start
        )
        // Text body
        Text(
            text = "Silahkan login untuk melanjutkan",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Image
        Image(
            painter = painterResource(id = R.drawable.login),
            contentDescription = "Login Image",
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()

        )

        Spacer(modifier = Modifier.padding(16.dp))

        // Email TextField
        EmailTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            isError = false // You can add error handling if needed
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password TextField
        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            isError = false // You can add error handling if needed
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login Button
        Button(
            onClick = { onLoginSuccess(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigate to Register
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register")
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginSuccess = { email, password ->
            // Handle login success
        },
        onNavigateToRegister = {
            // Handle navigation to register screen
        }
    )
}



