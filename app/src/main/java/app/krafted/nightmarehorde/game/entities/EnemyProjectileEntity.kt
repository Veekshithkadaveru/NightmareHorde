package app.krafted.nightmarehorde.game.entities

import androidx.compose.ui.graphics.Color
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.ParticleComponent
import app.krafted.nightmarehorde.engine.core.components.ProjectileComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.physics.Collider

/**
 * Factory for hostile projectiles fired by enemies and bosses (spit, axes, webs,
 * rubble, sweep segments, retaliation shockwaves).
 *
 * All such projectiles share the same component skeleton: a transform, a velocity,
 * a triggering [CollisionLayer.ENEMY] circle collider, and a [ProjectileComponent].
 * A sprite and/or a trailing particle are optional. Callers compute the spawn
 * position and velocity themselves so they control offsets and spread.
 */
object EnemyProjectileEntity {

    fun create(
        x: Float,
        y: Float,
        vx: Float,
        vy: Float,
        damage: Float,
        ownerId: Long,
        lifeTime: Float,
        colliderRadius: Float,
        scale: Float = 1f,
        penetrating: Boolean = false,
        spriteKey: String? = null,
        spriteSize: Float = 24f,
        particleColor: Color? = null,
        particleSize: Float = 0f,
        particleWidth: Float = 0f,
        particleHeight: Float = 0f
    ): Entity = Entity().apply {
        addComponent(TransformComponent(x = x, y = y, scale = scale))
        addComponent(VelocityComponent(vx = vx, vy = vy))
        addComponent(ColliderComponent(
            collider = Collider.Circle(colliderRadius),
            layer = CollisionLayer.ENEMY,
            isTrigger = true
        ))
        addComponent(ProjectileComponent(
            damage = damage,
            ownerId = ownerId,
            maxLifetime = lifeTime,
            penetrating = penetrating
        ))
        if (spriteKey != null) {
            addComponent(SpriteComponent(
                textureKey = spriteKey,
                width = spriteSize,
                height = spriteSize
            ))
        }
        if (particleColor != null) {
            addComponent(ParticleComponent(
                color = particleColor,
                size = particleSize,
                lifeTime = lifeTime,
                fadeOut = true,
                width = particleWidth,
                height = particleHeight
            ))
        }
    }
}
