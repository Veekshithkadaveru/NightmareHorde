package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem

/**
 * Manages the Day/Night cycle that directly affects gameplay difficulty and visuals.
 *
 * Cycle Structure (repeating):
 *  - Day   : 110 seconds  — standard difficulty
 *  - Dusk  :  10 seconds  — smooth transition to night
 *  - Night :  50 seconds  — hard mode
 *  - Dawn  :  10 seconds  — smooth transition back to day
 *  Total cycle = 180 seconds (3 minutes)
 *
 * Night modifiers:
 *  - Zombie speed  ×1.3
 *  - Zombie damage ×1.25
 *  - Spawn rate    ×1.5
 *
 * Runs at priority 1 (before all other systems) so multipliers are up-to-date
 * each frame before AI, combat, and spawning systems read them.
 */
class DayNightCycle : GameSystem(priority = 1) {

    // ─── Phase Enum ───────────────────────────────────────────────────────

    enum class TimePhase { DAY, DUSK, NIGHT, DAWN }

    // ─── Configuration ────────────────────────────────────────────────────

    companion object {
        /** Full cycle length in seconds */
        const val CYCLE_DURATION = 180f

        /** Phase boundaries within one cycle (seconds) */
        const val DAY_END = 110f       // 0 – 110 = Day
        const val DUSK_END = 120f      // 110 – 120 = Dusk transition
        const val NIGHT_END = 170f     // 120 – 170 = Night
        const val DAWN_END = 180f      // 170 – 180 = Dawn transition

        // Night-mode multipliers (full night)
        const val NIGHT_SPEED_MULTIPLIER = 1.3f
        const val NIGHT_DAMAGE_MULTIPLIER = 1.25f
        const val NIGHT_SPAWN_RATE_MULTIPLIER = 1.5f

        // Day-mode multipliers (baseline)
        const val DAY_SPEED_MULTIPLIER = 1.0f
        const val DAY_DAMAGE_MULTIPLIER = 1.0f
        const val DAY_SPAWN_RATE_MULTIPLIER = 1.0f

        // Visual overlay
        const val NIGHT_OVERLAY_ALPHA = 0.45f
    }

    // ─── State ────────────────────────────────────────────────────────────
    // All mutable fields are @Volatile because update() runs on the game-loop
    // thread (Dispatchers.Default) while the UI / HUD observer reads them
    // from the main thread. Without @Volatile, ARM memory ordering can cause
    // stale or torn reads.

    /** Total elapsed game time, continuously accumulated. */
    @Volatile var totalElapsedTime: Float = 0f
        private set

    /** Current position within the repeating cycle (0 .. CYCLE_DURATION). */
    @Volatile var cycleTime: Float = 0f
        private set

    /** The current phase of the day/night cycle. */
    @Volatile var currentPhase: TimePhase = TimePhase.DAY
        private set

    /**
     * Normalised "nightness" intensity: 0.0 = full day, 1.0 = full night.
     * Smoothly interpolates during Dusk and Dawn transitions.
     */
    @Volatile var nightIntensity: Float = 0f
        private set

    // ─── Gameplay Multipliers (read by other systems) ─────────────────────

    /** Applied to zombie movement speed. */
    @Volatile var speedMultiplier: Float = DAY_SPEED_MULTIPLIER
        private set

    /** Applied to zombie damage. */
    @Volatile var damageMultiplier: Float = DAY_DAMAGE_MULTIPLIER
        private set

    /** Applied to spawn interval (inverted — lower interval = more spawns). */
    @Volatile var spawnRateMultiplier: Float = DAY_SPAWN_RATE_MULTIPLIER
        private set

    /** Alpha of the night overlay tint (0.0 = invisible, 0.45 = night). */
    @Volatile var overlayAlpha: Float = 0f
        private set

    // ─── Cycle Counter ────────────────────────────────────────────────────

    /** How many full day/night cycles have completed. */
    @Volatile var cycleCount: Int = 0
        private set

    /** Progress through current phase (0.0 – 1.0). */
    @Volatile var phaseProgress: Float = 0f
        private set

    // ─── System Update ────────────────────────────────────────────────────

    override fun update(deltaTime: Float, entities: List<Entity>) {
        totalElapsedTime += deltaTime
        cycleTime = totalElapsedTime % CYCLE_DURATION
        cycleCount = (totalElapsedTime / CYCLE_DURATION).toInt()

        // Determine phase and night intensity
        when {
            cycleTime < DAY_END -> {
                currentPhase = TimePhase.DAY
                nightIntensity = 0f
                phaseProgress = cycleTime / DAY_END
            }
            cycleTime < DUSK_END -> {
                currentPhase = TimePhase.DUSK
                // Smooth interpolation from 0 → 1 across the dusk window
                val t = (cycleTime - DAY_END) / (DUSK_END - DAY_END)
                nightIntensity = smoothStep(t)
                phaseProgress = t
            }
            cycleTime < NIGHT_END -> {
                currentPhase = TimePhase.NIGHT
                nightIntensity = 1f
                phaseProgress = (cycleTime - DUSK_END) / (NIGHT_END - DUSK_END)
            }
            else -> {
                currentPhase = TimePhase.DAWN
                // Smooth interpolation from 1 → 0 across the dawn window
                val t = (cycleTime - NIGHT_END) / (DAWN_END - NIGHT_END)
                nightIntensity = smoothStep(1f - t)
                phaseProgress = t
            }
        }

        // Derive multipliers from night intensity
        speedMultiplier = lerp(DAY_SPEED_MULTIPLIER, NIGHT_SPEED_MULTIPLIER, nightIntensity)
        damageMultiplier = lerp(DAY_DAMAGE_MULTIPLIER, NIGHT_DAMAGE_MULTIPLIER, nightIntensity)
        spawnRateMultiplier = lerp(DAY_SPAWN_RATE_MULTIPLIER, NIGHT_SPAWN_RATE_MULTIPLIER, nightIntensity)
        overlayAlpha = lerp(0f, NIGHT_OVERLAY_ALPHA, nightIntensity)
    }

    /** Reset cycle state (called on game start / restart). */
    fun reset() {
        totalElapsedTime = 0f
        cycleTime = 0f
        currentPhase = TimePhase.DAY
        nightIntensity = 0f
        speedMultiplier = DAY_SPEED_MULTIPLIER
        damageMultiplier = DAY_DAMAGE_MULTIPLIER
        spawnRateMultiplier = DAY_SPAWN_RATE_MULTIPLIER
        overlayAlpha = 0f
        cycleCount = 0
        phaseProgress = 0f
    }

    // ─── Convenience Queries ──────────────────────────────────────────────

    /** True when night or transitioning into/out of night. */
    val isNightActive: Boolean
        get() = nightIntensity > 0f

    /** True only during the full-night phase (not transitions). */
    val isFullNight: Boolean
        get() = currentPhase == TimePhase.NIGHT

    // ─── Math Helpers ─────────────────────────────────────────────────────

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

    /** Smooth-step for pleasing transitions (ease-in-out). */
    private fun smoothStep(t: Float): Float {
        val clamped = t.coerceIn(0f, 1f)
        return clamped * clamped * (3f - 2f * clamped)
    }
}
