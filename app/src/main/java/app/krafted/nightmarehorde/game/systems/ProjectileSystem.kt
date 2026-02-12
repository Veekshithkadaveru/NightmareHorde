package app.krafted.nightmarehorde.game.systems

import android.util.Log
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.ProjectileComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.physics.CollisionSystem
import javax.inject.Inject

class ProjectileSystem(
    private val gameLoop: GameLoop,
    private val collisionSystem: CollisionSystem
) : GameSystem(priority = 45) { // Run before physics but after weapon? Or after physics? 
    // Distance check should happen after movement (priority 50). So priority 55? 
    // Or just check in update. Velocity * dt approximates distance.

    init {
        collisionSystem.addCollisionListener { event ->
            handleCollision(event.entityA, event.layerB)
            handleCollision(event.entityB, event.layerA)
        }
    }

    private fun handleCollision(entity: Entity, otherLayer: CollisionLayer) {
        val projectile = entity.getComponent(ProjectileComponent::class) ?: return
        
        // Obstacles stop all projectiles
        if (otherLayer == CollisionLayer.OBSTACLE) {
             gameLoop.removeEntity(entity)
             return
        }

        // Enemies stop non-penetrating projectiles
        if (otherLayer == CollisionLayer.ENEMY) {
            if (!projectile.penetrating) {
                gameLoop.removeEntity(entity)
            }
            // Logic to apply damage to enemy will be added here or in CombatSystem
        }
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        entities.forEach { entity ->
            val projectile = entity.getComponent(ProjectileComponent::class) ?: return@forEach
            val velocity = entity.getComponent(VelocityComponent::class) ?: return@forEach

            // Time-based lifetime check (for stationary projectiles like whip)
            if (projectile.maxLifetime > 0f) {
                projectile.timeAlive += deltaTime
                if (projectile.timeAlive >= projectile.maxLifetime) {
                    gameLoop.removeEntity(entity)
                    return@forEach
                }
                
                // --- Visual Effects (Growth & Fade) ---
                if (projectile.growthRate != 0f) {
                    val transform = entity.getComponent(TransformComponent::class)
                    if (transform != null) {
                        transform.scale += projectile.growthRate * deltaTime
                    }
                }
                
                if (projectile.fadeRate != 0f) {
                    val sprite = entity.getComponent(SpriteComponent::class)
                    if (sprite != null) {
                        sprite.alpha = (sprite.alpha - projectile.fadeRate * deltaTime).coerceAtLeast(0f)
                    }
                }
            }

            // Distance-based lifetime check
            val speed = kotlin.math.sqrt(velocity.vx * velocity.vx + velocity.vy * velocity.vy)
            val step = speed * deltaTime

            projectile.distanceTraveled += step

            if (projectile.distanceTraveled >= projectile.maxDistance) {
                gameLoop.removeEntity(entity)
            }
        }
    }
}
