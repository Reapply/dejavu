package com.dejavu.tasks

import com.dejavu.DejavuPlugin
import com.dejavu.WorldNPC
import com.dejavu.models.PlayerMemory
import com.dejavu.util.Constants
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

class DejavuMovementTask(
    private val plugin: DejavuPlugin,
    private val player: Player,
    private val npc: WorldNPC,
    private val memories: List<PlayerMemory>,
    private val onComplete: () -> Unit
) : BukkitRunnable() {
    private var memoryIndex = 0
    private var tickCount = 0

    override fun run() {
        tickCount++
        if (!player.isOnline) {
            cancel()
            onComplete()
            return
        }

        if (memoryIndex >= memories.size - 1) {
            cancel()
            onComplete()
            return
        }

        processMovement()
    }

    private fun processMovement() {
        val currentMemory = memories[memoryIndex]
        val nextMemory = memories[memoryIndex + 1]
        val npcLocation = npc.location

        val direction = nextMemory.location.toVector().subtract(npcLocation.toVector())
        val distance = direction.length()

        if (distance < Constants.MOVEMENT_SPEED) {
            handleWaypointReached(nextMemory)
        } else {
            moveTowardsNextPoint(npcLocation, direction, nextMemory)
        }
    }

    private fun handleWaypointReached(nextMemory: PlayerMemory) {
        memoryIndex++
        val newLocation = nextMemory.location.clone()
        npc.moveTo(newLocation)
        player.playSound(
            npc.location,
            Sound.BLOCK_AMETHYST_BLOCK_CHIME,
            0.2f,
            1.0f + (memoryIndex % 3) * 0.1f
        )
    }

    private fun moveTowardsNextPoint(currentLocation: Location, direction: Vector, nextMemory: PlayerMemory) {
        val moveVector = direction.normalize().multiply(Constants.MOVEMENT_SPEED)
        val newLocation = currentLocation.clone()

        newLocation.add(moveVector)
        interpolateRotation(newLocation, currentLocation, nextMemory)
        npc.moveTo(newLocation)
    }

    private fun interpolateRotation(newLocation: Location, currentLocation: Location, nextMemory: PlayerMemory) {
        val yawDiff = ((nextMemory.yaw - currentLocation.yaw + 180) % 360) - 180
        newLocation.yaw = currentLocation.yaw + yawDiff.coerceIn(-10f, 10f)

        val pitchDiff = nextMemory.pitch - currentLocation.pitch
        newLocation.pitch = currentLocation.pitch + pitchDiff.coerceIn(-10f, 10f)
    }
}