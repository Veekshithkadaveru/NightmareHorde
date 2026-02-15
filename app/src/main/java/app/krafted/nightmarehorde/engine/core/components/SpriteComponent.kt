package app.krafted.nightmarehorde.engine.core.components

import androidx.compose.ui.graphics.Color
import app.krafted.nightmarehorde.engine.core.Component

/**
 * Component for sprite rendering information.
 * Entities with this component will be rendered by the SpriteRenderer.
 */
data class SpriteComponent(
    /** Key to look up the texture in AssetManager */
    var textureKey: String,
    /** Render layer - lower values render first (background) */
    var layer: Int = 0,
    /** Whether this sprite should be rendered */
    var visible: Boolean = true,
    /** Tint color applied to the sprite */
    var tint: Color = Color.White,
    /** Width of the sprite in world units (0 = use texture width) */
    var width: Float = 0f,
    /** Height of the sprite in world units (0 = use texture height) */
    var height: Float = 0f,
    /** Animation state key (null = static sprite) */
    var animationKey: String? = null,
    /** Current animation frame index */
    var currentFrame: Int = 0,
    /** Frame width in pixels (for sprite sheets) */
    var frameWidth: Int = 0,
    /** Frame height in pixels (for sprite sheets) */
    var frameHeight: Int = 0,
    /** Horizontal flip */
    var flipX: Boolean = false,
    /** Vertical flip */
    var flipY: Boolean = false,
    /** Alpha transparency (0 = invisible, 1 = opaque) */
    var alpha: Float = 1f,
    /** If true, sprite fills the entire viewport (ignores width/height) */
    var fillViewport: Boolean = false,
    /** Total number of animation frames in this sprite sheet (0 = unknown/static) */
    var totalFrames: Int = 0
) : Component
