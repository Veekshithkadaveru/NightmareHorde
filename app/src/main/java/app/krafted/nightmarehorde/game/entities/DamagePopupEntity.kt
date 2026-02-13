package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.TransformComponent

data class DamagePopupComponent(
    val damageAmount: Int,
    val lifeTime: Float = 1.0f,
    var timeAlive: Float = 0f,
    val floatSpeed: Float = 50f
) : Component

class DamagePopupEntity(
    id: Long = Entity.nextId(),
    x: Float,
    y: Float,
    damage: Int
) : Entity(id) {
    init {
        addComponent(TransformComponent(x, y))
        addComponent(DamagePopupComponent(damage))
    }
}
