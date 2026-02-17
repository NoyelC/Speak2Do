package com.example.speak2do.network.gemini

import com.example.speak2do.network.KtorClientProvider

object Gemini {
    fun create(apiKey: String): GeminiService {
        return GeminiService(KtorClientProvider.httpClient, apiKey)
    }
}
