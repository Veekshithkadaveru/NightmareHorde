package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.AIComponent
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.core.components.ZombieTypeComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import app.krafted.nightmarehorde.game.data.ZombieType

class ZombieEntity(
    id: Long = Entity.nextId(),
    x: Float,
    y: Float,
    type: ZombieType
) : Entity(id) {

    init {
        addComponent(TransformComponent(x = x, y = y, scale = type.scale))
        addComponent(VelocityComponent())
        addComponent(SpriteComponent(
            textureKey = type.assetName,
            frameWidth = type.frameWidth,
            frameHeight = type.frameHeight,
            // Use 0 to default to frame size, let TransformComponent.scale handle the sizing
            width = 0f,
            height = 0f,
            layer = 1 // Above background (0), same as player and obstacles
        ))

        addComponent(ColliderComponent(
            collider = Collider.Circle(type.colliderRadius),
            layer = CollisionLayer.ENEMY
        ))

        addComponent(HealthComponent(maxHealth = type.maxHealth).apply {
            invincibilityDuration = 0f // Zombies have NO i-frames — every hit counts
        })
        addComponent(StatsComponent(
            moveSpeed = type.moveSpeed,
            baseDamage = type.damage
        ))

        addComponent(AIComponent(
            behavior = type.behavior,
            range = type.behaviorRange
        ))

        addComponent(ZombieTypeComponent(zombieType = type))
    }
}
