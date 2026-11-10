package com.example.reminders

enum class NotificationCategory {
    REGULAR_REMINDER,
    PROGRESS_CELEBRATION,
    STREAK_CELEBRATION,
    MISSED_GOAL_AND_RECOVERY,
    END_OF_DAY_NUDGE
}

data class NotificationMessage(
    val title: String,
    val body: String,
    val category: NotificationCategory
)

object OceanNotificationContent {

    // 1. Regular reminders (12 messages)
    val REGULAR_REMINDERS = listOf(
        NotificationMessage(
            title = "Poseidon is getting impatient 🌊",
            body = "The ocean called. It wants its water back. Take a sip!",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "A little hydration, please 🐠",
            body = "Even fish need water. Actually, especially fish.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "Breaking news 📰",
            body = "Local human spotted forgetting to drink water. Authorities concerned.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "Your water is getting lonely 🥲",
            body = "It's been sitting there waiting for you. Don't be rude.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "Hydration check! 💧",
            body = "Your body called. It would like a little maintenance.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "The waves are calling 🌊",
            body = "And no, scrolling past this notification doesn't count as drinking water.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "Tiny sip, big achievement 🏆",
            body = "Go on. Give your future self something to thank you for.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "Attention, land creature! 🦀",
            body = "Your hydration levels deserve a little attention.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "The ocean misses you 🐳",
            body = "Come on, let's get some water into that system.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "Water break! 🚨",
            body = "This is not a drill. Well, technically it's a reminder.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "Don't become a raisin 🍇",
            body = "A glass of water might be a good idea right about now.",
            category = NotificationCategory.REGULAR_REMINDER
        ),
        NotificationMessage(
            title = "Hydration.exe needs attention 🤖",
            body = "Input required: one glass of water.",
            category = NotificationCategory.REGULAR_REMINDER
        )
    )

    // 2. Progress celebrations (8 messages)
    val PROGRESS_MID_MILESTONES = listOf(
        NotificationMessage(
            title = "The tide is turning! 🌊",
            body = "You're making waves. Keep that hydration going!",
            category = NotificationCategory.PROGRESS_CELEBRATION
        ),
        NotificationMessage(
            title = "Look who's hydrating! 🐬",
            body = "Your water intake is looking pretty impressive.",
            category = NotificationCategory.PROGRESS_CELEBRATION
        ),
        NotificationMessage(
            title = "One sip closer! 💧",
            body = "Your daily goal is getting nervous.",
            category = NotificationCategory.PROGRESS_CELEBRATION
        ),
        NotificationMessage(
            title = "Ocean energy unlocked ⚡",
            body = "You're keeping the hydration momentum alive.",
            category = NotificationCategory.PROGRESS_CELEBRATION
        ),
        NotificationMessage(
            title = "Poseidon approves! 🔱",
            body = "The sea god has noticed your efforts.",
            category = NotificationCategory.PROGRESS_CELEBRATION
        )
    )

    val PROGRESS_GOAL_ACCOMPLISHED = listOf(
        NotificationMessage(
            title = "MISSION ACCOMPLISHED 🏆",
            body = "Daily water goal completed. You absolute legend!",
            category = NotificationCategory.PROGRESS_CELEBRATION
        ),
        NotificationMessage(
            title = "A legend of the deep! 🐙",
            body = "Today's hydration target has officially been conquered.",
            category = NotificationCategory.PROGRESS_CELEBRATION
        ),
        NotificationMessage(
            title = "The ocean bows to you 👑",
            body = "Another successful day. Your streak lives on!",
            category = NotificationCategory.PROGRESS_CELEBRATION
        )
    )

    val PROGRESS_CELEBRATIONS = PROGRESS_MID_MILESTONES + PROGRESS_GOAL_ACCOMPLISHED

    // 3. Streak celebrations (7 messages)
    val STREAK_CELEBRATIONS = listOf(
        NotificationMessage(
            title = "Three days? Not bad! 🐟",
            body = "Your ocean adventure has officially begun.",
            category = NotificationCategory.STREAK_CELEBRATION
        ),
        NotificationMessage(
            title = "A streak is forming! 🔥",
            body = "The waves are starting to recognise you.",
            category = NotificationCategory.STREAK_CELEBRATION
        ),
        NotificationMessage(
            title = "Triton has entered the chat 🔱",
            body = "Your dedication is making some serious waves.",
            category = NotificationCategory.STREAK_CELEBRATION
        ),
        NotificationMessage(
            title = "Neptune is watching 👀",
            body = "Another day, another reason to be proud.",
            category = NotificationCategory.STREAK_CELEBRATION
        ),
        NotificationMessage(
            title = "Kraken-level commitment! 🦑",
            body = "Your hydration streak is getting legendary.",
            category = NotificationCategory.STREAK_CELEBRATION
        ),
        NotificationMessage(
            title = "Leviathan energy 🐉",
            body = "You're becoming a force of nature.",
            category = NotificationCategory.STREAK_CELEBRATION
        ),
        NotificationMessage(
            title = "LORD OF THE SEVEN SEAS 👑",
            body = "Ten badges. One magnificent ocean legend.",
            category = NotificationCategory.STREAK_CELEBRATION
        )
    )

