package app.krafted.nightmarehorde.engine.core

/**
 * Base class for all systems.
 * Systems contain the logic to process entities with specific components.
 * 
 * @param priority Determines update order. Lower values run first.
 *                 Recommended ranges:
 *                 - 0-49: Pre-physics (input, AI decisions)
 *                 - 50-99: Physics (movement, collision)
 *                 - 100-149: Post-physics (damage resolution, spawning)
 *                 - 150+: Rendering and cleanup
 */
abstract class GameSystem(
    val priority: Int = 50
) {
    /**
     * Update the system logic.
     * @param deltaTime The time in seconds since the last frame.
     * @param entities The list of all active entities in the game.
     */
    abstract fun update(deltaTime: Float, entities: List<Entity>)
}

