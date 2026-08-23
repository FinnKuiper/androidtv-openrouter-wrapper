package com.finnolio.chat2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.finnolio.chat2.network.fetchAIStream
import com.finnolio.chat2.ui.components.Chat
import com.finnolio.chat2.ui.theme.Chat2Theme
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            Chat2Theme {
                val scope = rememberCoroutineScope()
                var aiResponseText by remember { mutableStateOf("Output will usually display here") }
                var isLoading by remember { mutableStateOf(false) }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(500.dp)
                                .padding(vertical = 32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .padding(bottom = 16.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                if (isLoading) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White
                                        )
                                        Text("Loading...")
                                    }
                                } else {
                                    MarkdownText(
                                        markdown = aiResponseText,
                                        style = TextStyle(color = Color.White)
                                    )
                                }
                            }

                            Chat(onSubmit = { userPrompt ->
                                isLoading = true
                                scope.launch {
                                    Log.d("TV_APP", "Message: $userPrompt")
                                    aiResponseText = ""
                                    fetchAIStream(userPrompt).collect { chunk ->
                                        isLoading = false
                                        aiResponseText += chunk
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