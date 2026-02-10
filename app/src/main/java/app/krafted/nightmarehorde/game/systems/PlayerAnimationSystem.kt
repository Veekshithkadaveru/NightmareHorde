package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.CharacterComponent
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.rendering.AnimationController
import app.krafted.nightmarehorde.game.data.CharacterType

/**
 * System that manages the player's sprite animation state machine.
 * Transitions between IDLE, RUN, and DIE animations based on velocity and health.
 *
 * Now fully data-driven — reads frame counts and texture keys from
 * the player's CharacterComponent instead of hardcoded constants.
 *
 * Priority 15 — runs after PlayerSystem (10) so velocity is already set,
 * but before MovementSystem (50).
 */
class PlayerAnimationSystem : GameSystem(priority = 15) {

    private val animationController = AnimationController()
    private var currentState: AnimState = AnimState.NONE

    private companion object {
        const val IDLE_FPS = 6f
        const val RUN_FPS = 12f
        const val DIE_FPS = 8f
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        val player = entities.firstOrNull { it.getComponent(PlayerTagComponent::class) != null }
            ?: return

        val sprite = player.getComponent(SpriteComponent::class) ?: return
        val velocity = player.getComponent(VelocityComponent::class) ?: return
        val health = player.getComponent(HealthComponent::class) ?: return
        val character = player.getComponent(CharacterComponent::class)?.characterType
            ?: CharacterType.CYBERPUNK_DETECTIVE

        // Determine desired animation state
        val desiredState = when {
            !health.isAlive -> AnimState.DIE
            velocity.speed > 1f -> AnimState.RUN
            else -> AnimState.IDLE
        }

        // Transition to new state if changed
        if (desiredState != currentState) {
            currentState = desiredState
            when (desiredState) {
                AnimState.IDLE -> {
                    sprite.textureKey = character.idleTextureKey
                    sprite.frameWidth = character.frameWidth
                    sprite.frameHeight = character.frameHeight
                    animationController.play(
                        startFrame = 0,
                        endFrame = character.idleFrameCount - 1,
                        fps = IDLE_FPS,
                        mode = AnimationController.AnimationMode.LOOP
                    )
                }
                AnimState.RUN -> {
                    sprite.textureKey = character.runTextureKey
                    sprite.frameWidth = character.frameWidth
                    sprite.frameHeight = character.frameHeight
                    animationController.play(
                        startFrame = 0,
                        endFrame = character.runFrameCount - 1,
                        fps = RUN_FPS,
                        mode = AnimationController.AnimationMode.LOOP
                    )
                }
                AnimState.DIE -> {
                    // Fall back to idle texture for death (not all characters have die sheets)
                    // The cyberpunk detective has a die sheet, others use idle as fallback
                    sprite.textureKey = character.idleTextureKey
                    sprite.frameWidth = character.frameWidth
                    sprite.frameHeight = character.frameHeight
                    animationController.play(
                        startFrame = 0,
                        endFrame = 0,
                        fps = DIE_FPS,
                        mode = AnimationController.AnimationMode.ONCE
                    )
                }
                AnimState.NONE -> { /* Initial state, do nothing */ }
            }
        }

        // Advance animation
        animationController.update(deltaTime)

        // Write current frame back to sprite component
        sprite.currentFrame = animationController.currentFrame
    }

    private enum class AnimState {
        NONE, IDLE, RUN, DIE
    }
}
