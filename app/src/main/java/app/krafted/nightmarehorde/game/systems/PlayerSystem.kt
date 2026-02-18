package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.Vector2
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponComponent
import app.krafted.nightmarehorde.engine.input.InputManager
import app.krafted.nightmarehorde.engine.rendering.Camera

/**
 * System that drives the player entity each frame:
 * - Reads joystick input and applies velocity based on move speed
 * - Flips sprite horizontally for left/right facing
 * - Updates the camera to follow the player
 * - Ticks invincibility timer on HealthComponent
 *
 * Priority 10 — runs before MovementSystem (50) so velocity is set before position updates.
 */
class PlayerSystem(
    private val inputManager: InputManager,
    private val camera: Camera
) : GameSystem(priority = 10) {

    /** Callback invoked when the player dies. Set by GameViewModel. */
    var onPlayerDeath: (() -> Unit)? = null

    /** Tracks whether death has already been fired this session */
    private var deathFired = false

    /** Accumulator for HP regen (fractional HP per frame) */
    private var regenAccumulator = 0f

    /** Track last horizontal direction for sprite flipping (1 = right, -1 = left) */
    private var lastFacingDirection = 1

    /** Last non-zero aim direction — persists when player stops moving */
    private var lastAimDirection = Vector2(1f, 0f)

    /** Accumulator for the invincibility flash effect (seconds) */
    private var flashTimer = 0f

    /** Flash toggle period in seconds */
    private companion object {
        const val FLASH_PERIOD = 0.1f
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        val player = entities.firstOrNull { it.getComponent(PlayerTagComponent::class) != null }
            ?: return

        val velocity = player.getComponent(VelocityComponent::class) ?: return
        val stats = player.getComponent(StatsComponent::class) ?: return
        val health = player.getComponent(HealthComponent::class) ?: return
        val sprite = player.getComponent(SpriteComponent::class)

        // --- Input → Velocity (VS-style: any joystick deflection = full speed) ---
        val rawDirection = inputManager.movementDirection.value
        val dirMagnitude = kotlin.math.sqrt(rawDirection.x * rawDirection.x + rawDirection.y * rawDirection.y)

        if (dirMagnitude > 0.01f) {
            // Normalize to magnitude 1 — full speed in the joystick direction
            val normX = rawDirection.x / dirMagnitude
            val normY = rawDirection.y / dirMagnitude
            velocity.vx = normX * stats.moveSpeed
            velocity.vy = normY * stats.moveSpeed

            // Save normalized direction for weapon aiming — persists when player stops
            lastAimDirection = Vector2(normX, normY)
        } else {
            velocity.vx = 0f
            velocity.vy = 0f
        }

        // --- Write aim direction into WeaponComponent so WeaponSystem always has it ---
        val weaponComp = player.getComponent(WeaponComponent::class)
        if (weaponComp != null) {
            weaponComp.facingDirection = lastAimDirection
        }

        // --- Sprite Flip ---
        if (sprite != null) {
            if (rawDirection.x < -0.01f) {
                lastFacingDirection = -1
                sprite.flipX = true
            } else if (rawDirection.x > 0.01f) {
                lastFacingDirection = 1
                sprite.flipX = false
            }
            // If direction.x is ~0, keep last facing direction
        }

        // --- Camera Follow (instant — VS-style) ---
        val playerTransform = player.getComponent(
            app.krafted.nightmarehorde.engine.core.components.TransformComponent::class
        )
        if (playerTransform != null) {
            camera.setPosition(playerTransform.x, playerTransform.y)
        }

        // --- HP Regen ---
        if (stats.hpRegen > 0f && health.isAlive && health.currentHealth < health.maxHealth) {
            regenAccumulator += stats.hpRegen * deltaTime
            if (regenAccumulator >= 1f) {
                val healAmount = regenAccumulator.toInt()
                health.heal(healAmount)
                regenAccumulator -= healAmount.toFloat()
            }
        }

        // --- Invincibility Timer ---
        health.updateInvincibility(deltaTime)

        // --- Invincibility Visual Flash ---
        if (sprite != null) {
            if (health.isInvincible) {
                flashTimer += deltaTime
                // Toggle between visible and semi-transparent on a fixed period
                sprite.alpha = if ((flashTimer / FLASH_PERIOD).toInt() % 2 == 0) 1f else 0.3f
            } else {
                flashTimer = 0f
                sprite.alpha = 1f
            }
        }

        // --- Death Check (with revival) ---
        if (!health.isAlive && !deathFired) {
            if (stats.revivalCount > 0) {
                stats.revivalCount--
                val reviveHP = (health.maxHealth * 0.3f).toInt().coerceAtLeast(1)
                health.setHealth(reviveHP)
                health.triggerInvincibility(2f)
            } else {
                deathFired = true
                onPlayerDeath?.invoke()
            }
        }
    }

    /** Reset system state for a new game or revive */
    fun reset() {
        deathFired = false
        lastFacingDirection = 1
        lastAimDirection = Vector2(1f, 0f)
        flashTimer = 0f
        regenAccumulator = 0f
    }
}
