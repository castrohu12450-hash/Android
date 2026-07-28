package com.kiminini.hospital.data.model.queue

data class TriageQuestion(
    val id: String,
    val question: String,
    val options: List<TriageOption>,
    val requiresVerification: Boolean = false,
    val maxScore: Int = 10,
    val minScore: Int = 0
)

data class TriageOption(
    val text: String,
    val score: Int,
    val isUrgent: Boolean = false,
    val requiresNurseOverride: Boolean = false
)

object TriageData {

    val questions = listOf(
        TriageQuestion(
            id = "breathing",
            question = "Do you have difficulty breathing?",
            requiresVerification = true,
            options = listOf(
                TriageOption("No difficulty", 0),
                TriageOption("Mild (after walking up stairs)", 2),
                TriageOption("Moderate (shortness of breath at rest)", 5, requiresNurseOverride = true),
                TriageOption("Severe (cannot speak full sentences)", 10, true, requiresNurseOverride = true)
            )
        ),
        TriageQuestion(
            id = "chest_pain",
            question = "Do you have chest pain or pressure?",
            requiresVerification = true,
            options = listOf(
                TriageOption("No chest pain", 0),
                TriageOption("Mild (comes and goes)", 2),
                TriageOption("Moderate (constant but bearable)", 5, requiresNurseOverride = true),
                TriageOption("Severe (crushing pain)", 10, true, requiresNurseOverride = true)
            )
        ),
        TriageQuestion(
            id = "consciousness",
            question = "What is your current level of consciousness?",
            requiresVerification = true,
            options = listOf(
                TriageOption("Fully alert", 0),
                TriageOption("Drowsy but easily awoken", 3),
                TriageOption("Difficult to arouse", 7, true, requiresNurseOverride = true),
                TriageOption("Unconscious", 10, true, requiresNurseOverride = true)
            )
        ),
        TriageQuestion(
            id = "bleeding",
            question = "Are you experiencing any bleeding?",
            options = listOf(
                TriageOption("No bleeding", 0),
                TriageOption("Minor (small cut, stops quickly)", 1),
                TriageOption("Moderate (requires pressure)", 3),
                TriageOption("Severe (uncontrolled)", 10, true, requiresNurseOverride = true)
            )
        ),
        TriageQuestion(
            id = "fever",
            question = "Do you have a fever?",
            options = listOf(
                TriageOption("No fever (normal temperature)", 0),
                TriageOption("Mild fever (37.5–38.5°C)", 1),
                TriageOption("High fever (>38.5°C)", 4, true)
            )
        ),
        TriageQuestion(
            id = "pain",
            question = "Rate your pain level (0 = none, 10 = worst imaginable)",
            options = listOf(
                TriageOption("0–3 (Mild)", 1),
                TriageOption("4–6 (Moderate)", 4),
                TriageOption("7–10 (Severe)", 7, true)
            )
        ),
        TriageQuestion(
            id = "symptom_duration",
            question = "How long have you had these symptoms?",
            options = listOf(
                TriageOption("< 6 hours", 0),
                TriageOption("6–24 hours", 1),
                TriageOption("1–3 days", 2),
                TriageOption("> 3 days", 3)
            )
        ),
        TriageQuestion(
            id = "chronic_conditions",
            question = "Do you have any chronic conditions (e.g., diabetes, hypertension, asthma)?",
            options = listOf(
                TriageOption("None", 0),
                TriageOption("Yes, well controlled", 2),
                TriageOption("Yes, not well controlled", 5, requiresNurseOverride = true)
            )
        ),
        TriageQuestion(
            id = "medications",
            question = "Are you taking any regular medications?",
            options = listOf(
                TriageOption("None", 0),
                TriageOption("Yes, no recent changes", 1),
                TriageOption("Yes, stopped or changed recently", 3, requiresNurseOverride = true)
            )
        ),
        TriageQuestion(
            id = "exposure",
            question = "Have you been in contact with anyone diagnosed with COVID-19 or other infectious disease?",
            options = listOf(
                TriageOption("No known exposure", 0),
                TriageOption("Possible exposure (unknowingly)", 2),
                TriageOption("Confirmed exposure", 5, requiresNurseOverride = true)
            )
        )
    )

    fun calculatePriorityScoreWithBreakdown(answers: Map<String, Int>, age: Int): Pair<Int, Map<String, Int>> {
        val breakdown = mutableMapOf<String, Int>()
        var totalRaw = 0

        answers.forEach { (qId, score) ->
            val weight = when (qId) {
                "breathing", "chest_pain", "consciousness" -> 2
                else -> 1
            }
            totalRaw += score * weight
            breakdown[qId] = score * weight
        }

        val agePoints = when {
            age >= 80 -> 10
            age >= 70 -> 8
            age >= 60 -> 6
            age >= 50 -> 4
            age >= 40 -> 2
            else -> 0
        }
        totalRaw += agePoints
        breakdown["Age ($age years)"] = agePoints

        val finalScore = (totalRaw / 5).coerceIn(0, 10)
        return Pair(finalScore, breakdown)
    }

    fun getPriorityLabel(score: Int): String = when {
        score >= 8 -> "High Priority 🚨"
        score >= 5 -> "Medium Priority ⚠️"
        else -> "Normal Priority ℹ️"
    }

    fun detectRedFlags(answers: Map<String, Int>): List<String> {
        val flags = mutableListOf<String>()
        if ((answers["chest_pain"] ?: 0) >= 5) flags.add("Potential cardiac issue")
        if ((answers["breathing"] ?: 0) >= 5) flags.add("Respiratory distress")
        if ((answers["consciousness"] ?: 0) >= 7) flags.add("Altered consciousness")
        if ((answers["bleeding"] ?: 0) >= 10) flags.add("Severe bleeding")
        if ((answers["fever"] ?: 0) >= 4) flags.add("High fever")
        if ((answers["pain"] ?: 0) >= 7) flags.add("Severe pain")
        if ((answers["chronic_conditions"] ?: 0) == 5) flags.add("Uncontrolled chronic condition")
        if ((answers["exposure"] ?: 0) == 5) flags.add("Known infectious exposure")
        return flags
    }

    fun calculateVerifiedPriority(selfReportedScore: Int, redFlags: List<String>): Pair<QueuePriority, Int> {
        var finalScore = selfReportedScore
        if (redFlags.isNotEmpty()) {
            finalScore = (finalScore * 0.8).toInt()
        }
        val priority = when {
            finalScore >= 8 -> QueuePriority.CRITICAL
            finalScore >= 5 -> QueuePriority.HIGH
            finalScore >= 3 -> QueuePriority.NORMAL
            else -> QueuePriority.LOW
        }
        return Pair(priority, finalScore)
    }

    fun calculatePriority(totalScore: Int): QueuePriority {
        return when {
            totalScore >= 8 -> QueuePriority.CRITICAL
            totalScore >= 5 -> QueuePriority.HIGH
            totalScore >= 3 -> QueuePriority.NORMAL
            else -> QueuePriority.LOW
        }
    }
}