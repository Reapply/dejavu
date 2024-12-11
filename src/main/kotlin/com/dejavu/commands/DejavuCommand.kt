package com.dejavu.commands

import com.dejavu.managers.DejavuManager
import com.dejavu.managers.MemoryManager
import com.dejavu.util.Constants
import com.dejavu.util.MessageUtil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DejavuCommand(
    private val dejavuManager: DejavuManager,
    private val memoryManager: MemoryManager
) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) return false

        when {
            memoryManager.isInDejavu(sender.uniqueId) -> {
                MessageUtil.sendMessage(sender, Constants.ALREADY_ACTIVE)
            }
            !memoryManager.hasRecentMemories(sender.uniqueId) -> {
                MessageUtil.sendMessage(sender, Constants.NO_MEMORIES)
            }
            else -> {
                dejavuManager.triggerDejavu(sender)
            }
        }
        return true
    }
}