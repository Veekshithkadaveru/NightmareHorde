package app.krafted.nightmarehorde.engine.core

/**
 * Base class for all systems.
 * Systems contain the logic to process entities with specific components.
 */
abstract class GameSystem {
    /**
     * Update the system logic.
     * @param deltaTime The time in seconds since the last frame.
     * @param entities The list of all active entities in the game.
     */
    abstract fun update(deltaTime: Float, entities: List<Entity>)
}
