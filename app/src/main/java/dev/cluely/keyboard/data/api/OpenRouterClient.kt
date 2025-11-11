package dev.cluely.keyboard.data.api

import android.util.Log
import dev.cluely.keyboard.data.storage.ApiKeyStore
import dev.cluely.keyboard.domain.models.ChatMessage
import dev.cluely.keyboard.domain.models.Message
import dev.cluely.keyboard.domain.models.OpenRouterMessage
import dev.cluely.keyboard.domain.models.OpenRouterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OpenRouterClient @Inject constructor(
    private val httpClient: HttpClient,
    private val apiKeyStore: ApiKeyStore
) {

    private val baseUrl = "https://openrouter.ai/api/v1"
    private val model = "openai/gpt-4-vision" // or "anthropic/claude-3-5-sonnet"

    suspend fun analyzeScreenshot(imageBase64: String, question: String?): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = apiKeyStore.getApiKey()
                if (apiKey.isEmpty()) {
                    return@withContext Result.failure(Exception("API key not configured"))
                }

                val userMessage = if (question != null) {
                    "[IMAGE]\nImage: <image>\n\nQuestion: $question"
                } else {
                    "[IMAGE]\nImage: <image>\n\nPlease analyze this screenshot and tell me what you see. What's the main content and purpose of this screen?"
                }

                val messages = listOf(
                    OpenRouterMessage(
                        role = "user",
                        content = """$userMessage
                            
[This is a base64 encoded image that should be displayed inline]
Data URL: data:image/png;base64,$imageBase64"""
                    )
                )

                val request = OpenRouterRequest(
                    model = model,
                    messages = messages,
                    max_tokens = 500
                )

                val response = httpClient.post("$baseUrl/chat/completions") {
                    header("Authorization", "Bearer $apiKey")
                    header("HTTP-Referer", "https://github.com/filiksyos/cluely-keyboard-android")
                    header("X-Title", "Cluely Keyboard")
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<Map<String, Any>>()

                @Suppress("UNCHECKED_CAST")
                val choices = response["choices"] as? List<Map<String, Any>>
                    ?: return@withContext Result.failure(Exception("Invalid response format"))

                @Suppress("UNCHECKED_CAST")
                val message = choices.firstOrNull()?.get("message") as? Map<String, String>
                    ?: return@withContext Result.failure(Exception("No message in response"))

                val content = message["content"]
                    ?: return@withContext Result.failure(Exception("No content in message"))

                Result.success(content)
            } catch (e: Exception) {
                Log.e("OpenRouterClient", "Error analyzing screenshot", e)
                Result.failure(e)
            }
        }
    }

    suspend fun chatMessage(messages: List<ChatMessage>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = apiKeyStore.getApiKey()
                if (apiKey.isEmpty()) {
                    return@withContext Result.failure(Exception("API key not configured"))
                }

                val openRouterMessages = messages.map {
                    OpenRouterMessage(
                        role = if (it.isUserMessage) "user" else "assistant",
                        content = it.content
                    )
                }

                val request = OpenRouterRequest(
                    model = model,
                    messages = openRouterMessages,
                    max_tokens = 500
                )

                val response = httpClient.post("$baseUrl/chat/completions") {
                    header("Authorization", "Bearer $apiKey")
                    header("HTTP-Referer", "https://github.com/filiksyos/cluely-keyboard-android")
                    header("X-Title", "Cluely Keyboard")
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }.body<Map<String, Any>>()

                @Suppress("UNCHECKED_CAST")
                val choices = response["choices"] as? List<Map<String, Any>>
                    ?: return@withContext Result.failure(Exception("Invalid response format"))

                @Suppress("UNCHECKED_CAST")
                val message = choices.firstOrNull()?.get("message") as? Map<String, String>
                    ?: return@withContext Result.failure(Exception("No message in response"))

                val content = message["content"]
                    ?: return@withContext Result.failure(Exception("No content in message"))

                Result.success(content)
            } catch (e: Exception) {
                Log.e("OpenRouterClient", "Error sending chat message", e)
                Result.failure(e)
            }
        }
    }
}