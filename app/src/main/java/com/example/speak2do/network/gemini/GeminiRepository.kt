package com.example.speak2do.network.gemini

class GeminiRepository(private val service: GeminiService) {
    suspend fun generateTaskJson(currentDate: String, transcript: String): Result<ExtractedTask> {
        val tuned = service.extractTaskFromTranscriptTuned(currentDate, transcript)
        val tunedTask = tuned.getOrNull()
        if (tunedTask != null && !isFallbackTask(tunedTask)) {
            return tuned
        }

        // Fallback path for cases where tuned response is empty/blocked/null-like.
        val standard = service.extractTaskFromTranscript(currentDate, transcript)
        val standardTask = standard.getOrNull()
        if (standardTask != null && !isFallbackTask(standardTask)) {
            return standard
        }

        return if (standard.isSuccess) standard else tuned
    }

    private fun isFallbackTask(task: ExtractedTask): Boolean {
        val description = task.description.trim().lowercase()
        val looksFallback = description.contains("no clear actionable task detected")
        val noTaskSignal = task.task_title.isNullOrBlank() && task.date_time.isNullOrBlank()
        return looksFallback && noTaskSignal
    }
}
