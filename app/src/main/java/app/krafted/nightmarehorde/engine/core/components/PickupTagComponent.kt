package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component

data class PickupTagComponent(
    var timeAlive: Float = 0f,
    val despawnAfterSeconds: Float = 30f,
    val baseY: Float = 0f
) : Component
