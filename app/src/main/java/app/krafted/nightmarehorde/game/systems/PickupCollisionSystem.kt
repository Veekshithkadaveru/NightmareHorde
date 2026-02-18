package app.krafted.nightmarehorde.game.systems

import android.util.Log
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.AmmoPickupComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.HealthPickupComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponInventoryComponent
import app.krafted.nightmarehorde.engine.core.components.XPComponent
import app.krafted.nightmarehorde.engine.core.components.XPOrbPickupComponent
import app.krafted.nightmarehorde.engine.physics.CollisionSystem

/**
 * Handles player-pickup collisions for both ammo and health pickups.
 *
 * Listens to collision events from [CollisionSystem] and processes PLAYER-PICKUP
 * interactions: applies ammo to the matching weapon slot or heals the player.
 *
 * A brief rejection cooldown prevents redundant per-frame processing when a
 * pickup cannot be collected (e.g. health orb at full HP).
 */
class PickupCollisionSystem(
    private val collisionSystem: CollisionSystem,
    private val gameLoop: GameLoop
) : GameSystem(priority = 40) {

    /** Tracks recently rejected pickups to avoid per-frame re-processing. */
    private val rejectionCooldowns = mutableMapOf<Long, Float>()

    companion object {
        /** Seconds to skip a pickup after it was rejected (e.g. health full). */
        private const val REJECTION_COOLDOWN_SEC = 1.0f
    }

    init {
        collisionSystem.addCollisionListener { event ->
            handleCollision(event)
        }
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        // Tick down rejection cooldowns
        val iter = rejectionCooldowns.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            entry.setValue(entry.value - deltaTime)
            if (entry.value <= 0f) iter.remove()
        }
    }

    private fun handleCollision(event: CollisionSystem.CollisionEvent) {
        val (entityA, entityB) = event

        if (event.layerA == CollisionLayer.PLAYER && event.layerB == CollisionLayer.PICKUP) {
            processPickup(player = entityA, pickup = entityB)
        } else if (event.layerB == CollisionLayer.PLAYER && event.layerA == CollisionLayer.PICKUP) {
            processPickup(player = entityB, pickup = entityA)
        }
    }

    private fun processPickup(player: Entity, pickup: Entity) {
        // Skip if this pickup is on rejection cooldown
        if (rejectionCooldowns.containsKey(pickup.id)) return

        // Try XP orb pickup
        val xpOrb = pickup.getComponent(XPOrbPickupComponent::class)
        if (xpOrb != null) {
            val xpComp = player.getComponent(XPComponent::class)
            if (xpComp != null) {
                val stats = player.getComponent(StatsComponent::class)
                val effectiveXP = (xpOrb.xpValue * (stats?.xpMultiplier ?: 1f)).toInt().coerceAtLeast(1)
                xpComp.addXP(effectiveXP)
                gameLoop.removeEntity(pickup)
            }
            return
        }

        // Try ammo pickup
        val ammoComp = pickup.getComponent(AmmoPickupComponent::class)
        if (ammoComp != null) {
            val inventory = player.getComponent(WeaponInventoryComponent::class) ?: return
            val stats = player.getComponent(StatsComponent::class)
            val targetType = ammoComp.weaponType ?: inventory.activeWeaponType
            if (inventory.hasWeapon(targetType)) {
                inventory.addAmmo(targetType, ammoComp.amount, stats?.ammoCapacityMultiplier ?: 1f)
                Log.d("PickupCollisionSystem", "Picked up ${ammoComp.amount} ammo for $targetType")
                gameLoop.removeEntity(pickup)
            }
            return
        }

        // Try health pickup
        val healthPickup = pickup.getComponent(HealthPickupComponent::class)
        if (healthPickup != null) {
            val health = player.getComponent(HealthComponent::class) ?: return
            if (health.currentHealth < health.maxHealth) {
                health.heal(healthPickup.healAmount)
                Log.d("PickupCollisionSystem", "Healed ${healthPickup.healAmount} HP")
                gameLoop.removeEntity(pickup)
            } else {
                // Player at full health — skip this pickup for a while
                rejectionCooldowns[pickup.id] = REJECTION_COOLDOWN_SEC
            }
            return
        }
    }
}
