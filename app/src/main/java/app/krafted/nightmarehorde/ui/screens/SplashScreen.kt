package app.krafted.nightmarehorde.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.R
import app.krafted.nightmarehorde.ui.theme.rememberGameFonts
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * A single drifting ash/ember particle. Position is expressed as fractions/phases
 * so the same list animates against any screen size without re-allocation.
 */
private class Ember(
    val x: Float,        // horizontal anchor as a fraction of the width (0..1)
    val phase: Float,    // per-particle offset into the rise cycle (0..1)
    val drift: Float,    // horizontal sway amplitude in px
    val radius: Float,   // base radius in px
    val hPhase: Float,   // phase of the horizontal sway (radians)
    val maxAlpha: Float, // peak opacity at the middle of the rise
    val kind: Int        // 0 = warm ember, 1 = red ember, 2 = cool ash (no glow)
)

/**
 * AAA "console intro" styled splash screen: a staged title reveal with a breathing
 * red bloom + chromatic offset, drifting embers/ash, and a slim determinate loader
 * that tops out in sync with the title settling. Auto-advances after ~2.9s (or sooner
 * via tap-to-skip, enabled once the title starts to appear), then fades the whole
 * screen out over ~280ms before calling [onSplashFinished] (~3.2s total). Everything
 * is drawn in-code apart from the existing background drawable.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val fonts = rememberGameFonts()
    val creepster = fonts.creepster
    val blackOpsOne = fonts.blackOpsOne

    // Rotating gameplay tips (adapted from the baseline splash).
    val gameTips = remember {
        listOf(
            "TIP: Orbital drones fuel up when you collect XP or kill zombies.",
            "TIP: Nights increase spawn rates and zombie speed by 25%. Stay alert!",
            "TIP: Save supplies to buy permanent stat upgrades in the Upgrades Shop.",
            "TIP: Look out for green ammo crates. Running out of bullets is fatal!",
            "TIP: Elite bosses spawn every 5 minutes. Maximize firepower before they arrive.",
            "TIP: Select different character classes to adapt passives to your playstyle."
        )
    }

    // Exit transition + tap-to-skip state. isExiting guards the exit so neither the
    // auto-advance nor a tap can fire (or overlap) the fade-out more than once.
    val exitAlpha = remember { Animatable(1f) }
    var isExiting by remember { mutableStateOf(false) }
    var canSkip by remember { mutableStateOf(false) }

    // Allow tap-to-skip only after the title has begun to appear.
    LaunchedEffect(Unit) {
        delay(1200)
        canSkip = true
    }

    // Auto-advance once the staged reveal has had time to read, then fall into the exit.
    LaunchedEffect(Unit) {
        delay(2900)
        isExiting = true
    }

    // Idempotent exit: fade the whole splash out over 280ms, then hand control back.
    LaunchedEffect(isExiting) {
        if (!isExiting) return@LaunchedEffect
        exitAlpha.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 280, easing = FastOutLinearInEasing)
        )
        onSplashFinished()
    }

    // Determinate load: 0 -> 1 over 2.0s, synced so the bar tops out as the title settles.
    val progressAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2000, easing = LinearOutSlowInEasing)
        )
    }
    val pct = (progressAnim.value * 100f).toInt().coerceIn(0, 100)

    // Staged reveal: studio line first, then the title + bottom UI.
    var stage by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(250)
        stage = 1
        delay(850)
        stage = 2
    }
    val studioAlpha by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0f,
        animationSpec = tween(700),
        label = "studioAlpha"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = if (stage >= 2) 1f else 0f,
        animationSpec = tween(900),
        label = "titleAlpha"
    )
    val titleScale by animateFloatAsState(
        targetValue = if (stage >= 2) 1f else 1.1f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "titleScale"
    )
    val uiAlpha by animateFloatAsState(
        targetValue = if (stage >= 2) 1f else 0f,
        animationSpec = tween(900),
        label = "uiAlpha"
    )

    // Rotating tip line.
    var tipIndex by remember { mutableIntStateOf(Random.nextInt(gameTips.size)) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1500)
            tipIndex = (tipIndex + 1) % gameTips.size
        }
    }

    // Persistent atmosphere animations.
    val embers = remember {
        List(36) {
            val k = Random.nextInt(0, 10)
            Ember(
                x = Random.nextFloat(),
                phase = Random.nextFloat(),
                drift = 8f + Random.nextFloat() * 40f,
                radius = 1.5f + Random.nextFloat() * 4f,
                hPhase = Random.nextFloat() * 6.2832f,
                maxAlpha = 0.18f + Random.nextFloat() * 0.5f,
                kind = if (k < 5) 0 else if (k < 8) 1 else 2
            )
        }
    }
    val atmosphere = rememberInfiniteTransition(label = "atmosphere")
    val emberTime by atmosphere.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "emberTime"
    )
    val glowPulse by atmosphere.animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    val scanShimmer by atmosphere.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanShimmer"
    )
    val loadingPulse by atmosphere.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loadingPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = exitAlpha.value }
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Skip straight to the exit once past the minimum reveal window.
                if (canSkip) isExiting = true
            }
    ) {
        // 1. Cinematic vertical artwork.
        Image(
            painter = painterResource(id = R.drawable.splash_screen),
            contentDescription = "Splash Screen Artwork",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2a. Vertical vignette (heavy top + bottom to seat the title and the loader).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.88f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.96f)
                        )
                    )
                )
        )
        // 2b. Side vignette.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.45f)
                        )
                    )
                )
        )

        // 3. Atmosphere layer: radial darkening, drifting embers/ash, faint scanlines.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Radial vignette focused slightly above center.
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.55f)
                    ),
                    center = Offset(w * 0.5f, h * 0.42f),
                    radius = maxOf(w, h) * 0.78f
                )
            )

            // Drifting embers / ash. Alpha eases to 0 at both ends so the loop reset is invisible.
            embers.forEach { e ->
                val p = (emberTime + e.phase) % 1f
                val y = h * (1f - p)
                val x = e.x * w + sin(p * 6.2832f + e.hPhase) * e.drift
                val a = sin(p * PI).toFloat().coerceIn(0f, 1f) * e.maxAlpha
                val col = when (e.kind) {
                    0 -> Color(0xFFFF7A1E)
                    1 -> Color(0xFFE23A1A)
                    else -> Color(0xFFBDB6A8)
                }
                if (e.kind != 2) {
                    drawCircle(col.copy(alpha = a * 0.32f), e.radius * 2.4f, Offset(x, y))
                }
                drawCircle(col.copy(alpha = a), e.radius, Offset(x, y))
            }

            // Faint shimmering scanlines.
            val step = 3.dp.toPx()
            var ly = scanShimmer * step - step
            while (ly < h) {
                drawLine(
                    color = Color.Black.copy(alpha = 0.05f),
                    start = Offset(0f, ly),
                    end = Offset(w, ly),
                    strokeWidth = 1f
                )
                ly += step
            }
        }

        // 4. Foreground content.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 40.dp, horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP: studio credit, divider, glowing title.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = "KRAFTED GAMES PRESENTS",
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontFamily = blackOpsOne,
                        color = Color.White.copy(alpha = 0.65f),
                        letterSpacing = 5.sp
                    ),
                    modifier = Modifier
                        .graphicsLayer { alpha = studioAlpha }
                        .padding(bottom = 10.dp)
                )

                Box(
                    modifier = Modifier
                        .graphicsLayer { alpha = studioAlpha }
                        .padding(bottom = 22.dp)
                        .width(180.dp)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFD11414).copy(alpha = 0.8f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                CinematicTitle(
                    text = "NIGHTMARE HORDE",
                    fontFamily = creepster,
                    glow = { glowPulse },
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = titleAlpha
                            scaleX = titleScale
                            scaleY = titleScale
                        }
                        .padding(horizontal = 8.dp)
                )
            }

            // BOTTOM: rotating tip, loading state + percentage, slim progress bar.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = uiAlpha }
                    .padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 26.dp)
                        .widthIn(max = 620.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFD11414).copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                        .padding(vertical = 10.dp, horizontal = 22.dp)
                ) {
                    Crossfade(
                        targetState = tipIndex,
                        animationSpec = tween(600),
                        label = "tip"
                    ) { idx ->
                        Text(
                            text = gameTips[idx],
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontFamily = blackOpsOne,
                                color = Color(0xFFCFCFCF),
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "LOADING…",
                        modifier = Modifier.graphicsLayer { alpha = loadingPulse },
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = blackOpsOne,
                            color = Color.White,
                            letterSpacing = 3.sp,
                            shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "$pct%",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = blackOpsOne,
                            color = Color(0xFFFFD700),
                            letterSpacing = 2.sp,
                            shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(4.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
                        .border(1.dp, Color.DarkGray.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
                ) {
                    LinearProgressIndicator(
                        progress = { progressAnim.value },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFFE8141C),
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

/**
 * The big title, layered for a cinematic look: a soft red bloom, a subtle
 * red/cyan chromatic offset, and a crisp top layer. [glow] (0..1) breathes the bloom.
 */
