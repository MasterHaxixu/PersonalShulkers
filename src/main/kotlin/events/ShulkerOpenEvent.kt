package com.masterhaxixu.events

import com.masterhaxixu.Main
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class ShulkerOpenEvent(private val plugin: Main) : Listener {

    @EventHandler
    private fun onOpenEvent(event: PlayerInteractEvent) {
        if (event.clickedBlock == null) return
        if (event.clickedBlock?.state !is ShulkerBox || !event.action.isRightClick) return
        val shulker = event.clickedBlock!!.state as ShulkerBox
        val ownerString = shulker.persistentDataContainer.get(plugin.keys.ownerKey, PersistentDataType.STRING) ?: return
        val shulkerKey = shulker.persistentDataContainer.get(plugin.keys.uniqueKey, PersistentDataType.STRING) ?: return


        if (!plugin.database.getShulker(shulkerKey)) {
            shulker.persistentDataContainer.remove(plugin.keys.uniqueKey)
            shulker.persistentDataContainer.remove(plugin.keys.ownerKey)
            shulker.update()
            return
        }


        if (UUID.fromString(ownerString) != event.player.uniqueId && !event.player.hasPermission("personalshulkers.bypass")) {
            event.isCancelled = true
            event.player.sendRichMessage(plugin.config.getString("messages.openMessage")!!)
        }
    }
}