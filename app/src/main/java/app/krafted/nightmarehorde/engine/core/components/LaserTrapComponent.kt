package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component

/**
 * Marks an entity as a laser trap hazard zone (Lab map).
 *
 * The entity must also have a ColliderComponent with isTrigger=true on the OBSTACLE layer.
 * MapFeatureSystem detects player overlap via AABB and applies periodic damage.
 */
data class LaserTrapComponent(
    /** Damage applied per interval while player is inside */
    val damagePerInterval: Float = 5f,
    /** How often (in seconds) damage is applied while inside */
    val damageInterval: Float = 1.0f,
    /** Accumulated time since last damage application (mutable) */
    var timeSinceDamage: Float = 0f
) : Component
