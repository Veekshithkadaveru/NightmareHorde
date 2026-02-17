package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.DroneComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import app.krafted.nightmarehorde.game.data.DroneType

/**
 * Factory for creating orbital drone entities.
 * Drones orbit the player, have no health (invulnerable), and are lost only via fuel depletion.
 * Uses TURRET collision layer to avoid interfering with player/enemy collisions.
 */
class DroneEntity(
    id: Long = Entity.nextId(),
    x: Float,
    y: Float,
    droneType: DroneType,
    ownerEntityId: Long,
    slotIndex: Int = 0,
    initialOrbitAngle: Float = 0f
) : Entity(id) {

    init {
        addComponent(TransformComponent(x = x, y = y, scale = 1.2f))

        addComponent(SpriteComponent(
            textureKey = droneType.textureKey,
            layer = 25,
            width = 32f,
            height = 32f
        ))

        addComponent(ColliderComponent(
            collider = Collider.Circle(10f),
            layer = CollisionLayer.TURRET,
            isTrigger = true
        ))

        addComponent(DroneComponent(
            droneType = droneType,
            ownerEntityId = ownerEntityId,
            slotIndex = slotIndex,
            orbitBaseAngle = initialOrbitAngle,
            fuel = droneType.maxFuel(1),
            maxFuel = droneType.maxFuel(1)
        ))
    }
}
