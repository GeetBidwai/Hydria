package com.example.domain.gamification

data class GamificationState(
    val overallStreak: Int = 0,
    val currentMissedStreak: Int = 0,
    val monthlyStreak: Int = 0,
    val monthlyHighestStreak: Int = 0,
    val currentMonthKey: String = "", // e.g. "2026-11"
    val daysInCurrentMonth: Int = 30,
    val currentMonthUnlockedBadgeIds: List<String> = emptyList(),
    val currentMonthBadgeCount: Int = 0, // 0 to 10
    val isCurrentMonthCompleted: Boolean = false,
    val currentDesignationTitle: String = Designation.DEFAULT_STARTER.title,
    val currentDesignationCategory: DesignationCategory = DesignationCategory.NOVICE,
    val currentDesignationDescription: String = Designation.DEFAULT_STARTER.description,
    val activeMonthlyDesignation: String? = null,
    val lifetimeHighestDesignation: String = Designation.DEFAULT_STARTER.title,
    val lifetimeUnlockedBadgeCount: Int = 0,
    val completedMonthlyCollectionsCount: Int = 0,
    val completedMonthlyCollections: List<MonthlyCollectionRecord> = emptyList(),
    val nextMilestoneTitle: String? = "Triton",
    val nextMilestoneDays: Int = 3,
    val progressFraction: Float = 0f,
    val isDisasterPath: Boolean = false,
    val totalSuccessfulDays: Int = 0,
    val lastEvaluatedDateKey: String = ""
) {
    // Backwards-compatibility getter for components or tests referencing currentStreak
    val currentStreak: Int get() = overallStreak
    val highestDesignationAchieved: String get() = lifetimeHighestDesignation
}
