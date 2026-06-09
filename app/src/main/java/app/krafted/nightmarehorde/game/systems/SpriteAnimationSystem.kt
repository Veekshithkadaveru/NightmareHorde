package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.rendering.AnimationController

/**
 * Base class for systems that drive looping sprite-sheet animations off a
 * per-entity type config (zombies, bosses, …).
 *
 * Subclasses supply the config key per entity ([configKey]) and the clip to
 * play for that key ([clipFor]); this class owns the [AnimationController]
 * lifecycle, the "only re-play when the clip changes" rule, frame write-back,
 * and cleanup of controllers for entities that have gone away.
 *
 * @param K the per-entity config key (e.g. ZombieType / BossType). Re-playing
 *          the clip only happens when this key changes for an entity.
 */
abstract class SpriteAnimationSystem<K : Any>(priority: Int) : GameSystem(priority) {

    /** A looping clip's end frame (inclusive) and playback rate. */
    protected data class Clip(val endFrame: Int, val fps: Float)

    private val controllers = mutableMapOf<Long, AnimationController>()
    private val configuredKeys = mutableMapOf<Long, K>()
    private val processedIds = HashSet<Long>(64)

    final override fun update(deltaTime: Float, entities: List<Entity>) {
        processedIds.clear()

        for (entity in entities) {
            val sprite = entity.getComponent(SpriteComponent::class) ?: continue
            val velocity = entity.getComponent(VelocityComponent::class) ?: continue
            val key = configKey(entity) ?: continue

            processedIds.add(entity.id)
            val controller = controllers.getOrPut(entity.id) { AnimationController() }

            // play() resets to frame 0, so only (re)configure when the key changes.
            // Calling it every frame would pin the animation on frame 0.
            if (configuredKeys[entity.id] != key) {
                val clip = clipFor(key)
                controller.play(
                    startFrame = 0,
                    endFrame = clip.endFrame,
                    fps = clip.fps,
                    mode = AnimationController.AnimationMode.LOOP
                )
                configuredKeys[entity.id] = key
            }

            controller.update(deltaTime)
            sprite.currentFrame = controller.currentFrame

            applyFacing(sprite, velocity, key)
        }

        // Drop controllers for entities no longer present.
        controllers.keys.retainAll(processedIds)
        configuredKeys.keys.retainAll(processedIds)
    }

    /** The animation config key for [entity], or null to skip it this frame. */
    protected abstract fun configKey(entity: Entity): K?

    /** The looping clip (end frame + fps) to play for [key]. */
    protected abstract fun clipFor(key: K): Clip

    /**
     * Update horizontal sprite facing from movement direction. Defaults to
     * facing the direction of travel; subclasses override for inverted sprites.
     */
    protected open fun applyFacing(sprite: SpriteComponent, velocity: VelocityComponent, key: K) {
        if (velocity.vx < -0.1f) {
            sprite.flipX = true
        } else if (velocity.vx > 0.1f) {
            sprite.flipX = false
        }
    }
}
