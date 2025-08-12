package com.example.thaparpg_gpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.tooling.preview.Preview
import com.example.thaparpg_gpt.ui.theme.ThaparPGGPTTheme
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()

            val viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

            AppNavGraph(navController = navController, viewModel = viewModel)
            }
        }
    }

