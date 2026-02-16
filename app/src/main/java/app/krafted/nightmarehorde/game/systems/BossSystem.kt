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
        // ─── Tank Constants ────────────────────────────────────────────
        const val TANK_SLAM_WINDUP = 0.8f       // seconds before slam lands
        const val TANK_SLAM_RADIUS = 120f       // AOE damage radius
        const val TANK_SLAM_COOLDOWN = 5f       // seconds between slams
        const val TANK_ROCK_SPEED = 300f        // projectile speed (was 250)
        const val TANK_ROCK_COOLDOWN = 2f       // seconds between rock throws (was 3)
        const val TANK_ROCK_LIFETIME = 5f       // projectile lifetime (was 4)
        const val TANK_ROCK_RANGE = 700f        // max range to fire rocks (was 400)
        const val TANK_CHARGE_SPEED = 200f      // charge movement speed
        const val TANK_CHARGE_DURATION = 0.8f   // charge duration
        const val TANK_CHARGE_COOLDOWN = 6f     // seconds between charges
        const val TANK_PHASE_DURATION = 2.5f    // seconds per attack phase

        // ─── Hive Queen Constants ──────────────────────────────────────
        const val QUEEN_MINION_COUNT = 4        // minions per spawn
        const val QUEEN_MINION_COOLDOWN = 8f    // seconds between minion waves
        const val QUEEN_ACID_PROJECTILES = 7    // projectiles per spray (was 5)
        const val QUEEN_ACID_SPREAD = 55f       // spray cone angle in degrees (was 45)
        const val QUEEN_ACID_SPEED = 240f       // acid projectile speed (was 200)
        const val QUEEN_ACID_COOLDOWN = 2.5f    // seconds between sprays (was 4)
        const val QUEEN_ACID_LIFETIME = 4f      // acid projectile lifetime (was 3)
        const val QUEEN_ACID_RANGE = 600f       // max range to fire acid (was 350)
        const val QUEEN_BURROW_DURATION = 1.5f  // seconds underground
        const val QUEEN_BURROW_COOLDOWN = 10f   // seconds between burrows
        const val QUEEN_BURROW_AOE_RADIUS = 100f
        const val QUEEN_PHASE_DURATION = 3f

        // ─── Abomination Constants ─────────────────────────────────────
        const val ABOM_SWIPE_ARC = 8           // number of projectile segments in arc
        const val ABOM_SWIPE_RADIUS = 100f     // melee arc reach
        const val ABOM_SWIPE_COOLDOWN = 3f     // seconds between swipes
        const val ABOM_SWIPE_DURATION = 0.4f   // swipe animation time
        const val ABOM_REGEN_INTERVAL = 2f     // seconds between regen ticks
        const val ABOM_REGEN_AMOUNT = 15       // HP per regen tick
        const val ABOM_ENRAGE_THRESHOLD = 0.3f // HP % to trigger enrage
        const val ABOM_ENRAGE_SPEED_MULT = 1.5f
        const val ABOM_ENRAGE_DAMAGE_MULT = 1.5f
        const val ABOM_PHASE_DURATION = 2f
        // Ground Spikes ranged attack
        const val ABOM_SPIKE_COUNT = 4         // number of spike projectiles
        const val ABOM_SPIKE_SPEED = 220f      // spike projectile speed
        const val ABOM_SPIKE_COOLDOWN = 3.5f   // seconds between spike attacks
        const val ABOM_SPIKE_LIFETIME = 3.5f   // spike projectile lifetime
        const val ABOM_SPIKE_RANGE = 500f      // max range to fire spikes
        const val ABOM_SPIKE_SPREAD = 20f      // spread angle in degrees

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
                BossType.TANK -> updateTank(entity, boss, transform, velocity, stats, health, playerTransform, player, deltaTime)
                BossType.HIVE_QUEEN -> updateHiveQueen(entity, boss, transform, velocity, stats, health, playerTransform, player, deltaTime)
                BossType.ABOMINATION -> updateAbomination(entity, boss, transform, velocity, stats, health, playerTransform, player, deltaTime)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // THE TANK
    // ═══════════════════════════════════════════════════════════════════════

    private fun updateTank(
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

        // Handle active charge
        if (boss.isCharging) {
            boss.chargeTimer -= deltaTime
            val cdx = boss.chargeTargetX - transform.x
            val cdy = boss.chargeTargetY - transform.y
            val cDist = sqrt(cdx * cdx + cdy * cdy)

            if (cDist > 10f && boss.chargeTimer > 0f) {
                val dirX = cdx / cDist
                val dirY = cdy / cDist
                velocity.vx = dirX * TANK_CHARGE_SPEED
                velocity.vy = dirY * TANK_CHARGE_SPEED
            } else {
                boss.isCharging = false
                boss.phaseCooldown = 1.5f
                velocity.vx = 0f
                velocity.vy = 0f
                // Spawn impact effect at current position
                spawnGroundSlamEffect(transform.x, transform.y, 60f, Color(0xFF886644))
            }
            return
        }

        // Handle active slam
        if (boss.isSlamming) {
            velocity.vx = 0f
            velocity.vy = 0f
            boss.slamWindUp -= deltaTime
            if (boss.slamWindUp <= 0f) {
                boss.isSlamming = false
                boss.phaseCooldown = 1.5f
                // Deal AOE damage at boss position
                applyAoeDamage(transform, stats.baseDamage, TANK_SLAM_RADIUS, player)
                spawnGroundSlamEffect(transform.x, transform.y, TANK_SLAM_RADIUS, Color(0xFF996633))
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

        // Opportunistic rock throw: can fire in ANY phase if cooldown is ready and in range
        if (boss.rockThrowCooldown <= 0f && distance < TANK_ROCK_RANGE && distance > TANK_SLAM_RADIUS) {
            fireRockProjectile(entity, transform, playerTransform, stats)
            boss.rockThrowCooldown = TANK_ROCK_COOLDOWN
        }

        // Phase cycling when cooldown is ready
        if (boss.phaseCooldown <= 0f) {
            boss.phaseTimer += deltaTime
            if (boss.phaseTimer >= TANK_PHASE_DURATION) {
                boss.phaseTimer = 0f
                boss.currentPhase = (boss.currentPhase + 1) % 3
            }

            when (boss.currentPhase) {
                0 -> { // Ground Slam — move close, then slam
                    if (distance > TANK_SLAM_RADIUS * 0.8f) {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    } else {
                        boss.isSlamming = true
                        boss.slamWindUp = TANK_SLAM_WINDUP
                        velocity.vx = 0f
                        velocity.vy = 0f
                    }
                }
                1 -> { // Rock Throw — fire from range, retreat if too close
                    if (distance < TANK_SLAM_RADIUS) {
                        // Too close for rock throw — back away
                        retreatFromPlayer(transform, velocity, stats, playerTransform)
                    } else {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
                2 -> { // Charge — dash towards player
                    if (!boss.isCharging && distance > 80f) {
                        boss.isCharging = true
                        boss.chargeTimer = TANK_CHARGE_DURATION
                        boss.chargeTargetX = playerTransform.x
                        boss.chargeTargetY = playerTransform.y
                    }
                }
            }
        } else {
            // During cooldown, slowly chase player
            moveTowards(transform, velocity, stats, playerTransform, distance)
        }
    }

    private fun fireRockProjectile(
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
                vx = dirX * TANK_ROCK_SPEED,
                vy = dirY * TANK_ROCK_SPEED
            ))
            addComponent(ColliderComponent(
                collider = Collider.Circle(12f),
                layer = CollisionLayer.ENEMY,
                isTrigger = true
            ))
            addComponent(ProjectileComponent(
                damage = stats.baseDamage,
                ownerId = owner.id,
                maxLifetime = TANK_ROCK_LIFETIME
            ))
            addComponent(ParticleComponent(
                color = Color(0xFF996633),   // Brown rock
                size = 18f,
                lifeTime = TANK_ROCK_LIFETIME,
                fadeOut = false,
                width = 22f,
                height = 18f
            ))
        }
        spawn(projectile)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HIVE QUEEN
    // ═══════════════════════════════════════════════════════════════════════

    private fun updateHiveQueen(
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

        // Handle burrowed state
        if (boss.isBurrowed) {
            velocity.vx = 0f
            velocity.vy = 0f
            boss.burrowTimer -= deltaTime

            // Make sprite invisible while burrowed
            entity.getComponent(SpriteComponent::class)?.alpha = 0.2f

            if (boss.burrowTimer <= 0f) {
                boss.isBurrowed = false
                boss.burrowCooldown = QUEEN_BURROW_COOLDOWN
                boss.phaseCooldown = 1.5f

                // Teleport near player
                val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                val teleportDist = 100f + Random.nextFloat() * 50f
                transform.x = playerTransform.x + cos(angle) * teleportDist
                transform.y = playerTransform.y + sin(angle) * teleportDist

                // Restore visibility
                entity.getComponent(SpriteComponent::class)?.alpha = 1f

                // Emerge AOE damage
                applyAoeDamage(transform, stats.baseDamage * 0.6f, QUEEN_BURROW_AOE_RADIUS, player)
                spawnGroundSlamEffect(transform.x, transform.y, QUEEN_BURROW_AOE_RADIUS, Color(0xFF44AA44))
            }
            return
        }

        // Handle acid spray
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

        // Retreat trigger: prefer staying at acid spray range
        if (!boss.isRetreating && boss.retreatCooldown <= 0f &&
            distance < boss.bossType.preferredRange * RETREAT_TRIGGER_FRACTION) {
            boss.isRetreating = true
            boss.retreatTimer = RETREAT_DURATION
            return
        }

        // Opportunistic acid spray: can fire in ANY phase if cooldown is ready and in range
        if (boss.acidSprayCooldown <= 0f && distance < QUEEN_ACID_RANGE && distance > 80f) {
            fireAcidSpray(entity, transform, playerTransform, stats)
            boss.acidSprayCooldown = QUEEN_ACID_COOLDOWN
            boss.isSprayingAcid = true
            boss.acidSprayTimer = 0.5f
        }

        // Phase cycling
        if (boss.phaseCooldown <= 0f) {
            boss.phaseTimer += deltaTime
            if (boss.phaseTimer >= QUEEN_PHASE_DURATION) {
                boss.phaseTimer = 0f
                boss.currentPhase = (boss.currentPhase + 1) % 3
            }

            when (boss.currentPhase) {
                0 -> { // Spawn Minions
                    if (boss.minionSpawnCooldown <= 0f) {
                        spawnMinions(transform, QUEEN_MINION_COUNT)
                        boss.minionSpawnCooldown = QUEEN_MINION_COOLDOWN
                        boss.phaseCooldown = 2f
                    } else {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
                1 -> { // Acid Spray — retreat to range, then fire
                    if (distance < 120f) {
                        // Too close — back away to spray range
                        retreatFromPlayer(transform, velocity, stats, playerTransform)
                    } else {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
                2 -> { // Burrow — dig underground and teleport
                    if (boss.burrowCooldown <= 0f) {
                        boss.isBurrowed = true
                        boss.burrowTimer = QUEEN_BURROW_DURATION
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

    private fun spawnMinions(bossTransform: TransformComponent, count: Int) {
        val spawn = onSpawnEntity ?: return
        for (i in 0 until count) {
            val angle = (i.toFloat() / count) * 2f * Math.PI.toFloat()
            val spawnDist = 60f
            val x = bossTransform.x + cos(angle) * spawnDist
            val y = bossTransform.y + sin(angle) * spawnDist

            // Spawn Runner-type zombies as minions
            val minion = ZombieEntity(x = x, y = y, type = ZombieType.RUNNER)
            spawn(minion)
        }

        // Spawn effect
        spawnGroundSlamEffect(bossTransform.x, bossTransform.y, 50f, Color(0xFFAAAA44))
    }

    private fun fireAcidSpray(
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
        val spreadRad = Math.toRadians(QUEEN_ACID_SPREAD.toDouble()).toFloat()
        val step = spreadRad / (QUEEN_ACID_PROJECTILES - 1).coerceAtLeast(1)
        val startAngle = baseAngle - spreadRad / 2f

        for (i in 0 until QUEEN_ACID_PROJECTILES) {
            val angle = startAngle + step * i
            val dirX = cos(angle)
            val dirY = sin(angle)

            val projectile = Entity().apply {
                addComponent(TransformComponent(
                    x = ownerTransform.x + dirX * 25f,
                    y = ownerTransform.y + dirY * 25f
                ))
                addComponent(VelocityComponent(
                    vx = dirX * QUEEN_ACID_SPEED,
                    vy = dirY * QUEEN_ACID_SPEED
                ))
                addComponent(ColliderComponent(
                    collider = Collider.Circle(8f),
                    layer = CollisionLayer.ENEMY,
                    isTrigger = true
                ))
                addComponent(ProjectileComponent(
                    damage = stats.baseDamage * 0.5f,
                    ownerId = owner.id,
                    maxLifetime = QUEEN_ACID_LIFETIME
                ))
                addComponent(ParticleComponent(
                    color = Color(0xFF44DD44),   // Toxic green acid glob
                    size = 12f,
                    lifeTime = QUEEN_ACID_LIFETIME,
                    fadeOut = true,
                    width = 14f,
                    height = 10f
                ))
            }
            spawn(projectile)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ABOMINATION
    // ═══════════════════════════════════════════════════════════════════════

    private fun updateAbomination(
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
        if (boss.regenTimer >= ABOM_REGEN_INTERVAL && health.isAlive) {
            boss.regenTimer = 0f
            val regenAmount = if (boss.isEnraged) ABOM_REGEN_AMOUNT / 2 else ABOM_REGEN_AMOUNT
            health.heal(regenAmount)
        }

        // --- Enrage check (below 30% HP) ---
        // Intentionally permanent — stats never revert even if HP regens above 30%.
        // The one-shot guard (!isEnraged) prevents re-application.
        if (!boss.isEnraged && health.healthPercent <= ABOM_ENRAGE_THRESHOLD) {
            boss.isEnraged = true
            stats.moveSpeed *= ABOM_ENRAGE_SPEED_MULT
            stats.damageMultiplier *= ABOM_ENRAGE_DAMAGE_MULT
            // Visual feedback: tint red
            entity.getComponent(SpriteComponent::class)?.tint = Color(0xFFFF4444)
            // Burst effect to signal enrage
            spawnGroundSlamEffect(transform.x, transform.y, 80f, Color(0xFFFF2222))
        }

        // Handle active swipe
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

        // Retreat trigger: back off to create distance for ground spikes
        if (!boss.isRetreating && boss.retreatCooldown <= 0f &&
            distance < boss.bossType.preferredRange * RETREAT_TRIGGER_FRACTION) {
            boss.isRetreating = true
            boss.retreatTimer = RETREAT_DURATION
            return
        }

        // Opportunistic ground spikes: fire at range if cooldown ready (new ranged attack)
        if (boss.groundSpikeCooldown <= 0f && distance < ABOM_SPIKE_RANGE && distance > ABOM_SWIPE_RADIUS) {
            fireGroundSpikes(entity, transform, playerTransform, stats)
            boss.groundSpikeCooldown = if (boss.isEnraged) ABOM_SPIKE_COOLDOWN * 0.6f else ABOM_SPIKE_COOLDOWN
        }

        // Phase cycling
        if (boss.phaseCooldown <= 0f) {
            boss.phaseTimer += deltaTime
            if (boss.phaseTimer >= ABOM_PHASE_DURATION) {
                boss.phaseTimer = 0f
                boss.currentPhase = (boss.currentPhase + 1) % 3
            }

            when (boss.currentPhase) {
                0 -> { // Multi-Arm Swipe — wide melee arc
                    if (distance < ABOM_SWIPE_RADIUS * 1.5f && boss.swipeCooldown <= 0f) {
                        performSwipe(entity, transform, playerTransform, stats)
                        boss.swipeCooldown = if (boss.isEnraged) ABOM_SWIPE_COOLDOWN * 0.6f else ABOM_SWIPE_COOLDOWN
                        boss.isSwinging = true
                        boss.swipeTimer = ABOM_SWIPE_DURATION
                    } else {
                        moveTowards(transform, velocity, stats, playerTransform, distance)
                    }
                }
                1 -> { // Ground Spikes + Regen phase — ranged attack while regenerating
                    // During regen phase, keep distance and launch ground spikes
                    if (distance < boss.bossType.preferredRange * 0.7f) {
                        // Too close — back away to spike range
                        retreatFromPlayer(transform, velocity, stats, playerTransform)
                    } else if (distance > 0.01f) {
                        val dirX = dx / distance
                        val dirY = dy / distance
                        val speed = stats.moveSpeed * 0.5f
                        velocity.vx = dirX * speed
                        velocity.vy = dirY * speed
                    }
                }
                2 -> { // Aggressive chase + swipe
                    val chaseSpeed = if (boss.isEnraged) stats.moveSpeed * 1.2f else stats.moveSpeed
                    if (distance > ABOM_SWIPE_RADIUS * 0.8f) {
                        if (distance > 0.01f) {
                            val dirX = dx / distance
                            val dirY = dy / distance
                            velocity.vx = dirX * chaseSpeed
                            velocity.vy = dirY * chaseSpeed
                        }
                    } else if (boss.swipeCooldown <= 0f) {
                        performSwipe(entity, transform, playerTransform, stats)
                        boss.swipeCooldown = if (boss.isEnraged) ABOM_SWIPE_COOLDOWN * 0.6f else ABOM_SWIPE_COOLDOWN
                        boss.isSwinging = true
                        boss.swipeTimer = ABOM_SWIPE_DURATION
                    }
                }
            }
        } else {
            moveTowards(transform, velocity, stats, playerTransform, distance)
        }
    }

    private fun performSwipe(
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

        // Spawn a fan of short-lived melee projectile segments
        val arcSpread = Math.toRadians(120.0).toFloat() // 120 degree arc
        val step = arcSpread / (ABOM_SWIPE_ARC - 1).coerceAtLeast(1)
        val startAngle = baseAngle - arcSpread / 2f

        for (i in 0 until ABOM_SWIPE_ARC) {
            val angle = startAngle + step * i
            val dirX = cos(angle)
            val dirY = sin(angle)
            val dist = ABOM_SWIPE_RADIUS * 0.7f

            val segment = Entity().apply {
                addComponent(TransformComponent(
                    x = ownerTransform.x + dirX * dist,
                    y = ownerTransform.y + dirY * dist
                ))
                addComponent(VelocityComponent(
                    vx = dirX * 60f,
                    vy = dirY * 60f
                ))
                addComponent(ColliderComponent(
                    collider = Collider.Circle(15f),
                    layer = CollisionLayer.ENEMY,
                    isTrigger = true
                ))
                addComponent(ProjectileComponent(
                    damage = stats.baseDamage * stats.damageMultiplier,
                    ownerId = owner.id,
                    maxLifetime = 0.3f,
                    penetrating = true
                ))
                addComponent(ParticleComponent(
                    color = if (stats.damageMultiplier > 1f) Color(0xFFFF4444) else Color(0xFFCC66FF),
                    size = 12f,
                    lifeTime = 0.3f,
                    width = 30f,
                    height = 8f
                ))
            }
            spawn(segment)
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ABOMINATION — GROUND SPIKES (RANGED ATTACK)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Abomination's ranged attack: erupts a line of ground spikes towards the player.
     * Spawns [ABOM_SPIKE_COUNT] projectiles in a narrow spread aimed at the player.
     * Spikes travel along the ground — themed as necrotic tentacles bursting from below.
     */
    private fun fireGroundSpikes(
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
        val spreadRad = Math.toRadians(ABOM_SPIKE_SPREAD.toDouble()).toFloat()
        val step = if (ABOM_SPIKE_COUNT > 1) spreadRad / (ABOM_SPIKE_COUNT - 1) else 0f
        val startAngle = baseAngle - spreadRad / 2f

        val spikeColor = if (stats.damageMultiplier > 1f) Color(0xFFFF4466) else Color(0xFFBB44FF)

        for (i in 0 until ABOM_SPIKE_COUNT) {
            val angle = startAngle + step * i
            val dirX = cos(angle)
            val dirY = sin(angle)

            // Stagger spawn positions slightly so spikes erupt in sequence
            val spawnOffset = 30f + i * 8f

            val spike = Entity().apply {
                addComponent(TransformComponent(
                    x = ownerTransform.x + dirX * spawnOffset,
                    y = ownerTransform.y + dirY * spawnOffset,
                    scale = 1.5f
                ))
                addComponent(VelocityComponent(
                    vx = dirX * ABOM_SPIKE_SPEED,
                    vy = dirY * ABOM_SPIKE_SPEED
                ))
                addComponent(ColliderComponent(
                    collider = Collider.Circle(10f),
                    layer = CollisionLayer.ENEMY,
                    isTrigger = true
                ))
                addComponent(ProjectileComponent(
                    damage = stats.baseDamage * 0.7f * stats.damageMultiplier,
                    ownerId = owner.id,
                    maxLifetime = ABOM_SPIKE_LIFETIME
                ))
                addComponent(ParticleComponent(
                    color = spikeColor,
                    size = 14f,
                    lifeTime = ABOM_SPIKE_LIFETIME,
                    fadeOut = true,
                    width = 20f,
                    height = 10f
                ))
            }
            spawn(spike)
        }

        // Visual burst at spawn point
        spawnGroundSlamEffect(ownerTransform.x, ownerTransform.y, 50f, spikeColor)
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
            BossType.TANK -> {
                if (boss.rockThrowCooldown <= 0f && distance < TANK_ROCK_RANGE) {
                    fireRockProjectile(entity, transform, playerTransform, stats)
                    boss.rockThrowCooldown = TANK_ROCK_COOLDOWN
                }
            }
            BossType.HIVE_QUEEN -> {
                if (boss.acidSprayCooldown <= 0f && distance < QUEEN_ACID_RANGE) {
                    fireAcidSpray(entity, transform, playerTransform, stats)
                    boss.acidSprayCooldown = QUEEN_ACID_COOLDOWN
                    // Don't set isSprayingAcid here — during retreat the queen should
                    // keep moving. The spray state would zero velocity next frame.
                }
            }
            BossType.ABOMINATION -> {
                if (boss.groundSpikeCooldown <= 0f && distance < ABOM_SPIKE_RANGE) {
                    fireGroundSpikes(entity, transform, playerTransform, stats)
                    boss.groundSpikeCooldown = if (boss.isEnraged) ABOM_SPIKE_COOLDOWN * 0.6f else ABOM_SPIKE_COOLDOWN
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

    // TODO: Phase D — extend AoE to also damage turret entities within radius
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
