package com.masterhaxixu.util

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.WorldGuard
import org.bukkit.Location
import kotlin.String

fun Location.isInRegion(regionNames: List<String>): Boolean {
    if (regionNames.isEmpty()) return false

    val regionManager = WorldGuard.getInstance().platform.regionContainer
        .get(BukkitAdapter.adapt(this.world)) ?: return false

    val playerLocation = BlockVector3.at(
        this.blockX,
        this.blockY,
        this.blockZ
    )

    return regionNames.any { name ->
        regionManager.getRegion(name)?.contains(playerLocation) == true
    }
}