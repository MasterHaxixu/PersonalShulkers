package com.masterhaxixu.database

import com.masterhaxixu.Main
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.sql.Connection
import java.util.concurrent.ConcurrentHashMap

class DatabaseHandler(private val plugin: Main) {

    private lateinit var dataSource: HikariDataSource
    private lateinit var dbType: String
    private val cache: MutableSet<String> = ConcurrentHashMap.newKeySet()

    fun connect() {
        dbType = plugin.config.getString("database.type", "sqlite")!!.lowercase().trim()

        val hikariConfig = HikariConfig()
        hikariConfig.poolName = "PersonalShulkers-Pool"

        when (dbType) {
            "mysql" -> {
                val host = plugin.config.getString("database.host", "localhost")
                val port = plugin.config.getInt("database.port", 3306)
                val database = plugin.config.getString("database.database", "personalshulkers")
                val username = plugin.config.getString("database.username", "root")
                val password = plugin.config.getString("database.password", "")
                hikariConfig.driverClassName = "com.mysql.cj.jdbc.Driver"
                hikariConfig.jdbcUrl =
                    "jdbc:mysql://$host:$port/$database?useSSL=false&autoReconnect=true&characterEncoding=utf8"
                hikariConfig.username = username
                hikariConfig.password = password
                hikariConfig.maximumPoolSize = plugin.config.getInt("database.pool-size", 10)
            }

            "mariadb" -> {
                val host = plugin.config.getString("database.host", "localhost")
                val port = plugin.config.getInt("database.port", 3306)
                val database = plugin.config.getString("database.database", "personalshulkers")
                val username = plugin.config.getString("database.username", "root")
                val password = plugin.config.getString("database.password", "")
                hikariConfig.driverClassName = "org.mariadb.jdbc.Driver"
                hikariConfig.jdbcUrl = "jdbc:mariadb://$host:$port/$database?autoReconnect=true&characterEncoding=utf8"
                hikariConfig.username = username
                hikariConfig.password = password
                hikariConfig.maximumPoolSize = plugin.config.getInt("database.pool-size", 10)
            }

            else -> {
                dbType = "sqlite"
                val dbFile = File(plugin.dataFolder, "shulkers.db")
                if (!plugin.dataFolder.exists()) plugin.dataFolder.mkdirs()
                hikariConfig.jdbcUrl = "jdbc:sqlite:${dbFile.absolutePath}"
                hikariConfig.driverClassName = "org.sqlite.JDBC"
                hikariConfig.maximumPoolSize = 1
            }
        }

        dataSource = HikariDataSource(hikariConfig)
        createTables()
        loadCache()
        plugin.logger.info("Connected to $dbType database. Loaded ${cache.size} shulker(s) into cache.")
    }

    fun disconnect() {
        if (::dataSource.isInitialized && !dataSource.isClosed) {
            dataSource.close()
        }
    }

    private fun createTables() {
        dataSource.connection.use { conn ->
            val sql = if (dbType == "sqlite") {
                """
                CREATE TABLE IF NOT EXISTS shulkers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    unique_shulker_key TEXT NOT NULL UNIQUE,
                    owner_uuid TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            } else {
                """
                CREATE TABLE IF NOT EXISTS shulkers (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    unique_shulker_key VARCHAR(255) NOT NULL UNIQUE,
                    owner_uuid VARCHAR(255) NOT NULL,
                    createdAt BIGINT NOT NULL
                )
                """.trimIndent()
            }
            conn.createStatement().executeUpdate(sql)
        }
    }

    private fun loadCache() {
        dataSource.connection.use { conn ->
            conn.createStatement().executeQuery("SELECT unique_shulker_key FROM shulkers").use { rs ->
                while (rs.next()) cache.add(rs.getString("unique_shulker_key"))
            }
        }
    }

    private fun reloadCache(conn: Connection) {
        val keys = mutableSetOf<String>()
        conn.createStatement().executeQuery("SELECT unique_shulker_key FROM shulkers").use { rs ->
            while (rs.next()) keys.add(rs.getString("unique_shulker_key"))
        }
        cache.clear()
        cache.addAll(keys)
    }

    fun getShulker(uniqueShulkerKey: String): Boolean = cache.contains(uniqueShulkerKey)

    suspend fun addShulker(uniqueShulkerKey: String, ownerUuid: String) {
        cache.add(uniqueShulkerKey)
        withContext(Dispatchers.IO) {
            dataSource.connection.use { conn ->
                conn.prepareStatement("INSERT INTO shulkers (unique_shulker_key, owner_uuid, createdAt) VALUES (?, ?, ?)")
                    .use { stmt ->
                        stmt.setString(1, uniqueShulkerKey)
                        stmt.setString(2, ownerUuid)
                        stmt.setLong(3, System.currentTimeMillis())
                        stmt.executeUpdate()
                    }
            }
        }
    }

    suspend fun removeShulker(uniqueShulkerKey: String) {
        cache.remove(uniqueShulkerKey)
        withContext(Dispatchers.IO) {
            dataSource.connection.use { conn ->
                conn.prepareStatement("DELETE FROM shulkers WHERE unique_shulker_key = ?").use { stmt ->
                    stmt.setString(1, uniqueShulkerKey)
                    stmt.executeUpdate()
                }
            }
        }
    }

    suspend fun removeShulkersByPlayer(ownerUuid: String): Int {
        return withContext(Dispatchers.IO) {
            dataSource.connection.use { conn ->
                val count = conn.prepareStatement("DELETE FROM shulkers WHERE owner_uuid = ?").use { stmt ->
                    stmt.setString(1, ownerUuid)
                    stmt.executeUpdate()
                }
                if (count > 0) reloadCache(conn)
                count
            }
        }
    }

    suspend fun removeOwnersSince(cutoffMillis: Long): Int {
        return withContext(Dispatchers.IO) {
            dataSource.connection.use { conn ->
                val count = conn.prepareStatement("DELETE FROM shulkers WHERE createdAt > ?").use { stmt ->
                    stmt.setLong(1, cutoffMillis)
                    stmt.executeUpdate()
                }
                if (count > 0) reloadCache(conn)
                count
            }
        }
    }
}