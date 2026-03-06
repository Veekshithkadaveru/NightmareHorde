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
    BOSS_EXECUTIONER, // Executioner: axe sweep, throwing axes, leaping strike
    BOSS_WIDOWMAKER,  // Widowmaker: spawn spiders, web throw, drop from ceiling
    BOSS_AMALGAM      // Flesh Amalgam: bone splinters, flesh sweep, rapidly mutates
}
