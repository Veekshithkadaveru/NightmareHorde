package app.krafted.nightmarehorde.engine.rendering

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.DroneComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sin

/**
 * Renders a compact horizontal fuel bar below each drone entity in world space.
 * Bar is hidden when fuel is full to reduce visual noise.
 * Color transitions: accent -> amber (<50%) -> pulsing red/orange (<15s) -> solid red (powered down).
 */
@Singleton
class DroneRenderer @Inject constructor() {

    fun render(
        drawScope: DrawScope,
        entities: List<Entity>,
        camera: Camera
    ) {
        val visibleBounds = camera.getVisibleBounds()

        for (entity in entities) {
            val drone = entity.getComponent(DroneComponent::class) ?: continue
            val transform = entity.getComponent(TransformComponent::class) ?: continue

            if (transform.x < visibleBounds.left - 30f || transform.x > visibleBounds.right + 30f ||
                transform.y < visibleBounds.top - 30f || transform.y > visibleBounds.bottom + 30f
            ) continue

            var screenX = 0f
            var screenY = 0f
            camera.worldToScreen(transform.x, transform.y) { sx, sy -> screenX = sx; screenY = sy }

            drawFuelBar(drawScope, screenX, screenY, drone, camera.zoom)
        }
    }

    private fun drawFuelBar(
        drawScope: DrawScope,
        screenX: Float,
        screenY: Float,
        drone: DroneComponent,
        zoom: Float
    ) {
        // Hide bar when fuel is full — reduce visual noise
        if (drone.fuelPercent >= 1f) return

        val barWidth = 20f * zoom
        val barHeight = 3f * zoom
        val cornerRadius = CornerRadius(barHeight / 2f, barHeight / 2f)

        // Position centered below the drone sprite
        val barX = screenX - barWidth / 2f
        val barY = screenY + 14f * zoom

        // Background track
        drawScope.drawRoundRect(
            color = Color(0x66000000),
            topLeft = Offset(barX, barY),
            size = Size(barWidth, barHeight),
            cornerRadius = cornerRadius
        )

        // Fuel fill
        val fillWidth = barWidth * drone.fuelPercent
        if (fillWidth <= 0f) return

        val fillColor = when {
            drone.isPoweredDown -> Color(0xFFFF2222)
            drone.isLowFuel -> {
                val pulse = sin(drone.glowPulseTimer * Math.PI.toFloat() * 2f)
                if (pulse > 0) Color(0xFFFF4400) else Color(0xFFFF8800)
            }
            drone.fuelPercent > 0.5f -> Color(drone.droneType.accentColor)
            else -> Color(0xFFFFAA00)
        }

        drawScope.drawRoundRect(
            color = fillColor,
            topLeft = Offset(barX, barY),
            size = Size(fillWidth, barHeight),
            cornerRadius = cornerRadius
        )
    }
}
