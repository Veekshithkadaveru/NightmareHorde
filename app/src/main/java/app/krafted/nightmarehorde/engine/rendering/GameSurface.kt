package app.krafted.nightmarehorde.engine.rendering

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import app.krafted.nightmarehorde.engine.core.Entity
import androidx.compose.ui.text.ExperimentalTextApi

/**
 * Compose Canvas surface for rendering the game world.
 * This is the main rendering component that displays all game entities.
 */
@Composable
@OptIn(ExperimentalTextApi::class)
fun GameSurface(
    entities: List<Entity>,
    camera: Camera,
    spriteRenderer: SpriteRenderer,
    damageNumberRenderer: DamageNumberRenderer,
    particleRenderer: ParticleRenderer,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black
) {
    val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .onSizeChanged { size ->
                camera.screenWidth = size.width.toFloat()
                camera.screenHeight = size.height.toFloat()
            }
    ) {
        // Render all sprites through the sprite renderer
        spriteRenderer.render(
            drawScope = this,
            entities = entities,
            camera = camera
        )

        // Render hit-effect particles
        particleRenderer.render(
            drawScope = this,
            entities = entities,
            camera = camera
        )

        // Render damage numbers on top
        damageNumberRenderer.render(
            drawScope = this,
            entities = entities,
            camera = camera,
            textMeasurer = textMeasurer
        )
    }
}

/**
 * Overload that accepts a function to provide entities.
 * Calls the provider directly each recomposition to ensure fresh entity list.
 * 
 * Note: Do not cache with remember() as entities change every frame.
 */
@Composable
fun GameSurface(
    entitiesProvider: () -> List<Entity>,
    camera: Camera,
    spriteRenderer: SpriteRenderer,
    damageNumberRenderer: DamageNumberRenderer,
    particleRenderer: ParticleRenderer,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black
) {
    GameSurface(
        entities = entitiesProvider(),
        camera = camera,
        spriteRenderer = spriteRenderer,
        damageNumberRenderer = damageNumberRenderer,
        particleRenderer = particleRenderer,
        modifier = modifier,
        backgroundColor = backgroundColor
    )
}