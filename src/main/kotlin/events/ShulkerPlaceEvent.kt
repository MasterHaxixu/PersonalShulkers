package com.masterhaxixu.events

import com.masterhaxixu.Main
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.ShulkerBox
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

class ShulkerPlaceEvent(private val plugin: Main) : Listener {

    @EventHandler
    private suspend fun onPlaceEvent(event: BlockPlaceEvent) {
        if(Tag.SHULKER_BOXES.isTagged(event.block.type)) {
            val regionCheck: String? = plugin.config.getString("regionCheck")
            val location: Location = if(regionCheck == "player") event.player.location
            else event.block.location
            if(plugin.regionUtils.isInRegion(location)) {
                val shulker: ShulkerBox = event.block.state as ShulkerBox
                val uuidKey = NamespacedKey(plugin, "personalshulkers_owner_uuid")
                val uniqueKey = NamespacedKey(plugin, "personalshulkers_unique_shulker_key")
                val uniqueShulkerKey = UUID.randomUUID().toString()

                shulker.persistentDataContainer.set(
                    uuidKey,
                    PersistentDataType.STRING,
                    event.player.uniqueId.toString()
                )

                shulker.persistentDataContainer.set(
                    uniqueKey,
                    PersistentDataType.STRING,
                    uniqueShulkerKey
                )

                shulker.update()
                plugin.database.addShulker(uniqueShulkerKey, event.player.uniqueId.toString())
            }

        }

    }

}