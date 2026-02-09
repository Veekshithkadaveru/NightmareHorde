package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component

/**
 * Component for entity movement velocity.
 * Used by MovementSystem to update position based on velocity and delta time.
 * 
 * Uses plain class (not data class) to match TransformComponent conventions,
 * avoiding unnecessary object allocation from copy() and allowing mutable state.
 */
class VelocityComponent(
    var vx: Float = 0f,
    var vy: Float = 0f
) : Component {
    
    /** Magnitude of the velocity vector */
    val speed: Float
        get() = kotlin.math.sqrt(vx * vx + vy * vy)
    
    /** Set velocity from angle (radians) and speed */
    fun setFromAngle(angleRadians: Float, speed: Float) {
        vx = kotlin.math.cos(angleRadians) * speed
        vy = kotlin.math.sin(angleRadians) * speed
    }
    
    /** Set velocity components directly */
    fun set(vx: Float, vy: Float) {
        this.vx = vx
        this.vy = vy
    }
    
    /** Multiply velocity by a scalar */
    fun scale(factor: Float) {
        vx *= factor
        vy *= factor
    }
    
    /** Stop all movement */
    fun stop() {
        vx = 0f
        vy = 0f
    }
    
    /** Normalize velocity to unit vector, then scale by given speed */
    fun normalize(targetSpeed: Float = 1f) {
        val currentSpeed = speed
        if (currentSpeed > 0.0001f) {
            vx = (vx / currentSpeed) * targetSpeed
            vy = (vy / currentSpeed) * targetSpeed
        }
    }
    
    override fun toString(): String {
        return "VelocityComponent(vx=$vx, vy=$vy)"
    }
}
