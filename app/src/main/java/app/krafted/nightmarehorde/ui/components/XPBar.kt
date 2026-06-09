package app.krafted.nightmarehorde.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact XP progress bar displayed beneath the health bar in the HUD.
 * Shows current level and progress toward the next level.
 */
@Composable
fun XPBar(
    xpProgress: Float,
    currentLevel: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "Lv$currentLevel",
            color = Color(0xFF42A5F5),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 4.dp)
        )

        ProgressBar(
            progress = xpProgress,
            fill = Brush.horizontalGradient(listOf(Color(0xFF1E88E5), Color(0xFF42A5F5))),
            trackColor = Color(0xFF1A1A2E),
            cornerRadius = 4.dp,
            modifier = Modifier
                .width(100.dp)
                .height(8.dp)
        )
    }
}
