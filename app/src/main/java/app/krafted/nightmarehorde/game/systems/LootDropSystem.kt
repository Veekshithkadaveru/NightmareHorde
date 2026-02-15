package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.ZombieTypeComponent
import app.krafted.nightmarehorde.game.entities.AmmoPickup
import app.krafted.nightmarehorde.game.entities.HealthPickup
import app.krafted.nightmarehorde.game.weapons.WeaponType
import kotlin.random.Random

/**
 * Utility class that rolls loot drops when an enemy dies.
 *
 * This is NOT a per-frame [GameSystem] — it is invoked directly by [CombatSystem]
 * on enemy death events. It lives in the `systems` package because it operates on
 * ECS entities and uses [GameLoop] to spawn pickup entities.
 */
class LootDropSystem(
    private val gameLoop: GameLoop
) {
    private val rng = Random(System.currentTimeMillis())

    companion object {
        const val AMMO_DROP_CHANCE = 0.25f
        const val HEALTH_DROP_CHANCE = 0.10f
    }

    private val ammoDropWeaponTypes = listOf(
        WeaponType.ASSAULT_RIFLE,
        WeaponType.SHOTGUN,
        WeaponType.SMG,
        WeaponType.FLAMETHROWER
    )

    fun tryDropLoot(
        deadEntity: Entity,
        elapsedGameTime: Float,
        unlockedWeaponTypes: List<WeaponType>
    ) {
        val transform = deadEntity.getComponent(TransformComponent::class) ?: return
        val zombieType = deadEntity.getComponent(ZombieTypeComponent::class)

        val xpMultiplier = (zombieType?.zombieType?.xpReward ?: 1).toFloat().coerceIn(1f, 10f) / 5f
        val timeScalar = when {
            elapsedGameTime < 60f -> 1.3f
            elapsedGameTime < 180f -> 1.1f
            elapsedGameTime < 600f -> 1.0f
            else -> 0.85f
        }

        val effectiveHealthDrop = HEALTH_DROP_CHANCE * xpMultiplier * timeScalar
        val effectiveAmmoDrop = AMMO_DROP_CHANCE * xpMultiplier * timeScalar

        val roll = rng.nextFloat()

        val offsetX = (rng.nextFloat() - 0.5f) * 20f
        val offsetY = (rng.nextFloat() - 0.5f) * 20f
        val dropX = transform.x + offsetX
        val dropY = transform.y + offsetY

        when {
            roll < effectiveHealthDrop -> {
                gameLoop.addEntity(HealthPickup.create(x = dropX, y = dropY, healAmount = 10))
            }
            roll < effectiveHealthDrop + effectiveAmmoDrop -> {
                val droppableTypes = unlockedWeaponTypes.filter { it in ammoDropWeaponTypes }
                if (droppableTypes.isNotEmpty()) {
                    val weaponType = droppableTypes[rng.nextInt(droppableTypes.size)]
                    val amount = getAmmoAmountForType(weaponType)
                    gameLoop.addEntity(
                        AmmoPickup.create(x = dropX, y = dropY, amount = amount, weaponType = weaponType)
                    )
                } else {
                    gameLoop.addEntity(HealthPickup.create(x = dropX, y = dropY, healAmount = 10))
                }
            }
        }
    }

    private fun getAmmoAmountForType(type: WeaponType): Int {
        return when (type) {
            WeaponType.ASSAULT_RIFLE -> 15  // ~50% of clip (30)
            WeaponType.SHOTGUN -> 4         // ~50% of clip (8)
            WeaponType.SMG -> 20            // ~50% of clip (40)
            WeaponType.FLAMETHROWER -> 30   // ~15% of pool (200) — balanced for high fire rate
            else -> 10
        }
    }
}
