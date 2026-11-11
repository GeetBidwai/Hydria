package com.example.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayDateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    private val shortDisplayDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun getTodayDateKey(): String {
        return dateKeyFormat.format(Date())
    }

    fun formatDateKey(timestamp: Long): String {
        return dateKeyFormat.format(Date(timestamp))
    }

    fun formatDisplayDate(timestamp: Long = System.currentTimeMillis()): String {
        return displayDateFormat.format(Date(timestamp))
    }

    fun formatShortDisplayDate(dateKey: String): String {
        val todayKey = getTodayDateKey()
        if (dateKey == todayKey) return "Today"

        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayKey = dateKeyFormat.format(cal.time)
        if (dateKey == yesterdayKey) return "Yesterday"

        val parsed = parseDateKey(dateKey) ?: return dateKey
        return shortDisplayDateFormat.format(parsed)
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun parseDateKey(dateKey: String): Date? {
        return try {
            dateKeyFormat.parse(dateKey)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Returns the date keys for the last [count] calendar days in chronological order, ending with today.
     */
    fun getLastNDaysDateKeys(count: Int = 7): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        // Reset to midnight
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Move to start day
        cal.add(Calendar.DAY_OF_YEAR, -(count - 1))
        for (i in 0 until count) {
            list.add(dateKeyFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    fun getDayOfWeekLabel(dateKey: String): String {
        if (dateKey == getTodayDateKey()) return "Today"
        val parsed = parseDateKey(dateKey) ?: return dateKey
        return dayOfWeekFormat.format(parsed)
    }

    fun formatMonthYear(year: Int, monthIndex: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return monthYearFormat.format(cal.time)
    }

    /**
     * Returns all calendar date keys for a specific year and month (0-indexed month).
     */
    fun getMonthDateKeys(year: Int, monthIndex: Int): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            list.add(dateKeyFormat.format(cal.time))
        }
        return list
    }

    /**
     * Returns the day-of-week for the 1st day of the given month (Calendar.SUNDAY = 1, MONDAY = 2, etc.)
     */
    fun getFirstDayOfWeekForMonth(year: Int, monthIndex: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.get(Calendar.DAY_OF_WEEK)
    }

    fun getShortMonthName(monthIndex: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, monthIndex)
        return SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
    }

    fun getMonthName(monthIndex: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MONTH, monthIndex)
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
    }
}

