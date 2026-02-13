package app.krafted.nightmarehorde.engine.rendering

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ParticleComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders ParticleComponent entities as small colored circles.
 * Handles fade-out based on remaining lifetime.
 */
@Singleton
class ParticleRenderer @Inject constructor() {

    fun render(
        drawScope: DrawScope,
        entities: List<Entity>,
        camera: Camera
    ) {
        val visibleBounds = camera.getVisibleBounds()

        entities.forEach { entity ->
            val particle = entity.getComponent(ParticleComponent::class) ?: return@forEach
            val transform = entity.getComponent(TransformComponent::class) ?: return@forEach

            // Cull off-screen particles
            if (transform.x < visibleBounds.left - 20f || transform.x > visibleBounds.right + 20f ||
                transform.y < visibleBounds.top - 20f || transform.y > visibleBounds.bottom + 20f
            ) return@forEach

            val (screenX, screenY) = camera.worldToScreen(transform.x, transform.y)

            // Fade out over lifetime
            val alpha = if (particle.fadeOut) {
                (1f - (particle.timeAlive / particle.lifeTime)).coerceIn(0f, 1f)
            } else {
                1f
            }

            val radius = particle.size * camera.zoom * 0.5f

            drawScope.drawCircle(
                color = particle.color.copy(alpha = alpha),
                radius = radius,
                center = Offset(screenX, screenY)
            )
        }
    }
}
