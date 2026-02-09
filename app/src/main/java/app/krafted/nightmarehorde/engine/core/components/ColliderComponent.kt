package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.engine.physics.Collider

/**
 * Component that gives an entity a collision shape.
 * Entities with this component will be tested for collisions by CollisionSystem.
 */
data class ColliderComponent(
    val collider: Collider,
    val layer: CollisionLayer = CollisionLayer.DEFAULT,
    val collidesWithLayers: Set<CollisionLayer> = setOf(CollisionLayer.DEFAULT),
    var isTrigger: Boolean = false  // If true, detects collision but doesn't block movement
) : Component {
    
    /**
     * Check if this collider should test against another layer.
     */
    fun shouldCollideWith(otherLayer: CollisionLayer): Boolean {
        return collidesWithLayers.contains(otherLayer)
    }
    
    companion object {
        /** Create a circle collider with default layer */
        fun circle(radius: Float, layer: CollisionLayer = CollisionLayer.DEFAULT): ColliderComponent {
            return ColliderComponent(
                collider = Collider.Circle(radius),
                layer = layer,
                collidesWithLayers = CollisionLayer.entries.toSet()
            )
        }
        
        /** Create an AABB collider with default layer */
        fun box(width: Float, height: Float, layer: CollisionLayer = CollisionLayer.DEFAULT): ColliderComponent {
            return ColliderComponent(
                collider = Collider.AABB(width, height),
                layer = layer,
                collidesWithLayers = CollisionLayer.entries.toSet()
            )
        }
    }
}

/**
 * Collision layers for filtering which entities can collide.
 */
enum class CollisionLayer {
    DEFAULT,      // Generic entities
    PLAYER,       // Player character
    ENEMY,        // Enemies (zombies)
    PROJECTILE,   // Bullets, rockets, etc.
    PICKUP,       // Ammo, health, XP orbs
    TURRET,       // Player turrets
    OBSTACLE      // Walls, barriers
}
