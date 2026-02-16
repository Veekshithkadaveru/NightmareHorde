package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.engine.core.Vector2

data class ProjectileComponent(
    val damage: Float,
    val maxDistance: Float = 1000f,
    var distanceTraveled: Float = 0f,
    val ownerId: Long, // Entity ID
    val penetrating: Boolean = false,
    val maxLifetime: Float = 2f, // Max time alive in seconds
    var timeAlive: Float = 0f,
    val growthRate: Float = 0f, // Per second
    val fadeRate: Float = 0f, // Alpha per second
    /** True for melee-origin projectiles (e.g. whip blade segments). Used by boss retaliation. */
    val isMelee: Boolean = false
) : Component
