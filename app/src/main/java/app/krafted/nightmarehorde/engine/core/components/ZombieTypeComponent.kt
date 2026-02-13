package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.game.data.ZombieType

/**
 * Stores the ZombieType on an entity so systems can access it directly
 * without reverse-looking up by asset name.
 */
data class ZombieTypeComponent(
    val zombieType: ZombieType
) : Component
