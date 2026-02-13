package app.krafted.nightmarehorde.engine.core.components

import app.krafted.nightmarehorde.engine.core.Component
import app.krafted.nightmarehorde.engine.core.Entity

data class AIComponent(
    var behavior: AIBehavior = AIBehavior.IDLE,
    var target: Entity? = null,
    var stateTimer: Float = 0f,
    var actionReady: Boolean = false,
    var range: Float = 0f,

    // Ranged attack cooldown
    var attackCooldown: Float = 0f,

    // Charge state (for Brute)
    var isCharging: Boolean = false,
    var chargeTimer: Float = 0f,
    var chargeCooldown: Float = 0f,
    var chargeTargetX: Float = 0f,
    var chargeTargetY: Float = 0f,

    // Buff tracking (for Screamer)
    var buffTimer: Float = 0f,
    var isBuffed: Boolean = false,
    var buffTimeRemaining: Float = 0f
) : Component
