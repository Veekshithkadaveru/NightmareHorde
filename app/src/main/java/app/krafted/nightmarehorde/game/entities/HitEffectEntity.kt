package app.krafted.nightmarehorde.game.entities

import androidx.compose.ui.graphics.Color
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ParticleComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent

/**
 * Factory for spawning short-lived hit-effect particles.
 * Each call creates a single particle that flies outward from the hit point,
 * fades out, and is removed after its lifetime expires.
 */
object HitEffectEntity {

    private val random = java.util.Random()

    /**
     * Spawn a burst of particles at the given position.
     * Returns a list of particle entities to be added to the game loop.
     */
    fun burst(
        x: Float,
        y: Float,
        count: Int = 5,
        baseColor: Color = Color(0xFFFF6644),
        speed: Float = 120f,
        lifeTime: Float = 0.35f,
        size: Float = 6f
    ): List<Entity> {
        return (0 until count).map {
            // Random outward direction
            val angle = random.nextFloat() * (2f * Math.PI.toFloat())
            val spd = speed * (0.5f + random.nextFloat() * 0.5f)
            val vx = kotlin.math.cos(angle) * spd
            val vy = kotlin.math.sin(angle) * spd

            // Slight color variation
            val r = (baseColor.red + (random.nextFloat() - 0.5f) * 0.2f).coerceIn(0f, 1f)
            val g = (baseColor.green + (random.nextFloat() - 0.5f) * 0.15f).coerceIn(0f, 1f)
            val b = (baseColor.blue + (random.nextFloat() - 0.5f) * 0.1f).coerceIn(0f, 1f)
            val color = Color(r, g, b, 1f)

            val particleSize = size * (0.6f + random.nextFloat() * 0.8f)

            Entity().apply {
                addComponent(TransformComponent(x = x, y = y))
                addComponent(VelocityComponent(vx = vx, vy = vy))
                addComponent(ParticleComponent(
                    color = color,
                    size = particleSize,
                    lifeTime = lifeTime * (0.7f + random.nextFloat() * 0.6f)
                ))
            }
        }
    }
}
