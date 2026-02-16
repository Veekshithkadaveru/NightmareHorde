package app.krafted.nightmarehorde.engine.rendering

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.krafted.nightmarehorde.game.systems.DayNightCycle

/**
 * Rendering system that draws a full-screen colour overlay based on the day/night cycle.
 *
 * Colour progression:
 *  - Day        → clear (no tint)
 *  - Dusk       → warm orange-amber → deep purple-blue
 *  - Full Night → dark blue-purple
 *  - Dawn       → deep purple-blue → warm orange-amber → clear
 *
 * This is NOT a GameSystem — it's called directly from the rendering pipeline
 * (GameScreen Canvas) after all sprites are drawn, so the tint sits on top of everything.
 */
class LightingSystem {

    companion object {
        // Colour palette for the cycle
        private val COLOR_DAY = Color(0x00000000)         // fully transparent
        private val COLOR_SUNSET = Color(0xFFFF8C42)      // warm amber-orange
        private val COLOR_NIGHT = Color(0xFF0D0B2E)       // deep dark blue-purple
    }

    /**
     * Draw the time-of-day overlay using individual parameters.
     * This variant is suitable for use with Compose state flows.
     *
     * SAFETY: The four parameters come from independent StateFlows that are
     * updated sequentially. Compose can recompose between any two updates,
     * so we may see an inconsistent snapshot (e.g. phase=DUSK but progress
     * from NIGHT). All intermediate values are clamped and the entire call
     * is wrapped in try/catch to prevent a bad Color from crashing the app.
     *
     * @param drawScope      Canvas draw scope.
     * @param phase          Current time phase.
     * @param nightIntensity 0.0 = full day, 1.0 = full night.
     * @param phaseProgress  Progress within the current phase (0..1).
     * @param overlayAlpha   Target alpha for the full-night overlay.
     */
    fun render(
        drawScope: DrawScope,
        phase: DayNightCycle.TimePhase,
        nightIntensity: Float,
        phaseProgress: Float,
        overlayAlpha: Float
    ) {
        // Clamp inputs — state flows can be slightly inconsistent across frames
        val ni = nightIntensity.coerceIn(0f, 1f)
        val pp = phaseProgress.coerceIn(0f, 1f)
        val oa = overlayAlpha.coerceIn(0f, 1f)

        if (ni <= 0f) return // Day — nothing to draw

        val overlayColor: Color = try {
            when (phase) {
                DayNightCycle.TimePhase.DUSK -> {
                    if (pp < 0.5f) {
                        val t = pp * 2f
                        lerpColor(COLOR_DAY, COLOR_SUNSET, t * ni)
                    } else {
                        val t = (pp - 0.5f) * 2f
                        lerpColor(COLOR_SUNSET, COLOR_NIGHT, t)
                            .copy(alpha = (ni * 0.8f).coerceIn(0f, 1f))
                    }
                }
                DayNightCycle.TimePhase.NIGHT -> {
                    COLOR_NIGHT.copy(alpha = oa)
                }
                DayNightCycle.TimePhase.DAWN -> {
                    if (pp < 0.5f) {
                        val t = pp * 2f
                        lerpColor(COLOR_NIGHT, COLOR_SUNSET, t)
                            .copy(alpha = (ni * 0.8f).coerceIn(0f, 1f))
                    } else {
                        val t = (pp - 0.5f) * 2f
                        val factor = (t * (1f - ni)).coerceIn(0f, 1f)
                        lerpColor(COLOR_SUNSET, COLOR_DAY, factor)
                            .copy(alpha = (ni * 0.5f).coerceIn(0f, 1f))
                    }
                }
                DayNightCycle.TimePhase.DAY -> return // no overlay
            }
        } catch (_: IllegalArgumentException) {
            // Defensive: if a Color component is somehow out of range, skip this frame
            return
        }

        drawScope.drawRect(
            color = overlayColor,
            size = drawScope.size
        )
    }

    // ─── Colour Helpers ───────────────────────────────────────────────────

    /**
     * Linearly interpolate between two Colors, clamping all components to [0, 1].
     */
    private fun lerpColor(from: Color, to: Color, t: Float): Color {
        val clamped = t.coerceIn(0f, 1f)
        return Color(
            red = lerp(from.red, to.red, clamped).coerceIn(0f, 1f),
            green = lerp(from.green, to.green, clamped).coerceIn(0f, 1f),
            blue = lerp(from.blue, to.blue, clamped).coerceIn(0f, 1f),
            alpha = lerp(from.alpha, to.alpha, clamped).coerceIn(0f, 1f)
        )
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t
}
