package app.krafted.nightmarehorde.game.systems

import android.util.Log
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.AmmoPickupComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.WeaponComponent
import app.krafted.nightmarehorde.engine.physics.CollisionSystem
import javax.inject.Inject

class AmmoSystem(
    private val collisionSystem: CollisionSystem,
    private val gameLoop: GameLoop
) : GameSystem(priority = 40) {

    init {
        collisionSystem.addCollisionListener { event ->
            handleCollision(event)
        }
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        // Nothing to do per frame, handled by events
    }

    private fun handleCollision(event: CollisionSystem.CollisionEvent) {
        val (entityA, entityB) = event
        
        // Check if player picked up ammo
        if (event.layerA == CollisionLayer.PLAYER && event.layerB == CollisionLayer.PICKUP) {
            processPickup(player = entityA, pickup = entityB)
        } else if (event.layerB == CollisionLayer.PLAYER && event.layerA == CollisionLayer.PICKUP) {
            processPickup(player = entityB, pickup = entityA)
        }
    }

    private fun processPickup(player: Entity, pickup: Entity) {
        val ammoComp = pickup.getComponent(AmmoPickupComponent::class) ?: return
        val weaponComp = player.getComponent(WeaponComponent::class) ?: return

        // Add ammo to current weapon (or all? Plan implies simple addition)
        val weapon = weaponComp.equippedWeapon
        if (weapon != null && !weapon.infiniteAmmo) {
            weaponComp.currentAmmo += ammoComp.amount
            
            // Cap at 3 clips or 999, whichever is lower (or just hard cap for now)
            val maxAllowed = 999
            if (weaponComp.currentAmmo > maxAllowed) weaponComp.currentAmmo = maxAllowed
            
            Log.d("AmmoSystem", "Picked up ${ammoComp.amount} ammo. Total: ${weaponComp.currentAmmo}")
            
            gameLoop.removeEntity(pickup)
        }
    }
}
