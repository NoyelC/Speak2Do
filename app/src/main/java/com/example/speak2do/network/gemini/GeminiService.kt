package com.example.speak2do.network.gemini

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class GeminiService(
    private val client: HttpClient,
    private val apiKey: String
) {
    suspend fun generateText(prompt: String, model: String = "gemini-1.5-flash"): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = prompt)))))
        val response: GeminiResponse = client.post(url) {
            parameter("key", apiKey)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        val candidate = response.candidates?.firstOrNull()
        val content = candidate?.content
        val part = content?.parts?.firstOrNull()
        return part?.text ?: ""
    }
}
