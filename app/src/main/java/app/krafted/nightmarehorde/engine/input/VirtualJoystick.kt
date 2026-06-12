package app.krafted.nightmarehorde.engine.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import app.krafted.nightmarehorde.engine.core.Vector2
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Virtual joystick composable for player movement control.
 * 
 * Renders a semi-transparent outer ring with a draggable inner knob.
 * The knob follows touch input within the ring's bounds and returns
 * a normalized direction vector to the callback.
 * 
 * Includes a configurable dead zone to prevent accidental drift from resting thumbs.
 * 
 * @param onDirectionChange Callback with normalized direction vector (-1 to 1)
 * @param onRelease Callback when joystick is released
 * @param modifier Modifier for the joystick container
 * @param outerRadius Radius of the outer ring
 * @param innerRadius Radius of the inner knob
 * @param deadZone Dead zone as fraction of max distance (0-1), default 0.12 (12%)
 * @param outerColor Color of the outer ring
 * @param innerColor Color of the inner knob
 * @param outerAlpha Alpha of the outer ring
 * @param innerAlpha Alpha of the inner knob
 */
@Composable
fun VirtualJoystick(
    onDirectionChange: (Vector2) -> Unit,
    onRelease: () -> Unit,
    modifier: Modifier = Modifier,
    outerRadius: Dp = 60.dp,
    innerRadius: Dp = 25.dp,
    deadZone: Float = 0.12f,
    outerColor: Color = Color.White,
    innerColor: Color = Color.White,
    outerAlpha: Float = 0.3f,
    innerAlpha: Float = 0.6f
) {
    val density = LocalDensity.current
    val outerRadiusPx = with(density) { outerRadius.toPx() }
    val innerRadiusPx = with(density) { innerRadius.toPx() }
    
    // Knob offset from center
    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .size(outerRadius * 2)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer ring
        Canvas(
            modifier = Modifier.size(outerRadius * 2)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            drawJoystickBase(center, outerRadiusPx, deadZone, outerColor, outerAlpha, active = isDragging)
        }
        
        // Inner knob
        Canvas(
            modifier = Modifier
                .size(innerRadius * 2)
                .offset { IntOffset(knobOffset.x.toInt(), knobOffset.y.toInt()) }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                        },
                        onDragEnd = {
                            isDragging = false
                            knobOffset = Offset.Zero
                            onRelease()
                        },
                        onDragCancel = {
                            isDragging = false
                            knobOffset = Offset.Zero
                            onRelease()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()

                            val maxDistance = outerRadiusPx - innerRadiusPx
                            knobOffset = clampToRadius(knobOffset + dragAmount, maxDistance)
                            onDirectionChange(computeJoystickDirection(knobOffset, maxDistance, deadZone))
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            drawJoystickKnob(center, innerRadiusPx, innerColor, innerAlpha, active = isDragging)
        }
    }
}

/**
 * Joystick that attaches to an InputManager.
 * Convenience wrapper that automatically updates the InputManager.
 * 
 * @param inputManager InputManager to receive direction updates
 * @param modifier Modifier for positioning
 * @param outerRadius Outer ring radius
 * @param innerRadius Inner knob radius
 * @param deadZone Dead zone as fraction (0-1), default 0.12 (12%)
 */
@Composable
fun VirtualJoystick(
    inputManager: InputManager,
    modifier: Modifier = Modifier,
    outerRadius: Dp = 60.dp,
    innerRadius: Dp = 25.dp,
    deadZone: Float = 0.12f
) {
    VirtualJoystick(
        onDirectionChange = { direction ->
            inputManager.updateMovementDirection(direction)
        },
        onRelease = {
            inputManager.releaseJoystick()
        },
        modifier = modifier,
        outerRadius = outerRadius,
        innerRadius = innerRadius,
        deadZone = deadZone
    )
}

/** Clamps [offset] to lie within [maxDistance] of the origin, preserving its direction. */
internal fun clampToRadius(offset: Offset, maxDistance: Float): Offset {
    val distance = sqrt(offset.x * offset.x + offset.y * offset.y)
    return if (distance > maxDistance) {
        val angle = atan2(offset.y, offset.x)
        Offset(cos(angle) * maxDistance, sin(angle) * maxDistance)
    } else {
        offset
    }
}

/**
 * Converts a clamped knob offset into a normalized direction vector, applying the dead
 * zone and remapping magnitude from `[deadZone, 1]` to `[0, 1]` for a smooth response.
 */
internal fun computeJoystickDirection(
    knobOffset: Offset,
    maxDistance: Float,
    deadZone: Float
): Vector2 {
    val distance = sqrt(knobOffset.x * knobOffset.x + knobOffset.y * knobOffset.y)
    val normalizedMagnitude = (distance / maxDistance).coerceIn(0f, 1f)
    if (normalizedMagnitude <= deadZone) return Vector2.ZERO

    val remappedMagnitude = (normalizedMagnitude - deadZone) / (1f - deadZone)
    val angle = atan2(knobOffset.y, knobOffset.x)
    return Vector2(cos(angle) * remappedMagnitude, sin(angle) * remappedMagnitude)
}

/** Draws the joystick base: outer ring stroke, subtle fill, and dead-zone indicator ring. */
internal fun DrawScope.drawJoystickBase(
    center: Offset,
    outerRadiusPx: Float,
    deadZone: Float,
    color: Color,
    alpha: Float,
    active: Boolean
) {
    drawCircle(
        color = color.copy(alpha = if (active) alpha * 1.5f else alpha),
        radius = outerRadiusPx,
        center = center,
        style = Stroke(width = 3.dp.toPx())
    )
    drawCircle(
        color = color.copy(alpha = alpha * 0.2f),
        radius = outerRadiusPx,
        center = center
    )
    drawCircle(
        color = color.copy(alpha = alpha * 0.15f),
        radius = outerRadiusPx * deadZone,
        center = center,
        style = Stroke(width = 1.dp.toPx())
    )
}

/** Draws the joystick knob: filled disc plus an off-centre highlight. */
internal fun DrawScope.drawJoystickKnob(
    center: Offset,
    innerRadiusPx: Float,
    color: Color,
    alpha: Float,
    active: Boolean
) {
    drawCircle(
        color = color.copy(alpha = if (active) alpha * 1.3f else alpha),
        radius = innerRadiusPx,
        center = center
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = innerRadiusPx * 0.6f,
        center = Offset(center.x - innerRadiusPx * 0.1f, center.y - innerRadiusPx * 0.1f)
    )
}

