package com.example.speak2do

object GeminiApiKeyProvider {
    fun getGeminiApiKey(): String {
        val keyFromBuildConfig = runCatching {
            val field = BuildConfig::class.java.getDeclaredField("GEMINI_API_KEY")
            (field.get(null) as? String).orEmpty()
        }.getOrDefault("")

        return keyFromBuildConfig.trim()
    }
}
