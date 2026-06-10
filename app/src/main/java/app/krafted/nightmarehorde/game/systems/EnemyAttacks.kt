package app.krafted.nightmarehorde.game.systems

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shared helpers for enemy/boss attacks. Centralizes the spread-firing and
 * area-of-effect-damage math that AISystem and BossSystem otherwise duplicate.
 */

/**
 * Invoke [emit] once per projectile in a fan of [count] shots evenly distributed
 * across [spreadRadians], centered on [centerAngle]. A single shot fires straight
 * along the center. Each call receives the shot index and its unit direction.
 */
internal inline fun emitAngularSpread(
    count: Int,
    centerAngle: Float,
    spreadRadians: Float,
    emit: (index: Int, dirX: Float, dirY: Float) -> Unit
) {
    if (count <= 0) return
    val step = if (count > 1) spreadRadians / (count - 1) else 0f
    val start = centerAngle - spreadRadians / 2f
    for (i in 0 until count) {
        val angle = start + step * i
        emit(i, cos(angle), sin(angle))
    }
}

/**
 * Apply [damage] to the player if it is within [radius] of ([centerX], [centerY]).
 * Honors the player's armor. Used by bloater explosions and boss AOE attacks.
 */
internal fun applyRadialDamageToPlayer(
    centerX: Float,
    centerY: Float,
    damage: Float,
    radius: Float,
    player: Entity
) {
    val playerTransform = player.getComponent(TransformComponent::class) ?: return
    val playerHealth = player.getComponent(HealthComponent::class) ?: return
    val playerStats = player.getComponent(StatsComponent::class)

    val dx = playerTransform.x - centerX
    val dy = playerTransform.y - centerY
    val distSq = dx * dx + dy * dy

    if (distSq <= radius * radius) {
        val armor = playerStats?.armor ?: 0
        playerHealth.takeDamage(damage.toInt(), armor)
    }
}
