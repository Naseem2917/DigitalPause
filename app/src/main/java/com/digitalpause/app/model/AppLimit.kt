package com.digitalpause.app.model

/**
 * Represents a configured daily screen-time limit for a specific Android package.
 */
data class AppLimit(
    val packageName: String,
    val appName: String,
    val dailyLimitMinutes: Int,
    val extraTimeUsedMinutes: Int = 0,
    val isEnabled: Boolean = true,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun totalAllowedMinutes(): Int = dailyLimitMinutes + extraTimeUsedMinutes

    fun remainingMinutes(usedMinutes: Int): Int {
        val remaining = totalAllowedMinutes() - usedMinutes
        return if (remaining > 0) remaining else 0
    }

    fun isLimitReached(usedMinutes: Int): Boolean {
        return usedMinutes >= totalAllowedMinutes()
    }

    fun isNearLimit(usedMinutes: Int): Boolean {
        // Near limit: used >= 80% of limit and remaining <= 15 minutes, but not yet reached
        val total = totalAllowedMinutes()
        if (total <= 0) return false
        val progress = usedMinutes.toFloat() / total.toFloat()
        return progress >= 0.8f && !isLimitReached(usedMinutes)
    }

    fun progressFraction(usedMinutes: Int): Float {
        val total = totalAllowedMinutes()
        if (total <= 0) return 0f
        return (usedMinutes.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    }
}
