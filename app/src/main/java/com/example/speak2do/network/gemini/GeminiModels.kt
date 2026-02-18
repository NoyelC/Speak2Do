package com.example.speak2do.network.gemini

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class Content(
    val parts: List<Part> = emptyList(),
    val role: String? = null
)

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null
)

@Serializable
data class GenerationConfig(
    @SerialName("responseMimeType")
    val responseMimeType: String? = null
)
