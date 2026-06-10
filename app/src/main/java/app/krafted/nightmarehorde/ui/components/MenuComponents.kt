package app.krafted.nightmarehorde.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.R

/** The dark, faintly red-tinted vertical wash behind the Shop and Settings screens. */
val DarkMenuBackground: Brush = Brush.verticalGradient(
    listOf(Color.Black, Color(0xFF0D0D0D), Color(0xFF1A0A0A), Color.Black)
)

/**
 * The shared menu backdrop: the painted menu artwork beneath a dark gradient
 * [overlay]. Place it as the first child of a full-screen [Box]; the foreground
 * content then sits on top.
 */
@Composable
fun MenuBackdrop(overlay: Brush, contentDescription: String? = null) {
    Image(
        painter = painterResource(id = R.drawable.menu_bg),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    Box(modifier = Modifier.fillMaxSize().background(overlay))
}

/**
 * A carousel tile for the character/map pickers. Selected tiles swap to the
 * [accent] color with a thicker, gently pulsing border. Sizing is left to the
 * caller via [modifier]; [content] fills the card body.
 */
@Composable
fun SelectableCard(
    isSelected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val borderWidth = if (isSelected) 3.dp else 1.5.dp

    val glow = rememberInfiniteTransition(label = "cardGlow")
    val glowAlpha by glow.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val borderColor = if (isSelected) accent.copy(alpha = glowAlpha) else Color(0xFF555555)

    Column(
        modifier = modifier
            .clip(shape)
            .border(borderWidth, borderColor, shape)
            .background(
                if (isSelected)
                    Brush.verticalGradient(listOf(accent.copy(alpha = 0.15f), Color(0xFF1A1A2E)))
                else
                    Brush.verticalGradient(listOf(Color(0xFF2A2A3E), Color(0xFF1A1A2E)))
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/**
 * The right-hand detail panel shared by the character and map pickers: a dark
 * card framed by an [accent]-tinted gradient border.
 */
@Composable
fun DetailPanel(
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF1E1E30), Color(0xFF12121E))))
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(listOf(accent.copy(alpha = 0.6f), accent.copy(alpha = 0.15f))),
                shape = shape
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

/**
 * An accent-tinted callout block (e.g. a class passive or a map's special
 * feature): an [accent] title above a muted [body] description.
 */
@Composable
fun AccentSection(
    title: String,
    body: String,
    accent: Color,
    blackOpsOne: FontFamily,
    modifier: Modifier = Modifier,
    titleFontSize: TextUnit = 12.sp,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontFamily = blackOpsOne,
            fontSize = titleFontSize,
            letterSpacing = 1.sp,
            color = accent
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = body,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = Color.White.copy(alpha = 0.75f)
        )
    }
}

/**
 * A compact label/value strip (e.g. WEAPON, SIZE) on a translucent rounded bar.
 */
@Composable
fun InfoChip(
    label: String,
    value: String,
    blackOpsOne: FontFamily,
    modifier: Modifier = Modifier,
    backgroundAlpha: Float = 0.3f,
    verticalPadding: Dp = 8.dp,
    gap: Dp = 12.dp,
    valueFontSize: TextUnit = 14.sp,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = backgroundAlpha))
            .padding(horizontal = 12.dp, vertical = verticalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = blackOpsOne,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = Color.White.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.width(gap))
        Text(
            text = value,
            fontFamily = blackOpsOne,
            fontSize = valueFontSize,
            letterSpacing = 1.sp,
            color = Color(0xFFFFCC00)
        )
    }
}

/**
 * Locked-state callout pinned to the bottom of a [DetailPanel], showing the
 * unlock [requirement]. Designed to sit in a [ColumnScope] so the leading
 * weighted spacer pushes it down.
 */
@Composable
fun ColumnScope.LockOverlay(requirement: String, blackOpsOne: FontFamily) {
    Spacer(modifier = Modifier.weight(1f))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xCC000000))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color(0xFFFF6666),
                    modifier = Modifier.size(20.dp).padding(end = 6.dp)
                )
                Text(
                    text = "LOCKED",
                    fontFamily = blackOpsOne,
                    fontSize = 18.sp,
                    letterSpacing = 3.sp,
                    color = Color(0xFFFF6666)
                )
            }
            Text(
                text = requirement,
                fontFamily = blackOpsOne,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/**
 * The pulsing red "DEPLOY" call-to-action shared by the character and map
 * pickers. Disabled and shown as "LOCKED" when [isLocked].
 */
@Composable
fun DeployButton(
    isLocked: Boolean,
    blackOpsOne: FontFamily,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberPulseScale(target = 1.04f, enabled = !isLocked)
    GameButton(
        onClick = onClick,
        enabled = !isLocked,
        shape = CutCornerShape(10.dp),
        fill = if (isLocked)
            Brush.horizontalGradient(listOf(Color(0xFF333333), Color(0xFF222222)))
        else
            Brush.horizontalGradient(listOf(Color(0xFFFF3300), Color(0xFF990000))),
        borderColor = if (isLocked) Color(0xFF555555) else Color(0xFFFFD700),
        modifier = modifier
            .scale(pulse)
            .height(48.dp)
            .widthIn(min = 180.dp)
    ) {
        Text(
            text = if (isLocked) "LOCKED" else "DEPLOY",
            fontFamily = blackOpsOne,
            fontSize = 20.sp,
            letterSpacing = 4.sp,
            color = if (isLocked) Color.Gray else Color.White,
            style = TextStyle(
                shadow = Shadow(color = Color.Black, offset = Offset(2f, 3f), blurRadius = 4f)
            )
        )
    }
}

/**
 * The gold-framed "◄ BACK" button anchored to the bottom-left of the Shop and
 * Settings screens.
 */
@Composable
fun BoxScope.BackButton(blackOpsOne: FontFamily, onClick: () -> Unit) {
    GameButton(
        onClick = onClick,
        shape = CutCornerShape(10.dp),
        fill = Brush.horizontalGradient(listOf(Color(0xFF333333), Color(0xFF111111))),
        borderColor = Color(0xFFFFD700),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(24.dp)
            .height(52.dp)
            .widthIn(min = 160.dp)
    ) {
        Text(
            text = "◄  BACK",
            fontFamily = blackOpsOne,
            fontSize = 18.sp,
            color = Color.White,
            letterSpacing = 4.sp
        )
    }
}
