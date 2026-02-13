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

class ProjectileEntity(
    id: Long = Entity.nextId(),
    x: Float,
    y: Float,
    rotation: Float,
    speed: Float,
    damage: Float,
    ownerId: Long,
    lifeTime: Float = 2.0f,
    spriteName: String = "projectile_standard",
    colliderRadius: Float = 5f,
    penetrating: Boolean = false,
    /** If set, renders as a colored particle circle instead of a sprite */
    particleColor: Color? = null,
    particleSize: Float = 6f,
    /** If > 0 (with particleColor), renders as an elongated oval (blade/slash) */
    particleWidth: Float = 0f,
    particleHeight: Float = 0f
) : Entity(id) {

    init {
        addComponent(TransformComponent(x, y, rotation, 1.0f))

        // Calculate velocity vector based on rotation (radians)
        val vx = (speed * kotlin.math.cos(rotation.toDouble())).toFloat()
        val vy = (speed * kotlin.math.sin(rotation.toDouble())).toFloat()
        
        addComponent(VelocityComponent(vx, vy))

        // Render as either a sprite or a particle
        if (particleColor != null) {
            addComponent(ParticleComponent(
                color = particleColor,
                size = particleSize,
                lifeTime = lifeTime,
                fadeOut = true,
                width = particleWidth,
                height = particleHeight
            ))
        } else {
            addComponent(SpriteComponent(spriteName))
        }
        
        addComponent(ColliderComponent(
            collider = Collider.Circle(colliderRadius),
            layer = CollisionLayer.PROJECTILE,
            isTrigger = true
        ))
        
        addComponent(ProjectileComponent(
            damage = damage,
            ownerId = ownerId,
            penetrating = penetrating,
            maxLifetime = lifeTime
        ))
    }
}
