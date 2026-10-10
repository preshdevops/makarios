package com.makarios.app.util

/**
 * Milestone helper for streak celebration recognition.
 */
object MilestoneHelper {

    val MILESTONES = listOf(3, 7, 14, 21, 30, 50, 75, 100, 150, 200, 250, 300, 365, 500, 750, 1000)

    fun isMilestone(streak: Int): Boolean = streak in MILESTONES

    fun nextMilestone(streak: Int): Int =
        MILESTONES.firstOrNull { it > streak } ?: (streak + 50)

    fun closestOrPastMilestone(streak: Int): Int {
        if (streak <= 0) return 30
        return MILESTONES.lastOrNull { it <= streak } ?: MILESTONES.first()
    }
}
