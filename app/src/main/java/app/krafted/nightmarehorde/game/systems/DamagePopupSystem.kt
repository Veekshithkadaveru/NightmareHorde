package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.game.entities.DamagePopupComponent

class DamagePopupSystem : GameSystem(priority = 120) { // Run after combat
    
    override fun update(deltaTime: Float, entities: List<Entity>) {
        entities.forEach { entity ->
            val popup = entity.getComponent(DamagePopupComponent::class)
            val transform = entity.getComponent(TransformComponent::class)
            
            if (popup != null && transform != null) {
                // Float up
                transform.y -= popup.floatSpeed * deltaTime
                
                // Update lifetime
                popup.timeAlive += deltaTime
                if (popup.timeAlive >= popup.lifeTime) {
                    entity.isActive = false
                }
            }
        }
    }
}
