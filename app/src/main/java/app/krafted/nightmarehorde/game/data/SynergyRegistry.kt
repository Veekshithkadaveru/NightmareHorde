package app.krafted.nightmarehorde.game.data

/**
 * Defines passive synergy bonuses that activate when specific upgrade combinations
 * meet minimum level requirements. Checked after each upgrade application.
 */
object SynergyRegistry {

    data class Synergy(
        val id: String,
        val name: String,
        val description: String,
        val requiredUpgradeIds: List<String>,
        val requiredMinLevels: List<Int>,
        val apply: (UpgradeContext) -> Unit
    )

    val ALL = listOf(
        Synergy(
            id = "glass_cannon",
            name = "Glass Cannon",
            description = "All damage +50%, armor reduced to 0",
            requiredUpgradeIds = listOf("hollow_points", "berserker"),
            requiredMinLevels = listOf(3, 2),
            apply = { ctx ->
                ctx.stats.damageMultiplier += 0.5f
                ctx.stats.armor = 0
            }
        ),
        Synergy(
            id = "iron_fortress",
            name = "Iron Fortress",
            description = "+20 armor, +50 HP, -20% move speed",
            requiredUpgradeIds = listOf("fortress", "kevlar_patch"),
            requiredMinLevels = listOf(2, 3),
            apply = { ctx ->
                ctx.stats.armor += 20
                ctx.stats.maxHealth += 50
                ctx.stats.moveSpeed *= 0.8f
            }
        ),
        Synergy(
            id = "drone_swarm",
            name = "Drone Swarm",
            description = "All drones +30% damage, +50% fuel efficiency",
            requiredUpgradeIds = listOf("drone_damage", "drone_fuel"),
            requiredMinLevels = listOf(3, 2),
            apply = { ctx ->
                ctx.stats.droneDamageMultiplier += 0.3f
                ctx.stats.droneFuelEfficiency += 0.5f
            }
        )
    )
}
