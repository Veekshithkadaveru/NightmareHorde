package app.krafted.nightmarehorde.game.data

import androidx.annotation.DrawableRes
import app.krafted.nightmarehorde.R
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponInventoryComponent
import app.krafted.nightmarehorde.game.systems.DroneManager

/**
 * Rarity tiers for upgrades. Higher rarity = rarer appearance + stronger effect.
 */
enum class UpgradeRarity(val weight: Int, val color: Long) {
    COMMON(100, 0xFFB0B0B0),
    RARE(40, 0xFF42A5F5),
    EPIC(15, 0xFFAB47BC),
    LEGENDARY(5, 0xFFFFD700);
}

/**
 * Categories for upgrade cards in the level-up UI.
 */
enum class UpgradeCategory {
    PASSIVE_STAT,
    DRONE_GRANT,
    WEAPON_EVOLUTION
}

/**
 * Context object passed to upgrade apply lambdas. Provides access to everything
 * an upgrade might need to modify.
 */
data class UpgradeContext(
    val stats: StatsComponent,
    val health: HealthComponent,
    val weaponInventory: WeaponInventoryComponent?,
    val droneManager: DroneManager?,
    val playerEntity: Entity,
    val currentLevel: Int
)

/**
 * Represents a stackable upgrade that can be offered at level-up.
 * Each upgrade can be picked up to [maxLevel] times.
 */
data class Upgrade(
    val id: String,
    val name: String,
    val description: String,
    val descriptionPerLevel: List<String>? = null,
    val rarity: UpgradeRarity = UpgradeRarity.COMMON,
    @DrawableRes val iconRes: Int,
    val maxLevel: Int = 5,
    val category: UpgradeCategory = UpgradeCategory.PASSIVE_STAT,
    val apply: (UpgradeContext) -> Unit
)

/**
 * Wraps an Upgrade with its current/next level for UI display.
 */
data class UpgradeChoice(
    val upgrade: Upgrade,
    val currentLevel: Int,
    val nextLevel: Int
) {
    val isMaxAfterPick: Boolean get() = nextLevel >= upgrade.maxLevel
    val displayDescription: String
        get() = upgrade.descriptionPerLevel?.getOrNull(nextLevel - 1) ?: upgrade.description
}

/**
 * Master list of all upgrades available in the game.
 * Stat modifiers are designed for incremental stacking across a 15-minute run.
 */
object Upgrades {

    /** Maps drone upgrade IDs to DroneType for prerequisite checks. */
    val DRONE_TYPE_MAP: Map<String, DroneType> = mapOf(
        "drone_scatter" to DroneType.SCATTER,
        "drone_inferno" to DroneType.INFERNO,
        "drone_arc" to DroneType.ARC,
        "drone_gunner_upgrade" to DroneType.GUNNER
    )

