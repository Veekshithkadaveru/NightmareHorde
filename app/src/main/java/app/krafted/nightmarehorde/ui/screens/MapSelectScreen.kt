package app.krafted.nightmarehorde.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.game.data.MapType
import app.krafted.nightmarehorde.ui.components.AccentSection
import app.krafted.nightmarehorde.ui.components.DeployButton
import app.krafted.nightmarehorde.ui.components.DetailPanel
import app.krafted.nightmarehorde.ui.components.GameButton
import app.krafted.nightmarehorde.ui.components.InfoChip
import app.krafted.nightmarehorde.ui.components.LockOverlay
import app.krafted.nightmarehorde.ui.components.MenuBackdrop
import app.krafted.nightmarehorde.ui.components.SelectableCard
import app.krafted.nightmarehorde.ui.theme.rememberGameFonts

@Composable
fun MapSelectScreen(
    characterClass: CharacterClass,
    isMapUnlocked: (MapType) -> Boolean,
    onMapSelected: (MapType) -> Unit,
    onBack: () -> Unit
) {
    val fonts = rememberGameFonts()
    val creepster = fonts.creepster
    val blackOpsOne = fonts.blackOpsOne

    val maps = MapType.entries
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedMap = maps[selectedIndex]
    val accent = Color(selectedMap.accentColor)

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        MenuBackdrop(
            overlay = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.75f),
                    Color.Black.copy(alpha = 0.55f),
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
                // ─── Left: Map carousel ───────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(0.55f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "SELECT DEPLOYMENT ZONE",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontFamily = creepster,
                            letterSpacing = 3.sp,
                            color = Color(0xFFFF3300),
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(3f, 5f),
                                blurRadius = 8f
                            )
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InfoChip(
                        label = "DEPLOYING:",
                        value = characterClass.displayName.uppercase(),
                        blackOpsOne = blackOpsOne,
                        modifier = Modifier.fillMaxWidth(),
                        backgroundAlpha = 0.4f,
                        verticalPadding = 6.dp,
                        gap = 8.dp,
                        valueFontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        state = rememberLazyListState(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(maps) { index, mapType ->
                            MapCard(
                                mapType = mapType,
                                isSelected = index == selectedIndex,
                                isUnlocked = isMapUnlocked(mapType),
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
                            isLocked = !isMapUnlocked(selectedMap),
                            blackOpsOne = blackOpsOne,
                            onClick = {
                                if (isMapUnlocked(selectedMap)) {
                                    onMapSelected(selectedMap)
                                }
                            }
                        )
                    }
                }

                // ─── Right: Map detail panel ──────────────────────────────
                MapDetailPanel(
                    mapType = selectedMap,
                    isUnlocked = isMapUnlocked(selectedMap),
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

// ─── Map Card ─────────────────────────────────────────────────────────────────

@Composable
private fun MapCard(
    mapType: MapType,
    isSelected: Boolean,
    isUnlocked: Boolean,
    blackOpsOne: FontFamily,
    onClick: () -> Unit
) {
    SelectableCard(
        isSelected = isSelected,
        accent = Color(mapType.accentColor),
        onClick = onClick,
        modifier = Modifier.size(width = 110.dp, height = 132.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(mapType.backgroundColor).copy(alpha = if (isUnlocked) 0.8f else 0.3f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = mapType.mapIcon,
                fontSize = 28.sp,
                modifier = Modifier.alpha(if (isUnlocked) 1f else 0.3f)
            )

            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = mapType.displayName.uppercase(),
            fontFamily = blackOpsOne,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = if (isSelected) Color(mapType.accentColor) else Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (!isUnlocked) {
            Text(
                text = mapType.unlockRequirement,
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

// ─── Map Detail Panel ────────────────────────────────────────────────────────

@Composable
private fun MapDetailPanel(
    mapType: MapType,
    isUnlocked: Boolean,
    accent: Color,
    creepster: FontFamily,
    blackOpsOne: FontFamily,
    modifier: Modifier = Modifier
) {
    DetailPanel(accent = accent, modifier = modifier) {
        Text(
            text = mapType.displayName.uppercase(),
            style = TextStyle(
                fontSize = 30.sp,
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

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = mapType.description,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoChip(
            label = "SIZE",
            value = "${mapType.mapWidth.toInt()} × ${mapType.mapHeight.toInt()}",
            blackOpsOne = blackOpsOne,
            modifier = Modifier.fillMaxWidth(),
            valueFontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        AccentSection(
            title = "SPECIAL FEATURE",
            body = mapType.specialFeature,
            accent = accent,
            blackOpsOne = blackOpsOne,
            modifier = Modifier.fillMaxWidth(),
            titleFontSize = 11.sp
        )

        if (!isUnlocked) {
            LockOverlay(requirement = mapType.unlockRequirement, blackOpsOne = blackOpsOne)
        }
    }
}

// ─── Icon helpers ──────────────────────────────────────────────────────────────

private val MapType.mapIcon: String
    get() = when (this) {
        MapType.SUBURBS -> "🏙"         // 🏙
        MapType.MALL -> "🛒"             // 🛒
        MapType.ASHEN_WASTES -> "🏜"     // 🏜
        MapType.MILITARY_BASE -> "🛡"    // 🛡
        MapType.LAB -> "🧬"              // 🧬
    }
