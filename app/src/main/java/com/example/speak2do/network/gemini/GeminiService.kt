package com.example.speak2do.network.gemini

import android.util.Log
import com.example.speak2do.network.KtorClientProvider.httpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

data class GeminiConfig(
    val modelName: String = "gemini-2.5-flash",
    val maxRetries: Int = 3,
    val baseRetryDelay: Long = 2000L,
    val maxRetryDelay: Long = 30000L,
    val defaultRequestTimeout: Long = 300_000L, // 5 minutes
    val analysisTimeout: Long = 600_000L, // 10 minutes
    val connectTimeout: Long = 60_000L, // 1 minute
    val socketTimeout: Long = 300_000L // 5 minutes
)
class GeminiService(
    private val client: HttpClient,
    private val apiKey: String ="AIzaSyA88ajuWbQYYuyYD_gXqC40XjwSsMXgSk8",
    private val config : GeminiConfig = GeminiConfig()
) {

   /* suspend fun generateText(
        currentDate: String,
        transcript: String,
        model: String = "gemini-2.0-flash"
    ): String {
        if (apiKey.isBlank()) {
            Log.e("GeminiService", "Missing Gemini API key")
            return fallbackJson("Missing API key")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"

        val finalPrompt = """
            Current Date: $currentDate
            Voice Note Transcript: $transcript
            Extract one primary actionable task.
            Return STRICT JSON only with fields:
            task_title, description, date_time, priority, additional_notes.
            Never include markdown code fences.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = finalPrompt))
                )
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json"
            )
        )
        val requestNoMime = GeminiRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = finalPrompt))
                )
            ),
            generationConfig = null
        )

        return try {
            val primaryRaw = generateWithModel(url, request)
            Log.d("GeminiService", "Primary raw response: ${primaryRaw.take(1200)}")
            val primaryParsed = extractTextFromRawResponse(primaryRaw)
            var rawText = primaryParsed.text

            if (rawText.isNullOrBlank()) {
                Log.w("GeminiService", "Empty response from $model with JSON mime. Retrying without JSON mime.")
                val retryRawNoMime = generateWithModel(url, requestNoMime)
                Log.d("GeminiService", "Retry(no mime) raw response: ${retryRawNoMime.take(1200)}")
                val retryNoMimeParsed = extractTextFromRawResponse(retryRawNoMime)
                rawText = retryNoMimeParsed.text
            }

            if (rawText.isNullOrBlank() && model != "gemini-1.5-flash") {
                val retryUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"
                Log.w("GeminiService", "Still empty. Retrying with gemini-1.5-flash without JSON mime.")
                val retryRaw = generateWithModel(retryUrl, requestNoMime)
                Log.d("GeminiService", "Retry raw response: ${retryRaw.take(1200)}")
                val retryParsed = extractTextFromRawResponse(retryRaw)
                rawText = retryParsed.text
                if (rawText.isNullOrBlank()) {
                    return fallbackJson(retryParsed.emptyReason ?: "Model returned empty response")
                }
            }

            val jsonText = normalizeJsonText(rawText.orEmpty())
            if (isJson(jsonText)) jsonText else fallbackJson("Invalid JSON returned by model")
        } catch (e: ResponseException) {
            Log.e("GeminiService", "HTTP error: ${e.response.status}", e)
            fallbackJson("HTTP error: ${e.response.status.value}")
        } catch (e: Throwable) {
            Log.e("GeminiService", "Gemini error", e)
            fallbackJson(e.message ?: "Unknown error")
        }
    }

    private fun normalizeJsonText(rawText: String): String {
        val text = rawText.trim()
        if (!text.startsWith("```")) return text

        return text
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private suspend fun generateWithModel(url: String, request: GeminiRequest): String {
        val response = client.post(url) {
            parameter("key", apiKey.trim())
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.bodyAsText()
    }

    private data class ParsedModelResponse(
        val text: String?,
        val emptyReason: String?
    )

    private fun extractTextFromRawResponse(raw: String): ParsedModelResponse {
        return try {
            val root = Json.parseToJsonElement(raw).asObject()
            val apiError = root?.get("error")?.asObject()?.get("message")?.asString()
            if (!apiError.isNullOrBlank()) {
                return ParsedModelResponse(null, "API error: $apiError")
            }

            val candidates = root?.get("candidates").asArray()
            val texts = candidates
                ?.mapNotNull { candidate ->
                    candidate.asObject()
                        ?.get("content")
                        .asObject()
                        ?.get("parts")
                        .asArray()
                        ?.mapNotNull { it.asObject()?.get("text").asString() }
                        ?.joinToString("\n")
                }
                ?.filter { it.isNotBlank() }
                .orEmpty()

            if (texts.isNotEmpty()) {
                ParsedModelResponse(texts.joinToString("\n").trim(), null)
            } else {
                val blockReason = root?.get("promptFeedback")
                    .asObject()
                    ?.get("blockReason")
                    .asString()
                val finishReason = candidates
                    ?.firstOrNull()
                    .asObject()
                    ?.get("finishReason")
                    .asString()
                val reason = listOfNotNull(
                    blockReason?.let { "Blocked: $it" },
                    finishReason?.let { "Finish reason: $it" }
                ).joinToString(" | ")
                ParsedModelResponse(null, if (reason.isBlank()) "Model returned empty response" else reason)
            }
        } catch (e: Exception) {
            ParsedModelResponse(null, "Failed to parse model response: ${e.message}")
        }
    }

    private fun isJson(text: String): Boolean {
        return try {
            Json.parseToJsonElement(text)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun fallbackJson(reason: String): String {
        return buildJsonObject {
            put("task_title", JsonNull)
            put("description", "No clear task detected: $reason")
            put("date_time", JsonNull)
            put("priority", "medium")
            put("additional_notes", JsonNull)
        }.toString()
    }

    private fun JsonElement?.asObject(): JsonObject? = this as? JsonObject
    private fun JsonElement?.asArray(): JsonArray? = this as? JsonArray
    private fun JsonElement?.asString(): String? = this?.jsonPrimitive?.contentOrNull*/

    private val generateUrl =
        "https://generativelanguage.googleapis.com/v1beta/models/${config.modelName}:generateContent?key=$apiKey"

    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Public API call to extract task from transcript
     */
    suspend fun extractTaskFromTranscript(
        currentDate: String?,
        transcript: String
    ): Result<ExtractedTask> = runCatching {

        val requestBody = createTaskExtractionRequest(currentDate, transcript)

        Log.e("GEMINI::", "extractTaskFromTranscript:--->$currentDate ", )
        Log.e("GEMINI::", "extractTaskFromTranscripttranscript:--->$transcript ", )

        val response = httpClient.post(generateUrl) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
            timeout {
                requestTimeoutMillis = config.defaultRequestTimeout
                connectTimeoutMillis = config.connectTimeout
            }
        }
        Log.e("GEMINI::", "extractTaskFromTranscript:----->$response ", )

        val responseText = extractResponseText(response)
        Log.e("GEMINI::", "extractTaskFromTranscript:response---->$responseText ", )
        val cleaned = responseText.cleanJson()

        jsonSerializer.decodeFromString<ExtractedTask>(cleaned)
    }

    // -----------------------------
    // Request Builder
    // -----------------------------

    private fun createTaskExtractionRequest(
        currentDate: String?,
        transcript: String
    ): JsonObject {

        val prompt = """
You are an expert task extraction assistant.

Analyze the provided voice note transcript and extract structured task details in strict JSON format.

The input will include:
- Current Date: ${currentDate ?: "MISSING"}
- Voice Note Transcript: $transcript

Your responsibilities:

1. Identify the primary actionable task clearly described in the transcript.

2. Extract and structure the following fields:

- task_title
- description
- date_time (ISO 8601)
- priority ["low","medium","high"]
- additional_notes

Date handling rules:
- today → Current Date
- tomorrow → Current Date + 1 day
- day after tomorrow → Current Date + 2 days
- next week → Current Date + 7 days
- next month → same day next month

Time handling:
- If date without time → default "09:00:00"
- If only time → use Current Date
- If both → combine
- If none → null
- If Current Date missing → null

Priority rules:
- urgent/asap/immediately/critical/important → high
- casual tone → low
- default → medium

If no actionable task exists:
{
  "task_title": null,
  "description": "No clear actionable task detected.",
  "date_time": null,
  "priority": "medium",
  "additional_notes": null
}

Return ONLY valid JSON.
"""

        return buildJsonObject {
            put("contents", buildJsonArray {
                add(buildJsonObject {
                    put("parts", buildJsonArray {
                        add(buildJsonObject {
                            put("text", prompt)
                        })
                    })
                })
            })

            put("generationConfig", buildJsonObject {
                put("temperature", 0.2)
                put("topP", 0.9)
                put("maxOutputTokens", 1024)
            })
        }
    }

    // -----------------------------
    // Response Extraction
    // -----------------------------

    private suspend fun extractResponseText(response: HttpResponse): String {

        if (!response.status.isSuccess()) {
            val error = response.bodyAsText()
            throw Exception("Gemini API error: ${response.status} - $error")
        }

        val responseBody = response.body<String>()

        val json = jsonSerializer.parseToJsonElement(responseBody).jsonObject

        json["error"]?.jsonObject?.let {
            val message = it["message"]?.jsonPrimitive?.content ?: "Unknown error"
            throw Exception(message)
        }

        return json["candidates"]?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray
            ?.firstOrNull()?.jsonObject
            ?.get("text")?.jsonPrimitive?.content
            ?: throw Exception("No valid response from Gemini")
    }

    private fun String.cleanJson(): String =
        trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()


}
@Serializable
data class ExtractedTask(
    val task_title: String? = null,
    val description: String,
    val date_time: String? = null,
    val priority: String,
    val additional_notes: String? = null
)
