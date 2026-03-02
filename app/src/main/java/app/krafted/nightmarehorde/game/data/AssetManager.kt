package app.krafted.nightmarehorde.game.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.krafted.nightmarehorde.engine.rendering.SpriteSheet
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages loading and caching of game assets (textures, sprite sheets).
 * All textures are loaded from the res/drawable folder.
 */
@Singleton
class AssetManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /** Cache of loaded ImageBitmaps by texture key */
    private val bitmapCache = mutableMapOf<String, ImageBitmap>()
    
    /** Cache of sprite sheets by key */
    private val spriteSheetCache = mutableMapOf<String, SpriteSheet>()
    
    /**
     * Get a bitmap by resource name (without extension).
     * Loads and caches the bitmap if not already loaded.
     */
    fun getBitmap(key: String): ImageBitmap? {
        return bitmapCache.getOrPut(key) {
            loadBitmapFromResources(key) ?: return null
        }
    }
    
    /**
     * Get or create a sprite sheet from a texture.
     * @param key Texture resource name
     * @param frameWidth Width of each frame in pixels
     * @param frameHeight Height of each frame in pixels
     */
    fun getSpriteSheet(key: String, frameWidth: Int, frameHeight: Int): SpriteSheet? {
        val sheetKey = "$key-${frameWidth}x${frameHeight}"
        return spriteSheetCache.getOrPut(sheetKey) {
            val bitmap = getBitmap(key) ?: return null
            SpriteSheet(bitmap, frameWidth, frameHeight)
        }
    }
    
    /**
     * Preload multiple textures to avoid loading during gameplay.
     */
    fun preload(vararg keys: String) {
        keys.forEach { key ->
            getBitmap(key)
        }
    }
    
    /**
     * Clear all cached assets.
     * Call this when transitioning between game states that use different assets.
     */
    fun clearCache() {
        // SpriteSheet references bitmaps from bitmapCache, so clear them first
        spriteSheetCache.clear()
        bitmapCache.clear()
    }
    
    /**
     * Check if a texture is already loaded.
     */
    fun isLoaded(key: String): Boolean {
        return bitmapCache.containsKey(key)
    }
    
    private fun loadBitmapFromResources(key: String): ImageBitmap? {
        return try {
            val resId = context.resources.getIdentifier(
                key,
                "drawable",
                context.packageName
            )
            if (resId == 0) {
                // Try mipmap if drawable fails (common for launcher icons)
                val mipmapId = context.resources.getIdentifier(
                    key,
                    "mipmap",
                    context.packageName
                )
                if (mipmapId != 0) {
                    return loadFromDrawable(mipmapId)
                }
                
                android.util.Log.w("AssetManager", "Resource not found: $key")
                return null
            }
            
            // Decode with explicit ARGB_8888 to guarantee alpha channel preservation
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            val androidBitmap = BitmapFactory.decodeResource(context.resources, resId, options)
            if (androidBitmap != null) {
                return androidBitmap.asImageBitmap()
            }
            
            // Fallback: It might be a VectorDrawable
            return loadFromDrawable(resId)
        } catch (e: Exception) {
            android.util.Log.e("AssetManager", "Failed to load bitmap: $key", e)
            null
        }
    }
    
    private fun loadFromDrawable(resId: Int): ImageBitmap? {
        val drawable = androidx.core.content.ContextCompat.getDrawable(context, resId) ?: return null
        
        val bitmap = android.graphics.Bitmap.createBitmap(
            drawable.intrinsicWidth.takeIf { it > 0 } ?: 64,
            drawable.intrinsicHeight.takeIf { it > 0 } ?: 64,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        
        // asImageBitmap() wraps the Android Bitmap - do NOT recycle the source bitmap
        // as it would invalidate the ImageBitmap. The bitmap will be GC'd with the cache.
        return bitmap.asImageBitmap()
    }
}
