package app.krafted.nightmarehorde.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.R
import app.krafted.nightmarehorde.game.data.CharacterClass

/** Accent colors per character class */
private val classAccentColors = mapOf(
    CharacterClass.ROOKIE to Color(0xFF4A90E2),        // Blue
    CharacterClass.SOLDIER to Color(0xFF3D8B37),        // Military green
    CharacterClass.COMMANDO to Color(0xFF9C27B0),       // Purple
    CharacterClass.SPACE_MARINE to Color(0xFF2196F3),   // Steel blue
    CharacterClass.ENFORCER to Color(0xFFFF9800),       // Orange
    CharacterClass.HUNTER to Color(0xFF795548),          // Dark brown
    CharacterClass.TERRIBLE_KNIGHT to Color(0xFFFFD700)  // Gold
)

/** Map CharacterClass to the idle sprite sheet resource ID */
private fun getIdleSpriteRes(characterClass: CharacterClass): Int {
    return when (characterClass) {
        CharacterClass.ROOKIE -> R.drawable.player_idle_sheet
        CharacterClass.SOLDIER -> R.drawable.soldier_idle_sheet
        CharacterClass.COMMANDO -> R.drawable.commando_idle_sheet
        CharacterClass.SPACE_MARINE -> R.drawable.spacemarine_idle_sheet
        CharacterClass.ENFORCER -> R.drawable.enforcer_idle_sheet
        CharacterClass.HUNTER -> R.drawable.hunter_idle_sheet
        CharacterClass.TERRIBLE_KNIGHT -> R.drawable.knight_idle_sheet
    }
}

