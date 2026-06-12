package app.krafted.nightmarehorde.engine.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.krafted.nightmarehorde.engine.core.Vector2

/**
 * Hoisted state for a floating joystick, shared between [Modifier.floatingJoystick]
 * (which writes touch state) and [FloatingJoystickOverlay] (which draws it).
 *
 * @property isActive True while a finger is down and driving the joystick.
 * @property basePosition Centre of the joystick base. Set where the finger lands and
 *   fixed for the whole gesture.
 * @property knobOffset Thumb position relative to [basePosition], clamped to the base
 *   radius — at full deflection the thumb's centre sits on the base's edge.
 */
@Stable
class FloatingJoystickState {
    var isActive by mutableStateOf(false)
        internal set

    var basePosition by mutableStateOf(Offset.Zero)
        internal set

    var knobOffset by mutableStateOf(Offset.Zero)
        internal set
}

/**
 * Remembers a [FloatingJoystickState] across recompositions.
 */
@Composable
fun rememberFloatingJoystickState(): FloatingJoystickState =
    remember { FloatingJoystickState() }

/**
 * Adds floating-joystick touch handling matching Vampire Survivors: the joystick activates
 * on touch-down centred under the finger, the base never moves during the gesture, and the
 * output is direction-only — a unit vector once the finger's raw distance from the base
 * exceeds [deadZone] x [baseRadius], [Vector2.ZERO] inside it. Deflection never scales speed.
 *
 * Tap compatibility: the down is never consumed and moves are consumed only past the system
 * touch slop, so tap/double-tap detectors on the same node (e.g. [Modifier.detectGameGestures])
 * still see clean taps, while real drags cancel them.
 *
 * @param state Hoisted state shared with [FloatingJoystickOverlay].
 * @param onDirectionChange Callback with a unit direction vector, or [Vector2.ZERO] inside
 *   the dead zone.
 * @param onRelease Callback when the finger lifts or the gesture is cancelled.
 * @param baseRadius Radius of the joystick base; also the thumb's travel limit.
 * @param deadZone Minimum finger distance from the base, as a fraction of [baseRadius].
 */
fun Modifier.floatingJoystick(
    state: FloatingJoystickState,
    onDirectionChange: (Vector2) -> Unit,
    onRelease: () -> Unit,
    baseRadius: Dp = 60.dp,
    deadZone: Float = VS_DEAD_ZONE
): Modifier = this.pointerInput(state, baseRadius, deadZone) {
    val baseRadiusPx = baseRadius.toPx()
    val forceMinPx = baseRadiusPx * deadZone
    val touchSlop = viewConfiguration.touchSlop

    awaitEachGesture {
        val down = awaitFirstDown()
        state.basePosition = down.position
        state.knobOffset = Offset.Zero
        state.isActive = true
        onDirectionChange(Vector2.ZERO)

        var pastSlop = false
        try {
            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break

                val rawVector = change.position - state.basePosition
                val force = rawVector.getDistance()
                state.knobOffset =
                    if (force > baseRadiusPx) rawVector * (baseRadiusPx / force) else rawVector

                if (!pastSlop &&
                    (change.position - down.position).getDistance() > touchSlop
                ) {
                    pastSlop = true
                }
                if (pastSlop && change.positionChanged()) {
                    change.consume()
                }

                onDirectionChange(
                    if (force > forceMinPx) {
                        Vector2(rawVector.x / force, rawVector.y / force)
                    } else {
                        Vector2.ZERO
                    }
                )
            }
        } finally {
            state.isActive = false
            state.knobOffset = Offset.Zero
            onRelease()
        }
    }
}

/**
 * Floating joystick wired directly to an [InputManager].
 *
 * Convenience overload of [Modifier.floatingJoystick] that forwards direction updates to
 * [InputManager.updateMovementDirection] and releases to [InputManager.releaseJoystick].
 *
 * @param state Hoisted state shared with [FloatingJoystickOverlay].
 * @param inputManager InputManager to receive direction updates.
 * @param baseRadius Radius of the joystick base; also the thumb's travel limit.
 * @param deadZone Minimum finger distance from the base, as a fraction of [baseRadius].
 */
fun Modifier.floatingJoystick(
    state: FloatingJoystickState,
    inputManager: InputManager,
    baseRadius: Dp = 60.dp,
    deadZone: Float = VS_DEAD_ZONE
): Modifier = floatingJoystick(
    state = state,
    onDirectionChange = { direction -> inputManager.updateMovementDirection(direction) },
    onRelease = { inputManager.releaseJoystick() },
    baseRadius = baseRadius,
    deadZone = deadZone
)

/**
 * Non-interactive visual for the floating joystick: two flat semi-transparent discs with
 * Vampire Survivors' exact colours, alphas, and 35/75 thumb-to-base ratio. The base is
 * intentionally drawn OVER the thumb, as in VS — the thumb shows through the translucent
 * base and only the part poking past its edge appears at full darkness.
 *
 * Registers no pointer input, so it never intercepts taps. Place it as a sibling filling
 * the same area as the node carrying [Modifier.floatingJoystick] so coordinates line up.
 *
 * @param state Hoisted state written by [Modifier.floatingJoystick].
 * @param modifier Modifier for the overlay canvas (should fill the touch area).
 * @param baseRadius Radius of the base disc; keep in sync with [Modifier.floatingJoystick].
 * @param thumbRadius Radius of the thumb disc.
 * @param baseColor Fill colour of the base disc.
 * @param thumbColor Fill colour of the thumb disc.
 * @param baseAlpha Opacity of the base disc.
 * @param thumbAlpha Opacity of the thumb disc.
 */
@Composable
fun FloatingJoystickOverlay(
    state: FloatingJoystickState,
    modifier: Modifier = Modifier,
    baseRadius: Dp = 60.dp,
    thumbRadius: Dp = 28.dp,
    baseColor: Color = Color(0xFFCCCCCC),
    thumbColor: Color = Color(0xFF888888),
    baseAlpha: Float = 0.5f,
    thumbAlpha: Float = 0.5f
) {
    if (!state.isActive) return

    val density = LocalDensity.current
    val baseRadiusPx = with(density) { baseRadius.toPx() }
    val thumbRadiusPx = with(density) { thumbRadius.toPx() }

    Canvas(modifier = modifier.fillMaxSize()) {
        val base = state.basePosition
        drawCircle(
            color = thumbColor.copy(alpha = thumbAlpha),
            radius = thumbRadiusPx,
            center = base + state.knobOffset
        )
        drawCircle(
            color = baseColor.copy(alpha = baseAlpha),
            radius = baseRadiusPx,
            center = base
        )
    }
}

/** Vampire Survivors' dead zone: forceMin 16 on a radius-75 joystick. */
private const val VS_DEAD_ZONE = 16f / 75f
