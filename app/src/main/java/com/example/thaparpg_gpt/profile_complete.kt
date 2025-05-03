package com.example.thaparpg_gpt

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material3.TextFieldDefaults
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
//import androidx.compose.material3.rememberScaffoldState
@Composable
fun DropDownSelector(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(24.dp))
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = if (selectedOption.isNotEmpty()) selectedOption else label,
            color = if (selectedOption.isNotEmpty()) Color.Black else Color.Gray
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCompletionScreen(
    onSkip: () -> Unit,
    onSubmitSuccess: (Map<String, Any?>) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var profilePhotoUri by remember { mutableStateOf<String?>(null) }
    var course by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profilePhotoUri = it.toString()
        }
    }
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    // val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val currentUserEmail = auth.currentUser?.email

    Box(
        modifier = Modifier
            .fillMaxSize() // Apply the padding to the content

    ) { }
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image filling the screen
        Image(
            painter = painterResource(id = R.drawable.bg2), // Replace with your background image name
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Complete Your Profile",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Photo Box
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(50.dp))
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(model = profilePhotoUri),
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent, shape = RoundedCornerShape(50.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Add Photo", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Full Name TextField
            TextField(
                value = fullName,
                onValueChange = { fullName = it },
                placeholder = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF0F0F0),
                    unfocusedContainerColor = Color(0xFFF0F0F0),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Gender TextField
            DropDownSelector(
                label = "Select Gender",
                options = listOf("Male", "Female", "Others"),
                selectedOption = gender,
                onOptionSelected = { gender = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Course TextField
            DropDownSelector(
                label = "Select Course",
                options = listOf("MTech", "MCA"),
                selectedOption = course,
                onOptionSelected = { course = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Semester TextField
            DropDownSelector(
                label = "Select Semester",
                options = listOf("1", "2", "3", "4"),
                selectedOption = semester,
                onOptionSelected = { semester = it }
            )


            Spacer(modifier = Modifier.height(12.dp))

            // Submit and Skip Buttons
            Row {
                Button(
                    onClick = {
                        if (currentUserEmail != null) {
                            val userProfileData = hashMapOf(
                                "fullName" to fullName,
                                "gender" to gender,
                                //"profilePhoto" to profilePhotoUri,
                                "course" to course,
                                "semester" to semester,
                                "profileCompleted" to true
                            )

                            val photoUri = profilePhotoUri
                            if (photoUri != null) {
                                userProfileData["profilePhoto"] = photoUri
                            }

                            val sanitizedEmail = currentUserEmail.replace(".", "_")
                            val ref = database.getReference("users").child(sanitizedEmail)
                            ref.setValue(userProfileData)
                                .addOnSuccessListener {
                                    // Show success message after submission
                                    scope.launch {
                                        onSubmitSuccess(userProfileData)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Handle failure
                                    scope.launch {
                                        //     scaffoldState.snackbarHostState.showSnackbar("Failed to update profile. Please try again.")
                                    }
                                }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1351B0))
                ) {
                    Text("Submit")
                }

                Spacer(modifier = Modifier.width(12.dp))

                TextButton(onClick = { onSkip() }) {
                    Text("Skip")
                }
            }
        }
    }
}
