package com.finnolio.chat2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Text
import com.finnolio.chat2.ChatMessage
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun Chat(
    modifier: Modifier = Modifier,
    chatHistory: SnapshotStateList<ChatMessage>,
    isLoading: Boolean,
    aiResponseText: String
) {
    val listState = rememberLazyListState()

    LaunchedEffect(chatHistory.size, aiResponseText.length) {
        val totalItems =
            chatHistory.size +
                    (if (aiResponseText.isNotEmpty()) 1 else 0) +
                    (if (isLoading) 1 else 0)

        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(chatHistory) { message ->
            if (message.isUser) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        message.text,
                        modifier = Modifier
                            .focusable()
                            .background(
                                color = Color.Black,
                                shape = RoundedCornerShape(50)
                            )
                            .padding(16.dp),
                        textAlign = TextAlign.End
                    )
                }
            } else {
                MarkdownText(
                    markdown = message.text,
                    style = TextStyle(color = Color.White)
                )
            }
        }

        if (aiResponseText.isNotEmpty()) {
            item {
                MarkdownText(
                    markdown = aiResponseText,
                    style = TextStyle(color = Color.White)
                )
            }
        }

        if (isLoading) {
            item {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
                Text("Loading...")
            }
        }
    }
}