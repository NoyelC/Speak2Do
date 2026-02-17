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
    suspend fun generateText(currentDate: String, transcript: String, model: String = "gemini-1.5-flash"): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent"
        val finalPrompt = """
            You are an expert task extraction assistant.

            Analyze the provided voice note transcript and extract structured task details in strict JSON format.

            The input will include:
            - Current Date: $currentDate
            - Voice Note Transcript: $transcript

            Your responsibilities:

            1. Identify the primary actionable task clearly described in the transcript.

            2. Extract and structure the following fields:

               - task_title:
                 A short, clear, action-oriented summary of the task.
                 (Example: "Call client regarding invoice", "Book flight tickets")

               - description:
                 A concise but detailed explanation of what needs to be done.
                 Include relevant context but avoid filler words.

               - date_time:
                 • Use ISO 8601 format: "YYYY-MM-DDTHH:MM:SS"
                 • Always calculate relative dates strictly using the provided "Current Date".
                 • Never assume or hardcode a calendar date.
                 
                 Relative date handling:
                   - "today" → Current Date
                   - "tomorrow" → Current Date + 1 day
                   - "day after tomorrow" → Current Date + 2 days
                   - "next week" → Current Date + 7 days
                   - "next month" → same day next month
                 
                 Time handling:
                   - If a date is mentioned without time → default time to "09:00:00"
                   - If only time is mentioned → use Current Date with that time
                   - If both date and time are mentioned → combine them
                   - If no date or time reference is mentioned → return null
                   - If "Current Date" is missing → return null

               - priority:
                 Choose strictly from: ["low", "medium", "high"]

                 Priority rules:
                   • Words like "urgent", "asap", "immediately", "critical" → high
                   • Words like "important" → high
                   • Casual or optional tone → low
                   • Default → medium

               - additional_notes:
                 Capture extra context such as:
                   • People involved
                   • Location
                   • Dependencies
                   • Constraints
                   • Special instructions
                 If none are present, return null.

            3. Interpretation Rules:

               - Ignore filler speech (e.g., "okay", "hmm", "so yeah").
               - Infer clear intent when wording is indirect but obvious.
               - Do NOT invent missing information.
               - If multiple tasks exist, extract only the primary actionable task.
               - If no actionable task exists, return:
                   - task_title: null
                   - date_time: null
                   - priority: "medium"
                   - additional_notes: null
                   - description: brief explanation stating no clear task detected.

            4. Output Requirements:

               - Return ONLY valid JSON.
               - Do NOT include explanations, markdown, or commentary.
               - Ensure strict JSON formatting.
               - Do NOT include trailing commas.
               - All fields must be present.

            Return output strictly in the following JSON structure:

            {
              "task_title": "",
              "description": "",
              "date_time": "YYYY-MM-DDTHH:MM:SS" | null,
              "priority": "low" | "medium" | "high",
              "additional_notes": ""
            }
        """.trimIndent()
        val request = GeminiRequest(contents = listOf(Content(parts = listOf(Part(text = finalPrompt)))))

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
