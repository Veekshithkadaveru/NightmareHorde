package app.krafted.nightmarehorde.engine.input

import android.util.Log
import app.krafted.nightmarehorde.engine.core.Vector2
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Represents different types of input events.
 */
sealed class InputEvent {
    data class Tap(val position: Vector2) : InputEvent()
    data class DoubleTap(val position: Vector2) : InputEvent()
    data class JoystickMove(val direction: Vector2) : InputEvent()
    data object JoystickRelease : InputEvent()
}

/**
 * Singleton class managing all touch input state for the game.
 * 
 * Provides reactive flows for:
 * - Movement direction from virtual joystick
 * - Tap and double-tap gesture events
 * 
 * Thread-safe for concurrent access from UI and game loop threads.
 */
@Singleton
class InputManager @Inject constructor() {
    
    companion object {
        private const val TAG = "InputManager"
    }
    
    // Movement direction from joystick (-1 to 1 on each axis)
    private val _movementDirection = MutableStateFlow(Vector2.ZERO)
    val movementDirection: StateFlow<Vector2> = _movementDirection.asStateFlow()
    
    // Gesture events (tap, double-tap)
    private val _gestureEvents = MutableSharedFlow<InputEvent>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val gestureEvents: SharedFlow<InputEvent> = _gestureEvents.asSharedFlow()
    
    // Double-tap events specifically (for turret menu)
    private val _doubleTapEvents = MutableSharedFlow<Vector2>(
        replay = 0,
        extraBufferCapacity = 8
    )
    val doubleTapEvents: SharedFlow<Vector2> = _doubleTapEvents.asSharedFlow()
    
    // Tap events
    private val _tapEvents = MutableSharedFlow<Vector2>(
        replay = 0,
        extraBufferCapacity = 8
    )
    val tapEvents: SharedFlow<Vector2> = _tapEvents.asSharedFlow()
    
    /**
     * Updates the current movement direction from the virtual joystick.
     * Called by VirtualJoystick composable.
     * 
     * @param direction Normalized direction vector with components in range [-1, 1]
     */
    fun updateMovementDirection(direction: Vector2) {
        _movementDirection.value = direction
    }
    
    /**
     * Resets movement to zero when joystick is released.
     */
    fun releaseJoystick() {
        _movementDirection.value = Vector2.ZERO
    }
    
    /**
     * Emits a single tap event.
     * Called by GestureHandler.
     * 
     * @param position Screen position where tap occurred
     */
    suspend fun emitTap(position: Vector2) {
        Log.d(TAG, "Tap at (${position.x}, ${position.y})")
        _tapEvents.emit(position)
        _gestureEvents.emit(InputEvent.Tap(position))
    }
    
    /**
     * Emits a double-tap event (used to open turret placement menu).
     * Called by GestureHandler.
     * 
     * @param position Screen position where double-tap occurred
     */
    suspend fun emitDoubleTap(position: Vector2) {
        Log.d(TAG, "Double-tap at (${position.x}, ${position.y})")
        _doubleTapEvents.emit(position)
        _gestureEvents.emit(InputEvent.DoubleTap(position))
    }
    
    /**
     * Clears all input state. Called when game pauses or resets.
     */
    fun reset() {
        _movementDirection.value = Vector2.ZERO
        Log.d(TAG, "Input state reset")
    }
}

