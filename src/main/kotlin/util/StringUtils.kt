package com.masterhaxixu.util

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.Player
import java.util.concurrent.TimeUnit

class StringUtils {
    private val pattern = Regex("""(\d+)([smhd])""")

    fun sendMessage(player: Player, message: String) {
        val p: Audience = player
        val mm: MiniMessage = MiniMessage.miniMessage()

        val parsed: Component = mm.deserialize(message)
        p.sendMessage(parsed)
    }

    fun parse(input: String): Long? {
        val match = pattern.matchEntire(input.lowercase()) ?: return null
        val value = match.groupValues[1].toLong()
        return when (match.groupValues[2]) {
            "s" -> TimeUnit.SECONDS.toMillis(value)
            "m" -> TimeUnit.MINUTES.toMillis(value)
            "h" -> TimeUnit.HOURS.toMillis(value)
            "d" -> TimeUnit.DAYS.toMillis(value)
            else -> null
        }
    }

}