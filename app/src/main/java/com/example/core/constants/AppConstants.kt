package com.example.core.constants

object AppConstants {
    const val DATABASE_NAME = "water_intake_database"
    const val PREFERENCES_NAME = "water_intake_preferences"
    
    // Default hydration constants
    const val DEFAULT_DAILY_TARGET_ML = 2500
    const val MIN_DAILY_TARGET_ML = 1000
    const val MAX_DAILY_TARGET_ML = 6000
    
    // Quick-add quantities in ml
    val QUICK_ADD_AMOUNTS = listOf(150, 250, 500, 750)
    
    // WorkManager tag
    const val REMINDER_WORK_TAG = "water_intake_reminder_work"
}
