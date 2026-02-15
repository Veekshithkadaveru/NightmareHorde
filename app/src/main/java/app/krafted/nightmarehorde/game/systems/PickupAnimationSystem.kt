package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.PickupTagComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.rendering.AnimationController

/**
 * Animates pickup orb sprites by cycling through their sprite sheet frames.
 *
 * Frame counts are read from [SpriteComponent.totalFrames] rather than being
 * hardcoded, so new pickup types only need to set `totalFrames` in their factory.
 */
class PickupAnimationSystem : GameSystem(priority = 26) {

    private val controllers = mutableMapOf<Long, AnimationController>()
    private val configuredTexture = mutableMapOf<Long, String>()

    companion object {
        private const val ANIMATION_FPS = 10f
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        entities.forEach { entity ->
            if (!entity.isActive) return@forEach
            entity.getComponent(PickupTagComponent::class) ?: return@forEach
            val sprite = entity.getComponent(SpriteComponent::class) ?: return@forEach

            val controller = controllers.getOrPut(entity.id) { AnimationController() }
            val textureKey = sprite.textureKey

            if (configuredTexture[entity.id] != textureKey) {
                val frameCount = sprite.totalFrames
                if (frameCount <= 1) return@forEach // Static sprite, nothing to animate
                controller.play(
                    startFrame = 0,
                    endFrame = frameCount - 1,
                    fps = ANIMATION_FPS,
                    mode = AnimationController.AnimationMode.LOOP
                )
                configuredTexture[entity.id] = textureKey
            }

            controller.update(deltaTime)
            sprite.currentFrame = controller.currentFrame
        }

        val aliveIds = entities.filter { it.isActive }.mapTo(HashSet()) { it.id }
        controllers.keys.retainAll(aliveIds)
        configuredTexture.keys.retainAll(aliveIds)
    }
}
