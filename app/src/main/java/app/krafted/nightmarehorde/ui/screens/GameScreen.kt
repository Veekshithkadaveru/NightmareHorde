package app.krafted.nightmarehorde.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.krafted.nightmarehorde.engine.input.GestureHandler
import app.krafted.nightmarehorde.engine.input.VirtualJoystick
import app.krafted.nightmarehorde.engine.input.detectGameGestures
import app.krafted.nightmarehorde.engine.rendering.GameSurface
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.ui.components.HealthBar

/**
 * Main game screen that displays the game world and HUD.
 */
@Composable
fun GameScreen(
    characterType: CharacterType = CharacterType.CYBERPUNK_DETECTIVE,
    viewModel: GameViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val playerHealth by viewModel.playerHealth.collectAsState()
    val currentWeaponName by viewModel.currentWeaponName.collectAsState()
    val kills by viewModel.killCount.collectAsState()
    val gameTimeSec by viewModel.gameTime.collectAsState()
    val scope = rememberCoroutineScope()

    // Frame tick counter — increments every vsync to force Canvas redraws
    var frameTick by remember { mutableIntStateOf(0) }

    // Create gesture handler for tap/double-tap detection
    val gestureHandler = remember(viewModel.inputManager, scope) {
        GestureHandler(viewModel.inputManager, scope)
    }

    // Lifecycle management
    LaunchedEffect(Unit) {
        viewModel.startGame(characterType)
    }

    // Drive frame-by-frame rendering — this is the game render loop
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { }
            frameTick++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopGame()
            gestureHandler.reset()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Game surface — entitiesProvider reads fresh data each frame
        GameSurface(
            entitiesProvider = {
                // Read frameTick to establish a Compose state dependency
                // so the Canvas re-renders every vsync
                frameTick
                viewModel.gameLoop.getEntitiesSnapshot()
            },
            camera = viewModel.camera,
            spriteRenderer = viewModel.spriteRenderer,
            damageNumberRenderer = viewModel.damageNumberRenderer,
            particleRenderer = viewModel.particleRenderer,
            backgroundColor = Color(0xFF1a1a2e),
            modifier = Modifier.detectGameGestures(gestureHandler, scope)
        )

        // HUD overlay — Health Bar (top-left)
        HealthBar(
            currentHealth = playerHealth.first,
            maxHealth = playerHealth.second,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // Game Timer (top-center, VS-style)
        val minutes = (gameTimeSec / 60f).toInt()
        val seconds = (gameTimeSec % 60f).toInt()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Kill Counter (top-right, VS-style)
        Text(
            text = "Kills: $kills",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        // Debug Weapon Selector (below timer)
        Button(
            onClick = { viewModel.debugCycleWeapon() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 44.dp)
        ) {
            Text(text = "Weapon: $currentWeaponName")
        }

        // Virtual Joystick - bottom left corner
        VirtualJoystick(
            inputManager = viewModel.inputManager,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        )
    }
}
