package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.AIComponent
import app.krafted.nightmarehorde.engine.core.components.BossComponent
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import app.krafted.nightmarehorde.game.data.BossType

/**
 * Factory for creating boss entities.
 * Bosses are large, high-HP enemies with unique AI behavior and multiple attack phases.
 * They share the ENEMY collision layer so player projectiles hit them.
 */
class BossEntity(
    id: Long = Entity.nextId(),
    x: Float,
    y: Float,
    type: BossType,
    bossNumber: Int = 1
) : Entity(id) {

    init {
        val scaledHp = type.scaledHealth(bossNumber)

        addComponent(TransformComponent(x = x, y = y, scale = type.scale))
        addComponent(VelocityComponent())

        addComponent(SpriteComponent(
            textureKey = type.assetName,
            frameWidth = type.frameWidth,
            frameHeight = type.frameHeight,
            width = 0f,
            height = 0f,
            layer = 2 // Above regular enemies
        ))

        addComponent(ColliderComponent(
            collider = Collider.Circle(type.colliderRadius),
            layer = CollisionLayer.ENEMY
        ))

        addComponent(HealthComponent(maxHealth = scaledHp).apply {
            invincibilityDuration = 0f // Bosses have no i-frames
        })

        addComponent(StatsComponent(
            moveSpeed = type.moveSpeed,
            baseDamage = type.damage
        ))

        addComponent(AIComponent(
            behavior = type.behavior,
            range = 300f // Bosses engage from further out
        ))

        addComponent(BossComponent(
            bossType = type,
            bossNumber = bossNumber
        ))
    }
}
