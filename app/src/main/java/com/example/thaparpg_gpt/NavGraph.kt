package com.example.thaparpg_gpt

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavGraph(navController: NavHostController, viewModel: ChatViewModel) {
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("chat") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onForgetPassword = {
                    navController.navigate("reset_password")
                }
            )
        }

        composable("reset_password") {
            ResetPasswordScreen(
                onResetSuccess = {
                    // Show a success message and navigate back to login
                    navController.navigate("login") {
                        popUpTo("reset_password") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.navigateUp()
                }
            )
        }
        composable("chat") {

            ChatScreenWithDrawer(viewModel = viewModel,  navController = navController)
        }
        composable("profile") {
            ProfileCompletionScreen(
                onSkip = { navController.popBackStack() },
                onSubmitSuccess = { userData ->
                    // Here you can navigate to profile page, or pop back
                    navController.navigate("profile_screen")
                }
            )
        }
        composable("feedback") { FeedbackScreen(navController) }

        composable("thankyou") { ThankYouScreen(navController)}



    }
}
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onForgetPassword: ()->Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()


    var showInfoDialog by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = R.drawable.vector), // Make sure you have an image in res/drawable
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = { showInfoDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            interactionSource = interactionSource
        ) {
            Image(
                painter = painterResource(id = R.drawable.info2), // your image here
                contentDescription = "App Info",
                modifier = Modifier.size(30.dp), // size it properly
                alpha = if (isPressed) 0.7f else 1f // 🔥 Simple hover effect (dim when pressed)
            )
        }

        Image(
            painter = painterResource(id = R.drawable.thapar_logo),
            contentDescription = "Thapar PG-GPT Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
                .size(140.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .background(Color.Black.copy(alpha = 0.50f), shape = RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Hey PostGrads!!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = Color.White) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.0f), shape = RoundedCornerShape(24.dp))
                        ,colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.2f),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy( // Customize text inside text field
                    color = Color.White, // Set the text color to white
                    fontSize = 16.sp, // Set font size
                    fontWeight = FontWeight.Normal, // Normal weight for text
                    letterSpacing = 0.5.sp // Slight letter-spacing
                )
                , shape=  RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = Color.White) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray.copy(alpha = 0.0f), shape = RoundedCornerShape(24.dp)),

                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Gray.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Gray.copy(alpha = 0.2f),
                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy( // Customize text inside text field
                    color = Color.White, // Set the text color to white
                    fontSize = 16.sp, // Set font size
                    fontWeight = FontWeight.Normal, // Normal weight for text
                    letterSpacing = 0.5.sp // Slight letter-spacing
                )
                , shape=  RoundedCornerShape(24.dp)
            )



            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {

                    errorMessage = null
                    successMessage = null

                    if (username.isEmpty()) {
                        // Show some error or Toast
                        errorMessage = "Username cannot be empty"
                        return@Button
                    }
                    if (!username.endsWith("@thapar.edu")) {
                        errorMessage = "Email must end with @thapar.edu"
                        return@Button
                    }
                    if (password.isEmpty()) {
                        errorMessage = "Password cannot be empty"
                        return@Button
                    }
                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters long"
                        return@Button
                    }
                    auth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                successMessage = "Login successful! Welcome, $username"
                                onLoginSuccess()
                            } else {
                                errorMessage = task.exception?.message ?: "Login failed"
                            }
                        }
                }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFCFCFC)
                )
            )
            {
                Text("Login",
                        color = Color.Black)
            }

            Text(
                text = "Forgot Password?",
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .align(Alignment.End) // Align it to the right
                    .clickable {
                        onForgetPassword()
                    },
                color = Color.Yellow
            )

            successMessage?.let {
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .background(
                            Color.Black.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = it,
                        color = Color.Green,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, // Bold for emphasis
                            fontSize = 16.sp, // Set a good font size
                            letterSpacing = 0.5.sp),
                                modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            errorMessage?.let {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                        .background(Color.Black.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = it,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, // Bold for emphasis
                            fontSize = 16.sp, // Set a good font size
                            letterSpacing = 0.5.sp ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Text(text = "About PG-GPT", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This app is designed to help Thapar PostGrad students access ChatGPT features and more. Login using your Thapar credentials to continue.")
            },
            confirmButton = {
                TextButton(
                    onClick = { showInfoDialog = false }
                ) {
                    Text("OK")
                }
            }
        )
    }
}