@Composable
fun CharacterSelectScreen(
    onCharacterSelected: (CharacterClass) -> Unit,
    onBack: () -> Unit
) {
    val creepster = FontFamily(Font(R.font.creepster))
    val blackOpsOne = FontFamily(Font(R.font.black_ops_one))

    val classes = CharacterClass.entries
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedClass = classes[selectedIndex]
    val accent = classAccentColors[selectedClass] ?: Color(0xFFFFD700)

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background
        Image(
            painter = painterResource(id = R.drawable.menu_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(500)) { -40 }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ─── Left: Character carousel ─────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = "SELECT YOUR CLASS",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontFamily = creepster,
                            letterSpacing = 4.sp,
                            color = Color(0xFFFF3300),
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(3f, 5f),
                                blurRadius = 8f
                            )
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Character cards row
                    LazyRow(
                        state = rememberLazyListState(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(classes) { index, charClass ->
                            CharacterCard(
                                characterClass = charClass,
                                isSelected = index == selectedIndex,
                                isLocked = !charClass.isDefaultUnlocked,
                                accent = classAccentColors[charClass] ?: Color.Gray,
                                blackOpsOne = blackOpsOne,
                                onClick = { selectedIndex = index }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        // Back button
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .widthIn(min = 120.dp)
                                .clip(CutCornerShape(8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF444444), Color(0xFF1A1A1A))
                                    )
                                )
                                .border(2.dp, Color.Gray, CutCornerShape(8.dp))
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "BACK",
                                fontFamily = blackOpsOne,
                                fontSize = 16.sp,
                                letterSpacing = 3.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        // Deploy button
                        DeployButton(
                            isLocked = !selectedClass.isDefaultUnlocked,
                            accent = accent,
                            blackOpsOne = blackOpsOne,
                            onClick = {
                                if (selectedClass.isDefaultUnlocked) {
                                    onCharacterSelected(selectedClass)
                                }
                            }
                        )
                    }
                }

                // ─── Right: Character detail panel ────────────────────────
                CharacterDetailPanel(
                    characterClass = selectedClass,
                    accent = accent,
                    creepster = creepster,
                    blackOpsOne = blackOpsOne,
                    modifier = Modifier
                        .weight(0.45f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

// ─── Character Card ───────────────────────────────────────────────────────────

@Composable
private fun CharacterCard(
    characterClass: CharacterClass,
    isSelected: Boolean,
    isLocked: Boolean,
    accent: Color,
    blackOpsOne: FontFamily,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) accent else Color(0xFF555555)
    val borderWidth = if (isSelected) 3.dp else 1.5.dp

    // Subtle glow animation for selected card
    val infiniteTransition = rememberInfiniteTransition(label = "cardGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(
                if (isSelected) Modifier.border(
                    borderWidth,
                    accent.copy(alpha = glowAlpha),
                    RoundedCornerShape(12.dp)
                )
                else Modifier.border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            )
            .background(
                if (isSelected)
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.15f), Color(0xFF1A1A2E))
                    )
                else
                    Brush.verticalGradient(
                        listOf(Color(0xFF2A2A3E), Color(0xFF1A1A2E))
                    )
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Character sprite preview (idle sheet, show first frame area)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            if (isLocked) {
                Text(
                    text = "\uD83D\uDD12",
                    fontSize = 28.sp,
                    modifier = Modifier.alpha(0.6f)
                )
            } else {
                // Draw only the first frame from the sprite sheet
                val charType = characterClass.characterType
                val context = LocalContext.current
                val firstFrame = remember(characterClass) {
                    val resId = getIdleSpriteRes(characterClass)
                    val opts = android.graphics.BitmapFactory.Options().apply {
                        inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                    }
                    val full = android.graphics.BitmapFactory.decodeResource(context.resources, resId, opts)
                    val frame = android.graphics.Bitmap.createBitmap(
                        full, 0, 0, charType.frameWidth, charType.frameHeight
                    )
                    if (frame !== full) full.recycle()

                    // Crop to non-transparent bounding box so characters uniformly fit the UI box
                    val width = frame.width
                    val height = frame.height
                    val pixels = IntArray(width * height)
                    frame.getPixels(pixels, 0, width, 0, 0, width, height)

                    var minX = width
                    var minY = height
                    var maxX = -1
                    var maxY = -1

                    for (y in 0 until height) {
                        for (x in 0 until width) {
                            val alpha = (pixels[y * width + x] ushr 24) and 0xFF
                            if (alpha > 0) {
                                if (x < minX) minX = x
                                if (x > maxX) maxX = x
                                if (y < minY) minY = y
                                if (y > maxY) maxY = y
                            }
                        }
                    }

                    val cropped = if (maxX >= minX && maxY >= minY) {
                        val cropWidth = maxX - minX + 1
                        val cropHeight = maxY - minY + 1
                        android.graphics.Bitmap.createBitmap(frame, minX, minY, cropWidth, cropHeight)
                    } else {
                        frame
                    }
                    if (cropped !== frame) frame.recycle()

                    cropped.asImageBitmap()
                }
                Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    val imgWidth = firstFrame.width.toFloat()
                    val imgHeight = firstFrame.height.toFloat()

                    // Scale the cropped sprite to fit nicely within the padded Canvas
                    val scale = minOf(size.width / imgWidth, size.height / imgHeight)
                    val targetWidth = imgWidth * scale
                    val targetHeight = imgHeight * scale

                    val xOffset = (size.width - targetWidth) / 2f
                    val yOffset = (size.height - targetHeight) / 2f

                    drawImage(
                        image = firstFrame,
                        dstOffset = IntOffset(xOffset.toInt(), yOffset.toInt()),
                        dstSize = IntSize(targetWidth.toInt(), targetHeight.toInt()),
                        filterQuality = FilterQuality.None
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Name
        Text(
            text = characterClass.displayName.uppercase(),
            fontFamily = blackOpsOne,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = if (isSelected) accent else Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Lock status
        if (isLocked) {
            Text(
                text = characterClass.unlockRequirement,
                fontSize = 8.sp,
                color = Color(0xFFFF6666),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 10.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ─── Character Detail Panel ───────────────────────────────────────────────────

@Composable
private fun CharacterDetailPanel(
    characterClass: CharacterClass,
    accent: Color,
    creepster: FontFamily,
    blackOpsOne: FontFamily,
    modifier: Modifier = Modifier
) {
    // Animate detail changes
    val isLocked = !characterClass.isDefaultUnlocked

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E1E30), Color(0xFF12121E))
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.6f), accent.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Character name
        Text(
            text = characterClass.displayName.uppercase(),
            style = TextStyle(
                fontSize = 32.sp,
                fontFamily = creepster,
                letterSpacing = 3.sp,
                color = accent,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 4f),
                    blurRadius = 6f
                )
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ─── Stats Section ───────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatBar(
                label = "HEALTH",
                value = characterClass.healthPercent,
                color = Color(0xFFE53935),
                displayText = "${characterClass.baseHp}",
                blackOpsOne = blackOpsOne
            )
            StatBar(
                label = "SPEED",
                value = characterClass.speedPercent,
                color = Color(0xFF43A047),
                displayText = "${characterClass.baseSpeed}",
                blackOpsOne = blackOpsOne
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ─── Weapon Section ──────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WEAPON",
                fontFamily = blackOpsOne,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = characterClass.startingWeaponDisplayName.uppercase(),
                fontFamily = blackOpsOne,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                color = Color(0xFFFFCC00)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ─── Passive Section ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.08f))
                .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "PASSIVE: ${characterClass.passiveName.uppercase()}",
                fontFamily = blackOpsOne,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = characterClass.passiveDescription,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
        }

        // ─── Lock overlay ────────────────────────────────────────
        if (isLocked) {
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
                    Text(
                        text = "\uD83D\uDD12 LOCKED",
                        fontFamily = blackOpsOne,
                        fontSize = 18.sp,
                        letterSpacing = 3.sp,
                        color = Color(0xFFFF6666)
                    )
                    Text(
                        text = characterClass.unlockRequirement,
                        fontFamily = blackOpsOne,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

// ─── Stat Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun StatBar(
    label: String,
    value: Float,
    color: Color,
    displayText: String,
    blackOpsOne: FontFamily
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontFamily = blackOpsOne,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.width(56.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.08f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = value.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(color, color.copy(alpha = 0.6f))
                        )
                    )
            )
        }

        Text(
            text = displayText,
            fontFamily = blackOpsOne,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.width(36.dp),
            textAlign = TextAlign.End
        )
    }
}

// ─── Deploy Button ────────────────────────────────────────────────────────────

@Composable
private fun DeployButton(
    isLocked: Boolean,
    accent: Color,
    blackOpsOne: FontFamily,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "deployPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .scale(if (!isLocked) pulseScale else 1f)
            .height(48.dp)
            .widthIn(min = 180.dp)
            .clip(CutCornerShape(10.dp))
            .background(
                if (isLocked)
                    Brush.horizontalGradient(listOf(Color(0xFF333333), Color(0xFF222222)))
                else
                    Brush.horizontalGradient(listOf(Color(0xFFFF3300), Color(0xFF990000)))
            )
            .border(
                width = 2.dp,
                color = if (isLocked) Color(0xFF555555) else Color(0xFFFFD700),
                shape = CutCornerShape(10.dp)
            )
            .clickable(enabled = !isLocked, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isLocked) "LOCKED" else "DEPLOY",
            fontFamily = blackOpsOne,
            fontSize = 20.sp,
            letterSpacing = 4.sp,
            color = if (isLocked) Color.Gray else Color.White,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 3f),
                    blurRadius = 4f
                )
            )
        )
    }
}
