package app.krafted.nightmarehorde.game.data

import app.krafted.nightmarehorde.game.weapons.WeaponType

/**
 * Defines weapon + passive upgrade combinations that produce evolved weapons.
 * When a weapon and its matching passive upgrade are both maxed,
 * the evolution upgrade appears in the level-up pool.
 */
object EvolutionRegistry {

    data class EvolutionRecipe(
        val evolutionUpgradeId: String,
        val baseWeaponType: WeaponType,
        val requiredPassiveId: String,
        val displayName: String,
        val description: String
    )

    private val recipes = listOf(
        EvolutionRecipe(
            evolutionUpgradeId = "evo_plasma_pistol",
            baseWeaponType = WeaponType.PISTOL,
            requiredPassiveId = "hollow_points",
            displayName = "Plasma Pistol",
            description = "Piercing plasma shots with +50% damage"
        ),
        EvolutionRecipe(
            evolutionUpgradeId = "evo_hellfire_shotgun",
            baseWeaponType = WeaponType.SHOTGUN,
            requiredPassiveId = "extra_projectile",
            displayName = "Hellfire Shotgun",
            description = "12 flaming pellets of destruction"
        ),
        EvolutionRecipe(
            evolutionUpgradeId = "evo_death_scythe",
            baseWeaponType = WeaponType.MELEE,
            requiredPassiveId = "area_boost",
            displayName = "Death Scythe",
            description = "Full 360\u00B0 sweep with devastating damage"
        ),
        EvolutionRecipe(
            evolutionUpgradeId = "evo_inferno_engine",
            baseWeaponType = WeaponType.FLAMETHROWER,
            requiredPassiveId = "cooldown_reduction",
            displayName = "Inferno Engine",
            description = "Devastating wall of fire with AOE burn"
        )
    )

    fun getRecipeForEvolution(evolutionUpgradeId: String): EvolutionRecipe? =
        recipes.firstOrNull { it.evolutionUpgradeId == evolutionUpgradeId }

    fun getAllRecipes(): List<EvolutionRecipe> = recipes
}
