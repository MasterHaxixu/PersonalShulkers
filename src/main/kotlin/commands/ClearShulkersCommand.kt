package com.masterhaxixu.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.masterhaxixu.Main
import com.masterhaxixu.util.parseIntoMs
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClearShulkersCommand(private val plugin: Main) : SuspendingCommandExecutor {

    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        val p: Player = sender as? Player ?: run {
            sender.sendMessage("This command can only be run by a player.")
            return true
        }

        if (!p.hasPermission("personalshulkers.clear")) {
            p.sendRichMessage(plugin.config.getString("messages.noPermission")!!)
            return true
        }

        if (args.isEmpty()) {
            p.sendRichMessage("<gold><bold>Usage:</bold></gold>")
            p.sendRichMessage("<gold><bold>/clearshulkerdata <duration></bold></gold>")
            p.sendRichMessage("<gold><bold>/clearshulkerdata player <playerName></bold></gold>")
            p.sendRichMessage("<gray>Duration format: 30s, 10m, 2h, 7d</gray>")
            p.sendRichMessage("<gray>Example: /clearshulkerdata 7d</gray>")
            p.sendRichMessage("<gray>Example: /clearshulkerdata player Steve</gray>")
            return true
        }

        if (args[0].equals("player", ignoreCase = true)) {
            if (args.size < 2) {
                p.sendRichMessage("<red>Usage: /clearshulkerdata player <playerName></red>")
                return true
            }
            val targetName = args[1]
            val targetUuid = Bukkit.getOfflinePlayer(targetName).uniqueId.toString()
            val count = plugin.database.removeShulkersByPlayer(targetUuid)
            p.sendRichMessage("<green>Removed $count shulker(s) belonging to $targetName.</green>")
        } else if (args[0].matches(Regex("^\\d+([smhd])$"))) {
            val duration = args[0].parseIntoMs() ?: run {
                p.sendRichMessage("<red>Invalid duration. Use format: 30s, 10m, 2h, 7d</red>")
                return true
            }

            val cutoff = System.currentTimeMillis() - duration
            val count = plugin.database.removeOwnersSince(cutoff)
            p.sendRichMessage("<green>Removed $count shulkers older than ${args[0]}.</green>")
        } else {
            p.sendRichMessage("<red>Invalid argument. Use a duration (e.g. 7d) or 'player <name>'</red>")
        }

        return true
    }
}