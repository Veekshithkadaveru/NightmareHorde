package app.krafted.nightmarehorde.engine.physics

/**
 * Collision shape types for physics detection.
 * Used by ColliderComponent to define entity collision bounds.
 */
sealed class Collider {
    
    /**
     * Circular collider - best for characters, projectiles, and pickups.
     * @param radius The radius of the circle in world units
     */
    data class Circle(val radius: Float) : Collider() {
        init {
            require(radius > 0f) { "Circle radius must be positive" }
        }
    }
    
    /**
     * Axis-Aligned Bounding Box - best for walls, platforms, and rectangular objects.
     * @param width The width of the box (centered on entity position)
     * @param height The height of the box (centered on entity position)
     */
    data class AABB(val width: Float, val height: Float) : Collider() {
        val halfWidth: Float get() = width / 2f
        val halfHeight: Float get() = height / 2f
        
        init {
            require(width > 0f && height > 0f) { "AABB dimensions must be positive" }
        }
    }
    
    companion object {
        /**
         * Check if two circles are colliding.
         * @return true if circles overlap
         */
        fun circleVsCircle(
            x1: Float, y1: Float, r1: Float,
            x2: Float, y2: Float, r2: Float
        ): Boolean {
            val dx = x2 - x1
            val dy = y2 - y1
            val distanceSquared = dx * dx + dy * dy
            val radiusSum = r1 + r2
            return distanceSquared <= radiusSum * radiusSum
        }
        
        /**
         * Check if two AABBs are colliding.
         * @return true if boxes overlap
         */
        fun aabbVsAabb(
            x1: Float, y1: Float, hw1: Float, hh1: Float,
            x2: Float, y2: Float, hw2: Float, hh2: Float
        ): Boolean {
            return kotlin.math.abs(x1 - x2) <= hw1 + hw2 &&
                   kotlin.math.abs(y1 - y2) <= hh1 + hh2
        }
        
        /**
         * Check if a circle and AABB are colliding.
         * @return true if they overlap
         */
        fun circleVsAabb(
            cx: Float, cy: Float, radius: Float,
            bx: Float, by: Float, halfWidth: Float, halfHeight: Float
        ): Boolean {
            // Find closest point on AABB to circle center
            val closestX = cx.coerceIn(bx - halfWidth, bx + halfWidth)
            val closestY = cy.coerceIn(by - halfHeight, by + halfHeight)
            
            val dx = cx - closestX
            val dy = cy - closestY
            
            return (dx * dx + dy * dy) <= radius * radius
        }
    }
}
