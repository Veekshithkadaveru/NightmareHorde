package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.core.components.ZombieTypeComponent
import app.krafted.nightmarehorde.engine.rendering.AnimationController
import app.krafted.nightmarehorde.game.data.ZombieType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles animation updates for all zombie entities.
 * Uses ZombieTypeComponent for direct type access instead of reverse-looking up by asset name.
 */
@Singleton
class ZombieAnimationSystem @Inject constructor() : GameSystem(priority = 16) {

    /** AnimationController per entity — keeps track of frame progress */
    private val controllers = mutableMapOf<Long, AnimationController>()

    /** Tracks which ZombieType was last configured per entity so we only call play() once */
    private val configuredType = mutableMapOf<Long, ZombieType>()

    override fun update(deltaTime: Float, entities: List<Entity>) {
        entities.forEach { entity ->
            val typeComp = entity.getComponent(ZombieTypeComponent::class) ?: return@forEach
            val sprite = entity.getComponent(SpriteComponent::class) ?: return@forEach
            val velocity = entity.getComponent(VelocityComponent::class) ?: return@forEach

            val controller = controllers.getOrPut(entity.id) { AnimationController() }
            val type = typeComp.zombieType

            // Only call play() when the animation hasn't been configured yet or the type changed.
            // play() resets the animation to frame 0, so calling it every frame prevents animation.
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

            // Flip sprite horizontally based on movement direction.
            // Types with defaultFlipX have their sprite drawn facing the
            // opposite direction, so the flip logic is inverted for them.
            if (type.defaultFlipX) {
                if (velocity.vx > 0.1f) {
                    sprite.flipX = true
                } else if (velocity.vx < -0.1f) {
                    sprite.flipX = false
                }
            } else {
                if (velocity.vx < -0.1f) {
                    sprite.flipX = true
                } else if (velocity.vx > 0.1f) {
                    sprite.flipX = false
                }
            }
        }

        // Clean up controllers and config for dead/removed entities
        val aliveIds = entities.mapTo(HashSet()) { it.id }
        controllers.keys.retainAll(aliveIds)
        configuredType.keys.retainAll(aliveIds)
    }
}
