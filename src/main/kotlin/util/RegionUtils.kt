package com.masterhaxixu.util

import com.masterhaxixu.Main
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import org.bukkit.Location
import org.bukkit.entity.Player




class RegionUtils(private val plugin: Main) {
    fun isInRegion(location: Location): Boolean {
        val regionNames = plugin.config.getStringList("allowedRegions")
        if (regionNames.isEmpty()) return false

        val regionManager = WorldGuard.getInstance().platform.regionContainer
            .get(BukkitAdapter.adapt(location.world)) ?: return false

        val playerLocation = BlockVector3.at(
            location.blockX,
            location.blockY,
            location.blockZ
        )

        return regionNames.any { name ->
            regionManager.getRegion(name)?.contains(playerLocation) == true
        }
    }
}