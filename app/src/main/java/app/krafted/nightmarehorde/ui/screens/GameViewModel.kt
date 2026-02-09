package app.krafted.nightmarehorde.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.input.InputManager
import app.krafted.nightmarehorde.engine.rendering.Camera
import app.krafted.nightmarehorde.engine.rendering.SpriteRenderer
import app.krafted.nightmarehorde.game.data.AssetManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameLoop: GameLoop,
    val camera: Camera,
    val spriteRenderer: SpriteRenderer,
    val assetManager: AssetManager,
    val inputManager: InputManager
) : ViewModel() {

    private val _entities = MutableStateFlow<List<Entity>>(emptyList())
    val entities: StateFlow<List<Entity>> = _entities.asStateFlow()

    private var isGameRunning = false

    fun startGame() {
        if (isGameRunning) return
        isGameRunning = true
        
        // Initialize test entities
        
        // Background - dynamically fills the viewport on any device
        val background = Entity().apply {
            addComponent(TransformComponent(x = 0f, y = 0f))
            addComponent(SpriteComponent(
                textureKey = "background_space",
                layer = 0,
                fillViewport = true  // Automatically fills the screen
            ))
        }
        gameLoop.addEntity(background)

        // Player
        val player = Entity().apply {
            addComponent(TransformComponent(x = 0f, y = 0f, scale = 3f))
            addComponent(SpriteComponent(
                textureKey = "player_idle",
                layer = 1
            ))
        }
        gameLoop.addEntity(player)
        
        gameLoop.start(viewModelScope)
        
        // Observe game loop entities
        // In a real engine, GameLoop would emit updates. For now we poll or expose the list.
        // We'll modify GameLoop to let us observe, or just poll for this phase.
        // Since GameLoop doesn't have an observable list yet, we'll brute-force updates for now
        // by launching a separate coroutine to sync state.
        viewModelScope.launch {
            while (isGameRunning) {
                // Get snapshot of entities for rendering
                // Note: This accesses the thread-safe list from GameLoop
                _entities.value = gameLoop.getEntitiesSnapshot() 
                kotlinx.coroutines.delay(16) // roughly 60 FPS update for UI binding
            }
        }
        
        // Observe input for debugging (will be used by player movement in Phase B)
        viewModelScope.launch {
            inputManager.movementDirection.collect { direction ->
                // Player movement will be implemented in Phase B
                // For now, this just logs that input is being received
            }
        }
        
        viewModelScope.launch {
            inputManager.doubleTapEvents.collect { position ->
                // Turret menu will be implemented in Phase D
                android.util.Log.d("GameViewModel", "Double-tap for turret menu at: $position")
            }
        }
    }

    fun stopGame() {
        isGameRunning = false
        gameLoop.stop()
        gameLoop.clear()
        inputManager.reset()
    }
}
