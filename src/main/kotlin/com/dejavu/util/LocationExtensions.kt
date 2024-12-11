package com.dejavu.util

import org.bukkit.Location
import org.bukkit.util.Vector

fun Location.interpolateTowards(target: Location, speed: Double): Location {
    val direction = target.toVector().subtract(this.toVector())
    val distance = direction.length()

    return if (distance < speed) {
        target.clone()
    } else {
        val moveVector = direction.normalize().multiply(speed)
        this.clone().add(moveVector)
    }
}

fun Location.interpolateRotation(target: Location, maxDelta: Float = 10f): Location {
    val yawDiff = ((target.yaw - this.yaw + 180) % 360) - 180
    val pitchDiff = target.pitch - this.pitch

    return this.clone().apply {
        yaw = this.yaw + yawDiff.coerceIn(-maxDelta, maxDelta)
        pitch = this.pitch + pitchDiff.coerceIn(-maxDelta, maxDelta)
    }
}