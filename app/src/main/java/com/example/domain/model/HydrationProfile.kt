package com.example.domain.model

enum class ActivityLevel(val label: String, val description: String, val multiplier: Float) {
    SEDENTARY("Sedentary", "Little to no regular exercise", 1.0f),
    LIGHT("Lightly Active", "Exercise 1–3 days per week", 1.15f),
    MODERATE("Moderately Active", "Exercise 3–5 days per week", 1.25f),
    VERY_ACTIVE("Very Active", "Heavy exercise 6–7 days per week", 1.4f);

    companion object {
        fun fromString(name: String?): ActivityLevel {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: MODERATE
        }
    }
}

enum class Climate(val label: String, val extraMl: Int) {
    TEMPERATE("Temperate / Normal", 0),
    WARM("Warm", 250),
    HOT_HUMID("Hot & Humid", 500);

    companion object {
        fun fromString(name: String?): Climate {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: TEMPERATE
        }
    }
}

data class UserProfile(
    val name: String = "",
    val age: Int = 28,
    val weightKg: Float = 70f,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val exerciseMinutes: Int = 30,
    val preferredUnit: String = "ml", // "ml" or "litres"
    val remindersEnabled: Boolean = true,
    val wakeTime: String = "07:00",
    val sleepTime: String = "23:00",
    val climate: Climate = Climate.TEMPERATE,
    val reminderIntervalMinutes: Int = 120,
    val reminderWeekendsEnabled: Boolean = true
)
