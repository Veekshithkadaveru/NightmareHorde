package app.krafted.nightmarehorde.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

private data class PermanentUpgrade(
    val name: String,
    val effect: String,
    val maxLevel: Int,
    val costPerLevel: Int
)

private val PERMANENT_UPGRADES = listOf(
    PermanentUpgrade("Toughness",    "+10 Max HP",         maxLevel = 10, costPerLevel = 100),
    PermanentUpgrade("Firepower",    "+5% Damage",         maxLevel = 10, costPerLevel = 150),
    PermanentUpgrade("Mobility",     "+5% Speed",          maxLevel = 5,  costPerLevel = 200),
    PermanentUpgrade("Scavenger",    "+10% More Drops",    maxLevel = 10, costPerLevel = 120),
    PermanentUpgrade("Drone Master", "+10% Drone Damage",  maxLevel = 5,  costPerLevel = 250),
    PermanentUpgrade("Ammo Belt",    "+10% Ammo",          maxLevel = 10, costPerLevel = 100),
    PermanentUpgrade("Second Wind",  "+1 Revive/Run",      maxLevel = 3,  costPerLevel = 500),
)

@Composable
fun ShopScreen(
    onBack: () -> Unit
) {
    val creepster = FontFamily(Font(R.font.creepster))
    val blackOpsOne = FontFamily(Font(R.font.black_ops_one))

    var supplies by remember { mutableIntStateOf(0) }
    val levels = remember { mutableStateListOf(*IntArray(PERMANENT_UPGRADES.size) { 0 }.toTypedArray()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black,
                        Color(0xFF0D0D0D),
                        Color(0xFF1A0A0A),
                        Color.Black
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPGRADES",
                    style = TextStyle(
                        fontSize = 56.sp,
                        fontFamily = creepster,
                        color = Color(0xFFFF3300),
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(4f, 8f),
                            blurRadius = 10f
                        )
                    )
                )

                // Supplies balance
                Box(
                    modifier = Modifier
                        .clip(CutCornerShape(8.dp))
                        .background(Color(0xFF1A1A1A))
                        .border(2.dp, Color(0xFFFFD700), CutCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "⚙  $supplies  SUPPLIES",
                        fontFamily = blackOpsOne,
                        fontSize = 16.sp,
                        color = Color(0xFFFFD700),
                        letterSpacing = 2.sp
                    )
                }
            }

            // Gold divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(1.dp)
                    .background(Color(0xFFFFD700).copy(alpha = 0.4f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Upgrade grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(PERMANENT_UPGRADES) { index, upgrade ->
                    val currentLevel = levels[index]
                    val isMaxed = currentLevel >= upgrade.maxLevel
                    val canAfford = supplies >= upgrade.costPerLevel
                    val canBuy = !isMaxed && canAfford

                    UpgradeCard(
                        upgrade = upgrade,
                        currentLevel = currentLevel,
                        canBuy = canBuy,
                        isMaxed = isMaxed,
                        blackOpsOne = blackOpsOne,
                        onBuy = {
                            supplies -= upgrade.costPerLevel
                            levels[index] = currentLevel + 1
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Back button — bottom left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .height(52.dp)
                .widthIn(min = 160.dp)
                .clip(CutCornerShape(10.dp))
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF333333), Color(0xFF111111)))
                )
                .border(2.dp, Color(0xFFFFD700), CutCornerShape(10.dp))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
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
}

@Composable
private fun UpgradeCard(
    upgrade: PermanentUpgrade,
    currentLevel: Int,
    canBuy: Boolean,
    isMaxed: Boolean,
    blackOpsOne: FontFamily,
    onBuy: () -> Unit
) {
    val borderColor = when {
        isMaxed -> Color(0xFFFFD700)
        canBuy -> Color(0xFF555555)
        else -> Color(0xFF333333)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(12.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF242424), Color(0xFF111111)))
            )
            .border(2.dp, borderColor, CutCornerShape(12.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Name
        Text(
            text = upgrade.name.uppercase(),
            fontFamily = blackOpsOne,
            fontSize = 14.sp,
            color = if (isMaxed) Color(0xFFFFD700) else Color.White,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Effect
        Text(
            text = upgrade.effect,
            fontSize = 12.sp,
            color = Color(0xFFAAAAAA),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Level progress
        Text(
            text = if (isMaxed) "MAX" else "Lv $currentLevel / ${upgrade.maxLevel}",
            fontFamily = blackOpsOne,
            fontSize = 11.sp,
            color = if (isMaxed) Color(0xFFFFD700) else Color(0xFF888888),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { currentLevel.toFloat() / upgrade.maxLevel },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (isMaxed) Color(0xFFFFD700) else Color(0xFFFF3300),
            trackColor = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Buy button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(CutCornerShape(8.dp))
                .background(
                    if (canBuy)
                        Brush.horizontalGradient(listOf(Color(0xFFFF3300), Color(0xFF990000)))
                    else
                        Brush.horizontalGradient(listOf(Color(0xFF333333), Color(0xFF222222)))
                )
                .then(
                    if (canBuy) Modifier.clickable(onClick = onBuy) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    isMaxed -> "MAXED"
                    !canBuy && currentLevel == 0 -> "⚙ ${upgrade.costPerLevel}"
                    else -> "⚙ ${upgrade.costPerLevel}"
                },
                fontFamily = blackOpsOne,
                fontSize = 13.sp,
                color = if (canBuy) Color.White else Color(0xFF666666),
                letterSpacing = 1.sp
            )
        }
    }
}
