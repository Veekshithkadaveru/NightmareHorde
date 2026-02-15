package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component

data class HealthPickupComponent(
    val healAmount: Int = 10
) : Component
