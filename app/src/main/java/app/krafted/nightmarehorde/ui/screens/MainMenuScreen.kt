package app.krafted.nightmarehorde.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.R
import kotlinx.coroutines.delay

@Composable
fun MainMenuScreen(
    onPlayClicked: () -> Unit
) {
    val context = LocalContext.current

    val creepster = FontFamily(Font(R.font.creepster))
    val blackOpsOne = FontFamily(Font(R.font.black_ops_one))

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.menu_bg),
            contentDescription = "Menu Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Atmospheric Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.9f)
                        )
                    )
                )
        )

        // Subtle Glitch/Flicker Effect for Title
        var isVisible by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            while (true) {
                delay((2000..5000).random().toLong())
                isVisible = false
                delay(50)
                isVisible = true
                delay(100)
                isVisible = false
                delay(50)
                isVisible = true
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            // Title text with shadow and alpha change
            Text(
                text = "NIGHTMARE HORDE",
                style = TextStyle(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = creepster,
                    letterSpacing = 8.sp,
                    color = Color.Red.copy(alpha = 0.9f),
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(6f, 10f),
                        blurRadius = 12f
                    )
                ),
                modifier = Modifier.alpha(if (isVisible) 1f else 0.8f)
            )
            Text(
                text = "SURVIVAL MODE",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = blackOpsOne,
                    letterSpacing = 6.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 6f),
                        blurRadius = 8f
                    )
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Menu Buttons
            PulseButton(
                text = "START RUN",
                isPrimary = true,
                fontFamily = blackOpsOne,
                onClick = onPlayClicked
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                MenuButton(
                    text = "UPGRADES",
                    fontFamily = blackOpsOne,
                    onClick = { Toast.makeText(context, "Coming Soon: Phase F2", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.weight(1f)
                )
                MenuButton(
                    text = "LOADOUT",
                    fontFamily = blackOpsOne,
                    onClick = { Toast.makeText(context, "Coming Soon: Phase F1", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.weight(1f)
                )
                MenuButton(
                    text = "SETTINGS",
                    fontFamily = blackOpsOne,
                    onClick = { Toast.makeText(context, "Coming Soon: Phase F1", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PulseButton(text: String, isPrimary: Boolean = false, fontFamily: FontFamily, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .scale(if (isPrimary) pulseScale else 1f)
            .widthIn(min = 280.dp)
            .height(64.dp)
            .clip(CutCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    colors = if (isPrimary) listOf(Color(0xFFFF3300), Color(0xFF990000))
                    else listOf(Color(0xFF333333), Color(0xFF111111))
                )
            )
            .border(
                width = 3.dp,
                color = if (isPrimary) Color(0xFFFFD700) else Color.DarkGray,
                shape = CutCornerShape(12.dp)
            )
            .background(Color.Black.copy(alpha = 0.2f)) // slight inner shadow effect
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = if (isPrimary) 26.sp else 20.sp,
            letterSpacing = 6.sp,
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
fun MenuButton(text: String, fontFamily: FontFamily, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(55.dp)
            .clip(CutCornerShape(8.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF444444), Color(0xFF1A1A1A))
                )
            )
            .border(
                width = 2.dp,
                color = Color.Gray,
                shape = CutCornerShape(8.dp)
            )
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.95f),
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp,
            letterSpacing = 4.sp
        )
    }
}
