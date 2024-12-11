package com.dejavu

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.npc.NPC
import com.github.retrooper.packetevents.protocol.player.TextureProperty
import com.github.retrooper.packetevents.protocol.player.UserProfile
import com.github.retrooper.packetevents.protocol.world.Location as PacketLocation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import org.bukkit.plugin.java.JavaPlugin

class WorldNPC private constructor(
    displayName: String,
    textureProperties: List<TextureProperty>?,
    var location: Location,
    private val plugin: JavaPlugin
) {
    private val userProfile = UserProfile(UUID.randomUUID(), displayName, textureProperties)
    private val id: Int = SpigotReflectionUtil.generateEntityId()
    private val nameComponent: Component = Component.text(displayName)
    val npc: NPC = NPC(userProfile, id, nameComponent)

    fun spawnFor(player: Player) {
        plugin.logger.info("[DEBUG-NPC] Attempting to spawn NPC for ${player.name} at $location")
        val playerUser = PacketEvents.getAPI().playerManager.getUser(player)
            ?: throw IllegalStateException("Could not find PacketEvents user for player ${player.name}")

        npc.location = location.toPacketLocation()
        plugin.logger.info("[DEBUG-NPC] Set initial NPC location to ${npc.location}")

        try {
            npc.spawn(playerUser.channel)
            plugin.logger.info("[DEBUG-NPC] Successfully spawned NPC")
        } catch (ex: Exception) {
            plugin.logger.severe("[DEBUG-NPC] Failed to spawn NPC: ${ex.message}")
            ex.printStackTrace()
            throw IllegalStateException("Failed to spawn NPC for player ${player.name}", ex)
        }

        try {
            val metadataWrapper = WrapperPlayServerEntityMetadata(
                id,
                listOf(EntityData(17, EntityDataTypes.BYTE, 127.toByte()))
            )
            PacketEvents.getAPI().playerManager.sendPacket(playerUser, metadataWrapper)
            plugin.logger.info("[DEBUG-NPC] Sent metadata packet")
        } catch (ex: Exception) {
            plugin.logger.severe("[DEBUG-NPC] Failed to send metadata: ${ex.message}")
            ex.printStackTrace()
        }
    }

    fun moveTo(newLocation: Location) {
        plugin.logger.info("[DEBUG-NPC] Moving NPC from $location to $newLocation")
        try {
            val packetLocation = newLocation.toPacketLocation()
            plugin.logger.info("[DEBUG-NPC] Created packet location: $packetLocation")

            npc.updateLocation(packetLocation)
            plugin.logger.info("[DEBUG-NPC] Updated NPC location in packet system")

            location = newLocation.clone()
            plugin.logger.info("[DEBUG-NPC] Updated internal location")
        } catch (ex: Exception) {
            plugin.logger.severe("[DEBUG-NPC] Failed to move NPC: ${ex.message}")
            ex.printStackTrace()
            throw ex
        }
    }

    fun spawnForAll() {
        plugin.logger.info("[DEBUG-NPC] Spawning NPC for all players")
        Bukkit.getOnlinePlayers().forEach {
            try {
                spawnFor(it)
            } catch (ex: Exception) {
                plugin.logger.severe("[DEBUG-NPC] Failed to spawn NPC for ${it.name}: ${ex.message}")
            }
        }
    }

    fun despawnFor(player: Player) {
        plugin.logger.info("[DEBUG-NPC] Despawning NPC for ${player.name}")
        val playerUser = PacketEvents.getAPI().playerManager.getUser(player)
            ?: throw IllegalStateException("Could not find PacketEvents user for player ${player.name}")
        try {
            npc.despawn(playerUser.channel)
            plugin.logger.info("[DEBUG-NPC] Successfully despawned NPC for ${player.name}")
        } catch (ex: Exception) {
            plugin.logger.severe("[DEBUG-NPC] Failed to despawn NPC: ${ex.message}")
            ex.printStackTrace()
            throw ex
        }
    }

    fun despawnForAll() {
        plugin.logger.info("[DEBUG-NPC] Despawning NPC for all players")
        Bukkit.getOnlinePlayers().forEach {
            try {
                despawnFor(it)
            } catch (ex: Exception) {
                plugin.logger.severe("[DEBUG-NPC] Failed to despawn NPC for ${it.name}: ${ex.message}")
            }
        }
    }

    private fun Location.toPacketLocation(): PacketLocation {
        return PacketLocation(x, y, z, yaw, pitch)
    }

    companion object {
        fun createFromLive(
            displayName: String,
            modelAfter: Player,
            location: Location,
            plugin: JavaPlugin
        ): WorldNPC {
            plugin.logger.info("[DEBUG-NPC] Creating NPC modeled after ${modelAfter.name} at $location")
            val playerUser = PacketEvents.getAPI().playerManager.getUser(modelAfter)
                ?: throw IllegalStateException("Could not find PacketEvents user for player ${modelAfter.name}")

            return WorldNPC(
                displayName = displayName,
                textureProperties = playerUser.profile.textureProperties,
                location = location.clone(),
                plugin = plugin
            ).also {
                plugin.logger.info("[DEBUG-NPC] Successfully created NPC instance")
            }
        }
    }
}