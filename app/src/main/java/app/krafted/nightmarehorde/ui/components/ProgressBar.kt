package app.krafted.nightmarehorde.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A horizontal rounded progress bar: a [trackColor] background with a [fill] that
 * spans [progress] (0..1) of the width, plus an optional border. Drawn on a single
 * [Canvas] so every HUD bar and stat bar shares identical geometry.
 *
 * A gradient [fill] is resolved against the filled rect, so it sweeps across the
 * filled portion rather than the whole track — matching a fraction-width fill box.
 */
@Composable
fun ProgressBar(
    progress: Float,
    fill: Brush,
    trackColor: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 4.dp,
    borderColor: Color? = null,
    borderWidth: Dp = 1.5.dp,
) {
    val clamped = progress.coerceIn(0f, 1f)
    Canvas(modifier = modifier.clip(RoundedCornerShape(cornerRadius))) {
        val radius = CornerRadius(cornerRadius.toPx())
        drawRoundRect(color = trackColor, cornerRadius = radius, size = size)
        if (clamped > 0f) {
            drawRoundRect(
                brush = fill,
                cornerRadius = radius,
                size = Size(size.width * clamped, size.height)
            )
        }
        borderColor?.let {
            drawRoundRect(
                color = it,
                cornerRadius = radius,
                size = size,
                style = Stroke(width = borderWidth.toPx())
            )
        }
    }
}
