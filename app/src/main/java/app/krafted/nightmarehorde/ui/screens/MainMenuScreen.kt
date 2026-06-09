package app.krafted.nightmarehorde.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.ui.components.GameButton
import app.krafted.nightmarehorde.ui.components.MenuBackdrop
import app.krafted.nightmarehorde.ui.components.rememberPulseScale
import app.krafted.nightmarehorde.ui.theme.rememberGameFonts
import kotlinx.coroutines.delay

@Composable
fun MainMenuScreen(
    onPlayClicked: () -> Unit,
    onShopClicked: () -> Unit = {},
    onSettingsClicked: () -> Unit = {}
) {
    val context = LocalContext.current
    val fonts = rememberGameFonts()

    Box(modifier = Modifier.fillMaxSize()) {
        MenuBackdrop(
            overlay = Brush.verticalGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.5f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.9f)
                )
            ),
            contentDescription = "Menu Background"
        )

        // Title flicker: occasional brief dips in opacity mimic a failing neon sign.
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
            Text(
                text = "NIGHTMARE HORDE",
                style = TextStyle(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = fonts.creepster,
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
                    fontFamily = fonts.blackOpsOne,
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

            PulseButton(
                text = "START RUN",
                isPrimary = true,
                fontFamily = fonts.blackOpsOne,
                onClick = onPlayClicked
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                MenuButton(
                    text = "UPGRADES",
                    fontFamily = fonts.blackOpsOne,
                    onClick = onShopClicked,
                    modifier = Modifier.weight(1f)
                )
                MenuButton(
                    text = "LOADOUT",
                    fontFamily = fonts.blackOpsOne,
                    onClick = { Toast.makeText(context, "Coming Soon: Phase F2", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.weight(1f)
                )
                MenuButton(
                    text = "SETTINGS",
                    fontFamily = fonts.blackOpsOne,
                    onClick = onSettingsClicked,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PulseButton(text: String, isPrimary: Boolean = false, fontFamily: FontFamily, onClick: () -> Unit) {
    val pulse = rememberPulseScale(enabled = isPrimary)
    GameButton(
        onClick = onClick,
        shape = CutCornerShape(12.dp),
        fill = Brush.horizontalGradient(
            colors = if (isPrimary) listOf(Color(0xFFFF3300), Color(0xFF990000))
            else listOf(Color(0xFF333333), Color(0xFF111111))
        ),
        borderColor = if (isPrimary) Color(0xFFFFD700) else Color.DarkGray,
        borderWidth = 3.dp,
        innerScrim = Color.Black.copy(alpha = 0.2f),
        modifier = Modifier
            .scale(if (isPrimary) pulse else 1f)
            .widthIn(min = 280.dp)
            .height(64.dp)
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
    GameButton(
        onClick = onClick,
        fill = Brush.verticalGradient(listOf(Color(0xFF444444), Color(0xFF1A1A1A))),
        borderColor = Color.Gray,
        innerScrim = Color.Black.copy(alpha = 0.3f),
        modifier = modifier.height(55.dp)
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
