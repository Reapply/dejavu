// File: com/dejavu/util/MessageUtil.kt (continued)
package com.dejavu.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.title.Title
import org.bukkit.entity.Player
import java.time.Duration

object MessageUtil {
    private val miniMessage = MiniMessage.miniMessage()

    fun parse(message: String): Component = miniMessage.deserialize(message)

    fun sendMessage(player: Player, message: String) {
        player.sendMessage(parse(message))
    }

    fun sendProgressBar(player: Player) {
        val progress = Component.text("∿").color(Constants.MAIN_COLOR)
            .append(Component.text("⟡").color(Constants.ACCENT_COLOR))
            .append(Component.text("∿").color(Constants.HIGHLIGHT_COLOR))
        player.sendActionBar(progress)
    }

    fun showEndTitle(player: Player) {
        player.showTitle(Title.title(
            parse(Constants.DEJAVU_END),
            Component.empty(),
            Title.Times.times(
                Duration.ofMillis(500),
                Duration.ofMillis(1500),
                Duration.ofMillis(500)
            )
        ))
    }
}