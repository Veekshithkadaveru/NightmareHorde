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
            renderTilingBackground(drawScope, bitmap, camera, data)
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
        data: RenderData
    ) {
        val scale = if (data.transform.scale > 0f) data.transform.scale else 1f
        val overallScale = scale * camera.zoom
        
        if (overallScale <= 0f) return
        
        // Exact pixel size on screen BEFORE any rounding
        val trueTileW = bitmap.width.toFloat() * overallScale
        val trueTileH = bitmap.height.toFloat() * overallScale
        
        // By rounding up the size slightly (adding 1 pixel), we ensure no gaps between tiles
        // when DrawScope internally snaps Rects to pixel grids during rasterization.
        val drawW = kotlin.math.ceil(trueTileW).toInt() + 1
        val drawH = kotlin.math.ceil(trueTileH).toInt() + 1
        
        val screenW = camera.screenWidth
        val screenH = camera.screenHeight
        
        // Camera position relative to the center of the screen
        val originX = (screenW / 2f) - (camera.x * camera.zoom)
        val originY = (screenH / 2f) - (camera.y * camera.zoom)
        
        // Modulo against true float widths so scrolling remains perfectly smooth
        val rawOffsetX = originX % trueTileW
        val rawOffsetY = originY % trueTileH
        
        val startX = if (rawOffsetX > 0) rawOffsetX - trueTileW else rawOffsetX
        val startY = if (rawOffsetY > 0) rawOffsetY - trueTileH else rawOffsetY
        
        var x = startX
        while (x < screenW) {
            var y = startY
            while (y < screenH) {
                // Notice we cast x, y to Int to snap to integer pixels, 
                // but draw size is artificially 1px larger to cover any rounding gap exactly
                drawScope.drawImage(
                    image = bitmap,
                    dstOffset = IntOffset(x.toInt(), y.toInt()),
                    dstSize = IntSize(drawW, drawH),
                    alpha = data.sprite.alpha
                )
                y += trueTileH
            }
            x += trueTileW
        }
    }
    
    private fun isVisible(
        transform: TransformComponent,
        sprite: SpriteComponent,
        bounds: Camera.VisibleBounds
    ): Boolean {
        // fillViewport sprites are always visible
        if (sprite.fillViewport) return true

        // Determine effective display size:
        // 1. Use explicit width/height if set
        // 2. Fall back to frameWidth/frameHeight for sprite sheets
        // 3. Fall back to conservative estimate
        val baseWidth = when {
            sprite.width > 0 -> sprite.width
            sprite.frameWidth > 0 -> sprite.frameWidth.toFloat()
            else -> SIZE_FALLBACK_CULLING
        }
        val baseHeight = when {
            sprite.height > 0 -> sprite.height
            sprite.frameHeight > 0 -> sprite.frameHeight.toFloat()
            else -> SIZE_FALLBACK_CULLING
        }

        // Account for entity scale (use at least 1x so we never cull too aggressively)
        val scale = if (transform.scale > 1f) transform.scale else 1f
        val halfWidth = (baseWidth * scale) / 2f
        val halfHeight = (baseHeight * scale) / 2f

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
