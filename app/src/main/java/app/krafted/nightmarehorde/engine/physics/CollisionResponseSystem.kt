package app.krafted.nightmarehorde.engine.physics

import android.util.Log
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Resolves collisions by pushing moving entities out of static obstacles.
 *
 * Runs AFTER MovementSystem (priority 55) so that positions have been updated.
 * Iterates all moving entities (PLAYER, ENEMY) and checks for overlap against
 * all OBSTACLE entities, applying minimum translation vector (MTV) push-back.
 *
 * Obstacles (OBSTACLE layer) are always treated as immovable.
 */
class CollisionResponseSystem : GameSystem(priority = 55) {

    private companion object {
        const val TAG = "CollisionResponse"
    }

    // Reusable buffers to avoid per-frame allocations
    private data class CollisionEntry(val entity: Entity, val transform: TransformComponent, val collider: ColliderComponent)
    private val obstacles = ArrayList<CollisionEntry>(64)
    private val movers = ArrayList<CollisionEntry>(128)

    override fun update(deltaTime: Float, entities: List<Entity>) {
        // Collect obstacles once per frame using reusable buffers
        obstacles.clear()
        movers.clear()

        for (entity in entities) {
            val transform = entity.getComponent(TransformComponent::class) ?: continue
            val collider = entity.getComponent(ColliderComponent::class) ?: continue

            when (collider.layer) {
                CollisionLayer.OBSTACLE -> {
                    if (!collider.isTrigger) {
                        obstacles.add(CollisionEntry(entity, transform, collider))
                    }
                }
                CollisionLayer.PLAYER, CollisionLayer.ENEMY -> {
                    movers.add(CollisionEntry(entity, transform, collider))
                }
                else -> { /* skip */ }
            }
        }

        if (obstacles.isEmpty() || movers.isEmpty()) return

        // For each mover, check against all nearby obstacles
        for (mover in movers) {
            for (obs in obstacles) {
                // Quick distance check to skip far obstacles
                val dx = mover.transform.x - obs.transform.x
                val dy = mover.transform.y - obs.transform.y
                val distSq = dx * dx + dy * dy
                val maxRange = getMaxExtent(mover.collider) + getMaxExtent(obs.collider)
                if (distSq > maxRange * maxRange) continue

                resolveOverlap(mover.transform, mover.collider, obs.transform, obs.collider)
            }
        }
    }

    private fun getMaxExtent(collider: ColliderComponent): Float {
        return when (val shape = collider.collider) {
            is Collider.Circle -> shape.radius
            is Collider.AABB -> maxOf(shape.halfWidth, shape.halfHeight)
        }
    }

    /**
     * Push the moving entity out of the obstacle using MTV (minimum translation vector).
     */
    private fun resolveOverlap(
        movingTransform: TransformComponent,
        movingCollider: ColliderComponent,
        obstacleTransform: TransformComponent,
        obstacleCollider: ColliderComponent
    ) {
        val movingShape = movingCollider.collider
        val obstacleShape = obstacleCollider.collider

        when {
            // Circle (player/enemy) vs AABB (obstacle) — most common case
            movingShape is Collider.Circle && obstacleShape is Collider.AABB -> {
                resolveCircleVsAABB(
                    movingTransform, movingShape.radius,
                    obstacleTransform, obstacleShape.halfWidth, obstacleShape.halfHeight
                )
            }
            // Circle vs Circle
            movingShape is Collider.Circle && obstacleShape is Collider.Circle -> {
                resolveCircleVsCircle(
                    movingTransform, movingShape.radius,
                    obstacleTransform, obstacleShape.radius
                )
            }
            // AABB vs AABB
            movingShape is Collider.AABB && obstacleShape is Collider.AABB -> {
                resolveAABBvsAABB(
                    movingTransform, movingShape.halfWidth, movingShape.halfHeight,
                    obstacleTransform, obstacleShape.halfWidth, obstacleShape.halfHeight
                )
            }
            // AABB (moving) vs Circle (obstacle)
            movingShape is Collider.AABB && obstacleShape is Collider.Circle -> {
                // Treat circle obstacle as AABB approximation
                resolveAABBvsAABB(
                    movingTransform, movingShape.halfWidth, movingShape.halfHeight,
                    obstacleTransform, obstacleShape.radius, obstacleShape.radius
                )
            }
        }
    }

    private fun resolveCircleVsAABB(
        circleTransform: TransformComponent, radius: Float,
        boxTransform: TransformComponent, halfW: Float, halfH: Float
    ) {
        // Find the closest point on the AABB to the circle center
        val closestX = circleTransform.x.coerceIn(
            boxTransform.x - halfW, boxTransform.x + halfW
        )
        val closestY = circleTransform.y.coerceIn(
            boxTransform.y - halfH, boxTransform.y + halfH
        )

        val dx = circleTransform.x - closestX
        val dy = circleTransform.y - closestY
        val distSq = dx * dx + dy * dy

        if (distSq < radius * radius && distSq > 0.0001f) {
            val dist = sqrt(distSq)
            val overlap = radius - dist
            // Push circle away from closest point
            val nx = dx / dist
            val ny = dy / dist
            circleTransform.x += nx * overlap
            circleTransform.y += ny * overlap
        } else if (distSq <= 0.0001f) {
            // Circle center is inside the AABB — push out via shortest axis
            val overlapX = halfW + radius - abs(circleTransform.x - boxTransform.x)
            val overlapY = halfH + radius - abs(circleTransform.y - boxTransform.y)
            if (overlapX < overlapY) {
                circleTransform.x += if (circleTransform.x < boxTransform.x) -overlapX else overlapX
            } else {
                circleTransform.y += if (circleTransform.y < boxTransform.y) -overlapY else overlapY
            }
        }
    }

    private fun resolveCircleVsCircle(
        movingTransform: TransformComponent, movingRadius: Float,
        obstacleTransform: TransformComponent, obstacleRadius: Float
    ) {
        val dx = movingTransform.x - obstacleTransform.x
        val dy = movingTransform.y - obstacleTransform.y
        val distSq = dx * dx + dy * dy
        val minDist = movingRadius + obstacleRadius

        if (distSq < minDist * minDist && distSq > 0.0001f) {
            val dist = sqrt(distSq)
            val overlap = minDist - dist
            val nx = dx / dist
            val ny = dy / dist
            movingTransform.x += nx * overlap
            movingTransform.y += ny * overlap
        } else if (distSq <= 0.0001f) {
            // Directly overlapping — push in arbitrary direction
            movingTransform.x += minDist
        }
    }

    private fun resolveAABBvsAABB(
        movingTransform: TransformComponent, mHalfW: Float, mHalfH: Float,
        obstacleTransform: TransformComponent, oHalfW: Float, oHalfH: Float
    ) {
        val overlapX = (mHalfW + oHalfW) - abs(movingTransform.x - obstacleTransform.x)
        val overlapY = (mHalfH + oHalfH) - abs(movingTransform.y - obstacleTransform.y)

        if (overlapX > 0 && overlapY > 0) {
            if (overlapX < overlapY) {
                movingTransform.x += if (movingTransform.x < obstacleTransform.x) -overlapX else overlapX
            } else {
                movingTransform.y += if (movingTransform.y < obstacleTransform.y) -overlapY else overlapY
            }
        }
    }
}
