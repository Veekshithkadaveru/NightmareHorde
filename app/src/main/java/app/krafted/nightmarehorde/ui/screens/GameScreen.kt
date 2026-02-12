package app.krafted.nightmarehorde.ui.screens

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
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

        // Debug Weapon Selector
        Button(
            onClick = { viewModel.debugCycleWeapon() },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
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
