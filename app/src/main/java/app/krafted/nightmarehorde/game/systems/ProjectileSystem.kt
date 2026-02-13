package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.ProjectileComponent

class ProjectileSystem : GameSystem(priority = 80) {
    
    override fun update(deltaTime: Float, entities: List<Entity>) {
        entities.forEach { entity ->
            // Fix: Use ::class
            val projectile = entity.getComponent(ProjectileComponent::class)
            
            if (projectile != null) {
                projectile.timeAlive += deltaTime
                
                if (projectile.timeAlive >= projectile.maxLifetime) {
                    entity.isActive = false
                }
            }
        }
    }
}
