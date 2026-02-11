package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.game.data.ObstacleType

/**
 * Marker component to identify obstacle entities.
 * Systems use this to find obstacles among all entities:
 *   entity.get<ObstacleTagComponent>() != null
 */
data class ObstacleTagComponent(
    val obstacleType: ObstacleType
) : Component
