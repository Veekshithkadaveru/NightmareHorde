package app.krafted.nightmarehorde.game.data

import app.krafted.nightmarehorde.engine.core.components.WeaponInventoryComponent
import app.krafted.nightmarehorde.game.systems.DroneManager
import kotlin.random.Random

/**
 * Manages the pool of available upgrades with per-upgrade level tracking.
 * Upgrades at max level are excluded from future rolls.
 * Supports luck-modified rarity weights.
 */
class UpgradePool {

    private val rng = Random(System.currentTimeMillis())

    /** Tracks current level for each upgrade by ID. 0 = never picked. */
    private val upgradeLevels = mutableMapOf<String, Int>()

    /**
     * Select [count] unique random upgrades from the available pool.
     * Excludes upgrades that have reached maxLevel.
     * Weights are modified by the player's luck stat.
     */
    fun getRandomUpgrades(
        count: Int = 3,
        luck: Float = 0f,
        droneManager: DroneManager? = null,
        weaponInventory: WeaponInventoryComponent? = null
    ): List<UpgradeChoice> {
        val pool = Upgrades.ALL.filter { upgrade ->
            val currentLevel = upgradeLevels.getOrDefault(upgrade.id, 0)
            if (currentLevel >= upgrade.maxLevel) return@filter false

            when (upgrade.category) {
                UpgradeCategory.DRONE_GRANT -> checkDronePrerequisites(upgrade, droneManager)
                UpgradeCategory.WEAPON_EVOLUTION -> checkEvolutionPrerequisites(upgrade, weaponInventory)
                UpgradeCategory.PASSIVE_STAT -> true
            }
        }.toMutableList()

        val selected = mutableListOf<UpgradeChoice>()

        repeat(count.coerceAtMost(pool.size)) {
            val totalWeight = pool.sumOf { computeWeight(it, luck) }
            if (totalWeight <= 0) return@repeat

            var roll = rng.nextInt(totalWeight)
            var pickedIndex = 0
            for (i in pool.indices) {
                roll -= computeWeight(pool[i], luck)
                if (roll < 0) {
                    pickedIndex = i
                    break
                }
            }

            val picked = pool[pickedIndex]
            val currentLevel = upgradeLevels.getOrDefault(picked.id, 0)
            selected.add(
                UpgradeChoice(
                    upgrade = picked,
                    currentLevel = currentLevel,
                    nextLevel = currentLevel + 1
                )
            )
            pool.removeAt(pickedIndex)
        }

        return selected
    }

    /** Record that an upgrade was picked. Increments its level. */
    fun recordUpgradePicked(upgradeId: String) {
        upgradeLevels[upgradeId] = (upgradeLevels.getOrDefault(upgradeId, 0)) + 1
    }

    fun getUpgradeLevel(upgradeId: String): Int =
        upgradeLevels.getOrDefault(upgradeId, 0)

    fun reset() {
        upgradeLevels.clear()
    }

    /**
     * Compute effective weight for an upgrade, modified by luck.
     * Higher luck shifts weight distribution toward rarer upgrades.
     */
    private fun computeWeight(upgrade: Upgrade, luck: Float): Int {
        val base = upgrade.rarity.weight
        return when (upgrade.rarity) {
            UpgradeRarity.COMMON -> (base * (1f - luck * 0.3f)).toInt().coerceAtLeast(10)
            UpgradeRarity.RARE -> (base * (1f + luck * 0.5f)).toInt()
            UpgradeRarity.EPIC -> (base * (1f + luck * 0.8f)).toInt()
            UpgradeRarity.LEGENDARY -> (base * (1f + luck * 1.2f)).toInt()
        }
    }

    private fun checkDronePrerequisites(upgrade: Upgrade, dm: DroneManager?): Boolean {
        if (dm == null) return false
        val droneType = Upgrades.DRONE_TYPE_MAP[upgrade.id] ?: return false

        // Gunner upgrade: only show if player already has Gunner
        if (upgrade.id == "drone_gunner_upgrade") {
            return dm.hasDroneType(droneType) && dm.getDroneLevel(droneType) < 3
        }

        val hasDrone = dm.hasDroneType(droneType)
        val droneLevel = dm.getDroneLevel(droneType)
        return if (hasDrone) droneLevel < 3 else dm.getActiveDroneCount() < DroneManager.MAX_DRONES
    }

    private fun checkEvolutionPrerequisites(
        upgrade: Upgrade,
        inventory: WeaponInventoryComponent?
    ): Boolean {
        if (inventory == null) return false
        val recipe = EvolutionRegistry.getRecipeForEvolution(upgrade.id) ?: return false
        val hasWeapon = inventory.hasWeapon(recipe.baseWeaponType)
        val passiveUpgrade = Upgrades.ALL.firstOrNull { it.id == recipe.requiredPassiveId }
            ?: return false
        val passiveMaxed = getUpgradeLevel(recipe.requiredPassiveId) >= passiveUpgrade.maxLevel
        return hasWeapon && passiveMaxed
    }
}
