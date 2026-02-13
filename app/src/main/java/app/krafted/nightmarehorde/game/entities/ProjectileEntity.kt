package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
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
    spriteName: String = "projectile_standard"
) : Entity(id) {

    init {
        addComponent(TransformComponent(x, y, rotation, 1.0f))

        // Calculate velocity vector based on rotation (radians)
        val vx = (speed * kotlin.math.cos(rotation.toDouble())).toFloat()
        val vy = (speed * kotlin.math.sin(rotation.toDouble())).toFloat()
        
        addComponent(VelocityComponent(vx, vy))
        addComponent(SpriteComponent(spriteName))
        
        // Fix: Use correct Collider factory methods and CollisionLayer
        addComponent(ColliderComponent(
            collider = Collider.Circle(5f),
            layer = CollisionLayer.PROJECTILE,
            isTrigger = true
        ))
        
        addComponent(ProjectileComponent(
            damage = damage,
            ownerId = ownerId,
            maxLifetime = lifeTime
        ))
    }
}
