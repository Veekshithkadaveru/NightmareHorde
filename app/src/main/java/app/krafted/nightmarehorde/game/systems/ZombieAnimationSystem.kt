package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.core.components.ZombieTypeComponent
import app.krafted.nightmarehorde.game.data.ZombieType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles animation updates for all zombie entities.
 * Uses ZombieTypeComponent for direct type access instead of reverse-looking up by asset name.
 */
@Singleton
class ZombieAnimationSystem @Inject constructor() : SpriteAnimationSystem<ZombieType>(priority = 16) {

    override fun configKey(entity: Entity): ZombieType? =
        entity.getComponent(ZombieTypeComponent::class)?.zombieType

    override fun clipFor(key: ZombieType): Clip =
        Clip(endFrame = key.frameCount - 1, fps = key.animationFps)

    override fun applyFacing(sprite: SpriteComponent, velocity: VelocityComponent, key: ZombieType) {
        // Types with defaultFlipX have their sprite drawn facing the opposite
        // direction, so the flip logic is inverted for them.
        if (key.defaultFlipX) {
            if (velocity.vx > 0.1f) {
                sprite.flipX = true
            } else if (velocity.vx < -0.1f) {
                sprite.flipX = false
            }
        } else {
            super.applyFacing(sprite, velocity, key)
        }
    }
}
