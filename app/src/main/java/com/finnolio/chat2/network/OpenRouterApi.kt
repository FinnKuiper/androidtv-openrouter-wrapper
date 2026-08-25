package com.finnolio.chat2.network

import android.util.Log
import com.finnolio.chat2.BuildConfig
import com.finnolio.chat2.ChatMessage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit


data class Message(val role: String, val content: String)
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = true
)

data class StreamResponse(val choices: List<StreamChoice>)
data class StreamChoice(val delta: Delta)
data class Delta(val content: String?)

data class ChatResponse(val choices: List<Choice>)
data class Choice(val message: Message)

/**
 * Sends a prompt to the OpenRouter chat-completion API and extracts the assistant's response.
 *
 * @param prompt The prompt to send to OpenRouter.
 * @return The trimmed assistant response, or an error message if the request fails or the response cannot be parsed.
 */
suspend fun fetchAIResponse(prompt: String): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val gson = Gson()

    val requestData = ChatRequest(
        model = "deepseek/deepseek-v4-flash-0731",
        messages = listOf(Message("user", prompt))
    )

    val jsonString = gson.toJson(requestData)
    val body = jsonString.toRequestBody("application/json".toMediaType())

    val request = Request.Builder().url("https://openrouter.ai/api/v1/chat/completions").post(body)
        .addHeader(
            "Authorization",
            "bearer ${BuildConfig.OPENROUTER_API_KEY}"
        ).build()

    client.newCall(request).execute()
        .use { response ->
            if (!response.isSuccessful) return@withContext "Error: ${response.code}"

//          get raw json
            val rawJson = response.body?.string()

            return@withContext try {
                val parsedResponse = gson.fromJson(rawJson, ChatResponse::class.java)

                val aiText = parsedResponse.choices[0].message.content
                aiText.trim()
            } catch (e: Exception) {
                "Error parsing response: ${e.message}"
            }
        }
}

/**
 * Streams an AI response for the supplied conversation history.
 *
 * @param history The conversation messages, including the latest message.
 * @return A flow of text chunks produced by the AI model.
 */
fun fetchAIStream(history: List<ChatMessage>): Flow<String> = callbackFlow {
    val client =
        OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS).build()
    val gson = Gson()

    val apiMessages = history.map({ msg ->
        val role = if (msg.isUser) "user" else "assistant"
        Message(role = role, content = msg.text)
    })

    val requestData =
        ChatRequest("deepseek/deepseek-v4-flash-0731", apiMessages)
    val request = Request.Builder()
        .url("https://openrouter.ai/api/v1/chat/completions")
        .post(gson.toJson(requestData).toRequestBody("application/json".toMediaType()))
        .addHeader("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
        .build()

    val listener = object : EventSourceListener() {
        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            if (data == "[DONE]") {
                close()
                return
            }
            try {
                val response = gson.fromJson(data, StreamResponse::class.java)
                response.choices.firstOrNull()?.delta?.content?.let { chunk ->
                    trySend(chunk)
                }
            } catch (e: Exception) {
            }
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            val serverErrorBody = response?.body?.string()

            Log.e("TV_APP_NETWORK", "HTTP Code: ${response?.code}")
            Log.e("TV_APP_NETWORK", "Exception: ${t?.message}")
            Log.e(
                "TV_APP_NETWORK",
                "OpenRouter Error: $serverErrorBody"
            )

            // 2. Try to show the server error, otherwise fallback to the exception
            val displayError = serverErrorBody ?: t?.message ?: "Unknown Error"
            trySend("\n[Error: $displayError]")
            close()
        }
    }
    EventSources.createFactory(client).newEventSource(request, listener)
    awaitClose { }
}