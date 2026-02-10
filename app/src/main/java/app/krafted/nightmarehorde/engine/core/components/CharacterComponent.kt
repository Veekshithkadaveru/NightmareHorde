package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.game.data.CharacterType

/**
 * Component that stores which character type this entity represents.
 * Attached to the player entity so systems can read animation/sprite
 * data from the selected CharacterType.
 */
data class CharacterComponent(
    val characterType: CharacterType
) : Component
