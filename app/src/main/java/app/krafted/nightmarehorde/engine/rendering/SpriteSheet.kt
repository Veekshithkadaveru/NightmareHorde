package app.krafted.nightmarehorde.engine.rendering

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

/**
 * Represents a sprite sheet (texture atlas) that can be divided into frames.
 * Used for animations and efficient texture packing.
 */
class SpriteSheet(
    val bitmap: ImageBitmap,
    val frameWidth: Int,
    val frameHeight: Int,
    val columns: Int = bitmap.width / frameWidth,
    val rows: Int = bitmap.height / frameHeight
) {
    /** Total number of frames in the sprite sheet */
    val frameCount: Int = columns * rows
    
    /** Cached frame rectangles for fast lookup */
    private val frameRects: Array<Rect> = Array(frameCount) { index ->
        val col = index % columns
        val row = index / columns
        Rect(
            left = (col * frameWidth).toFloat(),
            top = (row * frameHeight).toFloat(),
            right = ((col + 1) * frameWidth).toFloat(),
            bottom = ((row + 1) * frameHeight).toFloat()
        )
    }
    
    /**
     * Get the source rectangle for a frame by index.
     */
    fun getFrameRect(frameIndex: Int): Rect {
        val safeIndex = frameIndex.coerceIn(0, frameCount - 1)
        return frameRects[safeIndex]
    }
    
    /**
     * Get the source rectangle for a frame by row and column.
     */
    fun getFrameRect(row: Int, col: Int): Rect {
        val index = row * columns + col
        return getFrameRect(index)
    }
    
    /**
     * Get the IntOffset and IntSize for drawing with Canvas.
     * Useful for drawImage with srcOffset and srcSize parameters.
     */
    fun getFrameIntParams(frameIndex: Int): Pair<IntOffset, IntSize> {
        val safeIndex = frameIndex.coerceIn(0, frameCount - 1)
        val col = safeIndex % columns
        val row = safeIndex / columns
        return Pair(
            IntOffset(col * frameWidth, row * frameHeight),
            IntSize(frameWidth, frameHeight)
        )
    }
}
