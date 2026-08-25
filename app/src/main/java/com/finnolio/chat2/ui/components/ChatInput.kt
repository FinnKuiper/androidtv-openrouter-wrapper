package com.finnolio.chat2.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.finnolio.chat2.ui.theme.Chat2Theme

/**
 * The input for AI responses
 *
 * When input is submitted it will return the submit function
 *
 * @param onSubmit accepts the submit function
 */
/**
 * Displays a single-line input for submitting chat prompts.
 *
 * Empty prompts are ignored. After a valid submission, the input is cleared and the software keyboard is hidden.
 *
 * @param onSubmit Called with the submitted prompt text.
 */
@Composable
fun ChatInput(onSubmit: (String) -> Unit) {
    var promptText by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    BasicTextField(
        value = promptText,
        onValueChange = { promptText = it },
        textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = {
            if (promptText.isEmpty()) {
                return@KeyboardActions
            }
            onSubmit(promptText)
            keyboardController?.hide()
            promptText = ""
        }),
        modifier = Modifier
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .focusable()
            .width(500.dp),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(Color.DarkGray, shape = CircleShape)
                    .padding(16.dp)
            ) {
                if (promptText.isEmpty()) {
                    Text(
                        text = "Ask something...",
                        color = Color.LightGray
                    )
                }
                innerTextField()
            }

        }
    )
}

@Preview(showBackground = true)
@Composable
fun ChatPreview() {
    Chat2Theme {
        ChatInput(onSubmit = { userPrompt -> Log.d("PREVIEW", userPrompt) })
    }
}