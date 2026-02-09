package app.krafted.nightmarehorde.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
    
    // Lifecycle management
    LaunchedEffect(Unit) {
        viewModel.startGame()
    }
    
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopGame()
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Game surface - renders the game world
        // We use the basic overload since we have the list
        GameSurface(
            entities = entities,
            camera = viewModel.camera,
            spriteRenderer = viewModel.spriteRenderer,
            backgroundColor = Color(0xFF1a1a2e)
        )
        
        // HUD overlay (placeholder)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        ) {
            // Health, ammo, wave info will go here
        }
    }
}
