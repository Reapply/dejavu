package com.dejavu.models

import org.bukkit.Location

data class PlayerMemory(
    val location: Location,
    val timestamp: Long,
    val yaw: Float,
    val pitch: Float
)