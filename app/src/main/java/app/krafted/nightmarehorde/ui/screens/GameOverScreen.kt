package app.krafted.nightmarehorde.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.R
import app.krafted.nightmarehorde.ui.navigation.GameOverStats
import kotlin.math.floor

@Composable
fun GameOverScreen(
    stats: GameOverStats,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val creepster = FontFamily(Font(R.font.creepster))
    val blackOpsOne = FontFamily(Font(R.font.black_ops_one))

    val accentRed = Color(0xFFFF3300)
    val goldAccent = Color(0xFFFFD700)
    val darkBg = Color(0xFF0A0A0A)

    Box(modifier = Modifier.fillMaxSize()) {
        // Dark near-black base background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBg)
        )

        // Red vignette overlay — radial-style using vertical gradient stacked with a solid border fade
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentRed.copy(alpha = 0.18f),
                            Color.Transparent,
                            accentRed.copy(alpha = 0.28f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // ── "YOU DIED" title ──────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "YOU DIED",
                    style = TextStyle(
                        fontSize = 80.sp,
                        fontFamily = creepster,
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 10.sp,
                        color = accentRed,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(6f, 10f),
                            blurRadius = 16f
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "NIGHTMARE HORDE",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = blackOpsOne,
                        letterSpacing = 6.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // ── Stats panel ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .clip(CutCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A0000),
                                Color(0xFF0D0D0D)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        color = accentRed.copy(alpha = 0.6f),
                        shape = CutCornerShape(12.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatRow(
                        label = "SURVIVOR",
                        value = stats.characterType.name.replace('_', ' '),
                        labelFamily = blackOpsOne,
                        valueColor = goldAccent
                    )
                    StatRow(
                        label = "TIME SURVIVED",
                        value = formatTime(stats.survivalTimeSec),
                        labelFamily = blackOpsOne,
                        valueColor = goldAccent
                    )
                    StatRow(
                        label = "KILLS",
                        value = stats.killCount.toString(),
                        labelFamily = blackOpsOne,
                        valueColor = goldAccent
                    )
                    StatRow(
                        label = "LEVEL REACHED",
                        value = stats.levelReached.toString(),
                        labelFamily = blackOpsOne,
                        valueColor = goldAccent
                    )
                    StatRow(
                        label = "BOSSES DEFEATED",
                        value = stats.bossesDefeated.toString(),
                        labelFamily = blackOpsOne,
                        valueColor = if (stats.bossesDefeated > 0) goldAccent else Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // ── Buttons ───────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameOverPulseButton(
                    text = "PLAY AGAIN",
                    isPrimary = true,
                    fontFamily = blackOpsOne,
                    modifier = Modifier.weight(1f),
                    onClick = onPlayAgain
                )

                GameOverMenuButton(
                    text = "MAIN MENU",
                    fontFamily = blackOpsOne,
                    modifier = Modifier.weight(1f),
                    onClick = onMainMenu
                )
            }
        }
    }
}

// ── Helper composables ────────────────────────────────────────────────────────

@Composable
private fun StatRow(
    label: String,
    value: String,
    labelFamily: FontFamily,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = labelFamily,
                fontSize = 14.sp,
                letterSpacing = 3.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = labelFamily,
                fontSize = 16.sp,
                letterSpacing = 2.sp,
                color = valueColor,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 3f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Composable
private fun GameOverPulseButton(
    text: String,
    isPrimary: Boolean,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = modifier
            .scale(if (isPrimary) pulseScale else 1f)
            .height(64.dp)
            .clip(CutCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = if (isPrimary)
                        listOf(Color(0xFFFF3300), Color(0xFF990000))
                    else
                        listOf(Color(0xFF333333), Color(0xFF111111))
                )
            )
            .border(
                width = 3.dp,
                color = if (isPrimary) Color(0xFFFFD700) else Color.DarkGray,
                shape = CutCornerShape(12.dp)
            )
            .background(Color.Black.copy(alpha = 0.2f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (isPrimary) 26.sp else 20.sp,
            letterSpacing = 6.sp,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 4f),
                    blurRadius = 4f
                )
            )
        )
    }
}

@Composable
private fun GameOverMenuButton(
    text: String,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(CutCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF444444), Color(0xFF1A1A1A))
                )
            )
            .border(
                width = 2.dp,
                color = Color.Gray,
                shape = CutCornerShape(8.dp)
            )
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.95f),
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            letterSpacing = 4.sp
        )
    }
}

// ── Utility ──────────────────────────────────────────────────────────────────

private fun formatTime(seconds: Float): String {
    val totalSeconds = floor(seconds).toInt()
    val minutes = totalSeconds / 60
    val secs = totalSeconds % 60
    return "%02d:%02d".format(minutes, secs)
}
