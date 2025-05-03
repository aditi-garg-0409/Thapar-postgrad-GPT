package com.example.thaparpg_gpt

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(navController: NavController) {
    var rating by remember { mutableStateOf(0) }
    var feedbackText by remember { mutableStateOf("") }
    val primaryColor = Color(0xFF1351B0)
    val lightPrimaryColor = primaryColor.copy(alpha = 0.3f)
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feedback") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Star, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1351B0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Rate the App", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Row {
                (1..4).forEach { star ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "$star star",
                        tint = if (rating >= star) Color.Yellow else Color.Gray,
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { rating = star }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = feedbackText,
                onValueChange = { feedbackText = it },
                label = { Text("Your feedback (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    isSaving = true
                    saveFeedback(rating, feedbackText) { success ->
                        isSaving = false
                        if (success) {
                            navController.navigate("thankyou")
                        } else {
                            // Show error
                            Toast.makeText(
                                navController.context,
                                "Failed to save feedback. Please login first.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick =  { navController.popBackStack() }, // Just go back, no save
                colors = ButtonDefaults.buttonColors(containerColor = lightPrimaryColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go Back")
            }

            TextButton(
                onClick = { testFirebaseConnection() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Test Firebase Connection")
            }
        }
    }
}


//firebase data store
fun saveFeedback(rating: Int, feedbackText: String, onResult: (Boolean) -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val database = FirebaseDatabase.getInstance()

    user?.email?.let { email ->
        val sanitizedEmail = email.replace(".", "_") // Firebase doesn't allow '.' in keys
        val feedbackRef = database.reference.child("feedbacks").push()

        val feedbackData = mapOf(
            "userEmail" to user.email,
            "userId" to user.uid,
            "rating" to rating,
            "feedbackText" to feedbackText,
            "timestamp" to ServerValue.TIMESTAMP
        )


        Log.d("FeedbackScreen", "Attempting to save feedback for user: ${user.email}"
        )
        feedbackRef.setValue(feedbackData)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { exception ->
                Log.e("FeedbackScreen", "Failed to save feedback: ${exception.message}", exception)
                onResult(false) }
    } ?: run {
        onResult(false)
    }
}
fun testFirebaseConnection() {
    val database = FirebaseDatabase.getInstance()
    val testRef = database.reference.child("test").push()

    val testData = mapOf(
        "timestamp" to ServerValue.TIMESTAMP,
        "testValue" to "This is a test entry"
    )

    Log.d("FirebaseTest", "Attempting to write test data...")

    testRef.setValue(testData)
        .addOnSuccessListener {
            Log.d("FirebaseTest", "Test write successful!")
        }
        .addOnFailureListener { e ->
            Log.e("FirebaseTest", "Test write failed: ${e.message}", e)
        }
        .addOnCompleteListener {
            Log.d("FirebaseTest", "Test write operation completed")
        }
}

//thankyou screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThankYouScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thank You") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Star, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1351B0),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Thank you for your feedback!",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF1351B0)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.navigate("chat") }) {
                Text("Back to Chat")
            }
        }
    }
}