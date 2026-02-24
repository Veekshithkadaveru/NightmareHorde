package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.PickupTagComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.core.components.XPOrbPickupComponent

/**
 * Factory for creating XP orb pickup entities.
 * Parallels [HealthPickup] and [AmmoPickup] patterns.
 */
object XPOrbEntity {

    /**
     * Create an XP orb at the given world position.
     *
     * @param x World X position
     * @param y World Y position
     * @param xpValue How much XP this orb grants on pickup
     */
    fun create(x: Float, y: Float, xpValue: Int = 1): Entity {
        return Entity().apply {
            addComponent(TransformComponent(x = x, y = y, scale = 0.83f))
            addComponent(VelocityComponent())
            addComponent(ColliderComponent.circle(
                radius = 12f,
                layer = CollisionLayer.PICKUP
            ))
            addComponent(SpriteComponent(
                textureKey = "pickup_orb_blue",
                layer = 1,
                frameWidth = 16,
                frameHeight = 16,
                currentFrame = 0,
                animationKey = "idle"
            ))
            addComponent(PickupTagComponent(baseY = y))
            addComponent(XPOrbPickupComponent(xpValue = xpValue))
        }
    }
}
