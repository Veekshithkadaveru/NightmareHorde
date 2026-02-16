package app.krafted.nightmarehorde.engine.core.components

/**
 * Defines the high-level behavior state for AI entities.
 */
enum class AIBehavior {
    IDLE,
    CHASE,    // Move towards target (Walker, Runner, Crawler)
    RANGED,   // Move to range and fire projectiles (Spitter)
    EXPLODE,  // Move close and explode on death (Bloater)
    BUFF,     // Stay near allies and buff them (Screamer)
    CHARGE,   // Periodically charge at high speed (Brute)
    FLEE,

    // Boss behaviors — each boss cycles through attack phases
    BOSS_TANK,        // The Tank: ground slam, rock throw, charge
    BOSS_HIVE_QUEEN,  // Hive Queen: spawn minions, acid spray, burrow
    BOSS_ABOMINATION  // Abomination: multi-arm swipe, regen, enrage
}
