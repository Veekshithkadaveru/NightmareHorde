package app.krafted.nightmarehorde.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.components.AIBehavior
import app.krafted.nightmarehorde.engine.core.components.AIComponent
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.ZombieTypeComponent
import app.krafted.nightmarehorde.engine.input.InputManager
import app.krafted.nightmarehorde.engine.physics.CollisionResponseSystem
import app.krafted.nightmarehorde.engine.physics.CollisionSystem
import app.krafted.nightmarehorde.engine.physics.MovementSystem
import app.krafted.nightmarehorde.engine.physics.SpatialHashGrid
import app.krafted.nightmarehorde.engine.rendering.Camera
import app.krafted.nightmarehorde.engine.rendering.DamageNumberRenderer
import app.krafted.nightmarehorde.engine.rendering.ParticleRenderer
import app.krafted.nightmarehorde.engine.rendering.SpriteRenderer
import app.krafted.nightmarehorde.game.data.AssetManager
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.game.data.ObstacleType
import app.krafted.nightmarehorde.engine.core.components.WeaponComponent
import app.krafted.nightmarehorde.game.entities.AmmoPickup
import app.krafted.nightmarehorde.game.entities.HitEffectEntity
import app.krafted.nightmarehorde.game.entities.PlayerEntity
import app.krafted.nightmarehorde.game.entities.ZombieEntity
import app.krafted.nightmarehorde.game.systems.AmmoSystem
import app.krafted.nightmarehorde.game.systems.AutoAimSystem
import app.krafted.nightmarehorde.game.systems.CombatSystem
import app.krafted.nightmarehorde.game.systems.DamagePopupSystem
import app.krafted.nightmarehorde.game.systems.ObstacleSpawnSystem
import app.krafted.nightmarehorde.game.systems.PlayerAnimationSystem
import app.krafted.nightmarehorde.game.systems.PlayerSystem
import app.krafted.nightmarehorde.game.systems.ParticleSystem
import app.krafted.nightmarehorde.game.systems.ProjectileSystem
import app.krafted.nightmarehorde.game.systems.WeaponSystem
import app.krafted.nightmarehorde.game.systems.AISystem
import app.krafted.nightmarehorde.game.systems.ZombieAnimationSystem
import app.krafted.nightmarehorde.game.systems.ZombieDamageSystem
import app.krafted.nightmarehorde.game.data.ZombieType
import app.krafted.nightmarehorde.game.weapons.AssaultRifleWeapon
import app.krafted.nightmarehorde.game.weapons.FlamethrowerWeapon
import app.krafted.nightmarehorde.game.weapons.PistolWeapon
import app.krafted.nightmarehorde.game.weapons.SMGWeapon
import app.krafted.nightmarehorde.game.weapons.ShotgunWeapon
import app.krafted.nightmarehorde.game.weapons.SwordWeapon
import app.krafted.nightmarehorde.game.weapons.WeaponType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@HiltViewModel
class GameViewModel @Inject constructor(
    val gameLoop: GameLoop,
    val camera: Camera,
    val spriteRenderer: SpriteRenderer,
    val damageNumberRenderer: DamageNumberRenderer,
    val particleRenderer: ParticleRenderer,
    val assetManager: AssetManager,
    val inputManager: InputManager,
    val aiSystem: AISystem,
    val zombieAnimationSystem: ZombieAnimationSystem
) : ViewModel() {

    /** Player health exposed for the HUD */
    private val _playerHealth = MutableStateFlow(Pair(100, 100)) // (current, max)
    val playerHealth: StateFlow<Pair<Int, Int>> = _playerHealth.asStateFlow()
    
    /** Kill counter exposed for the HUD */
    private val _killCount = MutableStateFlow(0)
    val killCount: StateFlow<Int> = _killCount.asStateFlow()

    /** Elapsed game time in seconds */
    private val _gameTime = MutableStateFlow(0f)
    val gameTime: StateFlow<Float> = _gameTime.asStateFlow()

    // Debug: Exposed weapon name for UI
    private val _currentWeaponName = MutableStateFlow("Pistol")
    val currentWeaponName: StateFlow<String> = _currentWeaponName.asStateFlow()

    private var isGameRunning = false
    private var playerEntity: Entity? = null

    // ─── VS-Style Spawning Config ─────────────────────────────────────────
    companion object {
        /** Distance from player at which zombies spawn (just outside screen) */
        const val SPAWN_DISTANCE_MIN = 450f
        const val SPAWN_DISTANCE_MAX = 600f

        /** Distance from player at which zombies are despawned to save memory */
        const val DESPAWN_DISTANCE = 1200f

        /** Spawning interval in milliseconds — decreases over time */
        const val BASE_SPAWN_INTERVAL_MS = 800L
        const val MIN_SPAWN_INTERVAL_MS = 150L

        /** Maximum active zombie count — increases over time (VS-style) */
        const val BASE_MAX_ENEMIES = 15
        const val ABSOLUTE_MAX_ENEMIES = 100

        /** Time thresholds (seconds) for unlocking zombie types (matches dev plan) */
        const val RUNNER_UNLOCK_TIME = 60f       // 1 min
        const val BLOATER_UNLOCK_TIME = 180f     // 3 min
        const val SPITTER_UNLOCK_TIME = 180f     // 3 min
        const val BRUTE_UNLOCK_TIME = 300f       // 5 min
        const val CRAWLER_UNLOCK_TIME = 300f     // 5 min
        const val SCREAMER_UNLOCK_TIME = 600f    // 10 min
    }

    private var elapsedGameTime = 0f
    private val rng = Random(System.currentTimeMillis())

    fun startGame(characterType: CharacterType = CharacterType.CYBERPUNK_DETECTIVE) {
        if (isGameRunning) return
        isGameRunning = true
        elapsedGameTime = 0f
        _killCount.value = 0
        _gameTime.value = 0f

        // Preload sprite assets (background + selected character + obstacles + projectiles + zombies)
        assetManager.preload(
            characterType.idleTextureKey,
            characterType.runTextureKey,
            "background_space",
            "projectile_standard",
            *ZombieType.entries.map { it.assetName }.toTypedArray(),
            *ObstacleType.entries.map { it.textureKey }.toTypedArray()
        )

        // Shared spatial grid used by CollisionSystem
        val spatialGrid = SpatialHashGrid()

        // --- Register Systems (order by priority) ---

        // ObstacleSpawnSystem (5): procedural obstacle spawning around the player
        val obstacleSpawnSystem = ObstacleSpawnSystem().apply {
            onSpawnEntity = { entity -> gameLoop.addEntity(entity) }
            onDespawnEntity = { entityId ->
                val entity = gameLoop.getEntitiesSnapshot().find { it.id == entityId }
                if (entity != null) gameLoop.removeEntity(entity)
            }
        }
        gameLoop.addSystem(obstacleSpawnSystem)

        // PlayerSystem (10): input → velocity, camera follow
        val playerSystem = PlayerSystem(inputManager, camera).apply {
            onPlayerDeath = {
                Log.d("GameViewModel", "Player died! Game Over.")
                // Game over logic will be expanded in later phases
            }
        }
        gameLoop.addSystem(playerSystem)

        // PlayerAnimationSystem (15): animation state machine
        gameLoop.addSystem(PlayerAnimationSystem())
        
        // ZombieAnimationSystem (16): Update zombie sprite frames
        gameLoop.addSystem(zombieAnimationSystem)

        // AISystem (18): Update zombie behaviors before movement
        aiSystem.onSpawnEntity = { entity -> gameLoop.addEntity(entity) }
        gameLoop.addSystem(aiSystem)

        // AutoAimSystem (20): automatically aim at nearest enemy
        gameLoop.addSystem(AutoAimSystem())

        // WeaponSystem (30): firing logic
        gameLoop.addSystem(WeaponSystem(gameLoop))

        // AmmoSystem (40): pickup handling
        val collisionSystem = CollisionSystem(spatialGrid)
        gameLoop.addSystem(AmmoSystem(collisionSystem, gameLoop))

        // MovementSystem (50): applies velocity to position
        gameLoop.addSystem(MovementSystem())

        // CollisionResponseSystem (55): push entities out of obstacles
        gameLoop.addSystem(CollisionResponseSystem())

        // CollisionSystem (60): collision detection (populates spatial grid)
        gameLoop.addSystem(collisionSystem)

        // ProjectileSystem (80): handle projectile movement and lifetime
        gameLoop.addSystem(ProjectileSystem())

        // ZombieDamageSystem (95): handle zombie-to-player contact damage
        gameLoop.addSystem(ZombieDamageSystem())

        // CombatSystem (100): handle projectile-enemy collisions + death effects
        val combatSystem = CombatSystem(gameLoop).apply {
            onEnemyDeath = { deadEntity ->
                // Increment kill counter
                _killCount.value++

                // Bloater explodes on death (even when killed by projectiles)
                val ai = deadEntity.getComponent(AIComponent::class)
                if (ai?.behavior == AIBehavior.EXPLODE) {
                    val transform = deadEntity.getComponent(TransformComponent::class)
                    val stats = deadEntity.getComponent(StatsComponent::class)
                    if (transform != null && stats != null) {
                        // Damage player if nearby
                        playerEntity?.let { player ->
                            val playerTransform = player.getComponent(TransformComponent::class)
                            val playerHealth = player.getComponent(HealthComponent::class)
                            val playerStats = player.getComponent(StatsComponent::class)
                            if (playerTransform != null && playerHealth != null) {
                                val dx = playerTransform.x - transform.x
                                val dy = playerTransform.y - transform.y
                                val distSq = dx * dx + dy * dy
                                val radius = AISystem.EXPLOSION_RADIUS
                                if (distSq <= radius * radius) {
                                    playerHealth.takeDamage(
                                        stats.baseDamage.toInt(),
                                        playerStats?.armor ?: 0
                                    )
                                }
                            }
                        }
                        // Green explosion particles
                        HitEffectEntity.burst(
                            x = transform.x,
                            y = transform.y,
                            count = 12,
                            baseColor = androidx.compose.ui.graphics.Color(0xFF44FF44),
                            speed = 200f,
                            lifeTime = 0.5f,
                            size = 10f
                        ).forEach { gameLoop.addEntity(it) }
                    }
                }
            }
        }
        gameLoop.addSystem(combatSystem)
        
        // ParticleSystem (110): update hit-effect particle lifetime
        gameLoop.addSystem(ParticleSystem())

        // DamagePopupSystem (120): animate floating damage numbers
        gameLoop.addSystem(DamagePopupSystem())

        // --- Create Entities ---

        // Background
        val background = Entity().apply {
            addComponent(TransformComponent(x = 0f, y = 0f))
            addComponent(SpriteComponent(
                textureKey = "background_space",
                layer = 0,
                fillViewport = true
            ))
        }
        gameLoop.addEntity(background)

        // Player
        playerEntity = PlayerEntity.create(characterType = characterType, spawnX = 0f, spawnY = 0f)
        gameLoop.addEntity(playerEntity!!)
        
        // Pass player to AISystem
        aiSystem.setPlayer(playerEntity!!)

        // Test Ammo Pickup
        gameLoop.addEntity(AmmoPickup.create(x = 200f, y = 200f, amount = 20, weaponTypeIndex = 2))

        // Start the game loop
        gameLoop.start(viewModelScope)

        // ─── VS-Style Continuous Spawning Loop ────────────────────────────
        viewModelScope.launch {
            while (isGameRunning) {
                val spawnInterval = calculateSpawnInterval()
                kotlinx.coroutines.delay(spawnInterval)

                if (!isGameRunning) break

                // Update elapsed time
                elapsedGameTime += spawnInterval / 1000f
                _gameTime.value = elapsedGameTime

                // Count active zombies
                val activeZombies = countActiveZombies()
                val maxEnemies = calculateMaxEnemies()

                // Spawn if below cap
                if (activeZombies < maxEnemies) {
                    // Spawn a batch (VS spawns several at once as time progresses)
                    val batchSize = calculateBatchSize()
                    repeat(batchSize) {
                        if (countActiveZombies() < maxEnemies) {
                            spawnZombieOffScreen()
                        }
                    }
                }

                // Despawn zombies that wandered too far from the player
                despawnDistantZombies()
            }
        }

        // Observe player health for HUD
        viewModelScope.launch {
            while (isGameRunning) {
                playerEntity?.let { player ->
                    val health = player.getComponent(HealthComponent::class)
                    if (health != null) {
                        _playerHealth.value = Pair(health.currentHealth, health.maxHealth)
                    }
                }
                kotlinx.coroutines.delay(16) // ~60 FPS UI sync
            }
        }

        // Double-tap events (for turret menu in Phase D)
        viewModelScope.launch {
            inputManager.doubleTapEvents.collect { position ->
                Log.d("GameViewModel", "Double-tap for turret menu at: $position")
            }
        }
    }

    fun stopGame() {
        isGameRunning = false
        gameLoop.stop()
        gameLoop.clear()
        inputManager.reset()
        playerEntity = null
        elapsedGameTime = 0f
    }

    // ─── VS-Style Spawning Logic ──────────────────────────────────────────

    /**
     * Calculate the spawn interval based on elapsed time.
     * Starts slow, gets faster over time (more enemies flooding in).
     */
    private fun calculateSpawnInterval(): Long {
        // Linearly decrease from BASE to MIN over 15 minutes
        val progress = (elapsedGameTime / 900f).coerceIn(0f, 1f) // 900s = 15 min
        val interval = BASE_SPAWN_INTERVAL_MS - ((BASE_SPAWN_INTERVAL_MS - MIN_SPAWN_INTERVAL_MS) * progress).toLong()
        return interval.coerceAtLeast(MIN_SPAWN_INTERVAL_MS)
    }

    /**
     * Calculate the max enemy cap based on elapsed time.
     * Matches the wave progression table from the dev plan.
     */
    private fun calculateMaxEnemies(): Int {
        val maxByTime = when {
            elapsedGameTime < 60f -> 15    // 0-1 min
            elapsedGameTime < 180f -> 25   // 1-3 min
            elapsedGameTime < 300f -> 35   // 3-5 min
            elapsedGameTime < 600f -> 50   // 5-10 min
            elapsedGameTime < 900f -> 75   // 10-15 min
            else -> 100                     // 15+ min
        }
        return maxByTime.coerceAtMost(ABSOLUTE_MAX_ENEMIES)
    }

    /**
     * How many zombies to spawn per tick. Increases over time for VS "horde" feel.
     */
    private fun calculateBatchSize(): Int {
        return when {
            elapsedGameTime < 60f -> 1
            elapsedGameTime < 180f -> 2
            elapsedGameTime < 300f -> 3
            elapsedGameTime < 600f -> 4
            else -> 5
        }
    }

    /**
     * Get the pool of zombie types available at the current game time.
     * Types unlock progressively, matching the dev plan's wave progression table.
     */
    private fun getAvailableZombieTypes(): List<ZombieType> {
        val types = mutableListOf(ZombieType.WALKER) // Always available

        if (elapsedGameTime >= RUNNER_UNLOCK_TIME) types.add(ZombieType.RUNNER)
        if (elapsedGameTime >= BLOATER_UNLOCK_TIME) types.add(ZombieType.BLOATER)
        if (elapsedGameTime >= SPITTER_UNLOCK_TIME) types.add(ZombieType.SPITTER)
        if (elapsedGameTime >= BRUTE_UNLOCK_TIME) types.add(ZombieType.BRUTE)
        if (elapsedGameTime >= CRAWLER_UNLOCK_TIME) types.add(ZombieType.CRAWLER)
        if (elapsedGameTime >= SCREAMER_UNLOCK_TIME) types.add(ZombieType.SCREAMER)

        return types
    }

    /**
     * Spawn a zombie at a random position just outside the visible screen.
     * Uses random angle from player, placing the zombie at SPAWN_DISTANCE.
     */
    private fun spawnZombieOffScreen() {
        val player = playerEntity ?: return
        val playerTransform = player.getComponent(TransformComponent::class) ?: return

        // Random angle around the player (full 360°)
        val angle = rng.nextFloat() * 2f * Math.PI.toFloat()
        val distance = SPAWN_DISTANCE_MIN + rng.nextFloat() * (SPAWN_DISTANCE_MAX - SPAWN_DISTANCE_MIN)

        val spawnX = playerTransform.x + cos(angle) * distance
        val spawnY = playerTransform.y + sin(angle) * distance

        // Pick a random zombie type from the currently available pool
        val availableTypes = getAvailableZombieTypes()
        val type = pickWeightedZombieType(availableTypes)

        val zombie = ZombieEntity(x = spawnX, y = spawnY, type = type)
        gameLoop.addEntity(zombie)
    }

    /**
     * Weighted random selection — walkers are more common, special types are rarer.
     * Matches VS where basic enemies are the majority of the horde.
     */
    private fun pickWeightedZombieType(available: List<ZombieType>): ZombieType {
        // Weight table: higher = more common
        val weights = available.map { type ->
            when (type) {
                ZombieType.WALKER -> 40    // Very common
                ZombieType.RUNNER -> 25    // Common
                ZombieType.CRAWLER -> 15   // Moderate
                ZombieType.BLOATER -> 8    // Uncommon
                ZombieType.SPITTER -> 6    // Rare
                ZombieType.BRUTE -> 4      // Rare
                ZombieType.SCREAMER -> 2   // Very rare
            }
        }

        val totalWeight = weights.sum()
        var roll = rng.nextInt(totalWeight)

        for (i in available.indices) {
            roll -= weights[i]
            if (roll < 0) return available[i]
        }

        return available.last()
    }

    /**
     * Count currently active zombie entities.
     */
    private fun countActiveZombies(): Int {
        return gameLoop.getEntitiesSnapshot().count { entity ->
            entity.isActive && entity.hasComponent(ZombieTypeComponent::class)
        }
    }

    /**
     * Remove zombies that have wandered too far from the player.
     * In VS, enemies that fall far behind are quietly despawned.
     */
    private fun despawnDistantZombies() {
        val player = playerEntity ?: return
        val playerTransform = player.getComponent(TransformComponent::class) ?: return
        val despawnDistSq = DESPAWN_DISTANCE * DESPAWN_DISTANCE

        gameLoop.getEntitiesSnapshot().forEach { entity ->
            if (!entity.isActive) return@forEach
            if (!entity.hasComponent(ZombieTypeComponent::class)) return@forEach

            val transform = entity.getComponent(TransformComponent::class) ?: return@forEach
            val dx = transform.x - playerTransform.x
            val dy = transform.y - playerTransform.y
            val distSq = dx * dx + dy * dy

            if (distSq > despawnDistSq) {
                entity.isActive = false
            }
        }
    }

    // ─── Debug Helpers ────────────────────────────────────────────────────

    /** Cycle through weapons (debug) */
    fun debugCycleWeapon() {
        playerEntity?.let { player ->
            val weaponComp = player.getComponent(WeaponComponent::class) ?: return
            val currentType = weaponComp.equippedWeapon?.type ?: return
            
            val newWeapon = when (currentType) {
                WeaponType.PISTOL -> AssaultRifleWeapon()
                WeaponType.ASSAULT_RIFLE -> ShotgunWeapon()
                WeaponType.SHOTGUN -> SMGWeapon()
                WeaponType.SMG -> FlamethrowerWeapon()
                WeaponType.FLAMETHROWER -> SwordWeapon()
                WeaponType.MELEE -> PistolWeapon()
            }
            
            // Equip new weapon (give some ammo for testing)
            weaponComp.equippedWeapon = newWeapon
            if (!newWeapon.infiniteAmmo) {
                weaponComp.currentAmmo = newWeapon.maxAmmo 
            }
            
            _currentWeaponName.value = newWeapon.name
            Log.d("GameViewModel", "Switched weapon to: ${newWeapon.name}")
        }
    }
}