    val ALL: List<Upgrade> = listOf(
        // ─── Common Stat Upgrades (stackable) ───────────────────────────────

        Upgrade(
            id = "hollow_points",
            name = "Hollow Points",
            description = "+10% Damage",
            rarity = UpgradeRarity.COMMON,
            iconRes = R.drawable.upgrade_hollow_points,
            maxLevel = 5,
            apply = { ctx -> ctx.stats.damageMultiplier += 0.1f }
        ),
        Upgrade(
            id = "hair_trigger",
            name = "Hair Trigger",
            description = "+12% Fire Rate",
            rarity = UpgradeRarity.COMMON,
            iconRes = R.drawable.upgrade_hair_trigger,
            maxLevel = 5,
            apply = { ctx -> ctx.stats.fireRateMultiplier += 0.12f }
        ),
        Upgrade(
            id = "toughness",
            name = "Toughness",
            description = "+15 Max HP",
            rarity = UpgradeRarity.COMMON,
            iconRes = R.drawable.upgrade_toughness,
            maxLevel = 5,
            apply = { ctx -> ctx.stats.maxHealth += 15 }
        ),
        Upgrade(
            id = "adrenaline",
            name = "Adrenaline",
            description = "+8% Move Speed",
            rarity = UpgradeRarity.COMMON,
            iconRes = R.drawable.upgrade_adrenaline,
            maxLevel = 5,
            apply = { ctx -> ctx.stats.moveSpeed *= 1.08f }
        ),
        Upgrade(
            id = "kevlar_patch",
            name = "Kevlar Patch",
            description = "+2 Armor",
            rarity = UpgradeRarity.COMMON,
            iconRes = R.drawable.upgrade_kevlar,
            maxLevel = 5,
            apply = { ctx -> ctx.stats.armor += 2 }
        ),
        Upgrade(
            id = "magnet_range",
            name = "Magnetic Pull",
            description = "+20% Pickup Radius",
            rarity = UpgradeRarity.COMMON,
            iconRes = R.drawable.upgrade_hollow_points, // placeholder
            maxLevel = 5,
            apply = { ctx -> ctx.stats.pickupRadius *= 1.2f }
        ),
        Upgrade(
            id = "ammo_capacity",
            name = "Deep Pockets",
            description = "+25% Ammo Capacity",
            rarity = UpgradeRarity.COMMON,
            iconRes = R.drawable.upgrade_rapid_fire, // placeholder
            maxLevel = 3,
            apply = { ctx -> ctx.stats.ammoCapacityMultiplier += 0.25f }
        ),
        Upgrade(
            id = "xp_boost",
            name = "Quick Learner",
            description = "+15% XP Gain",
            rarity = UpgradeRarity.COMMON,
            iconRes = R.drawable.upgrade_adrenaline, // placeholder
            maxLevel = 3,
            apply = { ctx -> ctx.stats.xpMultiplier += 0.15f }
        ),

        // ─── Rare Stat Upgrades (stackable) ─────────────────────────────────

        Upgrade(
            id = "hp_regen",
            name = "Regeneration",
            description = "+1 HP/sec",
            rarity = UpgradeRarity.RARE,
            iconRes = R.drawable.upgrade_iron_will, // placeholder
            maxLevel = 5,
            apply = { ctx -> ctx.stats.hpRegen += 1f }
        ),
        Upgrade(
            id = "extra_projectile",
            name = "Multishot",
            description = "+1 Projectile",
            rarity = UpgradeRarity.RARE,
            iconRes = R.drawable.upgrade_rapid_fire, // placeholder
            maxLevel = 3,
            apply = { ctx -> ctx.stats.projectileCountBonus += 1 }
        ),
        Upgrade(
            id = "area_boost",
            name = "Blast Radius",
            description = "+15% Area",
            rarity = UpgradeRarity.RARE,
            iconRes = R.drawable.upgrade_incendiary, // placeholder
            maxLevel = 5,
            apply = { ctx -> ctx.stats.areaMultiplier += 0.15f }
        ),
        Upgrade(
            id = "cooldown_reduction",
            name = "Overclock",
            description = "+6% Cooldown Reduction",
            rarity = UpgradeRarity.RARE,
            iconRes = R.drawable.upgrade_hair_trigger, // placeholder
            maxLevel = 5,
            apply = { ctx ->
                ctx.stats.cooldownReduction = (ctx.stats.cooldownReduction + 0.06f).coerceAtMost(0.7f)
            }
        ),
        Upgrade(
            id = "luck_up",
            name = "Lucky Charm",
            description = "+0.3 Luck",
            rarity = UpgradeRarity.RARE,
            iconRes = R.drawable.upgrade_adrenaline, // placeholder
            maxLevel = 3,
            apply = { ctx -> ctx.stats.luck += 0.3f }
        ),
        Upgrade(
            id = "drone_damage",
            name = "Drone Overdrive",
            description = "+15% Drone Damage",
            rarity = UpgradeRarity.RARE,
            iconRes = R.drawable.upgrade_armor_piercing, // placeholder
            maxLevel = 5,
            apply = { ctx -> ctx.stats.droneDamageMultiplier += 0.15f }
        ),
        Upgrade(
            id = "drone_fuel",
            name = "Drone Fuel Cells",
            description = "Refuel all drones by 60s",
            rarity = UpgradeRarity.RARE,
            iconRes = R.drawable.upgrade_stimpack, // placeholder
            maxLevel = 5, // Can always be picked while drones are active
            apply = { ctx ->
                ctx.droneManager?.refuelAllDrones(app.krafted.nightmarehorde.game.systems.DroneManager.REFUEL_LEVEL_UP)
            }
        ),

        // ─── Rare Drone Grant Upgrades ──────────────────────────────────────

        Upgrade(
            id = "drone_scatter",
            name = "Scatter Drone",
            description = "Deploy or upgrade Scatter Drone",
            rarity = UpgradeRarity.RARE,
            iconRes = R.drawable.upgrade_rapid_fire, // placeholder
            maxLevel = 3,
            category = UpgradeCategory.DRONE_GRANT,
            apply = { ctx ->
                val dm = ctx.droneManager ?: return@Upgrade
                if (dm.hasDroneType(DroneType.SCATTER)) {
                    dm.upgradeDrone(DroneType.SCATTER)
                } else {
                    dm.grantDrone(DroneType.SCATTER, ctx.playerEntity)
                }
            }
        ),
        Upgrade(
            id = "drone_gunner_upgrade",
            name = "Gunner Boost",
            description = "Upgrade Gunner Drone",
            rarity = UpgradeRarity.RARE,
            iconRes = R.drawable.upgrade_armor_piercing, // placeholder
            maxLevel = 2,
            category = UpgradeCategory.DRONE_GRANT,
            apply = { ctx ->
                val dm = ctx.droneManager ?: return@Upgrade
                dm.upgradeDrone(DroneType.GUNNER)
            }
        ),

        // ─── Epic Upgrades ──────────────────────────────────────────────────

        Upgrade(
            id = "berserker",
            name = "Berserker",
            description = "+15% DMG, +10% Fire Rate, -10 HP",
            rarity = UpgradeRarity.EPIC,
            iconRes = R.drawable.upgrade_berserker,
            maxLevel = 3,
            apply = { ctx ->
                ctx.stats.damageMultiplier += 0.15f
                ctx.stats.fireRateMultiplier += 0.1f
                ctx.stats.maxHealth = (ctx.stats.maxHealth - 10).coerceAtLeast(10)
            }
        ),
        Upgrade(
            id = "fortress",
            name = "Fortress",
            description = "+5 Armor, +20 HP, -5% Speed",
            rarity = UpgradeRarity.EPIC,
            iconRes = R.drawable.upgrade_fortress,
            maxLevel = 3,
            apply = { ctx ->
                ctx.stats.armor += 5
                ctx.stats.maxHealth += 20
                ctx.stats.moveSpeed *= 0.95f
            }
        ),
        Upgrade(
            id = "drone_inferno",
            name = "Inferno Drone",
            description = "Deploy or upgrade Inferno Drone",
            rarity = UpgradeRarity.EPIC,
            iconRes = R.drawable.upgrade_incendiary, // placeholder
            maxLevel = 3,
            category = UpgradeCategory.DRONE_GRANT,
            apply = { ctx ->
                val dm = ctx.droneManager ?: return@Upgrade
                if (dm.hasDroneType(DroneType.INFERNO)) {
                    dm.upgradeDrone(DroneType.INFERNO)
                } else {
                    dm.grantDrone(DroneType.INFERNO, ctx.playerEntity)
                }
            }
        ),
        Upgrade(
            id = "drone_arc",
            name = "Arc Drone",
            description = "Deploy or upgrade Arc Drone",
            rarity = UpgradeRarity.EPIC,
            iconRes = R.drawable.upgrade_steel_plating, // placeholder
            maxLevel = 3,
            category = UpgradeCategory.DRONE_GRANT,
            apply = { ctx ->
                val dm = ctx.droneManager ?: return@Upgrade
                if (dm.hasDroneType(DroneType.ARC)) {
                    dm.upgradeDrone(DroneType.ARC)
                } else {
                    dm.grantDrone(DroneType.ARC, ctx.playerEntity)
                }
            }
        ),

        // ─── Legendary Upgrades ─────────────────────────────────────────────

        Upgrade(
            id = "death_dealer",
            name = "Death Dealer",
            description = "+25% Damage, +25% Fire Rate",
            rarity = UpgradeRarity.LEGENDARY,
            iconRes = R.drawable.upgrade_death_dealer,
            maxLevel = 2,
            apply = { ctx ->
                ctx.stats.damageMultiplier += 0.25f
                ctx.stats.fireRateMultiplier += 0.25f
            }
        ),
        Upgrade(
            id = "juggernaut",
            name = "Juggernaut",
            description = "+50 HP, +8 Armor",
            rarity = UpgradeRarity.LEGENDARY,
            iconRes = R.drawable.upgrade_juggernaut,
            maxLevel = 2,
            apply = { ctx ->
                ctx.stats.maxHealth += 50
                ctx.stats.armor += 8
            }
        ),
        Upgrade(
            id = "revival",
            name = "Second Wind",
            description = "+1 Revival",
            rarity = UpgradeRarity.LEGENDARY,
            iconRes = R.drawable.upgrade_iron_will, // placeholder
            maxLevel = 1,
            apply = { ctx -> ctx.stats.revivalCount += 1 }
        ),

        // ─── Weapon Evolution Upgrades ──────────────────────────────────────

        Upgrade(
            id = "evo_plasma_pistol",
            name = "Plasma Pistol",
            description = "Evolve Pistol: piercing plasma shots",
            rarity = UpgradeRarity.LEGENDARY,
            iconRes = R.drawable.upgrade_death_dealer, // placeholder
            maxLevel = 1,
            category = UpgradeCategory.WEAPON_EVOLUTION,
            apply = { _ -> } // Handled by GameViewModel.handleWeaponEvolution()
        ),
        Upgrade(
            id = "evo_hellfire_shotgun",
            name = "Hellfire Shotgun",
            description = "Evolve Shotgun: 12 flaming pellets",
            rarity = UpgradeRarity.LEGENDARY,
            iconRes = R.drawable.upgrade_incendiary, // placeholder
            maxLevel = 1,
            category = UpgradeCategory.WEAPON_EVOLUTION,
            apply = { _ -> } // Handled by GameViewModel.handleWeaponEvolution()
        ),
        Upgrade(
            id = "evo_death_scythe",
            name = "Death Scythe",
            description = "Evolve Melee: 360\u00B0 sweep",
            rarity = UpgradeRarity.LEGENDARY,
            iconRes = R.drawable.upgrade_berserker, // placeholder
            maxLevel = 1,
            category = UpgradeCategory.WEAPON_EVOLUTION,
            apply = { _ -> } // Handled by GameViewModel.handleWeaponEvolution()
        ),
        Upgrade(
            id = "evo_inferno_engine",
            name = "Inferno Engine",
            description = "Evolve Flamethrower: devastating fire wall",
            rarity = UpgradeRarity.LEGENDARY,
            iconRes = R.drawable.upgrade_fortress, // placeholder
            maxLevel = 1,
            category = UpgradeCategory.WEAPON_EVOLUTION,
            apply = { _ -> } // Handled by GameViewModel.handleWeaponEvolution()
        )
    )
}
