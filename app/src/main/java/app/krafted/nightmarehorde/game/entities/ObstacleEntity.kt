package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.ObstacleTagComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import app.krafted.nightmarehorde.game.data.ObstacleType

/**
 * Factory for creating obstacle entities (rocks, trees, barrels, etc.).
 * Obstacles are static, non-destructible props that block player and enemy movement.
 */
object ObstacleEntity {

    /**
     * Create an obstacle entity at the given world position.
     *
     * @param type The obstacle variant — determines sprite, collider
     * @param x World X position
     * @param y World Y position
     */
    fun create(type: ObstacleType, x: Float, y: Float): Entity {
        return Entity().apply {
            addComponent(ObstacleTagComponent(type))

            addComponent(TransformComponent(
                x = x,
                y = y,
                scale = type.scale
            ))

            // Static obstacle — uses full individual PNG, no animation
            addComponent(SpriteComponent(
                textureKey = type.textureKey,
                layer = 1,  // Same layer as entities
                width = type.spriteWidth,
                height = type.spriteHeight
            ))

            // Box collider for solid blocking
            // NOTE: Collider.AABB takes FULL width/height and halves internally
            addComponent(ColliderComponent(
                collider = Collider.AABB(
                    type.colliderHalfWidth * 2f,
                    type.colliderHalfHeight * 2f
                ),
                layer = CollisionLayer.OBSTACLE,
                collidesWithLayers = setOf(
                    CollisionLayer.PLAYER,
                    CollisionLayer.ENEMY
                ),
                isTrigger = false
            ))
        }
    }
}
