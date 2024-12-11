package com.dejavu.util

import net.kyori.adventure.text.format.TextColor

object Constants {
    val MAIN_COLOR = TextColor.color(0xB19CD9)
    val ACCENT_COLOR = TextColor.color(0xFFB6C1)
    val HIGHLIGHT_COLOR = TextColor.color(0x87CEEB)

    const val DEJAVU_START = "<gradient:#B19CD9:#FFB6C1>✧ You've been here before... ✧</gradient>"
    const val DEJAVU_END = "<gradient:#87CEEB:#B19CD9>✧ Reality snaps back into place ✧</gradient>"
    const val NO_MEMORIES = "<#FF6B6B>No recent memories to experience"
    const val ALREADY_ACTIVE = "<#FF6B6B>You're already experiencing déjà vu"

    const val MOVEMENT_SPEED = 0.4
    const val TICK_INTERVAL = 1L
}