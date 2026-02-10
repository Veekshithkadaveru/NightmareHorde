package app.krafted.nightmarehorde.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.input.InputManager
import app.krafted.nightmarehorde.engine.physics.MovementSystem
import app.krafted.nightmarehorde.engine.rendering.Camera
import app.krafted.nightmarehorde.engine.rendering.SpriteRenderer
import app.krafted.nightmarehorde.game.data.AssetManager
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.game.entities.PlayerEntity
import app.krafted.nightmarehorde.game.systems.PlayerAnimationSystem
import app.krafted.nightmarehorde.game.systems.PlayerSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    val gameLoop: GameLoop,
    val camera: Camera,
    val spriteRenderer: SpriteRenderer,
    val assetManager: AssetManager,
    val inputManager: InputManager
) : ViewModel() {

    /** Player health exposed for the HUD */
    private val _playerHealth = MutableStateFlow(Pair(100, 100)) // (current, max)
    val playerHealth: StateFlow<Pair<Int, Int>> = _playerHealth.asStateFlow()

    private var isGameRunning = false
    private var playerEntity: Entity? = null

    fun startGame(characterType: CharacterType = CharacterType.CYBERPUNK_DETECTIVE) {
        if (isGameRunning) return
        isGameRunning = true

        // Preload sprite assets (background + selected character)
        assetManager.preload(
            characterType.idleTextureKey,
            characterType.runTextureKey,
            "background_space"
        )

        // --- Register Systems (order by priority) ---
        // PlayerSystem (10): input → velocity, camera follow
        val playerSystem = PlayerSystem(inputManager, camera).apply {
            onPlayerDeath = {
                Log.d("GameViewModel", "Player died! Game Over.")
                // Game over logic will be expanded in later phases
            }
        }
        gameLoop.addSystem(playerSystem)

        // PlayerAnimationSystem (15): animation state machine
        gameLoop.addSystem(PlayerAnimationSystem())

        // MovementSystem (50): applies velocity to position
        gameLoop.addSystem(MovementSystem())

        // --- Create Entities ---

        // Background
        val background = Entity().apply {
            addComponent(TransformComponent(x = 0f, y = 0f))
            addComponent(SpriteComponent(
                textureKey = "background_space",
                layer = 0,
                fillViewport = true
            ))
        }
        gameLoop.addEntity(background)

        // Player
        playerEntity = PlayerEntity.create(characterType = characterType, spawnX = 0f, spawnY = 0f)
        gameLoop.addEntity(playerEntity!!)

        // Start the game loop
        gameLoop.start(viewModelScope)

        // Observe player health for HUD
        viewModelScope.launch {
            while (isGameRunning) {
                playerEntity?.let { player ->
                    val health = player.getComponent(HealthComponent::class)
                    if (health != null) {
                        _playerHealth.value = Pair(health.currentHealth, health.maxHealth)
                    }
                }

                kotlinx.coroutines.delay(16) // ~60 FPS UI sync
            }
        }

        // Double-tap events (for turret menu in Phase D)
        viewModelScope.launch {
            inputManager.doubleTapEvents.collect { position ->
                Log.d("GameViewModel", "Double-tap for turret menu at: $position")
            }
        }
    }

    fun stopGame() {
        isGameRunning = false
        gameLoop.stop()
        gameLoop.clear()
        inputManager.reset()
        playerEntity = null
    }
}
