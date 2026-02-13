package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.ParticleComponent

/**
 * Updates particle lifetime and marks expired particles for removal.
 * Runs after combat (priority 110) so hit-effect particles spawned this frame
 * get at least one frame of visibility before being ticked.
 */
class ParticleSystem : GameSystem(priority = 110) {

    override fun update(deltaTime: Float, entities: List<Entity>) {
        entities.forEach { entity ->
            val particle = entity.getComponent(ParticleComponent::class) ?: return@forEach

            particle.timeAlive += deltaTime
            if (particle.timeAlive >= particle.lifeTime) {
                entity.isActive = false
            }
        }
    }
}
