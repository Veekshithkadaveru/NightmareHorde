package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Tracks XP, current level, and level-up progression on the player entity.
 *
 * XP formula for the amount needed to reach the next level:
 *   `XP = 10 + (Level × 8) + (Level^1.4)`
 *
 * The component accumulates XP and sets [levelUpPending] to true when the
 * threshold is reached. The ViewModel reads this flag, pauses the game,
 * and presents the level-up UI. After the player picks an upgrade,
 * [consumeLevelUp] is called to clear the flag and advance the level.
 */
class XPComponent : Component {

    var currentXP: Int = 0
        private set

    var currentLevel: Int = 1
        private set

    /** XP required to advance from [currentLevel] to the next level. */
    var xpToNextLevel: Int = calculateXPForLevel(1)
        private set

    /** True when the player has enough XP to level up but hasn't picked an upgrade yet. */
    var levelUpPending: Boolean = false
        private set

    /** Total number of level-ups that have occurred this run. */
    var totalLevelUps: Int = 0
        private set

    /**
     * Add XP and check if the threshold is reached.
     * Supports multi-level overflow (if a huge XP drop crosses multiple levels).
     */
    fun addXP(amount: Int) {
        currentXP += amount
        checkLevelUp()
    }

    /**
     * Called by the ViewModel after the player selects an upgrade.
     * Advances the level and recalculates the threshold.
     */
    fun consumeLevelUp() {
        if (!levelUpPending) return
        currentLevel++
        totalLevelUps++
        xpToNextLevel = calculateXPForLevel(currentLevel)
        levelUpPending = false
        // Check again in case overflow XP crosses another level
        checkLevelUp()
    }

    /** XP progress as a percentage (0f to 1f) for the HUD bar. */
    val xpProgress: Float
        get() = if (xpToNextLevel > 0) currentXP.toFloat() / xpToNextLevel else 0f

    private fun checkLevelUp() {
        if (currentXP >= xpToNextLevel) {
            currentXP -= xpToNextLevel
            levelUpPending = true
        }
    }

    override fun toString(): String {
        return "XPComponent(lv=$currentLevel, xp=$currentXP/$xpToNextLevel, pending=$levelUpPending)"
    }

    companion object {
        /**
         * Formula based on Vampire Survivors pacing but adapted for 100% drop rate:
         * - Levels 1-20: Level * 30 (e.g. Lv 2 = 60 XP)
         * - Levels 21+:  Level * 50 (Steeper curve for late game)
         */
        fun calculateXPForLevel(level: Int): Int {
            return if (level <= 20) {
                level * 30
            } else {
                level * 50
            }
        }
    }
}
