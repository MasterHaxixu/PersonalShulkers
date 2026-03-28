package com.masterhaxixu.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.masterhaxixu.Main
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClearShulkersCommand(private val plugin: Main) : SuspendingCommandExecutor {

    override suspend fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val p: Player = sender as? Player ?: run {
            sender.sendMessage("This command can only be run by a player.")
            return true
        }

        if (!p.hasPermission("personalshulkers.clear")) {
            plugin.stringUtils.sendMessage(p, plugin.config.getString("messages.noPermission")!!)
            return true
        }

        if (args.isEmpty()) {
            plugin.stringUtils.sendMessage(p, "<gold><bold>Usage:</bold></gold>")
            plugin.stringUtils.sendMessage(p, "<gold><bold>/clearshulkerdata <duration></bold></gold>")
            plugin.stringUtils.sendMessage(p, "<gold><bold>/clearshulkerdata player <playerName></bold></gold>")
            plugin.stringUtils.sendMessage(p, "<gray>Duration format: 30s, 10m, 2h, 7d</gray>")
            plugin.stringUtils.sendMessage(p, "<gray>Example: /clearshulkerdata 7d</gray>")
            plugin.stringUtils.sendMessage(p, "<gray>Example: /clearshulkerdata player Steve</gray>")
            return true
        }

        if (args[0].equals("player", ignoreCase = true)) {
            if (args.size < 2) {
                plugin.stringUtils.sendMessage(p, "<red>Usage: /clearshulkerdata player <playerName></red>")
                return true
            }
            val targetName = args[1]
            val targetUuid = Bukkit.getOfflinePlayer(targetName).uniqueId.toString()
            val count = plugin.database.removeShulkersByPlayer(targetUuid)
            plugin.stringUtils.sendMessage(p, "<green>Removed $count shulker(s) belonging to $targetName.</green>")
        } else if (args[0].matches(Regex("^\\d+([smhd])$"))) {
            val duration = plugin.stringUtils.parse(args[0]) ?: run {
                plugin.stringUtils.sendMessage(p, "<red>Invalid duration. Use format: 30s, 10m, 2h, 7d</red>")
                return true
            }

            val cutoff = System.currentTimeMillis() - duration
            val count = plugin.database.removeOwnersSince(cutoff)
            plugin.stringUtils.sendMessage(p, "<green>Removed $count shulkers older than ${args[0]}.</green>")
        } else {
            plugin.stringUtils.sendMessage(p, "<red>Invalid argument. Use a duration (e.g. 7d) or 'player <name>'</red>")
        }

        return true
    }
}