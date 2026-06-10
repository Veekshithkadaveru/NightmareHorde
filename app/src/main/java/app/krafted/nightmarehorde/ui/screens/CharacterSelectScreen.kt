package app.krafted.nightmarehorde.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.R
import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.ui.components.AccentSection
import app.krafted.nightmarehorde.ui.components.DeployButton
import app.krafted.nightmarehorde.ui.components.DetailPanel
import app.krafted.nightmarehorde.ui.components.GameButton
import app.krafted.nightmarehorde.ui.components.InfoChip
import app.krafted.nightmarehorde.ui.components.LockOverlay
import app.krafted.nightmarehorde.ui.components.MenuBackdrop
import app.krafted.nightmarehorde.ui.components.ProgressBar
import app.krafted.nightmarehorde.ui.components.SelectableCard
import app.krafted.nightmarehorde.ui.theme.rememberGameFonts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    isCharacterUnlocked: (CharacterClass) -> Boolean,
    onCharacterSelected: (CharacterClass) -> Unit,
    onBack: () -> Unit
) {
    val fonts = rememberGameFonts()
    val creepster = fonts.creepster
    val blackOpsOne = fonts.blackOpsOne

    val classes = CharacterClass.entries
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedClass = classes[selectedIndex]
    val accent = classAccentColors[selectedClass] ?: Color(0xFFFFD700)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        MenuBackdrop(
            overlay = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.7f),
                    Color.Black.copy(alpha = 0.5f),
                    Color.Black.copy(alpha = 0.85f)
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
                                isLocked = !isCharacterUnlocked(charClass),
                                accent = classAccentColors[charClass] ?: Color.Gray,
                                blackOpsOne = blackOpsOne,
                                onClick = { selectedIndex = index }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        GameButton(
                            onClick = onBack,
                            fill = Brush.verticalGradient(listOf(Color(0xFF444444), Color(0xFF1A1A1A))),
                            borderColor = Color.Gray,
                            shape = CutCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .widthIn(min = 120.dp)
                        ) {
                            Text(
                                text = "BACK",
                                fontFamily = blackOpsOne,
                                fontSize = 16.sp,
                                letterSpacing = 3.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        DeployButton(
                            isLocked = !isCharacterUnlocked(selectedClass),
                            blackOpsOne = blackOpsOne,
                            onClick = {
                                if (isCharacterUnlocked(selectedClass)) {
                                    onCharacterSelected(selectedClass)
                                }
                            }
                        )
                    }
                }

                // ─── Right: Character detail panel ────────────────────────
                CharacterDetailPanel(
                    characterClass = selectedClass,
                    isLocked = !isCharacterUnlocked(selectedClass),
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
    SelectableCard(
        isSelected = isSelected,
        accent = accent,
        onClick = onClick,
        modifier = Modifier.size(width = 100.dp, height = 132.dp)
    ) {
        CharacterSpritePreview(characterClass = characterClass, isLocked = isLocked)

        Spacer(modifier = Modifier.height(6.dp))

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

/**
 * The cropped first-frame portrait shown on a character card. The idle sheet is
 * decoded off the main thread to avoid an ANR, then scaled to fit the tile.
 */
@Composable
private fun CharacterSpritePreview(characterClass: CharacterClass, isLocked: Boolean) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = if (isLocked) 0.6f else 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        val charType = characterClass.characterType
        val context = LocalContext.current
        var firstFrame by remember(characterClass) { mutableStateOf<ImageBitmap?>(null) }
        LaunchedEffect(characterClass) {
            firstFrame = withContext(Dispatchers.IO) {
                decodeCroppedIdleFrame(context, characterClass, charType)
            }
        }
        val loadedFrame = firstFrame
        if (loadedFrame != null) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val imgWidth = loadedFrame.width.toFloat()
                val imgHeight = loadedFrame.height.toFloat()

                val scale = minOf(size.width / imgWidth, size.height / imgHeight)
                val targetWidth = imgWidth * scale
                val targetHeight = imgHeight * scale

                val xOffset = (size.width - targetWidth) / 2f
                val yOffset = (size.height - targetHeight) / 2f

                drawImage(
                    image = loadedFrame,
                    dstOffset = IntOffset(xOffset.toInt(), yOffset.toInt()),
                    dstSize = IntSize(targetWidth.toInt(), targetHeight.toInt()),
                    filterQuality = FilterQuality.None,
                    colorFilter = if (isLocked) ColorFilter.tint(Color.DarkGray) else null
                )
            }
        }

        if (isLocked) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Locked",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Decodes the first frame of [characterClass]'s idle sheet and tightly crops the
 * transparent padding so the portrait fills its tile. Runs blocking bitmap work,
 * so call it off the main thread.
 */
private fun decodeCroppedIdleFrame(
    context: Context,
    characterClass: CharacterClass,
    charType: CharacterType
): ImageBitmap {
    val resId = getIdleSpriteRes(characterClass)
    val opts = BitmapFactory.Options().apply {
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val full = BitmapFactory.decodeResource(context.resources, resId, opts)
    val frame = Bitmap.createBitmap(full, 0, 0, charType.frameWidth, charType.frameHeight)
    if (frame !== full) full.recycle()

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
        Bitmap.createBitmap(frame, minX, minY, maxX - minX + 1, maxY - minY + 1)
    } else {
        frame
    }
    if (cropped !== frame) frame.recycle()

    return cropped.asImageBitmap()
}

// ─── Character Detail Panel ───────────────────────────────────────────────────

@Composable
private fun CharacterDetailPanel(
    characterClass: CharacterClass,
    isLocked: Boolean,
    accent: Color,
    creepster: FontFamily,
    blackOpsOne: FontFamily,
    modifier: Modifier = Modifier
) {
    DetailPanel(accent = accent, modifier = modifier) {
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

        InfoChip(
            label = "WEAPON",
            value = characterClass.startingWeaponDisplayName.uppercase(),
            blackOpsOne = blackOpsOne,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        AccentSection(
            title = "PASSIVE: ${characterClass.passiveName.uppercase()}",
            body = characterClass.passiveDescription,
            accent = accent,
            blackOpsOne = blackOpsOne,
            modifier = Modifier.fillMaxWidth()
        )

        if (isLocked) {
            LockOverlay(requirement = characterClass.unlockRequirement, blackOpsOne = blackOpsOne)
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

        ProgressBar(
            progress = value,
            fill = Brush.horizontalGradient(listOf(color, color.copy(alpha = 0.6f))),
            trackColor = Color.White.copy(alpha = 0.08f),
            cornerRadius = 4.dp,
            modifier = Modifier
                .weight(1f)
                .height(14.dp)
        )

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
