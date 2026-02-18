package app.krafted.nightmarehorde.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.game.data.UpgradeChoice
import app.krafted.nightmarehorde.game.data.UpgradeRarity

/**
 * Modal overlay presented when the player levels up.
 * Shows 3 upgrade choices in a card layout with level indicators.
 * Tapping a card applies the upgrade and resumes the game.
 *
 * @param isVisible Whether this overlay is shown
 * @param level The player's new level
 * @param upgrades The 3 upgrade options to present (with current/next level info)
 * @param onUpgradeSelected Callback when the player picks an upgrade
 */
@Composable
fun LevelUpScreen(
    isVisible: Boolean,
    level: Int,
    upgrades: List<UpgradeChoice>,
    onUpgradeSelected: (UpgradeChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.8f, animationSpec = tween(300)),
        exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.8f, animationSpec = tween(200)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC000000))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Title
                val infiniteTransition = rememberInfiniteTransition(label = "levelup_glow")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )

                Text(
                    text = "LEVEL UP!",
                    color = Color(0xFFFFD700).copy(alpha = glowAlpha),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Level $level",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                // Upgrade cards
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    upgrades.forEach { choice ->
                        UpgradeCard(
                            choice = choice,
                            onClick = { onUpgradeSelected(choice) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Tap an upgrade to continue",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun UpgradeCard(
    choice: UpgradeChoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val upgrade = choice.upgrade
    val borderColor = Color(upgrade.rarity.color)
    val bgGradient = when (upgrade.rarity) {
        UpgradeRarity.LEGENDARY -> Brush.verticalGradient(
            colors = listOf(Color(0xFF3D2E00), Color(0xFF1A1500))
        )
        UpgradeRarity.EPIC -> Brush.verticalGradient(
            colors = listOf(Color(0xFF2D1A3D), Color(0xFF150D1F))
        )
        UpgradeRarity.RARE -> Brush.verticalGradient(
            colors = listOf(Color(0xFF1A2D3D), Color(0xFF0D1520))
        )
        UpgradeRarity.COMMON -> Brush.verticalGradient(
            colors = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A))
        )
    }

    val borderWidth = when (upgrade.rarity) {
        UpgradeRarity.LEGENDARY -> 2.5.dp
        UpgradeRarity.EPIC -> 2.dp
        else -> 1.5.dp
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .background(bgGradient)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        // Icon
        Image(
            painter = painterResource(id = upgrade.iconRes),
            contentDescription = upgrade.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Name
        Text(
            text = upgrade.name,
            color = Color(upgrade.rarity.color),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        // Level indicator (only for stackable upgrades with maxLevel > 1)
        if (upgrade.maxLevel > 1) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lv ${choice.nextLevel}/${upgrade.maxLevel}",
                color = Color(0xFFFFD700),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            // Dot indicators
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..upgrade.maxLevel) {
                    val dotColor = when {
                        i <= choice.currentLevel -> Color(0xFFFFD700) // Already picked
                        i == choice.nextLevel -> Color(0xFFFFD700).copy(alpha = 0.5f) // About to pick
                        else -> Color.White.copy(alpha = 0.2f) // Not yet
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    if (i < upgrade.maxLevel) {
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Description (level-aware)
        Text(
            text = choice.displayDescription,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Rarity badge
        Text(
            text = upgrade.rarity.name,
            color = Color(upgrade.rarity.color),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .background(
                    Color(upgrade.rarity.color).copy(alpha = 0.15f),
                    RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}
