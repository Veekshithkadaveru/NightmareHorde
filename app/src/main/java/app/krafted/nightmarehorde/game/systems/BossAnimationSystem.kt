package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.BossComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.rendering.AnimationController
import app.krafted.nightmarehorde.game.data.BossType

/**
 * Handles animation updates for boss entities.
 * Uses BossComponent for direct type access.
 * Runs at priority 17, just after player animation and before zombie animation.
 */
class BossAnimationSystem : GameSystem(priority = 17) {

    private val controllers = mutableMapOf<Long, AnimationController>()
    private val configuredType = mutableMapOf<Long, BossType>()

    override fun update(deltaTime: Float, entities: List<Entity>) {
        for (entity in entities) {
            val bossComp = entity.getComponent(BossComponent::class) ?: continue
            val sprite = entity.getComponent(SpriteComponent::class) ?: continue
            val velocity = entity.getComponent(VelocityComponent::class) ?: continue

            val controller = controllers.getOrPut(entity.id) { AnimationController() }
            val type = bossComp.bossType

            if (configuredType[entity.id] != type) {
                controller.play(
                    startFrame = 0,
                    endFrame = type.frameCount - 1,
                    fps = type.animationFps,
                    mode = AnimationController.AnimationMode.LOOP
                )
                configuredType[entity.id] = type
            }

            controller.update(deltaTime)
            sprite.currentFrame = controller.currentFrame

            // Flip sprite horizontally based on movement direction
            if (velocity.vx < -0.1f) {
                sprite.flipX = true
            } else if (velocity.vx > 0.1f) {
                sprite.flipX = false
            }
        }

        // Clean up controllers for dead/removed boss entities.
        // Only iterate the small controller map instead of allocating a HashSet of all entity IDs.
        if (controllers.isNotEmpty()) {
            controllers.keys.removeAll { id ->
                entities.none { it.id == id && it.isActive && it.hasComponent(BossComponent::class) }
            }
            configuredType.keys.retainAll(controllers.keys)
        }
    }
}
