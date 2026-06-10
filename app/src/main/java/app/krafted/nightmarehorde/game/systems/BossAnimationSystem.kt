package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.BossComponent
import app.krafted.nightmarehorde.game.data.BossType

/**
 * Handles animation updates for boss entities.
 * Uses BossComponent for direct type access.
 * Runs at priority 17, just after player animation and before zombie animation.
 */
class BossAnimationSystem : SpriteAnimationSystem<BossType>(priority = 17) {

    override fun configKey(entity: Entity): BossType? =
        entity.getComponent(BossComponent::class)?.bossType

    override fun clipFor(key: BossType): Clip =
        Clip(endFrame = key.frameCount - 1, fps = key.animationFps)
}
