package app.krafted.nightmarehorde.game.weapons

import app.krafted.nightmarehorde.engine.core.Vector2

enum class WeaponType {
    PISTOL,
    ASSAULT_RIFLE,
    SHOTGUN,
    SMG,
    FLAMETHROWER,
    MELEE
}

abstract class Weapon(
    val type: WeaponType,
    val name: String,
    val damage: Float,
    val fireRate: Float, // Shots per second
    val range: Float,
    val maxAmmo: Int,
    val infiniteAmmo: Boolean = false,
    val projectileSpeed: Float = 500f,
    val projectileCount: Int = 1,
    val spreadAngle: Float = 0f,
    val penetrating: Boolean = false, // If true, passes through enemies (but still hits obstacles)
    val isFlame: Boolean = false, // Flame particles (short-lived, random spread)
    val isMelee: Boolean = false // Melee slash (VS-style sweep attack)
) {
    var checkCooldown: Float = 0f

    /** Tick cooldown down by dt. Call every frame. */
    fun tickCooldown(dt: Float) {
        checkCooldown = (checkCooldown - dt).coerceAtLeast(-0.1f) // Prevent accumulating huge negative values
    }

    /** Returns true if weapon is ready to fire (cooldown expired). */
    fun isReady(): Boolean = checkCooldown <= 0f

    fun resetCooldown() {
        checkCooldown = 1f / fireRate
    }

    /** Convenience: tick and check in one call. */
    fun canFire(dt: Float): Boolean {
        tickCooldown(dt)
        return isReady()
    }
}
