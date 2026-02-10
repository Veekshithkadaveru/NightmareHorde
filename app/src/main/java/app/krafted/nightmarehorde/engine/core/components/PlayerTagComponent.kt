package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component

/**
 * Marker component to identify the player entity.
 * Systems use this to find the player among all entities:
 *   entity.get<PlayerTagComponent>() != null
 */
class PlayerTagComponent : Component
