package app.krafted.nightmarehorde.engine.physics

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent

/**
 * System that applies velocity to entity positions.
 * Uses delta time for frame-rate independent movement.
 */
class MovementSystem : GameSystem(priority = 50) {
    
    /** Optional world bounds for clamping entity positions */
    var worldBounds: WorldBounds? = null
    
    /** Friction applied to velocity each frame (0 = no friction, 1 = instant stop) */
    var friction: Float = 0f
    
    override fun update(deltaTime: Float, entities: List<Entity>) {
        entities.forEach { entity ->
            val transform = entity.getComponent(TransformComponent::class) ?: return@forEach
            val velocity = entity.getComponent(VelocityComponent::class) ?: return@forEach
            
            // Apply velocity to position (v * dt)
            transform.x += velocity.vx * deltaTime
            transform.y += velocity.vy * deltaTime
            
            // Apply friction if set
            if (friction > 0f) {
                val frictionMultiplier = 1f - (friction * deltaTime).coerceIn(0f, 1f)
                velocity.vx *= frictionMultiplier
                velocity.vy *= frictionMultiplier
                
                // Stop completely if velocity is very small
                if (velocity.speed < 0.01f) {
                    velocity.stop()
                }
            }
            
            // Clamp to world bounds if set
            worldBounds?.let { bounds ->
                transform.x = transform.x.coerceIn(bounds.minX, bounds.maxX)
                transform.y = transform.y.coerceIn(bounds.minY, bounds.maxY)
            }
        }
    }
    
    /**
     * World bounds for constraining entity movement.
     */
    data class WorldBounds(
        val minX: Float,
        val minY: Float,
        val maxX: Float,
        val maxY: Float
    )
}
