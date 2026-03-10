package app.krafted.nightmarehorde.game.systems

import androidx.compose.ui.graphics.Color
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameSystem
import app.krafted.nightmarehorde.engine.core.components.BossComponent
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.ParticleComponent
import app.krafted.nightmarehorde.engine.core.components.ProjectileComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.physics.Collider
import app.krafted.nightmarehorde.game.data.BossType
import app.krafted.nightmarehorde.game.data.ZombieType
import app.krafted.nightmarehorde.game.entities.HitEffectEntity
import app.krafted.nightmarehorde.game.entities.ZombieEntity
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Boss AI controller system.
 * Handles all 3 boss types with unique attack patterns:
 * - The Tank: Ground Slam (AOE), Rock Throw (projectile), Charge (dash)
 * - Hive Queen: Spawn Minions, Acid Spray (cone), Burrow (teleport + AOE)
 * - Abomination: Multi-Arm Swipe (wide melee), Regen (passive), Enrage (below 30% HP)
 *
 * Runs at priority 19 (right after AISystem at 18) so boss AI is processed
 * after regular zombie AI but before physics.
 */
class BossSystem : GameSystem(priority = 19) {

    companion object {
        // ─── Executioner Constants ─────────────────────────────────────
        const val EXECUTIONER_SWEEP_WINDUP = 0.6f     // seconds before sweep hits
        const val EXECUTIONER_SWEEP_RADIUS = 140f     // melee reach
        const val EXECUTIONER_SWEEP_COOLDOWN = 4f     // seconds between sweeps
        const val EXECUTIONER_AXE_SPEED = 300f        // projectile throwing axe speed
        const val EXECUTIONER_AXE_COOLDOWN = 3f       // seconds between throws
        const val EXECUTIONER_AXE_LIFETIME = 4f       // throwing axe lifetime
        const val EXECUTIONER_AXE_RANGE = 500f        // shorter range than tank rocks
        const val EXECUTIONER_LEAP_SPEED = 400f       // fast leap speed
        const val EXECUTIONER_LEAP_DURATION = 0.5f    // very short burst
        const val EXECUTIONER_LEAP_COOLDOWN = 8f      // seconds between leaps
        const val EXECUTIONER_PHASE_DURATION = 2.5f   // seconds per attack phase

        // ─── Widowmaker Constants ──────────────────────────────────────
        const val WIDOWMAKER_MINION_COUNT = 4        // spiderlings per spawn
        const val WIDOWMAKER_MINION_COOLDOWN = 8f    // seconds between minion waves
        const val WIDOWMAKER_WEB_PROJECTILES = 7     // projectiles per throw
        const val WIDOWMAKER_WEB_SPREAD = 55f        // web throw angle in degrees
        const val WIDOWMAKER_WEB_SPEED = 240f        // web projectile speed
        const val WIDOWMAKER_WEB_COOLDOWN = 2.5f     // seconds between throws
        const val WIDOWMAKER_WEB_LIFETIME = 4f       // web lifetime
        const val WIDOWMAKER_WEB_RANGE = 600f        // max range to fire web
        const val WIDOWMAKER_DROP_DURATION = 1.5f    // seconds on ceiling
        const val WIDOWMAKER_DROP_COOLDOWN = 10f     // seconds between drops
        const val WIDOWMAKER_DROP_AOE_RADIUS = 100f
        const val WIDOWMAKER_PHASE_DURATION = 3f

        // ─── Flesh Amalgam Constants ─────────────────────────────────────
        const val AMALGAM_SWIPE_ARC = 8           // number of projectile segments in arc
        const val AMALGAM_SWIPE_RADIUS = 120f     // melee arc reach (slightly longer)
        const val AMALGAM_SWIPE_COOLDOWN = 3f     // seconds between swipes
        const val AMALGAM_SWIPE_DURATION = 0.4f   // swipe animation time
        const val AMALGAM_REGEN_INTERVAL = 2f     // seconds between regen ticks
        const val AMALGAM_REGEN_AMOUNT = 15       // HP per regen tick
        const val AMALGAM_ENRAGE_THRESHOLD = 0.3f // HP % to trigger enrage
        const val AMALGAM_ENRAGE_SPEED_MULT = 1.6f // Faster than Abomination when enraged
        const val AMALGAM_ENRAGE_DAMAGE_MULT = 1.5f
        const val AMALGAM_PHASE_DURATION = 2f
        // Bone Splinters ranged attack
        const val AMALGAM_SPIKE_COUNT = 5         // number of bone splinters (1 more than spikes)
        const val AMALGAM_SPIKE_SPEED = 240f      // splinter projectile speed
        const val AMALGAM_SPIKE_COOLDOWN = 3.5f   // seconds between splinter attacks
        const val AMALGAM_SPIKE_LIFETIME = 3.5f   // splinter projectile lifetime
        const val AMALGAM_SPIKE_RANGE = 500f      // max range to fire splinters
        const val AMALGAM_SPIKE_SPREAD = 30f      // spread angle in degrees (wider)

        // ─── Retreat Behavior Constants ─────────────────────────────────
        /** Max time a boss spends retreating (prevents infinite kiting) */
        const val RETREAT_DURATION = 1.5f
        /** Cooldown before boss can retreat again */
        const val RETREAT_COOLDOWN = 4f
        /** Speed multiplier while retreating (relative to base move speed) */
        const val RETREAT_SPEED_MULT = 0.9f
        /** Distance threshold — if player is closer than this fraction of preferredRange, boss may retreat */
        const val RETREAT_TRIGGER_FRACTION = 0.6f

        // ─── Melee Hit Cap Constants ─────────────────────────────────────
        /** Max melee/penetrating hits a boss can take per sweep window.
         *  Hard-caps whip blade (12 segments) to only 3 actual damage instances. */
        const val BOSS_MAX_MELEE_HITS_PER_WINDOW = 3
        /** Duration of the melee hit window in seconds — slightly longer than
         *  the whip sweep lifetime (0.25s) to cover all segments in one sweep. */
        const val BOSS_MELEE_HIT_WINDOW = 0.4f

        // ─── Melee Retaliation Constants ─────────────────────────────────
        /** Chance (0-1) that a melee hit triggers a retaliation shockwave */
        const val RETALIATION_CHANCE = 0.4f
        /** Minimum cooldown between retaliations (prevents multi-trigger per whip sweep) */
        const val RETALIATION_COOLDOWN = 0.8f
        /** Number of shockwave projectiles in the radial ring */
        const val RETALIATION_PROJECTILE_COUNT = 8
        /** Speed of shockwave projectiles */
        const val RETALIATION_SPEED = 280f
        /** Lifetime of shockwave projectiles */
        const val RETALIATION_LIFETIME = 0.5f
        /** Damage multiplier relative to boss base damage */
        const val RETALIATION_DAMAGE_MULT = 0.4f
    }

