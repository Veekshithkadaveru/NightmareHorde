package app.krafted.nightmarehorde.engine.rendering

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.game.data.AssetManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders sprites to a Compose Canvas.
 * Handles layer ordering, transforms, and sprite animations.
 */
@Singleton
class SpriteRenderer @Inject constructor(
    private val assetManager: AssetManager
) {
    companion object {
        /**
         * Fallback size for frustum culling when sprite dimensions are unknown.
         * This is a conservative estimate assuming most sprites are under 64 world units.
         * Used only when SpriteComponent.width/height are not set (0).
         */
        private const val SIZE_FALLBACK_CULLING = 64f
    }
    
    /**
     * Render all entities with sprites to the given DrawScope.
     * @param drawScope The Compose Canvas DrawScope
     * @param entities List of entities to render
     * @param camera Camera for world-to-screen transforms
     */
    fun render(
        drawScope: DrawScope,
        entities: List<Entity>,
        camera: Camera
    ) {
        // Filter to entities with both transform and sprite, then sort by layer
        // Optimization: In a real engine, we'd maintain a sorted Z-list or use a dedicated render system
        // providing a pre-sorted list. For now, we accept the sort overhead.
        val renderables = entities
            .mapNotNull { entity ->
                val transform = entity.getComponent(TransformComponent::class)
                val sprite = entity.getComponent(SpriteComponent::class)
                if (transform != null && sprite != null && sprite.visible) {
                    RenderData(entity, transform, sprite)
                } else null
            }
            .sortedBy { it.sprite.layer }
        
        // Get visible bounds for culling
        val visibleBounds = camera.getVisibleBounds()
        
        // Render each sprite
        renderables.forEach { data ->
            // Simple frustum culling (fillViewport sprites always pass)
            if (!isVisible(data.transform, data.sprite, visibleBounds)) return@forEach
            
            renderSprite(drawScope, data, camera)
        }
    }
    
    private fun renderSprite(
        drawScope: DrawScope,
        data: RenderData,
        camera: Camera
    ) {
        val bitmap = assetManager.getBitmap(data.sprite.textureKey) ?: return
        
        // FillViewport sprites use special tiling logic
        if (data.sprite.fillViewport) {
            renderTilingBackground(drawScope, bitmap, camera, data.sprite.alpha)
            return
        }
        
        // Source rect defaults to full bitmap
        var srcWidth = bitmap.width
        var srcHeight = bitmap.height
        var srcOffsetX = 0
        var srcOffsetY = 0
        
        // Animation / Sprite Sheet — calculate source rect FIRST so display size uses frame dimensions
        if (data.sprite.currentFrame >= 0 && data.sprite.frameWidth > 0 && data.sprite.frameHeight > 0) {
            val cols = bitmap.width / data.sprite.frameWidth
            if (cols > 0) {
                val col = data.sprite.currentFrame % cols
                val row = data.sprite.currentFrame / cols
                
                srcOffsetX = col * data.sprite.frameWidth
                srcOffsetY = row * data.sprite.frameHeight
                srcWidth = data.sprite.frameWidth
                srcHeight = data.sprite.frameHeight
            }
        }
        
        // Calculate display size and screen position
        val spriteWidth = if (data.sprite.width > 0) data.sprite.width else srcWidth.toFloat()
        val spriteHeight = if (data.sprite.height > 0) data.sprite.height else srcHeight.toFloat()
        val (screenX, screenY) = camera.worldToScreen(data.transform.x, data.transform.y)
        
        // Calculate screen size with zoom
        val screenWidth = spriteWidth * camera.zoom
        val screenHeight = spriteHeight * camera.zoom
        
        drawScope.withTransform({
            // Translate to position (centered on entity position)
            translate(
                left = screenX - screenWidth / 2f,
                top = screenY - screenHeight / 2f
            )
            
            // Apply rotation around center
            if (data.transform.rotation != 0f) {
                rotate(
                    degrees = Math.toDegrees(data.transform.rotation.toDouble()).toFloat(),
                    pivot = Offset(screenWidth / 2f, screenHeight / 2f)
                )
            }
            
            // Apply entity scale
            if (data.transform.scale != 1f) {
                scale(
                    scaleX = data.transform.scale * if (data.sprite.flipX) -1f else 1f,
                    scaleY = data.transform.scale * if (data.sprite.flipY) -1f else 1f,
                    pivot = Offset(screenWidth / 2f, screenHeight / 2f)
                )
            } else if (data.sprite.flipX || data.sprite.flipY) {
                scale(
                    scaleX = if (data.sprite.flipX) -1f else 1f,
                    scaleY = if (data.sprite.flipY) -1f else 1f,
                    pivot = Offset(screenWidth / 2f, screenHeight / 2f)
                )
            }
        }) {
            // Apply color tint and alpha
            val colorFilter = if (data.sprite.tint != androidx.compose.ui.graphics.Color.White) {
                ColorFilter.tint(data.sprite.tint)
            } else null
            
            // Draw the bitmap
            drawImage(
                image = bitmap,
                srcOffset = IntOffset(srcOffsetX, srcOffsetY),
                srcSize = IntSize(srcWidth, srcHeight),
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(screenWidth.toInt(), screenHeight.toInt()),
                alpha = data.sprite.alpha,
                colorFilter = colorFilter
            )
        }
    }
    
    /**
     * Renders a tiling background that scrolls with the camera.
     * Tiles the bitmap across the entire screen, offset by camera position,
     * giving the VS-style infinite scrolling ground effect.
     */
    private fun renderTilingBackground(
        drawScope: DrawScope,
        bitmap: androidx.compose.ui.graphics.ImageBitmap,
        camera: Camera,
        alpha: Float
    ) {
        val tileW = bitmap.width.toFloat()
        val tileH = bitmap.height.toFloat()
        
        if (tileW <= 0 || tileH <= 0) return
        
        val screenW = camera.screenWidth
        val screenH = camera.screenHeight
        
        // Calculate the offset so tiles scroll opposite to camera movement
        // Negate camera position: when camera moves RIGHT, tiles scroll LEFT
        val rawX = (-camera.x * camera.zoom) % tileW
        val rawY = (-camera.y * camera.zoom) % tileH
        val offsetX = if (rawX > 0) rawX - tileW else if (rawX < -tileW + 1) rawX + tileW else rawX
        val offsetY = if (rawY > 0) rawY - tileH else if (rawY < -tileH + 1) rawY + tileH else rawY
        
        // Draw enough tiles to cover the screen
        var x = offsetX
        while (x < screenW) {
            var y = offsetY
            while (y < screenH) {
                drawScope.drawImage(
                    image = bitmap,
                    dstOffset = IntOffset(x.toInt(), y.toInt()),
                    dstSize = IntSize(tileW.toInt(), tileH.toInt()),
                    alpha = alpha
                )
                y += tileH
            }
            x += tileW
        }
    }
    
    private fun isVisible(
        transform: TransformComponent,
        sprite: SpriteComponent,
        bounds: Camera.VisibleBounds
    ): Boolean {
        // fillViewport sprites are always visible
        if (sprite.fillViewport) return true
        
        // Use actual size if available, fallback to conservative estimate for culling
        val width = if (sprite.width > 0) sprite.width else SIZE_FALLBACK_CULLING
        val height = if (sprite.height > 0) sprite.height else SIZE_FALLBACK_CULLING
        
        val halfWidth = width / 2f
        val halfHeight = height / 2f
        
        return transform.x + halfWidth >= bounds.left &&
               transform.x - halfWidth <= bounds.right &&
               transform.y + halfHeight >= bounds.top &&
               transform.y - halfHeight <= bounds.bottom
    }
    
    private data class RenderData(
        val entity: Entity,
        val transform: TransformComponent,
        val sprite: SpriteComponent
    )
}
