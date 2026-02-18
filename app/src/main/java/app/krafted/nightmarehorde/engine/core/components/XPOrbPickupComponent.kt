package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component

/**
 * Tag component placed on XP orb entities.
 * The value is used by PickupCollisionSystem to grant XP to the player.
 */
data class XPOrbPickupComponent(
    val xpValue: Int = 1
) : Component
