package com.example.domain.gamification

data class MonthlyCollectionRecord(
    val monthKey: String, // e.g. "2026-11"
    val year: Int,
    val month: Int, // 1-indexed (1 to 12)
    val unlockedBadgeIds: List<String>,
    val highestStreak: Int,
    val isCompleted: Boolean,
    val designationAwarded: String? = null
) {
    fun toJsonString(): String {
        val badgesJson = unlockedBadgeIds.joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
        val desAwardedJson = if (designationAwarded != null) "\"$designationAwarded\"" else "null"
        return """{"monthKey":"$monthKey","year":$year,"month":$month,"unlockedBadgeIds":$badgesJson,"highestStreak":$highestStreak,"isCompleted":$isCompleted,"designationAwarded":$desAwardedJson}"""
    }

    companion object {
        fun listToJsonString(records: List<MonthlyCollectionRecord>): String {
            return records.joinToString(separator = ",", prefix = "[", postfix = "]") { it.toJsonString() }
        }

        fun listFromJsonString(jsonString: String?): List<MonthlyCollectionRecord> {
            if (jsonString.isNullOrBlank()) return emptyList()
            val list = mutableListOf<MonthlyCollectionRecord>()
            try {
                var trimmed = jsonString.trim()
                if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return emptyList()
                trimmed = trimmed.substring(1, trimmed.length - 1).trim()
                if (trimmed.isEmpty()) return emptyList()

                var depth = 0
                val objectStrings = mutableListOf<String>()
                var currentObj = StringBuilder()
                for (ch in trimmed) {
                    if (ch == '{') {
                        depth++
                        currentObj.append(ch)
                    } else if (ch == '}') {
                        depth--
                        currentObj.append(ch)
                        if (depth == 0) {
                            objectStrings.add(currentObj.toString())
                            currentObj = StringBuilder()
                        }
                    } else if (depth > 0) {
                        currentObj.append(ch)
                    }
                }

                for (objStr in objectStrings) {
                    parseRecord(objStr)?.let { list.add(it) }
                }
            } catch (_: Exception) {
                return emptyList()
            }
            return list
        }

        private fun parseRecord(objStr: String): MonthlyCollectionRecord? {
            return try {
                val monthKey = extractString(objStr, "monthKey") ?: return null
                val year = extractInt(objStr, "year") ?: return null
                val month = extractInt(objStr, "month") ?: return null
                val unlockedBadgeIds = extractStringList(objStr, "unlockedBadgeIds")
                val highestStreak = extractInt(objStr, "highestStreak") ?: 0
                val isCompleted = extractBoolean(objStr, "isCompleted") ?: false
                val designationAwarded = extractString(objStr, "designationAwarded")

                MonthlyCollectionRecord(
                    monthKey = monthKey,
                    year = year,
                    month = month,
                    unlockedBadgeIds = unlockedBadgeIds,
                    highestStreak = highestStreak,
                    isCompleted = isCompleted,
                    designationAwarded = designationAwarded
                )
            } catch (_: Exception) {
                null
            }
        }

        private fun extractString(json: String, key: String): String? {
            val pattern = Regex("\"$key\"\\s*:\\s*(?:\"([^\"]*)\"|(null))")
            val match = pattern.find(json) ?: return null
            if (match.groupValues[2] == "null") return null
            return match.groupValues[1]
        }

        private fun extractInt(json: String, key: String): Int? {
            val pattern = Regex("\"$key\"\\s*:\\s*(-?\\d+)")
            val match = pattern.find(json) ?: return null
            return match.groupValues[1].toIntOrNull()
        }

        private fun extractBoolean(json: String, key: String): Boolean? {
            val pattern = Regex("\"$key\"\\s*:\\s*(true|false)")
            val match = pattern.find(json) ?: return null
            return match.groupValues[1].toBoolean()
        }

        private fun extractStringList(json: String, key: String): List<String> {
            val pattern = Regex("\"$key\"\\s*:\\s*\\[([^\\]]*)\\]")
            val match = pattern.find(json) ?: return emptyList()
            val content = match.groupValues[1].trim()
            if (content.isEmpty()) return emptyList()
            val itemPattern = Regex("\"([^\"]*)\"")
            return itemPattern.findAll(content).map { it.groupValues[1] }.toList()
        }
    }
}
