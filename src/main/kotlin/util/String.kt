package com.masterhaxixu.util

import java.util.concurrent.TimeUnit
import kotlin.String


private val pattern = Regex("""(\d+)([smhd])""")
fun String.parseIntoMs(): Long? {
    val match = pattern.matchEntire(this.lowercase()) ?: return null
    val value = match.groupValues[1].toLong()
    return when (match.groupValues[2]) {
        "s" -> TimeUnit.SECONDS.toMillis(value)
        "m" -> TimeUnit.MINUTES.toMillis(value)
        "h" -> TimeUnit.HOURS.toMillis(value)
        "d" -> TimeUnit.DAYS.toMillis(value)
        else -> null
    }
}
