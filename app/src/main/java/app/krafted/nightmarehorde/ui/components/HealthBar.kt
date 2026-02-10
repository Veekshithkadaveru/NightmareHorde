package app.krafted.nightmarehorde.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * HUD health bar displaying the player's current and max health.
 * Color transitions from green → yellow → red as health decreases.
 */
@Composable
fun HealthBar(
    currentHealth: Int,
    maxHealth: Int,
    modifier: Modifier = Modifier
) {
    val healthPercent = if (maxHealth > 0) currentHealth.toFloat() / maxHealth else 0f

    // Color based on health percentage
    val barColor = when {
        healthPercent > 0.6f -> Color(0xFF4CAF50)  // Green
        healthPercent > 0.3f -> Color(0xFFFFC107)  // Yellow/amber
        else -> Color(0xFFF44336)                   // Red
    }

    val backgroundColor = Color(0xFF2A2A2A)
    val borderColor = Color(0xFF555555)

    Box(
        modifier = modifier
            .width(180.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cornerRadius = CornerRadius(6.dp.toPx())

            // Background
            drawRoundRect(
                color = backgroundColor,
                cornerRadius = cornerRadius,
                size = size
            )

            // Health fill
            if (healthPercent > 0f) {
                drawRoundRect(
                    color = barColor,
                    cornerRadius = cornerRadius,
                    size = Size(size.width * healthPercent, size.height)
                )
            }

            // Border
            drawRoundRect(
                color = borderColor,
                cornerRadius = cornerRadius,
                size = size,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Health text
        Text(
            text = "$currentHealth / $maxHealth",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
