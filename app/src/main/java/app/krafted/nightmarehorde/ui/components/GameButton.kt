package app.krafted.nightmarehorde.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The shared chrome behind every menu button: a beveled panel painted with [fill],
 * a [borderColor] frame, an optional [innerScrim] for a faux inner shadow, and a
 * centered [content] slot. Each call site supplies its own sizing via [modifier]
 * and its own label as [content], so the look stays identical to the hand-rolled
 * buttons it replaces.
 */
@Composable
fun GameButton(
    onClick: () -> Unit,
    fill: Brush,
    borderColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = CutCornerShape(8.dp),
    borderWidth: Dp = 2.dp,
    innerScrim: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(fill)
            .border(borderWidth, borderColor, shape)
            .then(if (innerScrim != null) Modifier.background(innerScrim) else Modifier)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

/**
 * A gentle "breathing" scale (1f..[target]) for drawing attention to primary
 * actions. Returns 1f when [enabled] is false so callers can toggle the pulse
 * without changing composition structure.
 */
@Composable
fun rememberPulseScale(target: Float = 1.05f, enabled: Boolean = true): Float {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = target,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    return if (enabled) scale else 1f
}
