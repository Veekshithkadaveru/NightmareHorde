package app.krafted.nightmarehorde.game.data

/**
 * Tracks map unlock state across game sessions (in-memory for Phase E3).
 * Phase F3 will add SharedPreferences persistence.
 *
 * SUBURBS is always unlocked. Other maps unlock via supplies or boss kills.
 */
object MapUnlockManager {

    /** Total boss kills accumulated across all sessions this app launch */
    var totalBossKills: Int = 10
        private set

    /** Total supplies accumulated (Phase F2 will populate this) */
    var totalSupplies: Int = 2000
        private set

    /**
     * Returns true if the given map is currently unlocked.
     */
    fun isUnlocked(mapType: MapType): Boolean = when {
        mapType.isDefaultUnlocked -> true
        mapType.requiredBossKills > 0 -> totalBossKills >= mapType.requiredBossKills
        mapType.unlockCost > 0 -> totalSupplies >= mapType.unlockCost
        else -> false
    }

    /**
     * Called when a game run ends. Accumulates boss kills toward map unlocks.
     * @param bossesDefeated Number of bosses defeated in this run.
     * @param suppliesEarned Supplies earned this run (Phase F2 integration point).
     */
    fun recordRunEnd(bossesDefeated: Int, suppliesEarned: Int = 0) {
        totalBossKills += bossesDefeated
        totalSupplies += suppliesEarned
    }

    /** Reset all unlock progress (for testing or new install). */
    fun reset() {
        totalBossKills = 0
        totalSupplies = 0
    }
}
