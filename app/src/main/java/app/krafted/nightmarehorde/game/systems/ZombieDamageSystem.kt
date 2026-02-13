package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.AIComponent
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import kotlin.math.sqrt

/**
 * Handles zombie-to-player contact damage.
 * When a zombie overlaps the player, the player takes damage based on the zombie's baseDamage.
 * Respects the player's invincibility frames from HealthComponent.
 */
class ZombieDamageSystem : GameSystem(priority = 95) {

    companion object {
        /** Damage tick interval: zombies deal damage at most this often (seconds) */
        const val DAMAGE_TICK_INTERVAL = 0.5f
    }

    private var damageCooldown: Float = 0f

    override fun update(deltaTime: Float, entities: List<Entity>) {
        damageCooldown -= deltaTime

        val player = entities.find { it.hasComponent(PlayerTagComponent::class) } ?: return
        val playerTransform = player.getComponent(TransformComponent::class) ?: return
        val playerHealth = player.getComponent(HealthComponent::class) ?: return
        val playerCollider = player.getComponent(ColliderComponent::class) ?: return
        val playerStats = player.getComponent(StatsComponent::class)

        if (!playerHealth.isAlive || playerHealth.isInvincible) return
        if (damageCooldown > 0f) return

        val playerRadius = (playerCollider.collider as? Collider.Circle)?.radius ?: return

        for (entity in entities) {
            if (!entity.isActive) continue
            val ai = entity.getComponent(AIComponent::class) ?: continue
            val stats = entity.getComponent(StatsComponent::class) ?: continue
            val transform = entity.getComponent(TransformComponent::class) ?: continue
            val collider = entity.getComponent(ColliderComponent::class) ?: continue

            val enemyRadius = (collider.collider as? Collider.Circle)?.radius ?: continue

            val dx = playerTransform.x - transform.x
            val dy = playerTransform.y - transform.y
            val distSq = dx * dx + dy * dy
            val radiusSum = playerRadius + enemyRadius

            if (distSq <= radiusSum * radiusSum) {
                val armor = playerStats?.armor ?: 0
                val damage = (stats.baseDamage * stats.damageMultiplier).toInt()
                playerHealth.takeDamage(damage, armor)
                damageCooldown = DAMAGE_TICK_INTERVAL
                break // Only one hit per tick
            }
        }
    }
}