    // 4. Missed goals (6 messages) & Recovery (2 messages)
    val MISSED_GOALS = listOf(
        NotificationMessage(
            title = "The fish are concerned 🐠",
            body = "Yesterday's water goal slipped away. Let's make a splash today.",
            category = NotificationCategory.MISSED_GOAL_AND_RECOVERY
        ),
        NotificationMessage(
            title = "Aquatic Intern reporting 🫡",
            body = "Your hydration performance needs a little supervision.",
            category = NotificationCategory.MISSED_GOAL_AND_RECOVERY
        ),
        NotificationMessage(
            title = "The ocean is confused 🐙",
            body = "We seem to have misplaced yesterday's water goal.",
            category = NotificationCategory.MISSED_GOAL_AND_RECOVERY
        ),
        NotificationMessage(
            title = "Dry Fish detected 🐟",
            body = "A new day means a fresh chance to get back on track.",
            category = NotificationCategory.MISSED_GOAL_AND_RECOVERY
        ),
        NotificationMessage(
            title = "A crab has filed a complaint 🦀",
            body = "Apparently, you've been spending too much time on land.",
            category = NotificationCategory.MISSED_GOAL_AND_RECOVERY
        ),
        NotificationMessage(
            title = "The Deep is disappointed 🌊",
            body = "Even the mysterious ocean floor expects better. Fresh start?",
            category = NotificationCategory.MISSED_GOAL_AND_RECOVERY
        )
    )

    val RECOVERIES = listOf(
        NotificationMessage(
            title = "THE COMEBACK! ⚡",
            body = "You recovered your hydration streak. That's how legends return.",
            category = NotificationCategory.MISSED_GOAL_AND_RECOVERY
        ),
        NotificationMessage(
            title = "Welcome back, ocean warrior 🐬",
            body = "One successful day and you're back in the current.",
            category = NotificationCategory.MISSED_GOAL_AND_RECOVERY
        )
    )

    val MISSED_GOAL_AND_RECOVERY = MISSED_GOALS + RECOVERIES

    // 5. End-of-day nudges (5 messages)
    val END_OF_DAY_NUDGES = listOf(
        NotificationMessage(
            title = "The sun is going down 🌅",
            body = "Your daily water goal is still waiting. There's time for a little progress.",
            category = NotificationCategory.END_OF_DAY_NUDGE
        ),
        NotificationMessage(
            title = "Last call from the ocean 📣",
            body = "Before the day ends, how about another glass?",
            category = NotificationCategory.END_OF_DAY_NUDGE
        ),
        NotificationMessage(
            title = "Don't leave the quest unfinished 🗺️",
            body = "Your daily hydration mission still has some distance to go.",
            category = NotificationCategory.END_OF_DAY_NUDGE
        ),
        NotificationMessage(
            title = "The tide is running out ⏳",
            body = "A little more water before bedtime, if it fits your routine.",
            category = NotificationCategory.END_OF_DAY_NUDGE
        ),
        NotificationMessage(
            title = "Tomorrow is another wave 🌊",
            body = "Today wasn't perfect? That's okay. Come back stronger tomorrow.",
            category = NotificationCategory.END_OF_DAY_NUDGE
        )
    )

    val DEFAULT_FALLBACK = NotificationMessage(
        title = "Time to hydrate! 💧",
        body = "Take a moment to enjoy a fresh glass of water.",
        category = NotificationCategory.REGULAR_REMINDER
    )

    fun getMessagesForCategory(category: NotificationCategory): List<NotificationMessage> {
        return when (category) {
            NotificationCategory.REGULAR_REMINDER -> REGULAR_REMINDERS
            NotificationCategory.PROGRESS_CELEBRATION -> PROGRESS_CELEBRATIONS
            NotificationCategory.STREAK_CELEBRATION -> STREAK_CELEBRATIONS
            NotificationCategory.MISSED_GOAL_AND_RECOVERY -> MISSED_GOAL_AND_RECOVERY
            NotificationCategory.END_OF_DAY_NUDGE -> END_OF_DAY_NUDGES
        }
    }

    /**
     * Selects a message from the specified category while avoiding immediate repetition
     * of the last shown message title where multiple messages are available.
     */
    fun selectMessage(
        category: NotificationCategory,
        lastShownTitle: String? = null,
        specificPool: List<NotificationMessage>? = null
    ): NotificationMessage {
        val pool = specificPool ?: getMessagesForCategory(category)
        if (pool.isEmpty()) return DEFAULT_FALLBACK
        if (pool.size == 1) return pool[0]

        val candidates = if (!lastShownTitle.isNullOrBlank()) {
            val filtered = pool.filter { it.title != lastShownTitle }
            if (filtered.isNotEmpty()) filtered else pool
        } else {
            pool
        }

        return candidates.random()
    }
}
