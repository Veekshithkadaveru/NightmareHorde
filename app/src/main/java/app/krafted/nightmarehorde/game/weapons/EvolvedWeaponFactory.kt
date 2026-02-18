package app.krafted.nightmarehorde.game.weapons

import app.krafted.nightmarehorde.game.data.EvolutionRegistry

/**
 * Factory that creates evolved weapon instances from evolution recipes.
 */
object EvolvedWeaponFactory {

    fun create(recipe: EvolutionRegistry.EvolutionRecipe): Weapon {
        return when (recipe.evolutionUpgradeId) {
            "evo_plasma_pistol" -> PlasmaPistolWeapon()
            "evo_hellfire_shotgun" -> HellfireShotgunWeapon()
            "evo_death_scythe" -> DeathScytheWeapon()
            "evo_inferno_engine" -> InfernoEngineWeapon()
            else -> throw IllegalArgumentException("Unknown evolution: ${recipe.evolutionUpgradeId}")
        }
    }
}
