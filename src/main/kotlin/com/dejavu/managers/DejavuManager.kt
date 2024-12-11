package com.dejavu.managers

import com.dejavu.DejavuPlugin
import com.dejavu.WorldNPC
import com.dejavu.models.PlayerMemory
import com.dejavu.tasks.DejavuMovementTask
import com.dejavu.util.Constants
import com.dejavu.util.MessageUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.util.*

class DejavuManager(
    private val plugin: DejavuPlugin,
    private val memoryManager: MemoryManager
) {
    private val activeNPCs = mutableMapOf<UUID, WorldNPC>()

    fun triggerDejavu(player: Player) {
        val memories = memoryManager.getMemories(player.uniqueId) ?: return
        if (memories.isEmpty()) return

        memoryManager.setDejavuState(player.uniqueId, true)
        val firstMemory = memories.first()

        val npc = WorldNPC.createFromLive(
            displayName = "",
            modelAfter = player,
            location = firstMemory.location.apply {
                yaw = firstMemory.yaw
                pitch = firstMemory.pitch
            },
            plugin = plugin
        ).also {
            it.spawnFor(player)
        }

        activeNPCs[player.uniqueId] = npc
        startDejavuEffects(player, npc, memories)
    }

    private fun startDejavuEffects(player: Player, npc: WorldNPC, memories: List<PlayerMemory>) {
        // Visual and sound effects
        player.playSound(player.location, Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f)
        player.showTitle(Title.title(
            MessageUtil.parse(Constants.DEJAVU_START),
            Component.empty(),
            Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(1500),
                Duration.ofMillis(500)
            )
        ))

        startNPCMovement(player, npc, memories)
        startProgressBar(player)
    }

    private fun startNPCMovement(player: Player, npc: WorldNPC, memories: List<PlayerMemory>) {
        DejavuMovementTask(
            plugin = plugin,
            player = player,
            npc = npc,
            memories = memories,
            onComplete = { endDejavu(player) }
        ).runTaskTimer(plugin, 0L, Constants.TICK_INTERVAL)
    }

    private fun startProgressBar(player: Player) {
        object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline || !memoryManager.isInDejavu(player.uniqueId)) {
                    cancel()
                    return
                }
                MessageUtil.sendProgressBar(player)
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    fun endDejavu(player: Player) {
        if (!player.isOnline) {
            cleanup(player.uniqueId)
            return
        }

        activeNPCs[player.uniqueId]?.let { npc ->
            try {
                npc.despawnFor(player)
            } catch (e: Exception) {
                plugin.logger.severe("Failed to despawn NPC: ${e.message}")
            }
            activeNPCs.remove(player.uniqueId)
        }

        memoryManager.setDejavuState(player.uniqueId, false)
        MessageUtil.showEndTitle(player)
    }

    fun cleanup() {
        activeNPCs.forEach { (_, npc) ->
            try {
                npc.despawnForAll()
            } catch (e: Exception) {
                plugin.logger.severe("Failed to despawn NPC during cleanup: ${e.message}")
            }
        }
        activeNPCs.clear()
    }

    private fun cleanup(playerId: UUID) {
        activeNPCs[playerId]?.let { npc ->
            try {
                npc.despawnForAll()
            } catch (e: Exception) {
                plugin.logger.severe("Failed to despawn NPC during cleanup: ${e.message}")
            }
        }
        activeNPCs.remove(playerId)
        memoryManager.setDejavuState(playerId, false)
    }
}
