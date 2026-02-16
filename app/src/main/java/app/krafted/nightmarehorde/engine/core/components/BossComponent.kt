package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.game.data.BossType

/**
 * Tag component to identify boss entities and track boss-specific state.
 * Bosses cycle through attack phases and have special mechanics like
 * enrage, regeneration, and minion spawning.
 */
data class BossComponent(
    val bossType: BossType,

    // ─── Phase Management ──────────────────────────────────────────────
    /** Current attack phase index (0, 1, 2) — cycles through the boss's attack list */
    var currentPhase: Int = 0,
    /** Timer for the current attack phase duration */
    var phaseTimer: Float = 0f,
    /** Cooldown between attack phases */
    var phaseCooldown: Float = 0f,
    /** Whether the boss is currently executing an attack */
    var isAttacking: Boolean = false,

    // ─── Attack-specific State ─────────────────────────────────────────
    /** Ground slam wind-up timer (Tank) */
    var slamWindUp: Float = 0f,
    /** Whether currently in a slam/attack animation */
    var isSlamming: Boolean = false,

    /** Rock throw cooldown (Tank) */
    var rockThrowCooldown: Float = 0f,

    /** Charge state (Tank) */
    var isCharging: Boolean = false,
    var chargeTimer: Float = 0f,
    var chargeTargetX: Float = 0f,
    var chargeTargetY: Float = 0f,

    /** Minion spawn cooldown (Hive Queen) */
    var minionSpawnCooldown: Float = 0f,
    /** Number of minions spawned this cycle */
    var minionCount: Int = 0,

    /** Acid spray state (Hive Queen) */
    var acidSprayCooldown: Float = 0f,
    var acidSprayTimer: Float = 0f,
    var isSprayingAcid: Boolean = false,

    /** Burrow state (Hive Queen) */
    var isBurrowed: Boolean = false,
    var burrowTimer: Float = 0f,
    var burrowCooldown: Float = 0f,

    /** Regen tick timer (Abomination) */
    var regenTimer: Float = 0f,

    /** Enrage state (Abomination — activates below 30% HP) */
    var isEnraged: Boolean = false,

    /** Multi-arm swipe cooldown (Abomination) */
    var swipeCooldown: Float = 0f,
    var isSwinging: Boolean = false,
    var swipeTimer: Float = 0f,

    /** Ground spikes ranged attack cooldown (Abomination) */
    var groundSpikeCooldown: Float = 0f,

    // ─── Melee Hit Cap ──────────────────────────────────────────────────
    /** Number of melee/penetrating hits taken in the current window */
    var meleeHitCount: Int = 0,
    /** Timer for the current melee hit window — resets hitCount when it expires */
    var meleeHitWindowTimer: Float = 0f,

    // ─── Retreat Behavior ───────────────────────────────────────────────
    /** Whether the boss is currently retreating to preferred range */
    var isRetreating: Boolean = false,
    /** Timer for how long the retreat lasts (prevents endless kiting) */
    var retreatTimer: Float = 0f,
    /** Cooldown before boss can retreat again */
    var retreatCooldown: Float = 0f,

    // ─── Tracking ──────────────────────────────────────────────────────
    /** Which boss number this is in the session (1st, 2nd, 3rd...) for HP scaling */
    var bossNumber: Int = 1
) : Component
