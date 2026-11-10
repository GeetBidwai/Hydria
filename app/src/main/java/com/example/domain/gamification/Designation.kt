package com.example.domain.gamification

enum class DesignationCategory {
    OCEAN_LEGEND,
    OCEAN_DISASTER,
    MONTHLY_DESIGNATION,
    NOVICE
}

data class Designation(
    val id: String,
    val title: String,
    val thresholdDays: Int, // Default/reference days in month
    val category: DesignationCategory,
    val description: String
) {
    companion object {
        // 1. Ocean Legends (10 Monthly Collectible Badges in exact required order)
        val OCEAN_LEGENDS = listOf(
            Designation(
                id = "triton",
                title = "Triton",
                thresholdDays = 3,
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Guardian of the shores, commanding the first tides of monthly discipline."
            ),
            Designation(
                id = "varuna",
                title = "Varuna",
                thresholdDays = 6,
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Cosmic sovereign of waters and keeper of the celestial balance."
            ),
            Designation(
                id = "aquaman",
                title = "Aquaman",
                thresholdDays = 9,
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Telepathic master of marine harmony and perseverance."
            ),
            Designation(
                id = "neptune",
                title = "Neptune",
                thresholdDays = 12,
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Mighty ruler of freshwater springs and habit transformation."
            ),
            Designation(
                id = "poseidon",
                title = "Poseidon",
                thresholdDays = 15,
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Earth-shaker of the boundless sea and mid-month mastery."
            ),
            Designation(
                id = "oceanus",
                title = "Oceanus",
                thresholdDays = 18,
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Titan of the great primordial river encircling the world."
            ),
            Designation(
                id = "kraken",
                title = "Kraken",
                thresholdDays = 21,
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Legendary titan of the abyss, crushing dehydration beneath giant waves."
            ),
            Designation(
                id = "leviathan",
                title = "Leviathan",
                thresholdDays = 24,
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Unstoppable ancient behemoth of supreme aquatic endurance."
            ),
            Designation(
                id = "naga_raja",
                title = "Naga Raja",
                thresholdDays = 27,
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Serpent king of subterranean sacred springs and pure vital waters."
            ),
            Designation(
                id = "lord_of_seven_seas",
                title = "Lord of the Seven Seas",
                thresholdDays = 30, // Dynamic: final day of the calendar month (28, 29, 30, 31)
                category = DesignationCategory.OCEAN_LEGEND,
                description = "Supreme emperor of all oceans, crowned by unbroken monthly mastery."
            )
        )

        /**
         * Resolves the required consecutive monthly streak days for badge at index [0..9]
         * given the actual number of calendar days in that month [daysInMonth].
         */
        fun getBadgeThreshold(badgeIndex: Int, daysInMonth: Int): Int {
            return when (badgeIndex) {
                0 -> 3
                1 -> 6
                2 -> 9
                3 -> 12
                4 -> 15
                5 -> 18
                6 -> 21
                7 -> 24
                8 -> 27
                9 -> daysInMonth // Final calendar day of the month (28, 29, 30, or 31)
                else -> 30
            }
        }

        // 2. Monthly Designations (Awarded for completing all 10 badges in a calendar month)
        val MONTHLY_DESIGNATION_TITLES = listOf(
            "Ocean Explorer",
            "Sea Guardian",
            "Poseidon's Champion",
            "Ocean Conqueror",
            "Master of Tides",
            "Ocean Sovereign",
            "Abyss Walker",
            "Tidebreaker",
            "Sea Emperor",
            "Lord of the Deep",
            "Eternal Mariner",
            "Keeper of the Seven Seas"
        )

        fun getMonthlyDesignation(completedCollectionsCount: Int): String? {
            if (completedCollectionsCount <= 0) return null
            val index = completedCollectionsCount - 1
            return if (index < MONTHLY_DESIGNATION_TITLES.size) {
                MONTHLY_DESIGNATION_TITLES[index]
            } else {
                val prestigeLevel = completedCollectionsCount - MONTHLY_DESIGNATION_TITLES.size
                "Keeper of the Seven Seas (Prestige $prestigeLevel)"
            }
        }

        // 3. Ocean Disasters (Humorous, playful consecutive missed goal days)
        val OCEAN_DISASTERS = listOf(
            Designation(
                id = "aquatic_intern",
                title = "Aquatic Intern",
                thresholdDays = 1,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "Still figuring out how cups and drinking water work."
            ),
            Designation(
                id = "confused_octopus",
                title = "Confused Octopus",
                thresholdDays = 2,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "Has eight arms, but somehow none of them can hold a water glass."
            ),
            Designation(
                id = "dry_fish",
                title = "Dry Fish",
                thresholdDays = 3,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "Flopping around desperately wondering where all the moisture went."
            ),
            Designation(
                id = "landlocked_crab",
                title = "Landlocked Crab",
                thresholdDays = 5,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "Scuttling sideways away from any known hydration source."
            ),
            Designation(
                id = "fish_outta_water",
                title = "Fish Outta Water",
                thresholdDays = 7,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "A whole week on dry land. The gills are seriously concerned."
            ),
            Designation(
                id = "desert_sponge",
                title = "Desert Sponge",
                thresholdDays = 10,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "Completely crunchy and absorbing nothing but warm dry air."
            ),
            Designation(
                id = "spongebobs_nemesis",
                title = "SpongeBob's Nemesis",
                thresholdDays = 14,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "Two weeks dry. Plankton has more moisture in his plans."
            ),
            Designation(
                id = "thirsty_squidward",
                title = "Thirsty Squidward",
                thresholdDays = 21,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "Three weeks without water, playing mournful tunes on a parched clarinet."
            ),
            Designation(
                id = "the_deep",
                title = "The Deep",
                thresholdDays = 30,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "Lost in the sunbaked abyssal plains, dreaming of high tide."
            ),
            Designation(
                id = "the_sahara_survivor",
                title = "The Sahara Survivor",
                thresholdDays = 45,
                category = DesignationCategory.OCEAN_DISASTER,
                description = "A mythological nomad thriving purely on mirages and sheer will."
            )
        )

        val DEFAULT_STARTER = Designation(
            id = "wave_seeker",
            title = "Wave Seeker",
            thresholdDays = 0,
            category = DesignationCategory.NOVICE,
            description = "Embarking on the journey toward monthly Ocean Legends."
        )

        fun getDisasterForMissedDays(consecutiveMissedDays: Int): Designation? {
            return OCEAN_DISASTERS.filter { it.thresholdDays <= consecutiveMissedDays }
                .maxByOrNull { it.thresholdDays }
        }

        fun getNextDisaster(consecutiveMissedDays: Int): Designation? {
            return OCEAN_DISASTERS.firstOrNull { it.thresholdDays > consecutiveMissedDays }
        }
    }
}
