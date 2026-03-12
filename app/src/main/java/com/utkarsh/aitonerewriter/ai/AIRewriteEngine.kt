package com.utkarsh.aitonerewriter.ai

import com.utkarsh.aitonerewriter.BuildConfig
import com.utkarsh.aitonerewriter.model.Mood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * AI-powered text rewriting engine.
 * Uses OpenAI Chat Completions API to rewrite messages based on selected mood.
 * No messages are stored — privacy first.
 */
class AIRewriteEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val apiKey: String
        get() = BuildConfig.OPENAI_API_KEY

    private val baseUrl = "https://api.openai.com/v1/chat/completions"
    private val model = "gpt-4o-mini"
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Rewrites the given text using the specified mood/tone.
     *
     * @param originalText The text to rewrite
     * @param mood The target mood/tone
     * @return Result containing the rewritten text or an error
     */
    suspend fun rewrite(originalText: String, mood: Mood): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(
                        IllegalStateException("OpenAI API key not configured. Add OPENAI_API_KEY to local.properties")
                    )
                }

                if (originalText.isBlank()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("No text to rewrite")
                    )
                }

                val requestBody = buildRequestBody(originalText, mood)
                val request = Request.Builder()
                    .url(baseUrl)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toString().toRequestBody(mediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (!response.isSuccessful || responseBody == null) {
                    return@withContext Result.failure(
                        RuntimeException("API error: ${response.code} - ${responseBody ?: "No response"}")
                    )
                }

                val json = JSONObject(responseBody)
                val rewrittenText = json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim()

                Result.success(rewrittenText)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun buildRequestBody(text: String, mood: Mood): JSONObject {
        val messages = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", buildSystemPrompt(mood))
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", text)
            })
        }

        return JSONObject().apply {
            put("model", model)
            put("messages", messages)
            put("max_tokens", 500)
            put("temperature", 0.7)
        }
    }

    private fun buildSystemPrompt(mood: Mood): String {
        return """
            ${mood.systemPrompt}
            
            Rules:
            - Return ONLY the rewritten message, nothing else.
            - Do NOT add quotes around the message.
            - Do NOT add any explanation or prefix.
            - Keep the same language as the original message.
            - Preserve any names, dates, or specific details mentioned.
            - Match the approximate length of the original (unless the mood is "Short & Crisp").
        """.trimIndent()
    }
}
