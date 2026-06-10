package app.krafted.nightmarehorde.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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

    // Green when healthy, amber when wounded, red when critical — a quick danger read.
    val barColor = when {
        healthPercent > 0.6f -> Color(0xFF4CAF50)
        healthPercent > 0.3f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Box(
        modifier = modifier
            .width(180.dp)
            .height(24.dp),
        contentAlignment = Alignment.Center
    ) {
        ProgressBar(
            progress = healthPercent,
            fill = SolidColor(barColor),
            trackColor = Color(0xFF2A2A2A),
            cornerRadius = 6.dp,
            borderColor = Color(0xFF555555),
            modifier = Modifier.matchParentSize()
        )

        Text(
            text = "$currentHealth / $maxHealth",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
