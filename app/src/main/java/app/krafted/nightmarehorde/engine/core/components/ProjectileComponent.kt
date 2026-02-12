package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.engine.core.Vector2

data class ProjectileComponent(
    val damage: Float,
    val maxDistance: Float,
    var distanceTraveled: Float = 0f,
    val ownerId: String, // To avoid hitting self
    val penetrating: Boolean = false,
    val maxLifetime: Float = 0f, // Max time alive in seconds (0 = use distance only)
    var timeAlive: Float = 0f,
    val growthRate: Float = 0f, // Per second
    val fadeRate: Float = 0f // Alpha per second
) : Component
