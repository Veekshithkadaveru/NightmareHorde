package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component

/**
 * Component for tracking entity health, armor, and invincibility state.
 * Used by PlayerSystem for damage/death logic and by HealthBar UI for display.
 */
class HealthComponent(
    var maxHealth: Int = 100
) : Component {

    var currentHealth: Int = maxHealth
        private set

    /** Whether the entity is still alive */
    val isAlive: Boolean get() = currentHealth > 0

    /** Health as a percentage (0f to 1f) */
    val healthPercent: Float get() = if (maxHealth > 0) currentHealth.toFloat() / maxHealth else 0f

    /** Invincibility state — prevents taking damage for a brief period after being hit */
    var isInvincible: Boolean = false
        private set

    /** Duration of invincibility in seconds after taking a hit */
    var invincibilityDuration: Float = 0.5f

    /** Remaining invincibility time in seconds */
    private var invincibilityTimer: Float = 0f

    /**
     * Apply damage, optionally reduced by armor. Triggers invincibility frames.
     * @param amount raw incoming damage
     * @param armor flat armor value to subtract (sourced from StatsComponent)
     * @return actual damage dealt after armor reduction
     */
    fun takeDamage(amount: Int, armor: Int = 0): Int {
        if (!isAlive || isInvincible) return 0

        val effectiveDamage = (amount - armor).coerceAtLeast(1)
        currentHealth = (currentHealth - effectiveDamage).coerceAtLeast(0)

        // Start invincibility frames (only if enabled — zombies have duration 0)
        if (invincibilityDuration > 0f) {
            isInvincible = true
            invincibilityTimer = invincibilityDuration
        }

        return effectiveDamage
    }

    /**
     * Heal the entity, clamped to max health.
     * Negative amounts are ignored for safety.
     */
    fun heal(amount: Int) {
        if (!isAlive || amount <= 0) return
        currentHealth = (currentHealth + amount).coerceAtMost(maxHealth)
    }

    /**
     * Set health directly (e.g., for initialization or revive).
     */
    fun setHealth(health: Int) {
        currentHealth = health.coerceIn(0, maxHealth)
    }

    /**
     * Update invincibility timer. Called each frame by PlayerSystem.
     */
    fun updateInvincibility(deltaTime: Float) {
        if (isInvincible) {
            invincibilityTimer -= deltaTime
            if (invincibilityTimer <= 0f) {
                isInvincible = false
                invincibilityTimer = 0f
            }
        }
    }

    override fun toString(): String {
        return "HealthComponent(hp=$currentHealth/$maxHealth, invincible=$isInvincible)"
    }
}
