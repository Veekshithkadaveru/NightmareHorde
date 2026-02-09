package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component

/**
 * Component for entity position, rotation, and scale in world space.
 * All entities that need to be rendered or participate in physics should have this.
 */
class TransformComponent(
    var x: Float = 0f,
    var y: Float = 0f,
    var rotation: Float = 0f, // in radians
    var scale: Float = 1f
) : Component {
    
    override fun toString(): String {
        return "TransformComponent(x=$x, y=$y, rotation=$rotation, scale=$scale)"
    }

    
    fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }
    
    fun translate(dx: Float, dy: Float) {
        x += dx
        y += dy
    }
    
    fun distanceTo(other: TransformComponent): Float {
        val dx = other.x - x
        val dy = other.y - y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
    
    fun distanceSquaredTo(other: TransformComponent): Float {
        val dx = other.x - x
        val dy = other.y - y
        return dx * dx + dy * dy
    }
}
