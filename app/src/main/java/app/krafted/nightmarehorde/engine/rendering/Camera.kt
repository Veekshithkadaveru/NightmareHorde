package app.krafted.nightmarehorde.engine.rendering

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Camera for 2D game world.
 * Handles smooth following, zoom, and coordinate transformations.
 */
@Singleton
class Camera @Inject constructor() {
    
    /** Camera position in world coordinates */
    var x: Float = 0f
        private set
    var y: Float = 0f
        private set
    
    /** Zoom level (1.0 = normal, 2.0 = 2x zoom) */
    var zoom: Float = 1f
    
    /** Screen dimensions (set by GameSurface) */
    var screenWidth: Float = 0f
    var screenHeight: Float = 0f
    
    /** World bounds for camera clamping */
    var worldMinX: Float = Float.NEGATIVE_INFINITY
    var worldMaxX: Float = Float.POSITIVE_INFINITY
    var worldMinY: Float = Float.NEGATIVE_INFINITY
    var worldMaxY: Float = Float.POSITIVE_INFINITY
    
    /** Smoothing factor for camera follow (0 = instant, 1 = never) */
    var followSmoothing: Float = 0.1f
    
    private var targetX: Float = 0f
    private var targetY: Float = 0f
    
    /**
     * Update camera to follow a target entity.
     * Uses lerp for smooth following.
     */
    fun follow(entity: Entity, deltaTime: Float) {
        val transform = entity.getComponent(TransformComponent::class) ?: return
        follow(transform.x, transform.y, deltaTime)
    }
    
    /**
     * Update camera to follow a target position.
     */
    fun follow(targetX: Float, targetY: Float, deltaTime: Float) {
        this.targetX = targetX
        this.targetY = targetY
        
        // Lerp towards target (frame-rate independent)
        // followSmoothing: 0 = instant, higher = smoother (slower)
        // Using formula: current = lerp(current, target, 1 - exp(-speed * dt))
        // To make "smoothing" intuitive: speed = 1 / smoothing (avoid div by zero)
        val speed = if (followSmoothing <= 0.001f) 1000f else 1f / followSmoothing
        val lerpFactor = 1f - kotlin.math.exp(-speed * deltaTime)
        
        x = lerp(x, targetX, lerpFactor)
        y = lerp(y, targetY, lerpFactor)
        
        // Clamp to world bounds
        clampToBounds()
    }
    
    /**
     * Instantly set camera position without smoothing.
     */
    fun setPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
        this.targetX = x
        this.targetY = y
        clampToBounds()
    }
    
    /**
     * Set world bounds for camera clamping.
     */
    fun setWorldBounds(minX: Float, minY: Float, maxX: Float, maxY: Float) {
        worldMinX = minX
        worldMinY = minY
        worldMaxX = maxX
        worldMaxY = maxY
    }
    
    /**
     * Convert screen coordinates to world coordinates.
     */
    fun screenToWorld(screenX: Float, screenY: Float): Pair<Float, Float> {
        val worldX = (screenX - screenWidth / 2f) / zoom + x
        val worldY = (screenY - screenHeight / 2f) / zoom + y
        return Pair(worldX, worldY)
    }
    
    /**
     * Convert world coordinates to screen coordinates.
     */
    fun worldToScreen(worldX: Float, worldY: Float): Pair<Float, Float> {
        val screenX = (worldX - x) * zoom + screenWidth / 2f
        val screenY = (worldY - y) * zoom + screenHeight / 2f
        return Pair(screenX, screenY)
    }
    
    /**
     * Get the visible world bounds based on camera position and screen size.
     */
    fun getVisibleBounds(): VisibleBounds {
        val halfWidth = screenWidth / (2f * zoom)
        val halfHeight = screenHeight / (2f * zoom)
        return VisibleBounds(
            left = x - halfWidth,
            top = y - halfHeight,
            right = x + halfWidth,
            bottom = y + halfHeight
        )
    }
    
    private fun clampToBounds() {
        val halfWidth = screenWidth / (2f * zoom)
        val halfHeight = screenHeight / (2f * zoom)
        
        // If world is smaller than screen, center the camera
        if (worldMaxX - worldMinX < halfWidth * 2) {
            x = (worldMinX + worldMaxX) / 2f
        } else {
            x = x.coerceIn(worldMinX + halfWidth, worldMaxX - halfWidth)
        }
        
        if (worldMaxY - worldMinY < halfHeight * 2) {
            y = (worldMinY + worldMaxY) / 2f
        } else {
            y = y.coerceIn(worldMinY + halfHeight, worldMaxY - halfHeight)
        }
    }
    
    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }
    
    data class VisibleBounds(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    ) {
        fun contains(x: Float, y: Float): Boolean {
            return x in left..right && y in top..bottom
        }
    }
}
