package app.krafted.nightmarehorde.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Professional boss health bar inspired by Souls-like and action RPG HUDs.
 *
 * Features:
 * - Smooth animated HP drain with a trailing "damage ghost" bar
 * - Gradient fill using the boss's accent color
 * - Segment tick marks every 10% for easy readability
 * - Decorative diamond end-caps and metallic frame
 * - Boss name with skull symbol centered above the bar
 * - HP percentage shown inside the bar
 */
@Composable
fun BossHealthBar(
    bossName: String,
    currentHealth: Int,
    maxHealth: Int,
    isVisible: Boolean,
    accentColor: Long = 0xFFFF4444,
    modifier: Modifier = Modifier
) {
    val healthPercent = if (maxHealth > 0) currentHealth.toFloat() / maxHealth else 0f

    // Smooth animated fill — the main bar eases to target over 400ms
    val animatedHealth by animateFloatAsState(
        targetValue = healthPercent,
        animationSpec = tween(durationMillis = 400),
        label = "bossHpFill"
    )

    // Trailing "damage ghost" — a slower bar that reveals the chunk of damage taken
    val ghostHealth by animateFloatAsState(
        targetValue = healthPercent,
        animationSpec = tween(durationMillis = 900),
        label = "bossHpGhost"
    )

    val accent = Color(accentColor)
    val textMeasurer = rememberTextMeasurer()

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            // Boss name with skull icon
            Text(
                text = "\u2620  $bossName  \u2620",
                color = accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // The actual health bar — drawn entirely in Canvas for pixel-perfect control
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawBossBar(
                        animatedHealth = animatedHealth,
                        ghostHealth = ghostHealth,
                        accent = accent,
                        healthPercent = healthPercent,
                        currentHealth = currentHealth,
                        maxHealth = maxHealth,
                        textMeasurer = textMeasurer
                    )
                }
            }
        }
    }
}

/**
 * Draws the full professional boss bar inside a [DrawScope].
 */
private fun DrawScope.drawBossBar(
    animatedHealth: Float,
    ghostHealth: Float,
    accent: Color,
    healthPercent: Float,
    currentHealth: Int,
    maxHealth: Int,
    textMeasurer: androidx.compose.ui.text.TextMeasurer
) {
    val barHeight = size.height
    val barWidth = size.width
    val cornerRad = CornerRadius(4.dp.toPx())

    // Inset: leave room for the decorative end-caps
    val capWidth = 8.dp.toPx()
    val innerLeft = capWidth
    val innerWidth = barWidth - capWidth * 2

    // ─── 1. Dark background ─────────────────────────────────────────
    drawRoundRect(
        color = Color(0xFF0A0A0A),
        topLeft = Offset(innerLeft, 0f),
        size = Size(innerWidth, barHeight),
        cornerRadius = cornerRad
    )

    // Subtle inner bevel (dark edges)
    drawRoundRect(
        color = Color(0xFF1A1A1A),
        topLeft = Offset(innerLeft + 1.dp.toPx(), 1.dp.toPx()),
        size = Size(innerWidth - 2.dp.toPx(), barHeight - 2.dp.toPx()),
        cornerRadius = cornerRad
    )

    // ─── 2. Damage ghost (trailing white/red bar) ───────────────────
    if (ghostHealth > animatedHealth && ghostHealth > 0f) {
        drawRoundRect(
            color = Color(0x99FF4444),
            topLeft = Offset(innerLeft, 0f),
            size = Size(innerWidth * ghostHealth, barHeight),
            cornerRadius = cornerRad
        )
    }

    // ─── 3. Main health fill — gradient from dark to accent color ───
    if (animatedHealth > 0f) {
        val fillWidth = innerWidth * animatedHealth

        // Darker shade of accent for the left end of the gradient
        val darkAccent = Color(
            red = (accent.red * 0.3f).coerceIn(0f, 1f),
            green = (accent.green * 0.3f).coerceIn(0f, 1f),
            blue = (accent.blue * 0.3f).coerceIn(0f, 1f),
            alpha = 1f
        )

        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(darkAccent, accent),
                startX = innerLeft,
                endX = innerLeft + fillWidth
            ),
            topLeft = Offset(innerLeft, 0f),
            size = Size(fillWidth, barHeight),
            cornerRadius = cornerRad
        )

        // Top highlight — gives the bar a glossy/beveled look
        val highlightColor = Color(1f, 1f, 1f, 0.18f)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(highlightColor, Color.Transparent),
                startY = 0f,
                endY = barHeight * 0.5f
            ),
            topLeft = Offset(innerLeft, 0f),
            size = Size(fillWidth, barHeight * 0.45f),
            cornerRadius = cornerRad
        )
    }

    // ─── 4. Segment tick marks (every 10%) ──────────────────────────
    val tickColor = Color(0x44000000)
    val tickWidth = 1.dp.toPx()
    for (i in 1..9) {
        val tickX = innerLeft + innerWidth * (i / 10f)
        drawLine(
            color = tickColor,
            start = Offset(tickX, 2.dp.toPx()),
            end = Offset(tickX, barHeight - 2.dp.toPx()),
            strokeWidth = tickWidth,
            cap = StrokeCap.Round
        )
    }

    // ─── 5. Metallic frame border ───────────────────────────────────
    // Outer frame — dark
    drawRoundRect(
        color = Color(0xFF333333),
        topLeft = Offset(innerLeft, 0f),
        size = Size(innerWidth, barHeight),
        cornerRadius = cornerRad,
        style = Stroke(width = 2.dp.toPx())
    )
    // Inner bright edge for metallic look
    drawRoundRect(
        color = Color(0xFF555555),
        topLeft = Offset(innerLeft + 1f, 1f),
        size = Size(innerWidth - 2f, barHeight - 2f),
        cornerRadius = cornerRad,
        style = Stroke(width = 0.5.dp.toPx())
    )

    // ─── 6. Decorative diamond end-caps ─────────────────────────────
    drawDiamondCap(
        centerX = capWidth * 0.5f,
        centerY = barHeight / 2f,
        halfSize = capWidth * 0.45f,
        color = Color(0xFF444444),
        accentColor = accent
    )
    drawDiamondCap(
        centerX = barWidth - capWidth * 0.5f,
        centerY = barHeight / 2f,
        halfSize = capWidth * 0.45f,
        color = Color(0xFF444444),
        accentColor = accent
    )

    // ─── 7. HP text centered in bar ─────────────────────────────────
    val hpText = "${currentHealth}/${maxHealth}"
    val textStyle = TextStyle(
        color = Color.White,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    )
    val measured = textMeasurer.measure(hpText, textStyle)
    drawText(
        textLayoutResult = measured,
        topLeft = Offset(
            x = (barWidth - measured.size.width) / 2f,
            y = (barHeight - measured.size.height) / 2f
        )
    )
}

/**
 * Draws a small diamond shape used as an end-cap decoration.
 */
private fun DrawScope.drawDiamondCap(
    centerX: Float,
    centerY: Float,
    halfSize: Float,
    color: Color,
    accentColor: Color
) {
    val path = Path().apply {
        moveTo(centerX, centerY - halfSize)       // top
        lineTo(centerX + halfSize, centerY)        // right
        lineTo(centerX, centerY + halfSize)        // bottom
        lineTo(centerX - halfSize, centerY)        // left
        close()
    }
    drawPath(path, color)
    drawPath(path, accentColor, style = Stroke(width = 1.dp.toPx()))
}
