package com.example.speak2do.network.gemini

class GeminiRepository(private val service: GeminiService) {
    suspend fun generateTaskJson(currentDate: String, transcript: String): String {
        return service.generateText(currentDate, transcript)
    }
}
