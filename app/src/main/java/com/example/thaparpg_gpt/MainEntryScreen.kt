package com.example.thaparpg_gpt

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun MainEntryScreen() {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    var showProfileCompletion by remember { mutableStateOf<Boolean?>(null) }
    var userData by remember { mutableStateOf<Map<String, Any?>?>(null) }


    LaunchedEffect(Unit) {
        val email = auth.currentUser?.email
        if (email != null) {
            val sanitizedEmail = email.replace(".", "_")
            val ref = database.getReference("users").child(sanitizedEmail)

            ref.get().addOnSuccessListener { snapshot ->
                val rawData = snapshot.value as? Map<*, *>
                val profileData = rawData?.mapKeys { it.key.toString() }
                if (profileData != null &&
                    profileData.containsKey("fullName") &&
                    profileData.containsKey("gender") &&
                    profileData.containsKey("course") &&
                    profileData.containsKey("semester")
                ) {
                    userData = profileData
                    showProfileCompletion = false
                } else {
                    showProfileCompletion = true
                }
            }.addOnFailureListener {
                showProfileCompletion = true
            }
        } else {
            showProfileCompletion = true
        }
    }

    when (showProfileCompletion) {
        true -> ProfileCompletionScreen(
            onSkip = { showProfileCompletion = false },
            onSubmitSuccess = { updatedData ->
                userData = updatedData
                showProfileCompletion = false
            }
        )

        false -> {
            if (userData != null) {
                ProfileScreen(userData!!)
            } else {
                Text("Loading profile...")
            }
        }

        null -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(200.dp))
                CircularProgressIndicator()
            }
        }
    }
}
