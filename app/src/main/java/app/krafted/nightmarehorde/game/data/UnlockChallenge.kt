package app.krafted.nightmarehorde.game.data

import app.krafted.nightmarehorde.data.local.GameStats

/**
 * Defines the gameplay challenges required to unlock characters.
 * Each challenge checks against lifetime [GameStats] tracked across all runs.
 */
enum class UnlockChallenge(
    val displayText: String
) {
    NONE(""),

    SURVIVE_5_MINUTES("Survive 5 minutes in a single run"),

    KILL_500_ZOMBIES("Kill 500 zombies total"),

    KILL_2000_ZOMBIES("Kill 2,000 zombies total"),

    DEFEAT_3_BOSSES("Defeat 3 bosses total"),

    DEFEAT_10_BOSSES("Defeat 10 bosses total"),

    COMPLETE_10_RUNS("Complete 10 runs");

    fun isCompleted(stats: GameStats): Boolean = when (this) {
        NONE -> true
        SURVIVE_5_MINUTES -> stats.longestRunSeconds >= 300
        KILL_500_ZOMBIES -> stats.totalKills >= 500
        KILL_2000_ZOMBIES -> stats.totalKills >= 2000
        DEFEAT_3_BOSSES -> stats.totalBossKills >= 3
        DEFEAT_10_BOSSES -> stats.totalBossKills >= 10
        COMPLETE_10_RUNS -> stats.totalRunsPlayed >= 10
    }
}
