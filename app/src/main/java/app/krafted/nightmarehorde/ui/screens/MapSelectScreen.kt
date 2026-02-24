package app.krafted.nightmarehorde.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import app.krafted.nightmarehorde.R
import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.game.data.MapType
import app.krafted.nightmarehorde.game.data.MapUnlockManager

@Composable
fun MapSelectScreen(
    characterClass: CharacterClass,
    onMapSelected: (MapType) -> Unit,
    onBack: () -> Unit
) {
    val creepster = FontFamily(Font(R.font.creepster))
    val blackOpsOne = FontFamily(Font(R.font.black_ops_one))

    val maps = MapType.entries
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedMap = maps[selectedIndex]
    val accent = Color(selectedMap.accentColor)

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
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.55f),
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

                    // Character info strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEPLOYING:",
                            fontFamily = blackOpsOne,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = characterClass.displayName.uppercase(),
                            fontFamily = blackOpsOne,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp,
                            color = Color(0xFFFFCC00)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Map cards carousel
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
                                isUnlocked = MapUnlockManager.isUnlocked(mapType),
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
                        MapDeployButton(
                            isLocked = !MapUnlockManager.isUnlocked(selectedMap),
                            accent = accent,
                            blackOpsOne = blackOpsOne,
                            onClick = {
                                if (MapUnlockManager.isUnlocked(selectedMap)) {
                                    onMapSelected(selectedMap)
                                }
                            }
                        )
                    }
                }

                // ─── Right: Map detail panel ──────────────────────────────
                MapDetailPanel(
                    mapType = selectedMap,
                    isUnlocked = MapUnlockManager.isUnlocked(selectedMap),
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
    val accent = Color(mapType.accentColor)
    val borderColor = if (isSelected) accent else Color(0xFF555555)
    val borderWidth = if (isSelected) 3.dp else 1.5.dp

    val infiniteTransition = rememberInfiniteTransition(label = "mapCardGlow")
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
            .width(110.dp)
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
        // Map color preview box
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(mapType.backgroundColor).copy(alpha = 0.8f),
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!isUnlocked) {
                Text(
                    text = "\uD83D\uDD12",
                    fontSize = 28.sp,
                    modifier = Modifier.alpha(0.7f)
                )
            } else {
                // Map icon based on type
                Text(
                    text = mapType.mapIcon,
                    fontSize = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = mapType.displayName.uppercase(),
            fontFamily = blackOpsOne,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            color = if (isSelected) accent else Color.White.copy(alpha = 0.8f),
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
        // Map name
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

        // Description
        Text(
            text = mapType.description,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Map size info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SIZE",
                fontFamily = blackOpsOne,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${mapType.mapWidth.toInt()} × ${mapType.mapHeight.toInt()}",
                fontFamily = blackOpsOne,
                fontSize = 13.sp,
                letterSpacing = 1.sp,
                color = Color(0xFFFFCC00)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Special feature section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.08f))
                .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "SPECIAL FEATURE",
                fontFamily = blackOpsOne,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                color = accent
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mapType.specialFeature,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
        }

        // Lock overlay
        if (!isUnlocked) {
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
                        text = mapType.unlockRequirement,
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

// ─── Deploy Button ────────────────────────────────────────────────────────────

@Composable
private fun MapDeployButton(
    isLocked: Boolean,
    accent: Color,
    blackOpsOne: FontFamily,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mapDeployPulse")
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

// ─── Icon helpers ──────────────────────────────────────────────────────────────

private val MapType.mapIcon: String
    get() = when (this) {
        MapType.SUBURBS -> "\uD83C\uDFD9"         // 🏙
        MapType.MALL -> "\uD83D\uDED2"             // 🛒
        MapType.ASHEN_WASTES -> "\uD83C\uDFDC"     // 🏜
        MapType.MILITARY_BASE -> "\uD83D\uDEE1"    // 🛡
        MapType.LAB -> "\uD83E\uDDEC"              // 🧬
    }
