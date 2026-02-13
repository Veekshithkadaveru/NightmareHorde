package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.AmmoPickupComponent
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent

object AmmoPickup {
    fun create(
        x: Float,
        y: Float,
        amount: Int = 20,
        weaponTypeIndex: Int = 0 // 0=Pistol, etc. Not used yet, for future specific ammo types
    ): Entity {
        return Entity().apply {
            addComponent(TransformComponent(x = x, y = y))
            val collider = ColliderComponent.circle(
                radius = 15f,
                layer = CollisionLayer.PICKUP
            )
            collider.isTrigger = true
            addComponent(collider)
            
            // Reusing power-up sprites or similar
            addComponent(SpriteComponent(
                textureKey = "weapon_shotgun", // Placeholder visual
                layer = 1
            ))
            
            // Adjust scale in TransformComponent if needed, not SpriteComponent
            val transform = getComponent(TransformComponent::class)
            transform?.scale = 0.8f
            
            addComponent(AmmoPickupComponent(amount = amount))
        }
    }
}
