package com.dejavu

import com.dejavu.commands.DejavuCommand
import com.dejavu.managers.DejavuManager
import com.dejavu.managers.MemoryManager
import com.github.retrooper.packetevents.PacketEvents
import gg.flyte.twilight.Twilight
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import org.bukkit.plugin.java.JavaPlugin

class DejavuPlugin : JavaPlugin() {
    val twilight by lazy { Twilight(this) }
    private lateinit var memoryManager: MemoryManager
    private lateinit var dejavuManager: DejavuManager

    override fun onLoad() {
        initializePacketEvents()
    }

    override fun onEnable() {
        twilight

        if (!PacketEvents.getAPI().isInitialized) {
            PacketEvents.getAPI().init()
        }

        memoryManager = MemoryManager(this)
        dejavuManager = DejavuManager(this, memoryManager)

        registerListeners()
        registerCommands()

        logger.info("Dejavu plugin enabled successfully")
    }

    override fun onDisable() {
        dejavuManager.cleanup()
        if (PacketEvents.getAPI().isInitialized) {
            PacketEvents.getAPI().terminate()
        }
        logger.info("Dejavu plugin disabled")
    }

    private fun initializePacketEvents() {
        val builder = SpigotPacketEventsBuilder.build(this)
        builder.settings
            .checkForUpdates(false)
            .debug(false)
            .bStats(true)

        PacketEvents.setAPI(builder)
        PacketEvents.getAPI().load()
    }

    private fun registerListeners() {
        memoryManager.registerListeners()
    }

    private fun registerCommands() {
        getCommand("dejavu")?.setExecutor(DejavuCommand(dejavuManager, memoryManager))
    }
}