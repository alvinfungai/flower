package com.alvinfungai.flower.ui.common

import android.content.res.Resources
import android.util.TypedValue
import android.text.format.DateUtils
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

/**
 * User defined for View-based fragments
 * Usage: 24.dp
 */
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

// Also useful for floats
val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        Resources.getSystem().displayMetrics
    )

object TimeUtils {
    /**
     * Converts an ISO 8601 string (from Supabase) to a relative time string.
     * Example: "2026-03-01T10:00:00Z" -> "4 days ago"
     */
    fun getRelativeTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "Never updated"

        return try {
            // 1. Parse the ISO string to milliseconds
            val timeInMillis = OffsetDateTime.parse(isoString).toInstant().toEpochMilli()
            val now = System.currentTimeMillis()

            // 2. Use Android's built-in formatter
            DateUtils.getRelativeTimeSpanString(
                timeInMillis,
                now,
                DateUtils.MINUTE_IN_MILLIS, // Smallest resolution (e.g., "5 mins ago")
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
        } catch (e: Exception) {
            "Recently"
        }
    }
}

fun formatGithubCount(count: Int): String {
    return when {
        count >= 1000 -> String.format("%.1fk", count / 1000.0)
        else -> count.toString()
    }
}