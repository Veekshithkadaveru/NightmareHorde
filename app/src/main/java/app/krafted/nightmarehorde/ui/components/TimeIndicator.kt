package app.krafted.nightmarehorde.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.game.systems.DayNightCycle

/**
 * A compact HUD indicator showing the current day/night phase.
 * Displays a sun or moon icon, phase label, and a thin progress bar.
 */
@Composable
fun TimeIndicator(
    phase: DayNightCycle.TimePhase,
    phaseProgress: Float,
    nightIntensity: Float,
    modifier: Modifier = Modifier
) {
    val phaseLabel = when (phase) {
        DayNightCycle.TimePhase.DAY -> "DAY"
        DayNightCycle.TimePhase.DUSK -> "DUSK"
        DayNightCycle.TimePhase.NIGHT -> "NIGHT"
        DayNightCycle.TimePhase.DAWN -> "DAWN"
    }

    val phaseColor by animateColorAsState(
        targetValue = when (phase) {
            DayNightCycle.TimePhase.DAY -> Color(0xFFFFD54F)    // warm yellow
            DayNightCycle.TimePhase.DUSK -> Color(0xFFFF8A65)   // orange
            DayNightCycle.TimePhase.NIGHT -> Color(0xFF90CAF9)  // light blue
            DayNightCycle.TimePhase.DAWN -> Color(0xFFFFAB91)   // light orange
        },
        animationSpec = tween(durationMillis = 800),
        label = "phaseColor"
    )

    val bgAlpha = 0.6f + nightIntensity * 0.2f

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .background(
                Color.Black.copy(alpha = bgAlpha),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        // Sun or Moon icon
        CelestialIcon(
            phase = phase,
            nightIntensity = nightIntensity,
            color = phaseColor,
            modifier = Modifier.size(18.dp)
        )

        // Phase label
        Text(
            text = phaseLabel,
            color = phaseColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        // Progress bar
        PhaseProgressBar(
            progress = phaseProgress,
            color = phaseColor,
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
        )
    }
}

/**
 * Draws a sun (circle with rays) or moon (crescent) based on the current phase.
 */
@Composable
private fun CelestialIcon(
    phase: DayNightCycle.TimePhase,
    nightIntensity: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2f * 0.6f

        if (nightIntensity < 0.5f) {
            // Sun: circle + rays
            drawCircle(color = color, radius = radius, center = Offset(cx, cy), style = Fill)

            // Draw 8 rays
            val rayLength = radius * 0.55f
            val rayStart = radius * 1.3f
            for (i in 0 until 8) {
                val angle = Math.toRadians(i * 45.0).toFloat()
                val startX = cx + kotlin.math.cos(angle) * rayStart
                val startY = cy + kotlin.math.sin(angle) * rayStart
                val endX = cx + kotlin.math.cos(angle) * (rayStart + rayLength)
                val endY = cy + kotlin.math.sin(angle) * (rayStart + rayLength)
                drawLine(
                    color = color,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.5f,
                    cap = StrokeCap.Round
                )
            }
        } else {
            // Moon: draw full circle then clip with offset dark circle for crescent
            drawCircle(color = color, radius = radius, center = Offset(cx, cy), style = Fill)
            drawCircle(
                color = Color.Black.copy(alpha = 0.85f),
                radius = radius * 0.85f,
                center = Offset(cx + radius * 0.45f, cy - radius * 0.2f),
                style = Fill
            )
        }
    }
}

/**
 * A thin horizontal progress bar for the current phase.
 */
@Composable
private fun PhaseProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val barHeight = size.height
        val barWidth = size.width

        // Background track
        drawRoundRect(
            color = Color.White.copy(alpha = 0.15f),
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2f)
        )

        // Filled progress
        val fillWidth = barWidth * progress.coerceIn(0f, 1f)
        if (fillWidth > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(color.copy(alpha = 0.7f), color)
                ),
                size = size.copy(width = fillWidth),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2f)
            )
        }
    }
}
