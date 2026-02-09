package app.krafted.nightmarehorde.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.krafted.nightmarehorde.engine.input.GestureHandler
import app.krafted.nightmarehorde.engine.input.VirtualJoystick
import app.krafted.nightmarehorde.engine.input.detectGameGestures
import app.krafted.nightmarehorde.engine.rendering.GameSurface

/**
 * Main game screen that displays the game world and HUD.
 */
@Composable
fun GameScreen(
    viewModel: GameViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val entities by viewModel.entities.collectAsState()
    val scope = rememberCoroutineScope()
    
    // Create gesture handler for tap/double-tap detection
    val gestureHandler = remember(viewModel.inputManager, scope) {
        GestureHandler(viewModel.inputManager, scope)
    }
    
    // Lifecycle management
    LaunchedEffect(Unit) {
        viewModel.startGame()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopGame()
            gestureHandler.reset()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Game surface with gesture detection overlay
        GameSurface(
            entities = entities,
            camera = viewModel.camera,
            spriteRenderer = viewModel.spriteRenderer,
            backgroundColor = Color(0xFF1a1a2e),
            modifier = Modifier.detectGameGestures(gestureHandler, scope)
        )
        
        // HUD overlay (placeholder)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            // Health, ammo, wave info will go here
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
