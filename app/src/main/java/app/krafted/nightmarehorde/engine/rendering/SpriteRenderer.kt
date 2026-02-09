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
            // Simple frustum culling
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
        
        // Calculate sprite size
        // If width/height is 0, use the actual bitmap dimensions (or frame dimensions if animated)
        // This fixes P1 issue where sprites would disappear or use arbitrary 64f
        var srcWidth = bitmap.width
        var srcHeight = bitmap.height
        var srcOffsetX = 0
        var srcOffsetY = 0
        
        // Handle Animation / Sprite Sheet
        // If sprite has a valid layer/frame index, we might need to look up the frame
        // But SpriteComponent currently just has 'currentFrame'. We need to know the frame dimensions.
        // We can infer this if we have a way to get the sprite sheet.
        // For now, we'll assume if animationKey is set, we try to get a sprite sheet.
        // Or if the sprite component had a reference to the sheet.
        // Since SpriteComponent is POD, it relies on external data.
        // Let's assume if width/height are set and < bitmap size, it might be a sheet? No, that's ambiguous.
        // Ideally, SpriteComponent should hold the frame rect or we look it up.
        // Let's check AssetManager for a sprite sheet if animationKey is present, OR if we just want a frame.
        // Simpler approach for now: if currentFrame > 0 or animationKey is not null, try to find a sheet.
        // But we don't know the frame size to request the sheet from AssetManager!
        // Design Refinement: SpriteComponent should probably store frameWidth/Height if it's using a sheet.
        // For this fix, let's assume if width/height are provided, they MIGHT be the frame size used for rendering too?
        // Actually, width/height in SpriteComponent are *world* dimensions.
        
        // Let's assume standard 1:1 pixel to world unit mapping if not specified.
        
        // Handle fillViewport - sprite fills entire screen
        val spriteWidth: Float
        val spriteHeight: Float
        if (data.sprite.fillViewport) {
            // Use screen dimensions directly (no zoom applied, covers full viewport)
            spriteWidth = camera.screenWidth / camera.zoom
            spriteHeight = camera.screenHeight / camera.zoom
        } else {
            spriteWidth = if (data.sprite.width > 0) data.sprite.width else srcWidth.toFloat()
            spriteHeight = if (data.sprite.height > 0) data.sprite.height else srcHeight.toFloat()
        }
        
        // Animation Logic
        if (data.sprite.currentFrame >= 0 && data.sprite.frameWidth > 0 && data.sprite.frameHeight > 0) {
            // We have frame info, so we can calculate the source rect
            // We don't strictly need the SpriteSheet object if we just want to render a specific frame 
            // and we know the math. SpriteSheet class assumes we have the bitmap object to create it.
            // Here we can just do the math directly to avoid object creation/lookup if we want, 
            // OR use AssetManager to get/create the sheet.
            // Let's use the helper in AssetManager to cache the sheet for efficiency if we were doing more,
            // but direct math is faster here than hash map lookups if we just need one rect.
            
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

        
        // Convert world position to screen position
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
    
    private fun isVisible(
        transform: TransformComponent,
        sprite: SpriteComponent,
        bounds: Camera.VisibleBounds
    ): Boolean {
        // fillViewport sprites are always visible
        if (sprite.fillViewport) return true
        
        // Use actual size if available, fallback to a reasonable default if 0 (though now we handle 0 above)
        // But here we don't have the bitmap, so we use a safe default or 0
        val width = if (sprite.width > 0) sprite.width else 64f // Approximation for culling
        val height = if (sprite.height > 0) sprite.height else 64f
        
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
