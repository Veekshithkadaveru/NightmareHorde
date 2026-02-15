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
import app.krafted.nightmarehorde.engine.core.components.WeaponInventoryComponent
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
import app.krafted.nightmarehorde.game.data.ZombieType
import app.krafted.nightmarehorde.game.entities.HitEffectEntity
import app.krafted.nightmarehorde.game.entities.PlayerEntity
import app.krafted.nightmarehorde.game.systems.AISystem
import app.krafted.nightmarehorde.game.systems.AutoAimSystem
import app.krafted.nightmarehorde.game.systems.CombatSystem
import app.krafted.nightmarehorde.game.systems.DamagePopupSystem
import app.krafted.nightmarehorde.game.systems.LootDropSystem
import app.krafted.nightmarehorde.game.systems.ObstacleSpawnSystem
import app.krafted.nightmarehorde.game.systems.ParticleSystem
import app.krafted.nightmarehorde.game.systems.PickupAnimationSystem
import app.krafted.nightmarehorde.game.systems.PickupCollisionSystem
import app.krafted.nightmarehorde.game.systems.PickupSystem
import app.krafted.nightmarehorde.game.systems.PlayerAnimationSystem
import app.krafted.nightmarehorde.game.systems.PlayerSystem
import app.krafted.nightmarehorde.game.systems.ProjectileSystem
import app.krafted.nightmarehorde.game.systems.WaveSpawner
import app.krafted.nightmarehorde.game.systems.WeaponManager
import app.krafted.nightmarehorde.game.systems.WeaponSystem
import app.krafted.nightmarehorde.game.systems.ZombieAnimationSystem
import app.krafted.nightmarehorde.game.systems.ZombieDamageSystem
import app.krafted.nightmarehorde.game.weapons.WeaponType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    // ─── HUD State ────────────────────────────────────────────────────────

    /** Player health exposed for the HUD */
    private val _playerHealth = MutableStateFlow(Pair(100, 100)) // (current, max)
    val playerHealth: StateFlow<Pair<Int, Int>> = _playerHealth.asStateFlow()

    /** Kill counter exposed for the HUD */
    private val _killCount = MutableStateFlow(0)
    val killCount: StateFlow<Int> = _killCount.asStateFlow()

    /** Elapsed game time in seconds */
    private val _gameTime = MutableStateFlow(0f)
    val gameTime: StateFlow<Float> = _gameTime.asStateFlow()

    // Delegate weapon HUD state to WeaponManager
    private val weaponManager = WeaponManager()
    val activeWeaponType: StateFlow<WeaponType> = weaponManager.activeWeaponType
    val currentAmmo: StateFlow<Int> = weaponManager.currentAmmo
    val currentWeaponName: StateFlow<String> = weaponManager.currentWeaponName
    val unlockedWeapons: StateFlow<List<WeaponType>> = weaponManager.unlockedWeapons
    val weaponUnlockNotification: StateFlow<WeaponType?> = weaponManager.weaponUnlockNotification

    // ─── Internal State ───────────────────────────────────────────────────

    private var isGameRunning = false
    private var playerEntity: Entity? = null
    private lateinit var lootDropSystem: LootDropSystem
    private lateinit var waveSpawner: WaveSpawner

    /** Reference to spawning coroutine for clean cancellation. */
    private var spawnJob: Job? = null
    /** Reference to HUD observer coroutine for clean cancellation. */
    private var hudObserverJob: Job? = null

    // ─── Game Lifecycle ───────────────────────────────────────────────────

    fun startGame(characterType: CharacterType = CharacterType.CYBERPUNK_DETECTIVE) {
        if (isGameRunning) return
        isGameRunning = true
        _killCount.value = 0
        _gameTime.value = 0f
        weaponManager.reset()

        assetManager.preload(
            characterType.idleTextureKey,
            characterType.runTextureKey,
            "background_space",
            "projectile_standard",
            *ZombieType.entries.map { it.assetName }.toTypedArray(),
            *ObstacleType.entries.map { it.textureKey }.toTypedArray(),
            "pickup_orb_green",
            "pickup_orb_yellow",
            "pickup_orb_red",
            "pickup_orb_blue"
        )

        val spatialGrid = SpatialHashGrid()

        // --- Initialize extracted managers ---
        waveSpawner = WaveSpawner(gameLoop)
        waveSpawner.resetTimer()

        lootDropSystem = LootDropSystem(gameLoop)

        // --- Register Systems (order by priority) ---

        val obstacleSpawnSystem = ObstacleSpawnSystem().apply {
            onSpawnEntity = { entity -> gameLoop.addEntity(entity) }
            onDespawnEntity = { entityId ->
                val entity = gameLoop.getEntitiesSnapshot().find { it.id == entityId }
                if (entity != null) gameLoop.removeEntity(entity)
            }
        }
        gameLoop.addSystem(obstacleSpawnSystem)

        val playerSystem = PlayerSystem(inputManager, camera).apply {
            onPlayerDeath = {
                Log.d("GameViewModel", "Player died! Game Over.")
            }
        }
        gameLoop.addSystem(playerSystem)

        gameLoop.addSystem(PlayerAnimationSystem())
        gameLoop.addSystem(zombieAnimationSystem)

        aiSystem.onSpawnEntity = { entity -> gameLoop.addEntity(entity) }
        gameLoop.addSystem(aiSystem)

        gameLoop.addSystem(AutoAimSystem())

        // PickupSystem (25): magnet attraction and despawn timer
        gameLoop.addSystem(PickupSystem())

        // PickupAnimationSystem (26): animate orb sprite frames
        gameLoop.addSystem(PickupAnimationSystem())

        // WeaponSystem (30): firing logic with inventory integration
        val weaponSystem = WeaponSystem(gameLoop).apply {
            onAmmoEmpty = { emptyWeaponType ->
                weaponManager.onAmmoEmpty(emptyWeaponType)
            }
        }
        gameLoop.addSystem(weaponSystem)

        // PickupCollisionSystem (40): pickup handling (ammo + health)
        val collisionSystem = CollisionSystem(spatialGrid)
        gameLoop.addSystem(PickupCollisionSystem(collisionSystem, gameLoop))

        gameLoop.addSystem(MovementSystem())
        gameLoop.addSystem(CollisionResponseSystem())
        gameLoop.addSystem(collisionSystem)
        gameLoop.addSystem(ProjectileSystem())
        gameLoop.addSystem(ZombieDamageSystem())

        // CombatSystem (100): handle projectile-enemy collisions + death effects
        val combatSystem = CombatSystem(gameLoop).apply {
            onEnemyDeath = { deadEntity -> handleEnemyDeath(deadEntity) }
        }
        gameLoop.addSystem(combatSystem)

        gameLoop.addSystem(ParticleSystem())
        gameLoop.addSystem(DamagePopupSystem())

        // --- Create Entities ---

        val background = Entity().apply {
            addComponent(TransformComponent(x = 0f, y = 0f))
            addComponent(SpriteComponent(
                textureKey = "background_space",
                layer = 0,
                fillViewport = true
            ))
        }
        gameLoop.addEntity(background)

        playerEntity = PlayerEntity.create(characterType = characterType, spawnX = 0f, spawnY = 0f)
        gameLoop.addEntity(playerEntity!!)

        aiSystem.setPlayer(playerEntity!!)

        gameLoop.start(viewModelScope)

        // ─── Spawning Loop (uses authoritative nanoTime clock) ────────────
        spawnJob = viewModelScope.launch {
            while (isGameRunning) {
                val spawnInterval = waveSpawner.calculateSpawnInterval()
                kotlinx.coroutines.delay(spawnInterval)

                if (!isGameRunning) break

                // Update authoritative timer
                waveSpawner.tick()
                _gameTime.value = waveSpawner.elapsedGameTime

                // Refresh zombie count once per tick (not per batch spawn)
                waveSpawner.refreshZombieCount()
                val maxEnemies = waveSpawner.calculateMaxEnemies()

                if (waveSpawner.cachedZombieCount < maxEnemies) {
                    val batchSize = waveSpawner.calculateBatchSize()
                    val playerTransform = playerEntity?.getComponent(TransformComponent::class)
                    if (playerTransform != null) {
                        // Use cached count + local counter to avoid re-scanning
                        var spawnedThisTick = 0
                        repeat(batchSize) {
                            if (waveSpawner.cachedZombieCount + spawnedThisTick < maxEnemies) {
                                waveSpawner.spawnZombieOffScreen(playerTransform)
                                spawnedThisTick++
                            }
                        }
                    }
                }

                // Despawn distant zombies
                val playerTransform = playerEntity?.getComponent(TransformComponent::class)
                if (playerTransform != null) {
                    waveSpawner.despawnDistantZombies(playerTransform)
                }
            }
        }

        // ─── HUD Observer ─────────────────────────────────────────────────
        hudObserverJob = viewModelScope.launch {
            while (isGameRunning) {
                playerEntity?.let { player ->
                    val health = player.getComponent(HealthComponent::class)
                    if (health != null) {
                        _playerHealth.value = Pair(health.currentHealth, health.maxHealth)
                    }
                    weaponManager.refreshHudState(player)
                }
                kotlinx.coroutines.delay(16)
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
        spawnJob?.cancel()
        hudObserverJob?.cancel()
        spawnJob = null
        hudObserverJob = null
        gameLoop.stop()
        gameLoop.clear()
        inputManager.reset()
        playerEntity = null
    }

    // ─── Weapon Switching (delegated) ─────────────────────────────────────

    fun cycleWeaponForward() {
        playerEntity?.let { weaponManager.cycleWeaponForward(it) }
    }

    fun switchWeapon(targetType: WeaponType) {
        playerEntity?.let { weaponManager.switchWeapon(targetType, it) }
    }

    fun dismissWeaponNotification() {
        weaponManager.dismissWeaponNotification()
    }

    // ─── Enemy Death Handler ──────────────────────────────────────────────

    private fun handleEnemyDeath(deadEntity: Entity) {
        _killCount.value++
        val kills = _killCount.value

        // Check weapon unlocks
        playerEntity?.let { weaponManager.checkWeaponUnlocks(kills, it) }

        // Spawn loot drops
        val inventory = playerEntity?.getComponent(WeaponInventoryComponent::class)
        lootDropSystem.tryDropLoot(
            deadEntity = deadEntity,
            elapsedGameTime = waveSpawner.elapsedGameTime,
            unlockedWeaponTypes = inventory?.getUnlockedTypes() ?: emptyList()
        )

        // Bloater explodes on death
        val ai = deadEntity.getComponent(AIComponent::class)
        if (ai?.behavior == AIBehavior.EXPLODE) {
            handleBloaterExplosion(deadEntity)
        }
    }

    private fun handleBloaterExplosion(deadEntity: Entity) {
        val transform = deadEntity.getComponent(TransformComponent::class) ?: return
        val stats = deadEntity.getComponent(StatsComponent::class) ?: return

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
