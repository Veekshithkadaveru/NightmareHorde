package app.krafted.nightmarehorde.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.R
import app.krafted.nightmarehorde.data.local.SettingsRepository
import androidx.compose.foundation.clickable

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit
) {
    val creepster = FontFamily(Font(R.font.creepster))
    val blackOpsOne = FontFamily(Font(R.font.black_ops_one))

    var musicVolume by remember { mutableFloatStateOf(settingsRepository.musicVolume) }
    var sfxVolume by remember { mutableFloatStateOf(settingsRepository.sfxVolume) }
    var showFps by remember { mutableStateOf(settingsRepository.showFps) }
    var screenShake by remember { mutableStateOf(settingsRepository.screenShake) }
    var performanceMode by remember { mutableStateOf(settingsRepository.performanceMode) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "SETTINGS",
                style = TextStyle(
                    fontSize = 64.sp,
                    fontFamily = creepster,
                    color = Color(0xFFFF3300),
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(4f, 8f),
                        blurRadius = 10f
                    )
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Gold divider
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .height(2.dp)
                    .background(Color(0xFFFFD700))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Music Volume
            SettingsSliderRow(
                label = "MUSIC VOLUME",
                value = musicVolume,
                onValueChange = { musicVolume = it; settingsRepository.musicVolume = it },
                blackOpsOne = blackOpsOne
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SFX Volume
            SettingsSliderRow(
                label = "SFX VOLUME",
                value = sfxVolume,
                onValueChange = { sfxVolume = it; settingsRepository.sfxVolume = it },
                blackOpsOne = blackOpsOne
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Show FPS toggle
            SettingsToggleRow(
                label = "SHOW FPS",
                checked = showFps,
                onCheckedChange = { showFps = it; settingsRepository.showFps = it },
                blackOpsOne = blackOpsOne
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Screen Shake toggle
            SettingsToggleRow(
                label = "SCREEN SHAKE",
                checked = screenShake,
                onCheckedChange = { screenShake = it; settingsRepository.screenShake = it },
                blackOpsOne = blackOpsOne
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Performance Mode toggle
            SettingsToggleRow(
                label = "30 FPS MODE",
                subLabel = "Improves performance on older devices",
                checked = performanceMode,
                onCheckedChange = { performanceMode = it; settingsRepository.performanceMode = it },
                blackOpsOne = blackOpsOne
            )

            Spacer(modifier = Modifier.height(48.dp))
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
                    Brush.horizontalGradient(
                        listOf(Color(0xFF333333), Color(0xFF111111))
                    )
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
private fun SettingsSliderRow(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    blackOpsOne: FontFamily
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A1A), Color(0xFF111111))
                )
            )
            .border(1.dp, Color(0xFF3A1010), CutCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontFamily = blackOpsOne,
                    fontSize = 16.sp,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "${value.toInt()}%",
                    fontFamily = blackOpsOne,
                    fontSize = 16.sp,
                    color = Color(0xFFFFD700)
                )
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF3300),
                    activeTrackColor = Color(0xFFFF3300),
                    inactiveTrackColor = Color(0xFF4A1A1A)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    blackOpsOne: FontFamily,
    subLabel: String? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A1A1A), Color(0xFF111111))
                )
            )
            .border(1.dp, Color(0xFF3A1010), CutCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontFamily = blackOpsOne,
                    fontSize = 16.sp,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                if (subLabel != null) {
                    Text(
                        text = subLabel,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFFF3300),
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF333333)
                )
            )
        }
    }
}