    private var playerEntity: Entity? = null

    /** Callback to spawn entities (projectiles, minions, effects) */
    var onSpawnEntity: ((Entity) -> Unit)? = null

    /** Per-boss retaliation cooldown timer (entity ID → remaining cooldown seconds) */
    private val retaliationCooldowns = mutableMapOf<Long, Float>()

    fun setPlayer(player: Entity) {
        playerEntity = player
    }

    override fun update(deltaTime: Float, entities: List<Entity>) {
        val player = playerEntity ?: return
        val playerTransform = player.getComponent(TransformComponent::class) ?: return

        // Tick retaliation cooldowns
        val cooldownIter = retaliationCooldowns.iterator()
        while (cooldownIter.hasNext()) {
            val entry = cooldownIter.next()
            entry.setValue(entry.value - deltaTime)
            if (entry.value <= 0f) cooldownIter.remove()
        }

        for (entity in entities) {
            val boss = entity.getComponent(BossComponent::class) ?: continue
            if (!entity.isActive) {
                retaliationCooldowns.remove(entity.id)
                continue
            }

            val transform = entity.getComponent(TransformComponent::class) ?: continue
            val velocity = entity.getComponent(VelocityComponent::class) ?: continue
            val stats = entity.getComponent(StatsComponent::class) ?: continue
            val health = entity.getComponent(HealthComponent::class) ?: continue

            // Tick phase cooldown
            if (boss.phaseCooldown > 0f) {
                boss.phaseCooldown -= deltaTime
            }

            // Tick melee hit window — reset hit count when window expires
            if (boss.meleeHitWindowTimer > 0f) {
                boss.meleeHitWindowTimer -= deltaTime
                if (boss.meleeHitWindowTimer <= 0f) {
                    boss.meleeHitCount = 0
                }
            }

            // Tick retreat cooldown
            if (boss.retreatCooldown > 0f) {
                boss.retreatCooldown -= deltaTime
            }

            // Handle active retreat (shared by all boss types)
            if (boss.isRetreating) {
                boss.retreatTimer -= deltaTime
                if (boss.retreatTimer <= 0f) {
                    boss.isRetreating = false
                    boss.retreatCooldown = RETREAT_COOLDOWN
                } else {
                    retreatFromPlayer(transform, velocity, stats, playerTransform)
                    // Fire ranged attacks while retreating if possible
                    fireOpportunisticRanged(entity, boss, transform, stats, playerTransform)
                    continue
                }
            }

            when (boss.bossType) {
                BossType.EXECUTIONER -> updateExecutioner(entity, boss, transform, velocity, stats, health, playerTransform, player, deltaTime)
                BossType.WIDOWMAKER -> updateWidowmaker(entity, boss, transform, velocity, stats, health, playerTransform, player, deltaTime)
                BossType.FLESH_AMALGAM -> updateAmalgam(entity, boss, transform, velocity, stats, health, playerTransform, player, deltaTime)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // THE EXECUTIONER
    // ═══════════════════════════════════════════════════════════════════════

    private fun updateExecutioner(
        entity: Entity,
        boss: BossComponent,
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        health: HealthComponent,
        playerTransform: TransformComponent,
        player: Entity,
        deltaTime: Float
    ) {
        val dx = playerTransform.x - transform.x
        val dy = playerTransform.y - transform.y
        val distance = sqrt(dx * dx + dy * dy)

        // Tick cooldowns
        if (boss.rockThrowCooldown > 0f) boss.rockThrowCooldown -= deltaTime

        // Handle active leap (charge)
        if (boss.isCharging) {
            boss.chargeTimer -= deltaTime
            val cdx = boss.chargeTargetX - transform.x
            val cdy = boss.chargeTargetY - transform.y
            val cDist = sqrt(cdx * cdx + cdy * cdy)

            if (cDist > 10f && boss.chargeTimer > 0f) {
                val dirX = cdx / cDist
                val dirY = cdy / cDist
                velocity.vx = dirX * EXECUTIONER_LEAP_SPEED
                velocity.vy = dirY * EXECUTIONER_LEAP_SPEED
            } else {
                boss.isCharging = false
                boss.phaseCooldown = 1.5f
                velocity.vx = 0f
                velocity.vy = 0f
                // Spawn impact effect at current position (blood red smash)
                spawnGroundSlamEffect(transform.x, transform.y, 80f, Color(0xFFAA2222))
            }
            return
        }

        // Handle active Axe Sweep (slam)
        if (boss.isSlamming) {
            velocity.vx = 0f
            velocity.vy = 0f
            boss.slamWindUp -= deltaTime
            if (boss.slamWindUp <= 0f) {
                boss.isSlamming = false
                boss.phaseCooldown = 1.5f
                // Deal heavy AOE damage at boss position
                applyAoeDamage(transform, stats.baseDamage * 1.5f, EXECUTIONER_SWEEP_RADIUS, player)
                spawnGroundSlamEffect(transform.x, transform.y, EXECUTIONER_SWEEP_RADIUS, Color(0xFFCC3333))
            }
            return
        }

        // Retreat trigger: if player is too close and retreat is off cooldown
        if (!boss.isRetreating && boss.retreatCooldown <= 0f &&
            distance < boss.bossType.preferredRange * RETREAT_TRIGGER_FRACTION) {
            boss.isRetreating = true
            boss.retreatTimer = RETREAT_DURATION
            return
        }

        // Opportunistic throwing axe: can fire in ANY phase if cooldown is ready and in range
        if (boss.rockThrowCooldown <= 0f && distance < EXECUTIONER_AXE_RANGE && distance > EXECUTIONER_SWEEP_RADIUS) {
            fireThrowingAxe(entity, transform, playerTransform, stats)
            boss.rockThrowCooldown = EXECUTIONER_AXE_COOLDOWN
        }

        // Phase cycling when cooldown is ready
        if (boss.phaseCooldown <= 0f) {
            boss.phaseTimer += deltaTime
            if (boss.phaseTimer >= EXECUTIONER_PHASE_DURATION) {
                boss.phaseTimer = 0f
                boss.currentPhase = (boss.currentPhase + 1) % 3
            }

            when (boss.currentPhase) {
                0 -> { // Axe Sweep — move close, then sweep (slam)
                    if (distance > EXECUTIONER_SWEEP_RADIUS * 0.8f) {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    } else {
                        boss.isSlamming = true
                        boss.slamWindUp = EXECUTIONER_SWEEP_WINDUP
                        velocity.vx = 0f
                        velocity.vy = 0f
                    }
                }
                1 -> { // Throwing Axe — fire from range, retreat if too close
                    if (distance < EXECUTIONER_SWEEP_RADIUS) {
                        // Too close for axe throw — back away
                        retreatFromPlayer(transform, velocity, stats, playerTransform)
                    } else {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
                2 -> { // Leaping Strike (charge)
                    if (!boss.isCharging && distance > 100f) {
                        boss.isCharging = true
                        // Dynamic charge duration: exactly enough time to reach target, capped at 3 seconds
                        val requiredTime = distance / EXECUTIONER_LEAP_SPEED
                        boss.chargeTimer = requiredTime.coerceAtMost(3f) 
                        boss.chargeTargetX = playerTransform.x
                        boss.chargeTargetY = playerTransform.y
                    } else if (!boss.isCharging) {
                         moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
            }
        } else {
            // During cooldown, slowly chase player
            moveTowards(transform, velocity, stats, playerTransform, distance)
        }
    }

    private fun fireThrowingAxe(
        owner: Entity,
        ownerTransform: TransformComponent,
        targetTransform: TransformComponent,
        stats: StatsComponent
    ) {
        val spawn = onSpawnEntity ?: return
        val dx = targetTransform.x - ownerTransform.x
        val dy = targetTransform.y - ownerTransform.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance <= 0f) return

        val dirX = dx / distance
        val dirY = dy / distance

        val projectile = Entity().apply {
            addComponent(TransformComponent(
                x = ownerTransform.x + dirX * 30f,
                y = ownerTransform.y + dirY * 30f,
                scale = 2f
            ))
            addComponent(VelocityComponent(
                vx = dirX * EXECUTIONER_AXE_SPEED,
                vy = dirY * EXECUTIONER_AXE_SPEED
            ))
            addComponent(ColliderComponent(
                collider = Collider.Circle(14f),
                layer = CollisionLayer.ENEMY,
                isTrigger = true
            ))
            addComponent(ProjectileComponent(
                damage = stats.baseDamage * 0.8f, // Ranged is slightly weaker than melee sweep
                ownerId = owner.id,
                maxLifetime = EXECUTIONER_AXE_LIFETIME,
                penetrating = true // Axe chops through!
            ))
            addComponent(SpriteComponent(
                textureKey = "projectile_axe",
                width = 24f,
                height = 24f
            ))
            // Keep a tiny blood trail
            addComponent(ParticleComponent(
                color = Color(0xFFAA2222),   
                size = 8f,
                lifeTime = EXECUTIONER_AXE_LIFETIME,
                fadeOut = true,
                width = 12f,
                height = 12f
            ))
        }
        spawn(projectile)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // WIDOWMAKER
    // ═══════════════════════════════════════════════════════════════════════

    private fun updateWidowmaker(
        entity: Entity,
        boss: BossComponent,
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        health: HealthComponent,
        playerTransform: TransformComponent,
        player: Entity,
        deltaTime: Float
    ) {
        val dx = playerTransform.x - transform.x
        val dy = playerTransform.y - transform.y
        val distance = sqrt(dx * dx + dy * dy)

        // Tick cooldowns
        if (boss.minionSpawnCooldown > 0f) boss.minionSpawnCooldown -= deltaTime
        if (boss.acidSprayCooldown > 0f) boss.acidSprayCooldown -= deltaTime
        if (boss.burrowCooldown > 0f) boss.burrowCooldown -= deltaTime

        // Handle ceiling drop state (repurposing burrow variables)
        if (boss.isBurrowed) {
            velocity.vx = 0f
            velocity.vy = 0f
            boss.burrowTimer -= deltaTime

            // Make sprite invisible while on ceiling
            entity.getComponent(SpriteComponent::class)?.alpha = 0f

            if (boss.burrowTimer <= 0f) {
                boss.isBurrowed = false
                boss.burrowCooldown = WIDOWMAKER_DROP_COOLDOWN
                boss.phaseCooldown = 1.5f

                // Drop exactly on top of player's current position (with slight variance)
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val DropVariance = Random.nextFloat() * 30f
                transform.x = playerTransform.x + cos(angle) * DropVariance
                transform.y = playerTransform.y + sin(angle) * DropVariance

                // Restore visibility
                entity.getComponent(SpriteComponent::class)?.alpha = 1f

                // Emerge AOE damage (heavy slam)
                applyAoeDamage(transform, stats.baseDamage * 0.8f, WIDOWMAKER_DROP_AOE_RADIUS, player)
                // Use a dark purple/black impact for the spider drop
                spawnGroundSlamEffect(transform.x, transform.y, WIDOWMAKER_DROP_AOE_RADIUS, Color(0xFF662288))
            }
            return
        }

        // Handle web spray
        if (boss.isSprayingAcid) {
            velocity.vx = 0f
            velocity.vy = 0f
            boss.acidSprayTimer -= deltaTime
            if (boss.acidSprayTimer <= 0f) {
                boss.isSprayingAcid = false
                boss.phaseCooldown = 1f
            }
            return
        }

        // Retreat trigger: prefer staying at web throw range
        if (!boss.isRetreating && boss.retreatCooldown <= 0f &&
            distance < boss.bossType.preferredRange * RETREAT_TRIGGER_FRACTION) {
            boss.isRetreating = true
            boss.retreatTimer = RETREAT_DURATION
            return
        }

        // Opportunistic web throw: can fire in ANY phase if cooldown is ready and in range
        if (boss.acidSprayCooldown <= 0f && distance < WIDOWMAKER_WEB_RANGE && distance > 80f) {
            fireWebs(entity, transform, playerTransform, stats)
            boss.acidSprayCooldown = WIDOWMAKER_WEB_COOLDOWN
            boss.isSprayingAcid = true
            boss.acidSprayTimer = 0.5f
        }

        // Phase cycling
        if (boss.phaseCooldown <= 0f) {
            boss.phaseTimer += deltaTime
            if (boss.phaseTimer >= WIDOWMAKER_PHASE_DURATION) {
                boss.phaseTimer = 0f
                boss.currentPhase = (boss.currentPhase + 1) % 3
            }

            when (boss.currentPhase) {
                0 -> { // Spawn Spiderlings
                    if (boss.minionSpawnCooldown <= 0f) {
                        spawnSpiderlings(transform, WIDOWMAKER_MINION_COUNT)
                        boss.minionSpawnCooldown = WIDOWMAKER_MINION_COOLDOWN
                        boss.phaseCooldown = 2f
                    } else {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
                1 -> { // Web Throw — retreat to range, then fire
                    if (distance < 150f) {
                        // Too close — back away to throw range
                        retreatFromPlayer(transform, velocity, stats, playerTransform)
                    } else {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
                2 -> { // Ceiling Drop — disappear and drop on player
                    if (boss.burrowCooldown <= 0f) {
                        boss.isBurrowed = true
                        boss.burrowTimer = WIDOWMAKER_DROP_DURATION
                        velocity.vx = 0f
                        velocity.vy = 0f
                    } else {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
            }
        } else {
            moveTowards(transform, velocity, stats, playerTransform, distance)
        }
    }

    private fun spawnSpiderlings(bossTransform: TransformComponent, count: Int) {
        val spawn = onSpawnEntity ?: return
        for (i in 0 until count) {
            val angle = (i.toFloat() / count) * 2f * Math.PI.toFloat()
            val spawnDist = 40f
            val x = bossTransform.x + cos(angle) * spawnDist
            val y = bossTransform.y + sin(angle) * spawnDist

            // Spawn fast Crawlers as spiderlings
            val spiderling = ZombieEntity(x = x, y = y, type = ZombieType.CRAWLER)
            spawn(spiderling)
        }

        // Spawn effect: purple venom splash
        spawnGroundSlamEffect(bossTransform.x, bossTransform.y, 40f, Color(0xFF8844FF))
    }

    private fun fireWebs(
        owner: Entity,
        ownerTransform: TransformComponent,
        targetTransform: TransformComponent,
        stats: StatsComponent
    ) {
        val spawn = onSpawnEntity ?: return
        val dx = targetTransform.x - ownerTransform.x
        val dy = targetTransform.y - ownerTransform.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance <= 0f) return

        val baseAngle = kotlin.math.atan2(dy, dx)
        val spreadRad = Math.toRadians(WIDOWMAKER_WEB_SPREAD.toDouble()).toFloat()
        val step = spreadRad / (WIDOWMAKER_WEB_PROJECTILES - 1).coerceAtLeast(1)
        val startAngle = baseAngle - spreadRad / 2f

        for (i in 0 until WIDOWMAKER_WEB_PROJECTILES) {
            val angle = startAngle + step * i
            val dirX = cos(angle)
            val dirY = sin(angle)

            val projectile = Entity().apply {
                addComponent(TransformComponent(
                    x = ownerTransform.x + dirX * 30f,
                    y = ownerTransform.y + dirY * 30f
                ))
                addComponent(VelocityComponent(
                    vx = dirX * WIDOWMAKER_WEB_SPEED,
                    vy = dirY * WIDOWMAKER_WEB_SPEED
                ))
                addComponent(ColliderComponent(
                    collider = Collider.Circle(10f),
                    layer = CollisionLayer.ENEMY,
                    isTrigger = true
                ))
                addComponent(ProjectileComponent(
                    damage = stats.baseDamage * 0.4f,
                    ownerId = owner.id,
                    maxLifetime = WIDOWMAKER_WEB_LIFETIME,
                    // Web slows the player severely (not implemented perfectly here, but simulated via projectile hit effects elsewhere)
                ))
                addComponent(SpriteComponent(
                    textureKey = "projectile_web",
                    width = 24f,
                    height = 24f
                ))
                addComponent(ParticleComponent(
                    color = Color(0xFFEEEEEE),   // White/gray web ball
                    size = 5f,
                    lifeTime = WIDOWMAKER_WEB_LIFETIME,
                    fadeOut = true,
                    width = 8f,
                    height = 8f
                ))
            }
            spawn(projectile)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // THE BEHEMOTH (Replaces Flesh Amalgam)
    // ═══════════════════════════════════════════════════════════════════════

    private fun updateAmalgam(
        entity: Entity,
        boss: BossComponent,
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        health: HealthComponent,
        playerTransform: TransformComponent,
        player: Entity,
        deltaTime: Float
    ) {
        val dx = playerTransform.x - transform.x
        val dy = playerTransform.y - transform.y
        val distance = sqrt(dx * dx + dy * dy)

        // Tick cooldowns
        if (boss.swipeCooldown > 0f) boss.swipeCooldown -= deltaTime
        if (boss.groundSpikeCooldown > 0f) boss.groundSpikeCooldown -= deltaTime
        boss.regenTimer += deltaTime

        // --- Passive: Regeneration ---
        if (boss.regenTimer >= AMALGAM_REGEN_INTERVAL && health.isAlive) {
            boss.regenTimer = 0f
            val regenAmount = if (boss.isEnraged) AMALGAM_REGEN_AMOUNT / 2 else AMALGAM_REGEN_AMOUNT
            health.heal(regenAmount)
        }

        // --- Mutate/Enrage check (below 30% HP) ---
        // The Behemoth enrages when near death!
        if (!boss.isEnraged && health.healthPercent <= AMALGAM_ENRAGE_THRESHOLD) {
            boss.isEnraged = true
            stats.moveSpeed *= AMALGAM_ENRAGE_SPEED_MULT
            stats.damageMultiplier *= AMALGAM_ENRAGE_DAMAGE_MULT
            // Visual feedback: tint deeper angry green/red
            entity.getComponent(SpriteComponent::class)?.tint = Color(0xFF88CC22)
            // Burst effect to signal mutate
            spawnGroundSlamEffect(transform.x, transform.y, 100f, Color(0xFF44AA33))
        }

        // Handle active Ground Slam Series
        if (boss.isSwinging) {
            velocity.vx = 0f
            velocity.vy = 0f
            boss.swipeTimer -= deltaTime
            if (boss.swipeTimer <= 0f) {
                boss.isSwinging = false
                boss.phaseCooldown = 1f
            }
            return
        }

        // Retreat trigger: back off to create distance for rubble throwing
        if (!boss.isRetreating && boss.retreatCooldown <= 0f &&
            distance < boss.bossType.preferredRange * RETREAT_TRIGGER_FRACTION) {
            boss.isRetreating = true
            boss.retreatTimer = RETREAT_DURATION
            return
        }

        // Opportunistic ground rubble: fire at range if cooldown ready (ranged attack)
        if (boss.groundSpikeCooldown <= 0f && distance < AMALGAM_SPIKE_RANGE && distance > AMALGAM_SWIPE_RADIUS) {
            fireGroundRubble(entity, transform, playerTransform, stats)
            boss.groundSpikeCooldown = if (boss.isEnraged) AMALGAM_SPIKE_COOLDOWN * 0.6f else AMALGAM_SPIKE_COOLDOWN
        }

        // Phase cycling
        if (boss.phaseCooldown <= 0f) {
            boss.phaseTimer += deltaTime
            if (boss.phaseTimer >= AMALGAM_PHASE_DURATION) {
                boss.phaseTimer = 0f
                boss.currentPhase = (boss.currentPhase + 1) % 3
            }

            when (boss.currentPhase) {
                0 -> { // Massive Ground Slam Series
                    if (distance < AMALGAM_SWIPE_RADIUS * 1.5f && boss.swipeCooldown <= 0f) {
                        performMassiveSlam(entity, transform, playerTransform, stats)
                        boss.swipeCooldown = if (boss.isEnraged) AMALGAM_SWIPE_COOLDOWN * 0.6f else AMALGAM_SWIPE_COOLDOWN
                        boss.isSwinging = true
                        boss.swipeTimer = AMALGAM_SWIPE_DURATION
                    } else {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
                1 -> { // Throw Rubble + Regen phase
                    if (distance < boss.bossType.preferredRange * 0.7f) {
                        // Too close — back away to throwing range
                        retreatFromPlayer(transform, velocity, stats, playerTransform)
                    } else if (distance > 0.01f) {
                        val dirX = dx / distance
                        val dirY = dy / distance
                        val speed = stats.moveSpeed * 0.5f // Moves slower while shooting/regenerating
                        velocity.vx = dirX * speed
                        velocity.vy = dirY * speed
                    }
                }
                2 -> { // Aggressive chase + slam
                    val chaseSpeed = if (boss.isEnraged) stats.moveSpeed * 1.5f else stats.moveSpeed * 1.2f
                    if (distance > AMALGAM_SWIPE_RADIUS * 0.8f) {
                        if (distance > 0.01f) {
                            val dirX = dx / distance
                            val dirY = dy / distance
                            velocity.vx = dirX * chaseSpeed
                            velocity.vy = dirY * chaseSpeed
                        }
                    } else if (boss.swipeCooldown <= 0f) {
                        performMassiveSlam(entity, transform, playerTransform, stats)
                        boss.swipeCooldown = if (boss.isEnraged) AMALGAM_SWIPE_COOLDOWN * 0.6f else AMALGAM_SWIPE_COOLDOWN
                        boss.isSwinging = true
                        boss.swipeTimer = AMALGAM_SWIPE_DURATION
                    }
                }
            }
        } else {
            moveTowards(transform, velocity, stats, playerTransform, distance)
        }
    }

    private fun performMassiveSlam(
        owner: Entity,
        ownerTransform: TransformComponent,
        targetTransform: TransformComponent,
        stats: StatsComponent
    ) {
        val spawn = onSpawnEntity ?: return
        val dx = targetTransform.x - ownerTransform.x
        val dy = targetTransform.y - ownerTransform.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance <= 0f) return

        val baseAngle = kotlin.math.atan2(dy, dx)

        // Spawn a fan of short-lived melee projectile segments mapped across 140 degree arc (massive arm sweep)
        val arcSpread = Math.toRadians(140.0).toFloat()
        val step = arcSpread / (AMALGAM_SWIPE_ARC - 1).coerceAtLeast(1)
        val startAngle = baseAngle - arcSpread / 2f

        for (i in 0 until AMALGAM_SWIPE_ARC) {
            val angle = startAngle + step * i
            val dirX = cos(angle)
            val dirY = sin(angle)
            val dist = AMALGAM_SWIPE_RADIUS * 0.7f

            val segment = Entity().apply {
                addComponent(TransformComponent(
                    x = ownerTransform.x + dirX * dist,
                    y = ownerTransform.y + dirY * dist
                ))
                addComponent(VelocityComponent(
                    vx = dirX * 70f,
                    vy = dirY * 70f
                ))
                addComponent(ColliderComponent(
                    collider = Collider.Circle(18f),
                    layer = CollisionLayer.ENEMY,
                    isTrigger = true
                ))
                addComponent(ProjectileComponent(
                    damage = stats.baseDamage * stats.damageMultiplier,
                    ownerId = owner.id,
                    maxLifetime = 0.35f,
                    penetrating = true
                ))
                addComponent(ParticleComponent(
                    color = if (stats.damageMultiplier > 1f) Color(0xFFCC4422) else Color(0xFF44AA33),
                    size = 14f,
                    lifeTime = 0.35f,
                    width = 35f,
                    height = 10f
                ))
            }
            spawn(segment)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // THE BEHEMOTH — GROUND RUBBLE (RANGED ATTACK)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Behemoth's ranged attack: Rips up concrete/ground and throws it in a spread.
     */
    private fun fireGroundRubble(
        owner: Entity,
        ownerTransform: TransformComponent,
        targetTransform: TransformComponent,
        stats: StatsComponent
    ) {
        val spawn = onSpawnEntity ?: return
        val dx = targetTransform.x - ownerTransform.x
        val dy = targetTransform.y - ownerTransform.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance <= 0f) return

        val baseAngle = kotlin.math.atan2(dy, dx)
        val spreadRad = Math.toRadians(AMALGAM_SPIKE_SPREAD.toDouble()).toFloat()
        val step = if (AMALGAM_SPIKE_COUNT > 1) spreadRad / (AMALGAM_SPIKE_COUNT - 1) else 0f
        val startAngle = baseAngle - spreadRad / 2f

        val rubbleColor = Color(0xFFAAAAAA) // Grey concrete color

        for (i in 0 until AMALGAM_SPIKE_COUNT) {
            val angle = startAngle + step * i
            val dirX = cos(angle)
            val dirY = sin(angle)

            // Extremely fast, heavy spread
            val spawnOffset = 35f + i * 5f

            val spike = Entity().apply {
                addComponent(TransformComponent(
                    x = ownerTransform.x + dirX * spawnOffset,
                    y = ownerTransform.y + dirY * spawnOffset,
                    scale = 1.0f
                ))
                addComponent(VelocityComponent(
                    vx = dirX * AMALGAM_SPIKE_SPEED,
                    vy = dirY * AMALGAM_SPIKE_SPEED
                ))
                addComponent(ColliderComponent(
                    collider = Collider.Circle(12f), // slightly larger than splinters
                    layer = CollisionLayer.ENEMY,
                    isTrigger = true
                ))
                addComponent(ProjectileComponent(
                    damage = stats.baseDamage * 0.6f * stats.damageMultiplier,
                    ownerId = owner.id,
                    maxLifetime = AMALGAM_SPIKE_LIFETIME
                ))
                addComponent(SpriteComponent(
                    // "projectile_bone" works fine as a grey chunk for rubble, since it's already generated and small, 
                    // but we can tint it dynamically grey
                    textureKey = "projectile_bone",
                    width = 24f,
                    height = 24f
                ))
                // Heavy concrete dust trail
                addComponent(ParticleComponent(
                    color = Color(0xFF666666),
                    size = 5f,
                    lifeTime = AMALGAM_SPIKE_LIFETIME,
                    fadeOut = true,
                    width = 10f,
                    height = 10f
                ))
            }
            spawn(spike)
        }
        // Visual burst at spawn point
        spawnGroundSlamEffect(ownerTransform.x, ownerTransform.y, 50f, rubbleColor)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // RETREAT & OPPORTUNISTIC RANGED ATTACKS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Move away from the player at [RETREAT_SPEED_MULT] of base speed.
     * Used when the player gets too close and the boss wants ranged distance.
     */
    private fun retreatFromPlayer(
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        playerTransform: TransformComponent
    ) {
        val dx = transform.x - playerTransform.x  // Away from player
        val dy = transform.y - playerTransform.y
        val distance = sqrt(dx * dx + dy * dy)
        if (distance > 0.01f) {
            val dirX = dx / distance
            val dirY = dy / distance
            val speed = stats.moveSpeed * RETREAT_SPEED_MULT
            velocity.vx = dirX * speed
            velocity.vy = dirY * speed
        }
    }

    /**
     * While retreating, bosses can still fire ranged attacks if their cooldowns are ready.
     * This makes retreat feel threatening rather than passive.
     */
    private fun fireOpportunisticRanged(
        entity: Entity,
        boss: BossComponent,
        transform: TransformComponent,
        stats: StatsComponent,
        playerTransform: TransformComponent
    ) {
        val dx = playerTransform.x - transform.x
        val dy = playerTransform.y - transform.y
        val distance = sqrt(dx * dx + dy * dy)

        when (boss.bossType) {
            BossType.EXECUTIONER -> {
                if (boss.rockThrowCooldown <= 0f && distance < EXECUTIONER_AXE_RANGE) {
                    fireThrowingAxe(entity, transform, playerTransform, stats)
                    boss.rockThrowCooldown = EXECUTIONER_AXE_COOLDOWN
                }
            }
            BossType.WIDOWMAKER -> {
                if (boss.acidSprayCooldown <= 0f && distance < WIDOWMAKER_WEB_RANGE) {
                    fireWebs(entity, transform, playerTransform, stats)
                    boss.acidSprayCooldown = WIDOWMAKER_WEB_COOLDOWN
                }
            }
            BossType.FLESH_AMALGAM -> {
                if (boss.groundSpikeCooldown <= 0f && distance < AMALGAM_SPIKE_RANGE) {
                    fireGroundRubble(entity, transform, playerTransform, stats)
                    boss.groundSpikeCooldown = if (boss.isEnraged) AMALGAM_SPIKE_COOLDOWN * 0.6f else AMALGAM_SPIKE_COOLDOWN
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MELEE RETALIATION
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Called by CombatSystem when a melee projectile (whip blade) hits a boss.
     * 40% chance to spawn a fast radial shockwave ring that damages and pushes the player back.
     * Cooldown prevents multiple triggers from a single whip sweep (12 segments in 0.25s).
     */
    fun handleMeleeRetaliation(bossEntity: Entity) {
        val spawn = onSpawnEntity ?: return
        if (!bossEntity.isActive) return

        // Check cooldown — skip if this boss retaliated recently
        val cooldown = retaliationCooldowns[bossEntity.id]
        if (cooldown != null && cooldown > 0f) return

        // 40% chance to retaliate
        if (Random.nextFloat() > RETALIATION_CHANCE) return

        // Set cooldown to prevent re-trigger from remaining whip segments
        retaliationCooldowns[bossEntity.id] = RETALIATION_COOLDOWN

        val transform = bossEntity.getComponent(TransformComponent::class) ?: return
        val stats = bossEntity.getComponent(StatsComponent::class) ?: return
        val bossComp = bossEntity.getComponent(BossComponent::class) ?: return

        val accentColor = Color(bossComp.bossType.accentColor)
        val damage = stats.baseDamage * RETALIATION_DAMAGE_MULT

        // Spawn radial shockwave ring — 8 projectiles at 45° intervals
        val angleStep = (2f * Math.PI / RETALIATION_PROJECTILE_COUNT).toFloat()
        for (i in 0 until RETALIATION_PROJECTILE_COUNT) {
            val angle = angleStep * i
            val dirX = cos(angle)
            val dirY = sin(angle)

            val shockwave = Entity().apply {
                addComponent(TransformComponent(
                    x = transform.x + dirX * 20f,
                    y = transform.y + dirY * 20f
                ))
                addComponent(VelocityComponent(
                    vx = dirX * RETALIATION_SPEED,
                    vy = dirY * RETALIATION_SPEED
                ))
                addComponent(ColliderComponent(
                    collider = Collider.Circle(10f),
                    layer = CollisionLayer.ENEMY,
                    isTrigger = true
                ))
                addComponent(ProjectileComponent(
                    damage = damage,
                    ownerId = bossEntity.id,
                    maxLifetime = RETALIATION_LIFETIME,
                    penetrating = true
                ))
                addComponent(ParticleComponent(
                    color = accentColor,
                    size = 14f,
                    lifeTime = RETALIATION_LIFETIME,
                    fadeOut = true,
                    width = 18f,
                    height = 18f
                ))
            }
            spawn(shockwave)
        }

        // Visual burst at boss position to signal the retaliation
        spawnGroundSlamEffect(transform.x, transform.y, 60f, accentColor)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SHARED UTILITIES
    // ═══════════════════════════════════════════════════════════════════════

    private fun moveTowards(
        transform: TransformComponent,
        velocity: VelocityComponent,
        stats: StatsComponent,
        targetTransform: TransformComponent,
        distance: Float
    ) {
        if (distance > 0.01f) {
            val dx = targetTransform.x - transform.x
            val dy = targetTransform.y - transform.y
            val dirX = dx / distance
            val dirY = dy / distance
            velocity.vx = dirX * stats.moveSpeed
            velocity.vy = dirY * stats.moveSpeed
        }
    }

    private fun applyAoeDamage(
        center: TransformComponent,
        damage: Float,
        radius: Float,
        player: Entity
    ) {
        val playerTransform = player.getComponent(TransformComponent::class) ?: return
        val playerHealth = player.getComponent(HealthComponent::class) ?: return
        val playerStats = player.getComponent(StatsComponent::class)

        val dx = playerTransform.x - center.x
        val dy = playerTransform.y - center.y
        val distSq = dx * dx + dy * dy

        if (distSq <= radius * radius) {
            val armor = playerStats?.armor ?: 0
            playerHealth.takeDamage(damage.toInt(), armor)
        }
    }

    private fun spawnGroundSlamEffect(x: Float, y: Float, radius: Float, color: Color) {
        val spawn = onSpawnEntity ?: return
        val particleCount = (radius / 8f).toInt().coerceIn(8, 20)
        HitEffectEntity.burst(
            x = x,
            y = y,
            count = particleCount,
            baseColor = color,
            speed = radius * 1.5f,
            lifeTime = 0.6f,
            size = 10f
        ).forEach { spawn(it) }
    }
}
