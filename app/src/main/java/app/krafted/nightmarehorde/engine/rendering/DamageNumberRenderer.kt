package app.krafted.nightmarehorde.engine.rendering

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.sp
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.game.entities.DamagePopupComponent
import javax.inject.Inject
import javax.inject.Singleton

import androidx.compose.ui.text.ExperimentalTextApi

@Singleton
@OptIn(ExperimentalTextApi::class)
class DamageNumberRenderer @Inject constructor() {

    private val textStyle = TextStyle(
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(2f, 2f),
            blurRadius = 2f
        )
    )

    // Cached style per zoom level to reduce per-popup allocations
    private var cachedZoom = -1f
    private var cachedBaseStyle = textStyle

    fun render(
        drawScope: DrawScope,
        entities: List<Entity>,
        camera: Camera,
        textMeasurer: TextMeasurer
    ) {
        val visibleBounds = camera.getVisibleBounds()

        // Rebuild base style only when zoom changes (once per frame at most)
        val currentZoom = camera.zoom.coerceAtLeast(0.5f)
        if (currentZoom != cachedZoom) {
            cachedZoom = currentZoom
            cachedBaseStyle = textStyle.copy(fontSize = (16f * currentZoom).sp)
        }

        entities.forEach { entity ->
            val popup = entity.getComponent(DamagePopupComponent::class)
            val transform = entity.getComponent(TransformComponent::class)

            if (popup != null && transform != null) {
                // Simple culling
                if (transform.x < visibleBounds.left || transform.x > visibleBounds.right ||
                    transform.y < visibleBounds.top || transform.y > visibleBounds.bottom
                ) return@forEach

                var screenX = 0f
                var screenY = 0f
                camera.worldToScreen(transform.x, transform.y) { sx, sy -> screenX = sx; screenY = sy }

                val alpha = (1f - (popup.timeAlive / popup.lifeTime)).coerceIn(0f, 1f)
                val textLayoutResult = textMeasurer.measure(
                    text = popup.damageAmount.toString(),
                    style = cachedBaseStyle.copy(color = Color.White.copy(alpha = alpha))
                )
                drawScope.withTransform({
                    translate(left = screenX, top = screenY)
                }) {
                    drawText(textLayoutResult = textLayoutResult)
                }
            }
        }
    }
}
