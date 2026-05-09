package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.data.local.GameStats
import app.krafted.nightmarehorde.data.local.SaveManager
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.game.data.PermanentUpgrade
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuppliesManager @Inject constructor(
    private val saveManager: SaveManager,
) {

    val supplies: StateFlow<Int> = saveManager.saveFlow
        .map { it.supplies }
        .stateIn(MainScope(), SharingStarted.Eagerly, saveManager.currentSave.supplies)

    val upgradeLevels: StateFlow<Map<String, Int>> = saveManager.saveFlow
        .map { it.permanentUpgrades }
        .stateIn(MainScope(), SharingStarted.Eagerly, saveManager.currentSave.permanentUpgrades)

    /** Observable lifetime stats — drives unlock recomposition. */
    val statsFlow: StateFlow<GameStats> = saveManager.saveFlow
        .map { it.stats }
        .stateIn(MainScope(), SharingStarted.Eagerly, saveManager.currentSave.stats)

    fun getUpgradeLevel(upgrade: PermanentUpgrade): Int =
        upgradeLevels.value[upgrade.name] ?: 0

    fun isMaxLevel(upgrade: PermanentUpgrade): Boolean =
        getUpgradeLevel(upgrade) >= upgrade.maxLevel

    fun canPurchase(upgrade: PermanentUpgrade): Boolean =
        !isMaxLevel(upgrade) && supplies.value >= upgrade.costPerLevel

    fun purchase(upgrade: PermanentUpgrade): Boolean {
        if (!canPurchase(upgrade)) return false
        val cost = upgrade.costPerLevel
        val newLevel = getUpgradeLevel(upgrade) + 1
        saveManager.update { save ->
            save.copy(
                supplies = save.supplies - cost,
                permanentUpgrades = save.permanentUpgrades + (upgrade.name to newLevel),
            )
        }
        return true
    }

    fun computeRunEarnings(kills: Int, bossesDefeated: Int, survivalTimeSec: Float): Int =
        kills * 2 + bossesDefeated * 200 + (survivalTimeSec / 10f).toInt()

    fun awardRunEarnings(kills: Int, bossesDefeated: Int, survivalTimeSec: Float): Int {
        val earned = computeRunEarnings(kills, bossesDefeated, survivalTimeSec)
        if (earned <= 0) return 0
        saveManager.update { save ->
            save.copy(
                supplies = save.supplies + earned,
                stats = save.stats.copy(
                    totalSuppliesEarned = save.stats.totalSuppliesEarned + earned,
                ),
            )
        }
        return earned
    }

    fun applyToStats(stats: StatsComponent) {
        getUpgradeLevel(PermanentUpgrade.TOUGHNESS).let { lvl ->
            if (lvl > 0) stats.maxHealth += 10 * lvl
        }
        getUpgradeLevel(PermanentUpgrade.FIREPOWER).let { lvl ->
            if (lvl > 0) stats.damageMultiplier *= (1f + 0.05f * lvl)
        }
        getUpgradeLevel(PermanentUpgrade.MOBILITY).let { lvl ->
            if (lvl > 0) stats.moveSpeed *= (1f + 0.05f * lvl)
        }
        getUpgradeLevel(PermanentUpgrade.DRONE_MASTER).let { lvl ->
            if (lvl > 0) stats.droneDamageMultiplier *= (1f + 0.10f * lvl)
        }
        getUpgradeLevel(PermanentUpgrade.AMMO_BELT).let { lvl ->
            if (lvl > 0) stats.ammoCapacityMultiplier *= (1f + 0.10f * lvl)
        }
        getUpgradeLevel(PermanentUpgrade.SECOND_WIND).let { lvl ->
            if (lvl > 0) stats.revivalCount += lvl
        }
    }

    fun getScavengerMultiplier(): Float =
        1f + 0.10f * getUpgradeLevel(PermanentUpgrade.SCAVENGER)

    fun isCharacterUnlocked(characterClass: CharacterClass): Boolean =
        characterClass.unlockChallenge.isCompleted(saveManager.currentSave.stats)

    fun recordRunStats(kills: Int, bossesDefeated: Int, survivalTimeSec: Float) {
        saveManager.update { save ->
            val runSeconds = survivalTimeSec.toInt()
            save.copy(
                stats = save.stats.copy(
                    totalKills = save.stats.totalKills + kills,
                    totalBossKills = save.stats.totalBossKills + bossesDefeated,
                    totalRunsPlayed = save.stats.totalRunsPlayed + 1,
                    longestRunSeconds = maxOf(save.stats.longestRunSeconds, runSeconds),
                ),
            )
        }
    }
}
