package com.dejavu.managers

import com.dejavu.DejavuPlugin
import com.dejavu.models.PlayerMemory
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class MemoryManager(private val plugin: DejavuPlugin) : Listener {
    private val playerMemories = mutableMapOf<UUID, MutableList<PlayerMemory>>()
    private val playersInDejavu = mutableSetOf<UUID>()

    companion object {
        private const val MEMORY_INTERVAL = 2L
        private const val MEMORY_RETENTION_TIME = 30000L // 30 seconds
        private const val SIGNIFICANT_MOVE_THRESHOLD = 0.1
        private const val SIGNIFICANT_ROTATION_THRESHOLD = 5.0
    }

    fun registerListeners() {
        plugin.server.pluginManager.registerEvents(this, plugin)
        startCleanupTask()
    }

    private fun startCleanupTask() {
        object : BukkitRunnable() {
            override fun run() {
                cleanOldMemories()
            }
        }.runTaskTimer(plugin, 100L, 100L) // 5 seconds = 100 ticks
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!playersInDejavu.contains(event.player.uniqueId) &&
            plugin.server.currentTick % MEMORY_INTERVAL == 0L) {
            recordMemory(event.player)
        }
    }

    fun getMemories(playerId: UUID): List<PlayerMemory>? = playerMemories[playerId]?.toList()

    fun hasRecentMemories(playerId: UUID): Boolean =
        playerMemories[playerId]?.isNotEmpty() == true

    fun setDejavuState(playerId: UUID, inDejavu: Boolean) {
        if (inDejavu) {
            playersInDejavu.add(playerId)
        } else {
            playersInDejavu.remove(playerId)
            cleanupPlayerMemories(playerId)
        }
    }

    fun isInDejavu(playerId: UUID): Boolean = playersInDejavu.contains(playerId)

    private fun recordMemory(player: Player) {
        if (!player.hasMovedSignificantly()) {
            return
        }

        val memories = playerMemories.getOrPut(player.uniqueId) { mutableListOf() }

        val newMemory = PlayerMemory(
            location = player.location.clone(),
            timestamp = System.currentTimeMillis(),
            yaw = player.location.yaw,
            pitch = player.location.pitch
        )

        memories.add(newMemory)
        trimOldMemories(memories)
    }

    private fun Player.hasMovedSignificantly(): Boolean {
        val memories = playerMemories[uniqueId] ?: return true
        if (memories.isEmpty()) return true

        val lastMemory = memories.last()
        val distance = location.distance(lastMemory.location)
        val yawDiff = Math.abs(normalizeAngle(location.yaw - lastMemory.yaw))
        val pitchDiff = Math.abs(location.pitch - lastMemory.pitch)

        return distance > SIGNIFICANT_MOVE_THRESHOLD ||
                yawDiff > SIGNIFICANT_ROTATION_THRESHOLD ||
                pitchDiff > SIGNIFICANT_ROTATION_THRESHOLD
    }

    private fun normalizeAngle(angle: Float): Float {
        var result = angle
        while (result < -180) result += 360
        while (result > 180) result -= 360
        return result
    }

    private fun trimOldMemories(memories: MutableList<PlayerMemory>) {
        val cutoffTime = System.currentTimeMillis() - MEMORY_RETENTION_TIME
        memories.removeAll { it.timestamp < cutoffTime }
    }

    private fun cleanOldMemories() {
        val cutoffTime = System.currentTimeMillis() - MEMORY_RETENTION_TIME
        playerMemories.forEach { (playerId, memories) ->
            memories.removeAll { it.timestamp < cutoffTime }
            if (memories.isEmpty()) {
                playerMemories.remove(playerId)
            }
        }
    }

    private fun cleanupPlayerMemories(playerId: UUID) {
        playerMemories.remove(playerId)
    }

    fun cleanup() {
        playerMemories.clear()
        playersInDejavu.clear()
    }
}