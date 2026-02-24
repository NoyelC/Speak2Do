package com.example.speak2do.util

object TaskCategorizer {

    private val workKeywords = setOf(
        "meeting", "client", "office", "project", "deadline", "presentation",
        "email", "report", "sprint", "deploy", "review", "team", "manager"
    )
    private val personalKeywords = setOf(
        "family", "home", "friend", "birthday", "call mom", "call dad",
        "personal", "self", "journal", "plan trip", "vacation"
    )
    private val shoppingKeywords = setOf(
        "buy", "purchase", "order", "grocery", "groceries", "mall", "shop",
        "shopping", "milk", "bread", "vegetables", "amazon"
    )
    private val healthKeywords = setOf(
        "doctor", "hospital", "medicine", "workout", "gym", "run", "walk",
        "exercise", "yoga", "checkup", "appointment", "water", "diet"
    )
    private val financeKeywords = setOf(
        "bill", "payment", "pay", "invoice", "budget", "bank", "tax",
        "salary", "expense", "emi", "insurance"
    )
    private val learningKeywords = setOf(
        "study", "learn", "course", "class", "exam", "assignment", "read",
        "practice", "tutorial", "homework"
    )

    fun categorize(text: String, duration: String = ""): String {
        if (duration.equals("EVENT", ignoreCase = true)) return "Schedule"

        val normalized = text.lowercase()

        return when {
            containsAny(normalized, workKeywords) -> "Work"
            containsAny(normalized, shoppingKeywords) -> "Shopping"
            containsAny(normalized, healthKeywords) -> "Health"
            containsAny(normalized, financeKeywords) -> "Finance"
            containsAny(normalized, learningKeywords) -> "Learning"
            containsAny(normalized, personalKeywords) -> "Personal"
            else -> "General"
        }
    }

    private fun containsAny(text: String, keywords: Set<String>): Boolean {
        return keywords.any { keyword -> text.contains(keyword) }
    }
}
