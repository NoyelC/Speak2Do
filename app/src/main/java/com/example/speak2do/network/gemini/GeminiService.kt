package com.example.speak2do.network.gemini

import android.util.Log
import com.example.speak2do.network.KtorClientProvider.httpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
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
import java.io.IOException
import java.net.SocketException
import java.net.SocketTimeoutException
import kotlin.math.min

data class GeminiConfig(
    val modelName: String = "gemini-2.5-flash-lite",
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
    private val apiKey: String,
    private val config : GeminiConfig = GeminiConfig()
) {
    private data class GeminiHttpException(val statusCode: Int, override val message: String) : Exception(message)

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
        if (apiKey.isBlank()) {
            throw Exception("Gemini API key is missing. Configure GEMINI_API_KEY in BuildConfig.")
        }

        val requestBody = createTaskExtractionRequest(currentDate, transcript)
        executeWithRetry("extractTaskFromTranscript") { attempt ->
            val response = httpClient.post(generateUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                timeout {
                    requestTimeoutMillis = config.defaultRequestTimeout
                    connectTimeoutMillis = config.connectTimeout
                }
            }

            val responseText = extractResponseText(response)
            if (attempt > 1) {
                Log.w("GeminiService", "Request recovered on retry attempt $attempt.")
            }
            decodeExtractedTaskSafely(responseText)
        }
    }

    /**
     * Optional tuned path:
     * - Keeps the existing prompt unchanged
     * - Uses a stricter, shorter prompt for better JSON reliability
     */
    suspend fun extractTaskFromTranscriptTuned(
        currentDate: String?,
        transcript: String
    ): Result<ExtractedTask> = runCatching {
        if (apiKey.isBlank()) {
            throw Exception("Gemini API key is missing. Configure GEMINI_API_KEY in BuildConfig.")
        }

        val requestBody = createTaskExtractionRequestTuned(currentDate, transcript)
        executeWithRetry("extractTaskFromTranscriptTuned") { attempt ->
            val response = httpClient.post(generateUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
                timeout {
                    requestTimeoutMillis = config.defaultRequestTimeout
                    connectTimeoutMillis = config.connectTimeout
                }
            }

            val responseText = extractResponseText(response)
            if (attempt > 1) {
                Log.w("GeminiService", "Tuned request recovered on retry attempt $attempt.")
            }
            decodeExtractedTaskSafely(responseText)
        }
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

    private fun createTaskExtractionRequestTuned(
        currentDate: String?,
        transcript: String
    ): JsonObject {
        val prompt = """
You extract one actionable task from voice transcript input.

INPUT
current_date: ${currentDate ?: "MISSING"}
transcript: $transcript

OUTPUT RULES
1) Return exactly one JSON object. No markdown, no backticks, no explanation.
2) JSON keys must be exactly:
   task_title, description, date_time, priority, additional_notes
3) Allowed values:
   - task_title: string or null
   - description: string (always non-empty)
   - date_time: ISO 8601 string or null
   - priority: "low" | "medium" | "high"
   - additional_notes: string or null
4) If no actionable task exists, return:
{"task_title":null,"description":"No clear actionable task detected.","date_time":null,"priority":"medium","additional_notes":null}
5) Date/time resolution:
   - today = current_date
   - tomorrow = current_date + 1 day
   - day after tomorrow = current_date + 2 days
   - next week = current_date + 7 days
   - next month = same day next month
   - date without time => 09:00:00 local time
   - time without date => use current_date
   - if current_date missing and only relative date/time is present => date_time null
6) Priority mapping:
   - urgent/asap/immediately/critical/important => high
   - casual/non-urgent wording => low
   - default => medium
7) Ensure valid, closed JSON. No trailing commas.
""".trimIndent()

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
                put("temperature", 0.1)
                put("topP", 0.8)
                put("maxOutputTokens", 256)
                put("responseMimeType", "application/json")
            })
        }
    }

    // -----------------------------
    // Response Extraction
    // -----------------------------

    private suspend fun extractResponseText(response: HttpResponse): String {

        if (!response.status.isSuccess()) {
            val error = response.bodyAsText()
            if (error.contains("reported as leaked", ignoreCase = true)) {
                throw Exception("Gemini API key was revoked as leaked. Generate a new key and update GEMINI_API_KEY.")
            }
            throw GeminiHttpException(
                statusCode = response.status.value,
                message = "Gemini API error: ${response.status} - $error"
            )
        }

        val responseBody = response.body<String>()

        val json = jsonSerializer.parseToJsonElement(responseBody).jsonObject

        json["error"]?.jsonObject?.let {
            val message = it["message"]?.jsonPrimitive?.content ?: "Unknown error"
            val statusCode = it["code"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: -1
            throw GeminiHttpException(statusCode = statusCode, message = message)
        }

        val candidates = json["candidates"]?.jsonArray.orEmpty()
        val extractedTexts = candidates
            .mapNotNull { candidate ->
                candidate.jsonObject["content"]?.jsonObject
                    ?.get("parts")?.jsonArray
                    ?.mapNotNull { part ->
                        part.jsonObject["text"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                    }
                    ?.joinToString("\n")
                    ?.takeIf { it.isNotBlank() }
            }

        if (extractedTexts.isNotEmpty()) {
            return extractedTexts.joinToString("\n").trim()
        }

        val blockReason = json["promptFeedback"]?.jsonObject
            ?.get("blockReason")?.jsonPrimitive?.contentOrNull
        val finishReason = candidates.firstOrNull()?.jsonObject
            ?.get("finishReason")?.jsonPrimitive?.contentOrNull
        val reason = listOfNotNull(
            blockReason?.let { "Blocked: $it" },
            finishReason?.let { "Finish reason: $it" }
        ).joinToString(" | ").ifBlank { "Empty Gemini response" }

        Log.w("GeminiService", "Gemini returned no text candidate. Falling back. $reason")
        return emptyTaskJson(reason)
    }

    private suspend fun <T> executeWithRetry(
        operationName: String,
        block: suspend (attempt: Int) -> T
    ): T {
        val totalAttempts = maxOf(1, config.maxRetries)
        var attempt = 1
        var lastError: Throwable? = null

        while (attempt <= totalAttempts) {
            try {
                return block(attempt)
            } catch (t: Throwable) {
                if (t is CancellationException) throw t
                lastError = t

                val shouldRetry = attempt < totalAttempts && isRetryableFailure(t)
                if (!shouldRetry) break

                val delayMs = computeRetryDelay(attempt)
                Log.w(
                    "GeminiService",
                    "$operationName failed on attempt $attempt/$totalAttempts. Retrying in ${delayMs}ms. Cause: ${t.message}"
                )
                delay(delayMs)
                attempt++
            }
        }

        throw lastError ?: Exception("$operationName failed with unknown error")
    }

    private fun isRetryableFailure(t: Throwable): Boolean {
        return when (t) {
            is GeminiHttpException -> t.statusCode in setOf(429, 500, 502, 503, 504) ||
                t.message.contains("high demand", ignoreCase = true) ||
                t.message.contains("UNAVAILABLE", ignoreCase = true)
            is ResponseException -> t.response.status.value in setOf(429, 500, 502, 503, 504)
            is HttpRequestTimeoutException,
            is SocketTimeoutException,
            is SocketException,
            is IOException -> true
            else -> t.message?.contains("Software caused connection abort", ignoreCase = true) == true
        }
    }

    private fun computeRetryDelay(attempt: Int): Long {
        val exponent = 1L shl (attempt - 1).coerceAtMost(30)
        return min(config.baseRetryDelay * exponent, config.maxRetryDelay)
    }

    private fun emptyTaskJson(reason: String): String {
        return buildJsonObject {
            put("task_title", JsonNull)
            put("description", "No clear actionable task detected. $reason")
            put("date_time", JsonNull)
            put("priority", "medium")
            put("additional_notes", JsonNull)
        }.toString()
    }

    private fun String.cleanJson(): String =
        trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

    private fun decodeExtractedTaskSafely(rawText: String): ExtractedTask {
        val cleaned = rawText.cleanJson()

        runCatching {
            return jsonSerializer.decodeFromString<ExtractedTask>(cleaned)
        }

        // Fallback for malformed/truncated responses (for example missing closing quote/brace).
        val title = extractNullableField(cleaned, "task_title")
        val description = extractStringField(cleaned, "description")
            ?: "No clear actionable task detected."
        val dateTime = extractNullableField(cleaned, "date_time")
        val priority = normalizePriority(
            extractStringField(cleaned, "priority")
                ?: extractUnclosedStringPrefix(cleaned, "priority")
        )
        val notes = extractNullableField(cleaned, "additional_notes")

        return ExtractedTask(
            task_title = title,
            description = description,
            date_time = dateTime,
            priority = priority,
            additional_notes = notes
        )
    }

    private fun extractNullableField(input: String, key: String): String? {
        val nullPattern = Regex("\"$key\"\\s*:\\s*null", RegexOption.IGNORE_CASE)
        if (nullPattern.containsMatchIn(input)) return null
        return extractStringField(input, key) ?: extractUnclosedStringPrefix(input, key)
    }

    private fun extractStringField(input: String, key: String): String? {
        val pattern = Regex("\"$key\"\\s*:\\s*\"([^\"]*)\"", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        return pattern.find(input)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun extractUnclosedStringPrefix(input: String, key: String): String? {
        val keyPattern = Regex("\"$key\"\\s*:\\s*\"?", RegexOption.IGNORE_CASE)
        val match = keyPattern.find(input) ?: return null
        val start = match.range.last + 1
        if (start !in input.indices) return null
        val remainder = input.substring(start)
        val value = remainder
            .lineSequence()
            .firstOrNull()
            .orEmpty()
            .substringBefore(",")
            .substringBefore("}")
            .trim()
            .trim('"')
        return value.ifBlank { null }
    }

    private fun normalizePriority(raw: String?): String {
        return when (raw?.trim()?.lowercase()) {
            "low" -> "low"
            "high" -> "high"
            "medium" -> "medium"
            else -> "medium"
        }
    }


}
@Serializable
data class ExtractedTask(
    val task_title: String? = null,
    val description: String,
    val date_time: String? = null,
    val priority: String,
    val additional_notes: String? = null
)
