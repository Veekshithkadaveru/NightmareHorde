package app.krafted.nightmarehorde.engine.core

import kotlin.math.sqrt

/**
 * Represents a 2D direction/position vector.
 * Used throughout the engine for positions, directions, and velocities.
 */
data class Vector2(
    val x: Float = 0f,
    val y: Float = 0f
) {
    companion object {
        val ZERO = Vector2(0f, 0f)
        val UP = Vector2(0f, -1f)
        val DOWN = Vector2(0f, 1f)
        val LEFT = Vector2(-1f, 0f)
        val RIGHT = Vector2(1f, 0f)
    }
    
    /**
     * Returns the length (magnitude) of this vector.
     */
    fun length(): Float = sqrt(x * x + y * y)
    
    /**
     * Returns the squared length of this vector.
     * More efficient than length() when only comparing magnitudes.
     */
    fun lengthSquared(): Float = x * x + y * y
    
    /**
     * Returns a normalized (unit length) version of this vector.
     * Returns ZERO if the vector has zero length.
     */
    fun normalized(): Vector2 {
        val len = length()
        return if (len > 0f) Vector2(x / len, y / len) else ZERO
    }
    
    /**
     * Returns the distance to another vector.
     */
    fun distanceTo(other: Vector2): Float {
        val dx = other.x - x
        val dy = other.y - y
        return sqrt(dx * dx + dy * dy)
    }
    
    /**
     * Returns the dot product with another vector.
     */
    fun dot(other: Vector2): Float = x * other.x + y * other.y
    
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)
    operator fun times(scalar: Float) = Vector2(x * scalar, y * scalar)
    operator fun div(scalar: Float) = Vector2(x / scalar, y / scalar)
    operator fun unaryMinus() = Vector2(-x, -y)
}
