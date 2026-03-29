package com.masterhaxixu

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.registerSuspendingEvents
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import com.masterhaxixu.commands.ClearShulkersCommand
import com.masterhaxixu.database.DatabaseHandler
import com.masterhaxixu.events.ShulkerBreakEvent
import com.masterhaxixu.events.ShulkerOpenEvent
import com.masterhaxixu.events.ShulkerPlaceEvent
import com.masterhaxixu.keys.ShulkerKeys
import dev.faststats.bukkit.BukkitMetrics
import dev.faststats.core.Metrics


class Main : SuspendingJavaPlugin() {
    lateinit var database: DatabaseHandler
        private set
    lateinit var keys: ShulkerKeys
        private set

    private val metrics: Metrics? = BukkitMetrics.factory()
        .token("58130142b027e3d4e70d7b0b81ccfb79")
        .create(this)

    override fun onEnable() {
        this.logger.info("PersonalShulkers has been enabled!")
        saveDefaultConfig()
        config.options().copyDefaults(true)
        saveConfig()
        database = DatabaseHandler(this)
        keys = ShulkerKeys(this)
        metrics?.ready()
        database.connect()
        server.pluginManager.registerSuspendingEvents(ShulkerPlaceEvent(this), this)
        server.pluginManager.registerSuspendingEvents(ShulkerOpenEvent(this), this)
        server.pluginManager.registerSuspendingEvents(ShulkerBreakEvent(this), this)
        getCommand("clearshulkerdata")?.setSuspendingExecutor(ClearShulkersCommand(this))
    }

    override fun onDisable() {
        database.disconnect()
        metrics?.shutdown()
    }

}