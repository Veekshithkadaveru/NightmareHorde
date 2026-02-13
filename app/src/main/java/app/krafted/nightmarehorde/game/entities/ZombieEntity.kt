package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.physics.Collider

class ZombieEntity(
    id: Long = Entity.nextId(),
    x: Float,
    y: Float,
    spriteName: String = "zombie_walker"
) : Entity(id) {

    init {
        addComponent(TransformComponent(x, y, 0f, 1.0f))
        addComponent(SpriteComponent(spriteName))
        
        // Fix: Use correct Collider factory usage
        addComponent(ColliderComponent(
            collider = Collider.Circle(15f),
            layer = CollisionLayer.ENEMY
        ))
        
        addComponent(HealthComponent(maxHealth = 20))
        addComponent(StatsComponent(moveSpeed = 0f)) // Static for now
    }
}
