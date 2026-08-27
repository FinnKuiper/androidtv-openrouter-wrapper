package com.finnolio.chat2.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text

/**
 * Displays a focus-responsive sidebar with a profile row and saved chat sessions.
 *
 * The sidebar expands when it or one of its focusable descendants receives focus and
 * collapses when focus is lost.
 */
@Composable
fun Sidebar() {
    var isExpanded by remember { mutableStateOf(false) }

    val sidebarWidth by animateDpAsState(
        targetValue = if (isExpanded) 224.dp else 68.dp,
        label = "sidebarwidth"
    )

    Column(
        modifier = Modifier
            .width(sidebarWidth)
            .background(Color.DarkGray)
            .padding(8.dp)
            .fillMaxHeight()
            .onFocusChanged { focusState ->
                isExpanded = focusState.hasFocus
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, shape = RoundedCornerShape(8.dp))
                .focusable()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp),
                painter = painterResource(com.finnolio.chat2.R.drawable.profile),
                contentDescription = "Profile",
                tint = Color.White
            )
            if (isExpanded) {
                Text("user")
            }
        }
        if (isExpanded) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "SAVED CHATS",
                color = Color.Gray,
                style = TextStyle(fontSize = 12.sp),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(3) { index ->
                    Text(
                        text = "Chat Session ${index + 1}",
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusable() // Lets the TV remote scroll through them
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}