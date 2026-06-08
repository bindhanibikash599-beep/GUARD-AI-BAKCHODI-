package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object GeminiNetworkService {
    private const val TAG = "GeminiNetworkService"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com"
    private const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private const val SYSTEM_PROMPT = """You are Guard English AI, a professional English writing assistant designed by Bikash Bindhani for security guards, supervisors, housekeeping staff, facility management teams, and field employees.
Convert user inputs into correct, professional workplace English suitable for: Attendance Reports, Incident Reports, Leave Applications, Visitor Logs, Shift Handovers, Daily Security Logs, and professional WhatsApp communications.
Translate from local Indian languages (Hindi, Odia, Bengali, Tamil, Telugu, Marathi, Hinglish, etc.) into executive, high-quality, correct workplace English.
Always maintain:
• Professional, respectful and assertive tone.
• Impeccable grammar and clear presentation.
• Industry-standard terminology (e.g., 'Relieving Guard', 'Occurrence Register', 'On-duty Supervisor', 'Visitor Logbook').
• Ready-to-paste WhatsApp or corporate layout.
Ensure your response is concise, high-quality, and directly represents the clean text ready for use in the field."""

    /**
     * Sends a request to the AI model. 
     * Uses OpenRouter if apiKey is set and looks like OpenRouter, otherwise defaults to Gemini API.
     */
    suspend fun generateContent(
        prompt: String,
        customModelName: String = "",
        openRouterKey: String = "",
        systemModePrompt: String = ""
    ): String = withContext(Dispatchers.IO) {
        val selectedSystem = if (systemModePrompt.isNotEmpty()) "$SYSTEM_PROMPT\nSpecific Task Instruction: $systemModePrompt" else SYSTEM_PROMPT

        val resolvedOpenRouterKey = if (openRouterKey.isNotEmpty()) {
            openRouterKey
        } else {
            val buildConfigKey = BuildConfig.OPENROUTER_API_KEY
            if (buildConfigKey.isNotEmpty() && buildConfigKey != "MY_OPENROUTER_API_KEY") {
                buildConfigKey
            } else {
                ""
            }
        }

        // Check if we should use OpenRouter
        if (resolvedOpenRouterKey.isNotEmpty() && resolvedOpenRouterKey.startsWith("sk-or-")) {
            return@withContext callOpenRouter(prompt, customModelName.ifEmpty { "z-ai/glm-4.5-air:free" }, resolvedOpenRouterKey, selectedSystem)
        } else {
            return@withContext callGemini(prompt, customModelName.ifEmpty { "gemini-3.5-flash" }, selectedSystem)
        }
    }

    private fun callGemini(prompt: String, model: String, sysPrompt: String): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is empty or placeholder in BuildConfig!")
            return "Error: Gemini API Key is missing. Please configure it in the Secrets panel in AI Studio."
        }

        val resolvedModel = if (model.contains("gemini")) model else "gemini-3.5-flash"
        val url = "$GEMINI_BASE_URL/v1beta/models/$resolvedModel:generateContent?key=$apiKey"

        try {
            // Build the standard Gemini REST JSON request payload
            val root = JSONObject()
            
            // systemInstruction
            val systemInstruction = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", sysPrompt))
            systemInstruction.put("parts", sysParts)
            root.put("systemInstruction", systemInstruction)

            // contents
            val contents = JSONArray()
            val partObject = JSONObject()
            partObject.put("text", prompt)
            val partsArray = JSONArray()
            partsArray.put(partObject)
            val contentObj = JSONObject()
            contentObj.put("parts", partsArray)
            contents.put(contentObj)
            root.put("contents", contents)

            // generationConfig
            val config = JSONObject()
            config.put("temperature", 0.7)
            root.put("generationConfig", config)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = root.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini call failed: ${response.code} / $responseStr")
                    return "Error from Gemini API: Code ${response.code}. Please ensure your internet is connected and API Key is valid."
                }

                val jsonResponse = JSONObject(responseStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return parts.getJSONObject(0).optString("text", "No response content found.")
                        }
                    }
                }
                return "AI Response structure was empty or unexpected: $responseStr"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during callGemini: ${e.message}", e)
            return "Network Error: ${e.localizedMessage ?: "Failed to reach AI service."}"
        }
    }

    private fun callOpenRouter(prompt: String, model: String, key: String, sysPrompt: String): String {
        val url = "$OPENROUTER_BASE_URL/chat/completions"

        try {
            val root = JSONObject()
            root.put("model", model)

            val messages = JSONArray()
            
            // System message
            messages.put(JSONObject().apply {
                put("role", "system")
                put("content", sysPrompt)
            })

            // User message
            messages.put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })

            root.put("messages", messages)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = root.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $key")
                .addHeader("HTTP-Referer", "https://github.com/com.example/guard-english-ai")
                .addHeader("X-Title", "Guard English AI")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "OpenRouter call failed: ${response.code} / $responseStr")
                    return "Error from OpenRouter: Code ${response.code}. Response: $responseStr"
                }

                val jsonResponse = JSONObject(responseStr)
                val choices = jsonResponse.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val message = choices.getJSONObject(0).optJSONObject("message")
                    if (message != null) {
                        return message.optString("content", "No contents found.")
                    }
                }
                return "Unexpected OpenRouter response structure: $responseStr"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during callOpenRouter: ${e.message}", e)
            return "OpenRouter Network Error: ${e.localizedMessage}"
        }
    }
}
