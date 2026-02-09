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
            
            // Outer ring (stroke)
            drawCircle(
                color = outerColor.copy(alpha = if (isDragging) outerAlpha * 1.5f else outerAlpha),
                radius = outerRadiusPx,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )
            
            // Outer fill (very subtle)
            drawCircle(
                color = outerColor.copy(alpha = outerAlpha * 0.2f),
                radius = outerRadiusPx,
                center = center
            )
            
            // Dead zone indicator (subtle inner ring)
            val deadZoneRadius = outerRadiusPx * deadZone
            drawCircle(
                color = outerColor.copy(alpha = outerAlpha * 0.15f),
                radius = deadZoneRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
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
                            
                            // Calculate new potential offset
                            val newOffset = knobOffset + dragAmount
                            val distance = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)
                            val maxDistance = outerRadiusPx - innerRadiusPx
                            
                            // Clamp to within outer ring bounds
                            knobOffset = if (distance > maxDistance) {
                                val angle = atan2(newOffset.y, newOffset.x)
                                Offset(
                                    cos(angle) * maxDistance,
                                    sin(angle) * maxDistance
                                )
                            } else {
                                newOffset
                            }
                            
                            // Calculate normalized magnitude (0 to 1)
                            val normalizedMagnitude = (distance / maxDistance).coerceIn(0f, 1f)
                            
                            // Apply dead zone - only emit if outside dead zone
                            if (normalizedMagnitude > deadZone) {
                                // Remap from [deadZone, 1] to [0, 1] for smooth response
                                val remappedMagnitude = (normalizedMagnitude - deadZone) / (1f - deadZone)
                                val angle = atan2(newOffset.y, newOffset.x)
                                
                                val normalizedX = cos(angle) * remappedMagnitude
                                val normalizedY = sin(angle) * remappedMagnitude
                                
                                onDirectionChange(Vector2(normalizedX, normalizedY))
                            } else {
                                // Inside dead zone - no movement
                                onDirectionChange(Vector2.ZERO)
                            }
                        }
                    )
                }
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            
            // Inner knob fill
            drawCircle(
                color = innerColor.copy(alpha = if (isDragging) innerAlpha * 1.3f else innerAlpha),
                radius = innerRadiusPx,
                center = center
            )
            
            // Inner knob highlight
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = innerRadiusPx * 0.6f,
                center = Offset(center.x - innerRadiusPx * 0.1f, center.y - innerRadiusPx * 0.1f)
            )
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

