package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.Vector2
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponComponent

class AutoAimSystem : GameSystem(priority = 20) {

    override fun update(deltaTime: Float, entities: List<Entity>) {
        // Fix: Use ::class instead of ::class.java for getComponent if it expects KClass
        // BUT Entity.kt usually takes KClass. Let's check Entity.kt definition in step 56:
        // fun <T : Component> getComponent(type: KClass<T>): T?
        // So we must use ::class
        
        val player = entities.firstOrNull { it.hasComponent(PlayerTagComponent::class) } ?: return
        val playerTransform = player.getComponent(TransformComponent::class) ?: return
        val weaponComp = player.getComponent(WeaponComponent::class) ?: return

        var nearestEnemy: Entity? = null
        var minDistanceSquared = Float.MAX_VALUE
        val rangelimitSquared = 1000f * 1000f

        for (entity in entities) {
            if (entity.id == player.id || !entity.isActive) continue
            // Optimization: Only check entities that have Health (potential targets)
            if (!entity.hasComponent(HealthComponent::class)) continue
            
            val transform = entity.getComponent(TransformComponent::class) ?: continue
            
            val dx = transform.x - playerTransform.x
            val dy = transform.y - playerTransform.y
            val distSq = dx * dx + dy * dy
            
            if (distSq < rangelimitSquared && distSq < minDistanceSquared) {
                minDistanceSquared = distSq
                nearestEnemy = entity
            }
        }

        if (nearestEnemy != null) {
            val targetTransform = nearestEnemy.getComponent(TransformComponent::class)!!
            val direction = Vector2(
                targetTransform.x - playerTransform.x,
                targetTransform.y - playerTransform.y
            ).normalized()
            
            weaponComp.facingDirection = direction
        }
    }
}
