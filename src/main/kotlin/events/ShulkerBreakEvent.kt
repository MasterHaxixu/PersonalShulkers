package com.masterhaxixu.events

import com.masterhaxixu.Main
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*


class ShulkerBreakEvent(private val plugin: Main) : Listener {

    @EventHandler
    private suspend fun onBreakEvent(event: BlockBreakEvent) {
        if (!Tag.SHULKER_BOXES.isTagged(event.block.type)) return

        val shulker = event.block.state as ShulkerBox
        val ownerKey = NamespacedKey(plugin, "personalshulkers_owner_uuid")
        val uniqueKey = NamespacedKey(plugin, "personalshulkers_unique_shulker_key")

        val ownerString = shulker.persistentDataContainer.get(ownerKey, PersistentDataType.STRING) ?: return
        val shulkerKey = shulker.persistentDataContainer.get(uniqueKey, PersistentDataType.STRING) ?: return

        if (!plugin.database.getShulker(shulkerKey)) {
            shulker.persistentDataContainer.remove(uniqueKey)
            shulker.persistentDataContainer.remove(ownerKey)
            shulker.update()
            return
        }

        if (UUID.fromString(ownerString) != event.player.uniqueId && !event.player.hasPermission("personalshulkers.bypass")) {
            event.isCancelled = true
            plugin.stringUtils.sendMessage(event.player, plugin.config.getString("messages.breakMessage")!!)
            return
        }

        plugin.database.removeShulker(shulkerKey)
        shulker.persistentDataContainer.remove(uniqueKey)
        shulker.persistentDataContainer.remove(ownerKey)
        shulker.update()
    }

}