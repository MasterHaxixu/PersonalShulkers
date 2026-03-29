package com.masterhaxixu.keys

import com.masterhaxixu.Main
import org.bukkit.NamespacedKey

class ShulkerKeys(plugin: Main) {
    val ownerKey = NamespacedKey(plugin, "personalshulkers_owner_uuid")
    val uniqueKey = NamespacedKey(plugin, "personalshulkers_unique_shulker_key")
}