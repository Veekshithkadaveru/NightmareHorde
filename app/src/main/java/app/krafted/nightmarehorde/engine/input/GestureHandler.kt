package app.krafted.nightmarehorde.engine.input

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import app.krafted.nightmarehorde.engine.core.Vector2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Configuration for gesture detection thresholds.
 */
object GestureConfig {
    /** Maximum time in ms for a touch to be considered a tap */
    const val TAP_TIMEOUT_MS = 200L
    
    /** Maximum time in ms between taps for double-tap */
    const val DOUBLE_TAP_TIMEOUT_MS = 300L
    
    /** Maximum movement in pixels before drag cancels tap */
    const val TAP_SLOP_PX = 20f
}

/**
 * Gesture handler that detects taps and double-taps.
 * 
 * Uses Compose's built-in gesture detection and adds double-tap
 * timing logic to distinguish between single and double taps.
 * 
 * IMPORTANT: This class must be remembered/stable across recompositions
 * to maintain double-tap timing state. Use remember {} when creating.
 * 
 * @param inputManager InputManager to emit events to
 * @param scope CoroutineScope for async operations
 */
class GestureHandler(
    private val inputManager: InputManager,
    private val scope: CoroutineScope
) {
    companion object {
        private const val TAG = "GestureHandler"
    }
    
    private var lastTapTime: Long = 0L
    private var lastTapPosition: Offset = Offset.Zero
    private var pendingTapJob: Job? = null
    
    /**
     * Handles a tap event, detecting double-taps based on timing.
     * 
     * When a tap occurs:
     * - If within DOUBLE_TAP_TIMEOUT_MS of last tap → emit double-tap
     * - Otherwise → wait briefly and emit single tap (unless another tap comes)
     */
    suspend fun onTap(position: Offset) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastTap = currentTime - lastTapTime
        val positionDelta = (position - lastTapPosition).getDistance()
        
        Log.v(TAG, "Tap detected at ($position), time since last: ${timeSinceLastTap}ms")
        
        if (timeSinceLastTap < GestureConfig.DOUBLE_TAP_TIMEOUT_MS && 
            positionDelta < GestureConfig.TAP_SLOP_PX * 2) {
            // Double-tap detected
            pendingTapJob?.cancel()
            pendingTapJob = null
            lastTapTime = 0L // Reset to prevent triple-tap
            
            inputManager.emitDoubleTap(Vector2(position.x, position.y))
        } else {
            // Potential single tap - wait to see if another tap follows
            pendingTapJob?.cancel()
            lastTapTime = currentTime
            lastTapPosition = position
            
            pendingTapJob = scope.launch {
                delay(GestureConfig.DOUBLE_TAP_TIMEOUT_MS)
                // No second tap came, emit single tap
                inputManager.emitTap(Vector2(position.x, position.y))
            }
        }
    }
    
    /**
     * Resets gesture state. Called when game pauses or resets.
     */
    fun reset() {
        pendingTapJob?.cancel()
        pendingTapJob = null
        lastTapTime = 0L
        lastTapPosition = Offset.Zero
    }
}

/**
 * Extension function to add gesture detection to any Modifier.
 * Detects taps and routes them through a GestureHandler.
 * 
 * IMPORTANT: The gestureHandler must be stable (remembered) to preserve
 * state across recompositions. Create it with remember {} in your composable.
 * 
 * @param gestureHandler The handler to process gestures (must be remembered)
 * @param scope CoroutineScope for the tap handler suspend function
 */
fun Modifier.detectGameGestures(
    gestureHandler: GestureHandler,
    scope: CoroutineScope
): Modifier = this.pointerInput(gestureHandler) {
    detectTapGestures(
        onTap = { offset ->
            scope.launch {
                gestureHandler.onTap(offset)
            }
        }
    )
}

