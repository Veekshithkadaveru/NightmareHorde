package app.krafted.nightmarehorde.game.data

import app.krafted.nightmarehorde.data.local.SaveManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.MainScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapUnlockManager @Inject constructor(
    private val saveManager: SaveManager,
) {

    val totalBossKills: Int
        get() = saveManager.currentSave.stats.totalBossKills

    // Lifetime supplies earned — monotonic so unlocks don't regress when supplies are spent.
    val totalSuppliesEarned: Int
        get() = saveManager.currentSave.stats.totalSuppliesEarned

    /** Observable unlock-relevant snapshot that triggers Compose recomposition on change. */
    val unlockState: StateFlow<UnlockState> = saveManager.saveFlow
        .map { UnlockState(it.stats.totalBossKills, it.stats.totalSuppliesEarned) }
        .stateIn(
            scope = MainScope(),
            started = SharingStarted.Eagerly,
            initialValue = UnlockState(totalBossKills, totalSuppliesEarned),
        )

    fun isUnlocked(mapType: MapType): Boolean = isUnlocked(mapType, unlockState.value)

    fun isUnlocked(mapType: MapType, state: UnlockState): Boolean = when {
        mapType.isDefaultUnlocked -> true
        mapType.requiredBossKills > 0 -> state.totalBossKills >= mapType.requiredBossKills
        mapType.unlockCost > 0 -> state.totalSuppliesEarned >= mapType.unlockCost
        else -> false
    }

    data class UnlockState(val totalBossKills: Int, val totalSuppliesEarned: Int)
}
