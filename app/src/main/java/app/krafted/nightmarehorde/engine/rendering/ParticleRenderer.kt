package app.krafted.nightmarehorde.engine.rendering

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.ParticleComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Renders ParticleComponent entities.
 * - Circle particles: drawn as simple circles (fire, explosions).
 * - Elongated particles (width > 0 && height > 0): drawn as rotated ovals
 *   using the entity's TransformComponent rotation (blade slashes, trails).
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

            // Cull off-screen particles (use larger margin for elongated shapes)
            val margin = if (particle.width > 0f) particle.width else 20f
            if (transform.x < visibleBounds.left - margin || transform.x > visibleBounds.right + margin ||
                transform.y < visibleBounds.top - margin || transform.y > visibleBounds.bottom + margin
            ) return@forEach

            val (screenX, screenY) = camera.worldToScreen(transform.x, transform.y)

            // Fade out over lifetime
            val alpha = if (particle.fadeOut) {
                (1f - (particle.timeAlive / particle.lifeTime)).coerceIn(0f, 1f)
            } else {
                1f
            }

            if (particle.width > 0f && particle.height > 0f) {
                // ── Elongated slash / blade segment ──
                drawSlashParticle(drawScope, particle, transform, screenX, screenY, alpha, camera.zoom)
            } else {
                // ── Standard circle particle ──
                val radius = particle.size * camera.zoom * 0.5f
                drawScope.drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(screenX, screenY)
                )
            }
        }
    }

    /**
     * Draws an elongated blade-like particle as a rotated oval with a glow halo.
     * The rotation comes from TransformComponent (radians, 0 = right).
     */
    private fun drawSlashParticle(
        drawScope: DrawScope,
        particle: ParticleComponent,
        transform: TransformComponent,
        screenX: Float,
        screenY: Float,
        alpha: Float,
        zoom: Float
    ) {
        val w = particle.width * zoom
        val h = particle.height * zoom

        // TransformComponent stores rotation in radians; Canvas rotate() uses degrees
        val angleDegrees = Math.toDegrees(transform.rotation.toDouble()).toFloat()

        val topLeft = Offset(screenX - w / 2f, screenY - h / 2f)

        // Outer glow (larger, more transparent)
        drawScope.rotate(degrees = angleDegrees, pivot = Offset(screenX, screenY)) {
            drawOval(
                color = particle.color.copy(alpha = (alpha * 0.35f).coerceIn(0f, 1f)),
                topLeft = Offset(screenX - w * 0.7f, screenY - h * 1.1f),
                size = Size(w * 1.4f, h * 2.2f)
            )
            // Core blade shape
            drawOval(
                color = particle.color.copy(alpha = alpha),
                topLeft = topLeft,
                size = Size(w, h)
            )
            // Bright inner highlight (narrower, brighter)
            drawOval(
                color = particle.color.copy(alpha = (alpha * 0.9f).coerceIn(0f, 1f)),
                topLeft = Offset(screenX - w * 0.35f, screenY - h * 0.3f),
                size = Size(w * 0.7f, h * 0.6f)
            )
        }
    }
}
