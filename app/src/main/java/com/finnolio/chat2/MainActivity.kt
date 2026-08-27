package com.finnolio.chat2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.finnolio.chat2.network.fetchAIStream
import com.finnolio.chat2.ui.components.Chat
import com.finnolio.chat2.ui.components.ChatInput
import com.finnolio.chat2.ui.components.Sidebar
import com.finnolio.chat2.ui.theme.Chat2Theme
import kotlinx.coroutines.launch

data class ChatMessage(val text: String, val isUser: Boolean)
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            Chat2Theme {
                val scope = rememberCoroutineScope()
                val chatHistory = remember { mutableStateListOf<ChatMessage>() }

                var aiResponseText by remember { mutableStateOf("") }
                var isStreaming by remember { mutableStateOf(false) }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    Row {
                        Sidebar()
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(500.dp)
                                    .padding(vertical = 32.dp)
                            ) {
                                Chat(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize()
                                        .padding(bottom = 16.dp),
                                    chatHistory,
                                    isStreaming,
                                    aiResponseText
                                )
                                ChatInput(onSubmit = { userPrompt ->
                                    if (isStreaming) return@ChatInput

                                    chatHistory.add(
                                        ChatMessage(userPrompt, true)
                                    )
                                    aiResponseText = ""
                                    isStreaming = true

                                    scope.launch {
                                        try {
                                            fetchAIStream(chatHistory.toList()).collect { chunk ->
                                                aiResponseText += chunk
                                            }
                                        } finally {
                                            if (aiResponseText.isNotEmpty()) {
                                                chatHistory.add(
                                                    ChatMessage(
                                                        text = aiResponseText,
                                                        isUser = false
                                                    )
                                                )
                                            }
                                            aiResponseText = ""
                                            isStreaming = false
                                        }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    }
}