@Composable
private fun CinematicTitle(
    text: String,
    fontFamily: FontFamily,
    glow: () -> Float,
    modifier: Modifier = Modifier
) {
    // Shared geometry for every layer; each layer only overrides color + shadow.
    val base = TextStyle(
        fontSize = 58.sp,
        fontFamily = fontFamily,
        letterSpacing = 7.sp,
        textAlign = TextAlign.Center
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Outer red bloom.
        Text(
            text = text,
            style = base.copy(
                color = Color(0xFFFF1414).copy(alpha = 0.22f * glow()),
                shadow = Shadow(Color(0xFFFF0000), Offset(0f, 0f), 38f)
            )
        )
        // Inner red bloom.
        Text(
            text = text,
            style = base.copy(
                color = Color(0xFFB00000).copy(alpha = 0.45f * glow()),
                shadow = Shadow(Color(0xFF7A0000), Offset(0f, 0f), 16f)
            )
        )
        // Chromatic aberration: red shifted left, cyan shifted right.
        Text(
            text = text,
            modifier = Modifier.offset(x = (-2.5).dp),
            style = base.copy(color = Color(0xFFFF2D2D).copy(alpha = 0.5f))
        )
        Text(
            text = text,
            modifier = Modifier.offset(x = 2.5.dp),
            style = base.copy(color = Color(0xFF1FE3FF).copy(alpha = 0.4f))
        )
        // Crisp top layer.
        Text(
            text = text,
            style = base.copy(
                color = Color(0xFFE8141C),
                shadow = Shadow(Color(0xFF4A0000), Offset(2f, 4f), 8f)
            )
        )
    }
}
