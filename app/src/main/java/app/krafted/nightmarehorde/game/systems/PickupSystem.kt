package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.PickupTagComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import kotlin.math.sin
import kotlin.math.sqrt

class PickupSystem : GameSystem(priority = 25) {

    companion object {
        const val MAGNET_RADIUS = 100f
        const val MAGNET_SPEED = 300f
        const val SNAP_DISTANCE = 15f
        const val BOB_AMPLITUDE = 3f
        const val BOB_SPEED = 4f
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        var playerTransform: TransformComponent? = null
        var playerStats: StatsComponent? = null
        for (entity in entities) {
            if (entity.hasComponent(PlayerTagComponent::class)) {
                playerTransform = entity.getComponent(TransformComponent::class)
                playerStats = entity.getComponent(StatsComponent::class)
                break
            }
        }
        if (playerTransform == null) return

        val px = playerTransform.x
        val py = playerTransform.y
        val magnetRadius = playerStats?.pickupRadius ?: MAGNET_RADIUS

        for (entity in entities) {
            if (!entity.isActive) continue
            val pickupTag = entity.getComponent(PickupTagComponent::class) ?: continue
            val transform = entity.getComponent(TransformComponent::class) ?: continue
            val velocity = entity.getComponent(VelocityComponent::class)

            pickupTag.timeAlive += deltaTime
            if (pickupTag.timeAlive >= pickupTag.despawnAfterSeconds) {
                entity.isActive = false
                continue
            }

            val dx = px - transform.x
            val dy = py - transform.y
            val distSq = dx * dx + dy * dy
            val magnetRadiusSq = magnetRadius * magnetRadius

            if (distSq < magnetRadiusSq && velocity != null) {
                val dist = sqrt(distSq)
                if (dist < SNAP_DISTANCE) {
                    transform.x = px
                    transform.y = py
                    velocity.stop()
                } else {
                    val dirX = dx / dist
                    val dirY = dy / dist
                    velocity.vx = dirX * MAGNET_SPEED
                    velocity.vy = dirY * MAGNET_SPEED
                }
            } else {
                // Bobbing effect when not being attracted
                transform.y = pickupTag.baseY + sin(pickupTag.timeAlive * BOB_SPEED) * BOB_AMPLITUDE
                velocity?.stop()
            }
        }
    }
}
