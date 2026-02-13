package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component

/**
 * Component holding gameplay stats that affect movement, combat, and progression.
 * Stats can be modified by upgrades during a run (Phase E).
 */
class StatsComponent(
    /** Base movement speed in world units per second */
    var moveSpeed: Float = 450f,
    /** Maximum health points */
    var maxHealth: Int = 100,
    /** Flat armor value subtracted from incoming damage */
    var armor: Int = 0,
    /** Multiplier applied to outgoing damage (1.0 = 100%) */
    var damageMultiplier: Float = 1f,
    /** Multiplier applied to weapon fire rate (1.0 = 100%) */
    var fireRateMultiplier: Float = 1f,
    /** Base physical damage (e.g. for collision/melee) */
    var baseDamage: Float = 0f
) : Component {

    override fun toString(): String {
        return "StatsComponent(speed=$moveSpeed, maxHp=$maxHealth, armor=$armor, dmg×=$damageMultiplier, rate×=$fireRateMultiplier)"
    }
}
