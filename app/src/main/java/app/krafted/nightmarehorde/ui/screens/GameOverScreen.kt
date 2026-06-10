package app.krafted.nightmarehorde.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.ui.components.GameButton
import app.krafted.nightmarehorde.ui.components.rememberPulseScale
import app.krafted.nightmarehorde.ui.navigation.GameOverStats
import app.krafted.nightmarehorde.ui.theme.rememberGameFonts
import kotlin.math.floor

@Composable
fun GameOverScreen(
    stats: GameOverStats,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit
) {
    val fonts = rememberGameFonts()
    val creepster = fonts.creepster
    val blackOpsOne = fonts.blackOpsOne

    val accentRed = Color(0xFFFF3300)
    val goldAccent = Color(0xFFFFD700)
    val darkBg = Color(0xFF0A0A0A)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(darkBg)
        )

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

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Left column: Title + Buttons ─────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "YOU DIED",
                        style = TextStyle(
                            fontSize = 64.sp,
                            fontFamily = creepster,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 8.sp,
                            color = accentRed,
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(5f, 8f),
                                blurRadius = 14f
                            )
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "NIGHTMARE HORDE",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = blackOpsOne,
                            letterSpacing = 5.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
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

            // ── Right column: Stats panel ────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
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
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRow(
                        label = "CLASS",
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
                    StatRow(
                        label = "SUPPLIES EARNED",
                        value = "+${stats.suppliesEarned}",
                        labelFamily = blackOpsOne,
                        valueColor = goldAccent
                    )
                }
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
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = labelFamily,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
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
    val pulse = rememberPulseScale(enabled = isPrimary)
    GameButton(
        onClick = onClick,
        shape = CutCornerShape(12.dp),
        fill = Brush.horizontalGradient(
            colors = if (isPrimary)
                listOf(Color(0xFFFF3300), Color(0xFF990000))
            else
                listOf(Color(0xFF333333), Color(0xFF111111))
        ),
        borderColor = if (isPrimary) Color(0xFFFFD700) else Color.DarkGray,
        borderWidth = 3.dp,
        innerScrim = Color.Black.copy(alpha = 0.2f),
        modifier = modifier
            .scale(if (isPrimary) pulse else 1f)
            .height(56.dp)
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (isPrimary) 20.sp else 16.sp,
            letterSpacing = 4.sp,
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
    GameButton(
        onClick = onClick,
        fill = Brush.verticalGradient(listOf(Color(0xFF444444), Color(0xFF1A1A1A))),
        borderColor = Color.Gray,
        innerScrim = Color.Black.copy(alpha = 0.3f),
        modifier = modifier.height(56.dp)
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.95f),
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            letterSpacing = 3.sp
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
