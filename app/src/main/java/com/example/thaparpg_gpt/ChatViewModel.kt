package com.example.thaparpg_gpt

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.ai.client.generativeai.GenerativeModel


class ChatViewModel: ViewModel() {
    private val _messages= mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> get()=  _messages


    fun sendMessage(content: String,isUserMessage: Boolean) {

        val message = ChatMessage(content, isUserMessage)
        _messages.add(message)

    }
    fun clearMessages() {
        _messages.clear()
    }

    private fun getModelResponse(message: String): String {
        // Call your LLM model API here to get a response
        return "This is a response to: $message"  // Simulating response
    }
}
