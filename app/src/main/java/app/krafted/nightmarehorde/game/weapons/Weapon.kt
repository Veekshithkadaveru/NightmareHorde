package app.krafted.nightmarehorde.game.weapons

import app.krafted.nightmarehorde.engine.core.Vector2

/**
 * Base class for all weapons.
 * Defines common properties like damage, fire rate, and projectile characteristics.
 */
abstract class Weapon(
    val name: String,
    val damage: Float,
    val fireRate: Float, // Shots per second
    val range: Float,
    val projectileSpeed: Float = 500f,
    val maxAmmo: Int = 100,
    val infiniteAmmo: Boolean = false,
    val projectileCount: Int = 1,
    val spreadAngle: Float = 0f, // Total spread angle in degrees
    val penetrating: Boolean = false,
    val type: WeaponType,
    /** Whether this weapon uses flame-style particle firing (cone of fire particles). */
    val isFlame: Boolean = false,
    /** Whether this weapon uses melee-style arc sweep (whip blade). */
    val isMelee: Boolean = false
) {
    private var cooldownTimer: Float = 0f

    fun tickCooldown(deltaTime: Float) {
        if (cooldownTimer > 0f) {
            cooldownTimer -= deltaTime
        }
    }

    fun isReady(): Boolean {
        return cooldownTimer <= 0f
    }

    fun resetCooldown() {
        cooldownTimer = 1f / fireRate
    }
}

enum class WeaponType {
    PISTOL,
    ASSAULT_RIFLE,
    SHOTGUN,
    SMG,
    FLAMETHROWER,
    MELEE
}
