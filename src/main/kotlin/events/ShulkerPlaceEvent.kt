package com.masterhaxixu.events

import com.masterhaxixu.Main
import com.masterhaxixu.util.isInRegion
import org.bukkit.Location
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
        if (Tag.SHULKER_BOXES.isTagged(event.block.type)) {
            val regionCheck: String? = plugin.config.getString("regionCheck")
            val location: Location = if (regionCheck == "player") event.player.location
            else event.block.location
            if (location.isInRegion(plugin.config.getStringList("allowedRegions"))) {
                val shulker: ShulkerBox = event.block.state as ShulkerBox
                val uniqueShulkerKey = UUID.randomUUID().toString()

                shulker.persistentDataContainer.set(
                    plugin.keys.ownerKey,
                    PersistentDataType.STRING,
                    event.player.uniqueId.toString()
                )

                shulker.persistentDataContainer.set(
                    plugin.keys.uniqueKey,
                    PersistentDataType.STRING,
                    uniqueShulkerKey
                )

                shulker.update()
                plugin.database.addShulker(uniqueShulkerKey, event.player.uniqueId.toString())
            }

        }

    }

}
