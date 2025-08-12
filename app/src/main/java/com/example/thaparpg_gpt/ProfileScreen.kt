package com.example.thaparpg_gpt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.*

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

import coil.compose.rememberAsyncImagePainter



@Composable
fun ProfileScreen(userData: Map<String, Any?>) {


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hi, ${userData["fullName"]}!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Gender: ${userData["gender"]}")
        Text("Course: ${userData["course"]}")
        Text("Semester: ${userData["semester"]}")

        if (userData["profilePhoto"] != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Image(
                painter = rememberAsyncImagePainter(model = userData["profilePhoto"]),
                contentDescription = "Profile Photo",
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(60.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
