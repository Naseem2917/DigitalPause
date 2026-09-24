package com.digitalpause.app.model

enum class TemporaryAccessType {
    EXTRA_TIME,
    PARENT_OVERRIDE,
    SHORT_VIDEO_OVERRIDE
}

/**
 * Represents a temporary granted window of access before protection resumes.
 */
data class TemporaryAccess(
    val packageName: String,
    val type: TemporaryAccessType,
    val grantedAtMillis: Long = System.currentTimeMillis(),
    val durationMinutes: Int,
    val expiresAtMillis: Long = grantedAtMillis + (durationMinutes * 60 * 1000L)
) {
    fun isExpired(): Boolean = System.currentTimeMillis() >= expiresAtMillis

    fun remainingSeconds(): Long {
        val remaining = (expiresAtMillis - System.currentTimeMillis()) / 1000L
        return if (remaining > 0) remaining else 0
    }

    fun formattedRemaining(): String {
        val totalSecs = remainingSeconds()
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return "%02d:%02d".format(mins, secs)
    }
}
