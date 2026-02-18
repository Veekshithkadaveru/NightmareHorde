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
    var baseDamage: Float = 0f,

    // ─── Progression stats (modified by level-up upgrades) ───────────────

    /** Radius for magnet pickup attraction (default matches PickupSystem.MAGNET_RADIUS) */
    var pickupRadius: Float = 100f,
    /** Multiplier for ammo capacity across all weapons (1.0 = 100%) */
    var ammoCapacityMultiplier: Float = 1f,
    /** Multiplier for XP gained from orbs (1.0 = 100%) */
    var xpMultiplier: Float = 1f,
    /** HP regenerated per second. 0 = no regen */
    var hpRegen: Float = 0f,
    /** Flat bonus to projectile count for standard weapons */
    var projectileCountBonus: Int = 0,
    /** Multiplier for area/AOE effects (weapon range, melee arc) */
    var areaMultiplier: Float = 1f,
    /** Reduction factor for weapon cooldowns. 0.0 = none, capped at 0.7 */
    var cooldownReduction: Float = 0f,
    /** Bonus to rarity roll weights. Higher = more rare/epic/legendary pulls */
    var luck: Float = 0f,
    /** Number of revival chances remaining */
    var revivalCount: Int = 0,
    /** Multiplier for drone damage output */
    var droneDamageMultiplier: Float = 1f,
    /** Multiplier for drone fuel efficiency (higher = slower drain) */
    var droneFuelEfficiency: Float = 1f
) : Component {

    override fun toString(): String {
        return "StatsComponent(speed=$moveSpeed, maxHp=$maxHealth, armor=$armor, " +
                "dmg×=$damageMultiplier, rate×=$fireRateMultiplier, pickup=$pickupRadius, " +
                "regen=$hpRegen, luck=$luck, revivals=$revivalCount)"
    }
}
