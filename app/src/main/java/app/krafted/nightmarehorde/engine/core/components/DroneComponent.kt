package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.game.data.DroneType

data class DroneComponent(
    val droneType: DroneType,
    val ownerEntityId: Long,

    // Level & upgrade
    var level: Int = 1,

    // Orbit state (radians) — base angle advances continuously, formation offset is additive
    var orbitBaseAngle: Float = 0f,
    var slotIndex: Int = 0,

    // Fuel state (seconds)
    var fuel: Float = 60f,
    var maxFuel: Float = 60f,

    // Power-down grace
    var isPoweredDown: Boolean = false,
    var graceTimer: Float = 0f,
    var isLost: Boolean = false,

    // Firing state
    var fireCooldown: Float = 0f,
    var currentTargetId: Long = -1L,

    // Gunner Lv3 burst state
    var burstShotsRemaining: Int = 0,
    var burstCooldown: Float = 0f,

    // Visual state
    var glowPulseTimer: Float = 0f,
    var dimAlpha: Float = 1f
) : Component {

    val fuelPercent: Float get() = if (maxFuel > 0f) fuel / maxFuel else 0f
    val isLowFuel: Boolean get() = fuel > 0f && fuel < 15f
    val isOperational: Boolean get() = !isPoweredDown && !isLost && fuel > 0f

    companion object {
        const val GRACE_WINDOW_SECONDS = 10f
        const val ORBIT_RADIUS = 100f
        const val ORBIT_SPEED = 2.5f
        const val POWERED_DOWN_ORBIT_SPEED = 0.5f
        const val LOW_FUEL_PULSE_SPEED = 4f
    }
}
