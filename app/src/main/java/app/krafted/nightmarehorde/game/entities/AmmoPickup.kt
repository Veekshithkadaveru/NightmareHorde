package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.AmmoPickupComponent
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent

object AmmoPickup {
    fun create(x: Float, y: Float, amount: Int = 10, weaponTypeIndex: Int = 0): Entity {
        // Simple mapping for icon: 0=Pistol(unused), 1=Rifle, 2=Shotgun, 3=SMG
        // We can just use a generic crate or specific weapon icons.
        // Let's use weapon_rifle for now as a generic ammo box if not specified
        val texture = when (weaponTypeIndex) {
            1 -> "weapon_rifle"
            2 -> "weapon_shotgun"
            3 -> "weapon_smg"
            else -> "weapon_pistol" 
        }

        return Entity().apply {
            addComponent(TransformComponent(x = x, y = y, scale = 1.0f))
            val collider = ColliderComponent.circle(
                radius = 20f,
                layer = CollisionLayer.PICKUP
            )
            collider.isTrigger = true
            addComponent(collider)
            addComponent(SpriteComponent(
                textureKey = texture,
                layer = 0
            ))
            addComponent(AmmoPickupComponent(amount))
        }
    }
}
