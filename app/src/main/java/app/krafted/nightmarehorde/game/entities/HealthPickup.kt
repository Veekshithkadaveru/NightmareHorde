package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthPickupComponent
import app.krafted.nightmarehorde.engine.core.components.PickupTagComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent

object HealthPickup {
    fun create(
        x: Float,
        y: Float,
        healAmount: Int = 10
    ): Entity {
        return Entity().apply {
            addComponent(TransformComponent(x = x, y = y, scale = 2f))
            addComponent(VelocityComponent())

            val collider = ColliderComponent.circle(
                radius = 12f,
                layer = CollisionLayer.PICKUP
            )
            collider.isTrigger = true
            addComponent(collider)

            addComponent(SpriteComponent(
                textureKey = "pickup_orb_green",
                layer = 1,
                frameWidth = 16,
                frameHeight = 16,
                currentFrame = 0,
                totalFrames = 12
            ))

            addComponent(HealthPickupComponent(healAmount = healAmount))
            addComponent(PickupTagComponent(baseY = y))
        }
    }
}
