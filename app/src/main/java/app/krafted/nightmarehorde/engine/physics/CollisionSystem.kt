package app.krafted.nightmarehorde.engine.physics

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.TransformComponent

/**
 * System for detecting and handling collisions between entities.
 * Uses SpatialHashGrid for efficient broad-phase detection.
 */
class CollisionSystem(
    private val spatialGrid: SpatialHashGrid = SpatialHashGrid()
) : GameSystem(priority = 60) {  // Run after MovementSystem (50)
    
    /** Callback invoked when two entities collide */
    var onCollision: ((CollisionEvent) -> Unit)? = null
    
    /** Set of collision pairs already processed this frame (to avoid duplicates) */
    private val processedPairs = mutableSetOf<Long>()
    
    override fun update(deltaTime: Float, entities: List<Entity>) {
        // Clear state from previous frame
        spatialGrid.clear()
        processedPairs.clear()
        
        // Phase 1: Populate spatial grid with collidable entities
        entities.forEach { entity ->
            if (entity.hasComponent(ColliderComponent::class) && 
                entity.hasComponent(TransformComponent::class)) {
                spatialGrid.insert(entity)
            }
        }
        
        // Phase 2: Check collisions using spatial queries
        entities.forEach { entityA ->
            val transformA = entityA.getComponent(TransformComponent::class) ?: return@forEach
            val colliderA = entityA.getComponent(ColliderComponent::class) ?: return@forEach
            
            // Query nearby entities from spatial grid
            val queryRadius = getQueryRadius(colliderA)
            val nearby = spatialGrid.query(transformA.x, transformA.y, queryRadius)
            
            nearby.forEach { entityB ->
                // Skip self-collision
                if (entityA.id == entityB.id) return@forEach
                
                // Skip if already processed (A,B or B,A)
                val pairKey = makePairKey(entityA.id, entityB.id)
                if (pairKey in processedPairs) return@forEach
                processedPairs.add(pairKey)
                
                val transformB = entityB.getComponent(TransformComponent::class) ?: return@forEach
                val colliderB = entityB.getComponent(ColliderComponent::class) ?: return@forEach
                
                // Check layer compatibility
                if (!shouldCollide(colliderA, colliderB)) return@forEach
                
                // Narrow-phase: actual collision test
                if (checkCollision(transformA, colliderA, transformB, colliderB)) {
                    onCollision?.invoke(
                        CollisionEvent(entityA, entityB, colliderA.layer, colliderB.layer)
                    )
                }
            }
        }
    }
    
    /**
     * Get the radius to use for spatial grid queries based on collider type.
     */
    private fun getQueryRadius(colliderComp: ColliderComponent): Float {
        return when (val collider = colliderComp.collider) {
            is Collider.Circle -> collider.radius * 2f // Query double radius to catch nearby
            is Collider.AABB -> kotlin.math.max(collider.halfWidth, collider.halfHeight) * 2f
        }
    }
    
    /**
     * Check if two collider components should test for collision based on layers.
     * 
     * Design Decision: Uses OR logic (A wants B OR B wants A) rather than AND logic.
     * This allows asymmetric collision setups, for example:
     * - Player projectiles can hit enemies (projectile wants enemy)
     * - Even if enemies don't explicitly list projectiles in their collision mask
     * 
     * For stricter collision control, change to AND logic:
     *   return a.shouldCollideWith(b.layer) && b.shouldCollideWith(a.layer)
     */
    private fun shouldCollide(a: ColliderComponent, b: ColliderComponent): Boolean {
        return a.shouldCollideWith(b.layer) || b.shouldCollideWith(a.layer)
    }
    
    /**
     * Perform narrow-phase collision detection between two entities.
     */
    private fun checkCollision(
        transformA: TransformComponent, colliderA: ColliderComponent,
        transformB: TransformComponent, colliderB: ColliderComponent
    ): Boolean {
        val shapeA = colliderA.collider
        val shapeB = colliderB.collider
        
        return when {
            // Circle vs Circle
            shapeA is Collider.Circle && shapeB is Collider.Circle -> {
                Collider.circleVsCircle(
                    transformA.x, transformA.y, shapeA.radius,
                    transformB.x, transformB.y, shapeB.radius
                )
            }
            // AABB vs AABB
            shapeA is Collider.AABB && shapeB is Collider.AABB -> {
                Collider.aabbVsAabb(
                    transformA.x, transformA.y, shapeA.halfWidth, shapeA.halfHeight,
                    transformB.x, transformB.y, shapeB.halfWidth, shapeB.halfHeight
                )
            }
            // Circle vs AABB
            shapeA is Collider.Circle && shapeB is Collider.AABB -> {
                Collider.circleVsAabb(
                    transformA.x, transformA.y, shapeA.radius,
                    transformB.x, transformB.y, shapeB.halfWidth, shapeB.halfHeight
                )
            }
            // AABB vs Circle (swap and call)
            shapeA is Collider.AABB && shapeB is Collider.Circle -> {
                Collider.circleVsAabb(
                    transformB.x, transformB.y, shapeB.radius,
                    transformA.x, transformA.y, shapeA.halfWidth, shapeA.halfHeight
                )
            }
            else -> false
        }
    }
    
    /**
     * Create a unique key for an entity pair (order-independent).
     */
    private fun makePairKey(idA: Long, idB: Long): Long {
        val min = minOf(idA, idB)
        val max = maxOf(idA, idB)
        return (min shl 32) or (max and 0xFFFFFFFFL)
    }
    
    /**
     * Collision event data passed to callback.
     */
    data class CollisionEvent(
        val entityA: Entity,
        val entityB: Entity,
        val layerA: CollisionLayer,
        val layerB: CollisionLayer
    )
}
