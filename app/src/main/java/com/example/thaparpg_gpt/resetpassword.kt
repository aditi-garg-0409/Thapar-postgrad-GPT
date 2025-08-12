package com.example.thaparpg_gpt



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ResetPasswordScreen(onResetSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    // All the ResetPasswordScreen code from my previous response
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var resetCode by remember { mutableStateOf("") }
    var isCodeSent by remember { mutableStateOf(false) }
    var passwordsMatch by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.login1),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Logo
        Image(
            painter = painterResource(id = R.drawable.thapar_logo),
            contentDescription = "Thapar PG-GPT Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
                .size(120.dp)
        )

        // Reset Password Form
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.50f), shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Reset Password",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            // Rest of the form implementation...
            // First stage: Email input to receive reset code
            if (!isCodeSent) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Enter your email", color = Color.White) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            // Ideally send code here
                            isCodeSent = true
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1351B0))
                ) {
                    Text("Send Reset Code")
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Back to Login",
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickable { onBackToLogin() },
                    color = Color.White
                )

            } else {
                OutlinedTextField(
                    value = resetCode,
                    onValueChange = { resetCode = it },
                    label = { Text("Enter Reset Code", color = Color.White) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        passwordsMatch = newPassword == confirmPassword
                    },
                    label = { Text("Confirm Password", color = Color.White) },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (!passwordsMatch) {
                    Text(
                        "Passwords do not match",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )

                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Back to Login",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.CenterHorizontally)
                        .clickable { onBackToLogin() },
                    color = Color.White
                )
            }
        }
    }
}