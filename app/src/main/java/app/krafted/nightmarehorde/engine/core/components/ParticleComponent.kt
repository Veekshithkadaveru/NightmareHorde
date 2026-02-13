package app.krafted.nightmarehorde.engine.core.components

import androidx.compose.ui.graphics.Color
import app.krafted.nightmarehorde.engine.core.Component

data class ParticleComponent(
    val color: Color,
    val size: Float,
    val lifeTime: Float,
    var timeAlive: Float = 0f,
    val fadeOut: Boolean = true,
    /** When > 0, renders as a rotated elongated oval instead of a circle.
     *  width = length along the rotation axis, height = thickness perpendicular. */
    val width: Float = 0f,
    val height: Float = 0f
) : Component
