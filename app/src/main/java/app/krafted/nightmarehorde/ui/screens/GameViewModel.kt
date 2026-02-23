package app.krafted.nightmarehorde.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.GameLoop
import app.krafted.nightmarehorde.engine.core.components.AIBehavior
import app.krafted.nightmarehorde.engine.core.components.AIComponent
import app.krafted.nightmarehorde.engine.core.components.BossComponent
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponInventoryComponent
import app.krafted.nightmarehorde.engine.core.components.XPComponent
import app.krafted.nightmarehorde.engine.input.InputManager
import app.krafted.nightmarehorde.engine.physics.CollisionResponseSystem
import app.krafted.nightmarehorde.engine.physics.CollisionSystem
import app.krafted.nightmarehorde.engine.physics.MovementSystem
import app.krafted.nightmarehorde.engine.physics.SpatialHashGrid
import app.krafted.nightmarehorde.engine.rendering.Camera
import app.krafted.nightmarehorde.engine.rendering.DamageNumberRenderer
import app.krafted.nightmarehorde.engine.rendering.DroneRenderer
import app.krafted.nightmarehorde.engine.rendering.ParticleRenderer
import app.krafted.nightmarehorde.engine.rendering.SpriteRenderer
import app.krafted.nightmarehorde.game.data.AssetManager
import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.game.data.DroneType
import app.krafted.nightmarehorde.game.data.ObstacleType
import app.krafted.nightmarehorde.game.data.BossType
import app.krafted.nightmarehorde.game.data.EvolutionRegistry
import app.krafted.nightmarehorde.game.data.SynergyRegistry
import app.krafted.nightmarehorde.game.data.UpgradeCategory
import app.krafted.nightmarehorde.game.data.UpgradeChoice
import app.krafted.nightmarehorde.game.data.UpgradeContext
import app.krafted.nightmarehorde.game.data.UpgradePool
import app.krafted.nightmarehorde.game.data.ZombieType
import app.krafted.nightmarehorde.game.entities.AmmoPickup
import app.krafted.nightmarehorde.game.entities.BossEntity
import app.krafted.nightmarehorde.game.entities.HealthPickup
import app.krafted.nightmarehorde.game.entities.HitEffectEntity
import app.krafted.nightmarehorde.game.entities.PlayerEntity
import app.krafted.nightmarehorde.engine.rendering.LightingSystem
import app.krafted.nightmarehorde.game.systems.AISystem
import app.krafted.nightmarehorde.game.systems.AutoAimSystem
import app.krafted.nightmarehorde.game.systems.DroneManager
import app.krafted.nightmarehorde.game.systems.DroneSystem
import app.krafted.nightmarehorde.game.systems.BossAnimationSystem
import app.krafted.nightmarehorde.game.systems.BossSystem
import app.krafted.nightmarehorde.game.systems.CombatSystem
import app.krafted.nightmarehorde.game.systems.DamagePopupSystem
import app.krafted.nightmarehorde.game.systems.DayNightCycle
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
import app.krafted.nightmarehorde.game.weapons.EvolvedWeaponFactory
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
    val zombieAnimationSystem: ZombieAnimationSystem,
    val droneRenderer: DroneRenderer
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

    // Day/Night cycle state exposed for HUD + rendering overlay.
    // Bundled into a single data class so a Compose frame always sees a
    // consistent snapshot (no partial update between phase / intensity / progress).
    data class DayNightState(
        val phase: DayNightCycle.TimePhase = DayNightCycle.TimePhase.DAY,
        val nightIntensity: Float = 0f,
        val phaseProgress: Float = 0f,
        val overlayAlpha: Float = 0f
    )

    private val _dayNightState = MutableStateFlow(DayNightState())
    val dayNightState: StateFlow<DayNightState> = _dayNightState.asStateFlow()

    /** LightingSystem for the rendering overlay — read by GameScreen. */
    val lightingSystem = LightingSystem()

    // ─── Boss HUD State ────────────────────────────────────────────────
    data class BossState(
        val isActive: Boolean = false,
        val name: String = "",
        val currentHealth: Int = 0,
        val maxHealth: Int = 0,
        val accentColor: Long = 0xFFFF4444
    )

    private val _bossState = MutableStateFlow(BossState())
    val bossState: StateFlow<BossState> = _bossState.asStateFlow()

    // ─── Drone HUD State ────────────────────────────────────────────────
    private var droneManager: DroneManager? = null
    private val _droneHudState = MutableStateFlow<List<DroneManager.DroneHudInfo>>(emptyList())
    val droneHudState: StateFlow<List<DroneManager.DroneHudInfo>> = _droneHudState.asStateFlow()
    private val _droneUnlockNotification = MutableStateFlow<DroneType?>(null)
    val droneUnlockNotification: StateFlow<DroneType?> = _droneUnlockNotification.asStateFlow()

    // ─── XP / Level-Up State ────────────────────────────────────────────
    data class XPState(
        val currentXP: Int = 0,
        val xpToNextLevel: Int = 19,
        val currentLevel: Int = 1,
        val xpProgress: Float = 0f
    )

    data class LevelUpState(
        val isShowing: Boolean = false,
        val level: Int = 1,
        val upgrades: List<UpgradeChoice> = emptyList()
    )

    private val _xpState = MutableStateFlow(XPState())
    val xpState: StateFlow<XPState> = _xpState.asStateFlow()

    private val _levelUpState = MutableStateFlow(LevelUpState())
    val levelUpState: StateFlow<LevelUpState> = _levelUpState.asStateFlow()

    private val upgradePool = UpgradePool()

    // ─── Internal State ───────────────────────────────────────────────────

    private var isGameRunning = false
    private var playerEntity: Entity? = null
    private lateinit var lootDropSystem: LootDropSystem
    private lateinit var waveSpawner: WaveSpawner
    private var dayNightCycle: DayNightCycle? = null

    /** Reference to spawning coroutine for clean cancellation. */
    private var spawnJob: Job? = null
    /** Reference to HUD observer coroutine for clean cancellation. */
    private var hudObserverJob: Job? = null

    // ─── Boss State ────────────────────────────────────────────────────
    private var activeBossEntity: Entity? = null
    private var bossesDefeated: Int = 0
    private var lastBossSpawnTime: Float = 0f
    private var isBossFightActive: Boolean = false
    private var droneGrantedWave3: Boolean = false

    companion object {
        /** Boss spawns every 5 minutes (300 seconds) */
        const val BOSS_SPAWN_INTERVAL = 300f
        /** Distance from the player at which a boss spawns */
        const val BOSS_SPAWN_DISTANCE = 500f
        /** Number of guaranteed loot drops on boss death */
        const val BOSS_LOOT_DROP_COUNT = 6
    }

    // ─── Game Lifecycle ───────────────────────────────────────────────────

    fun startGame(characterClass: CharacterClass = CharacterClass.ROOKIE) {
        val characterType = characterClass.characterType
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
            "pickup_orb_blue",
            // Boss assets
            "boss_idle",
            "boss_hive_queen",
            "boss_abomination",
            "boss_thrust",
            "boss_bolt",
            // Drone assets
            *DroneType.entries.map { it.textureKey }.toTypedArray()
        )

        val spatialGrid = SpatialHashGrid()

        // --- Initialize extracted managers ---
        waveSpawner = WaveSpawner(gameLoop)
        waveSpawner.resetTimer()
        // dayNightCycle will be assigned after it's created below

        lootDropSystem = LootDropSystem(gameLoop)

        // --- Day/Night Cycle (priority 1 — runs before everything) ---
        val cycle = DayNightCycle()
        cycle.reset()
        dayNightCycle = cycle
        gameLoop.addSystem(cycle)

        // Wire day/night cycle into wave spawner
        waveSpawner.dayNightCycle = cycle

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
        gameLoop.addSystem(BossAnimationSystem())

        aiSystem.onSpawnEntity = { entity -> gameLoop.addEntity(entity) }
        aiSystem.dayNightCycle = cycle
        gameLoop.addSystem(aiSystem)

        // BossSystem (priority 19): boss AI — runs right after regular AI
        val bossSystem = BossSystem().apply {
            onSpawnEntity = { entity -> gameLoop.addEntity(entity) }
        }
        gameLoop.addSystem(bossSystem)

        gameLoop.addSystem(AutoAimSystem())

        // DroneSystem (22): orbital drone orbit, targeting, firing, fuel
        val dm = DroneManager(gameLoop)
        droneManager = dm
        val droneSystem = DroneSystem(gameLoop).apply {
            onDroneKill = { deadEntity, droneEntityId ->
                handleEnemyDeath(deadEntity)
                dm.refuelDroneById(droneEntityId, DroneManager.REFUEL_DRONE_KILL)
            }
        }
        gameLoop.addSystem(droneSystem)

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
        gameLoop.addSystem(ZombieDamageSystem().apply {
            dayNightCycle = cycle
        })

        // CombatSystem (100): handle projectile-enemy collisions + death effects
        val combatSystem = CombatSystem(gameLoop).apply {
            onEnemyDeath = { deadEntity -> handleEnemyDeath(deadEntity) }
            // Wire melee retaliation: when a melee projectile hits a boss, BossSystem may counter-attack
            onBossMeleeHit = { bossEntity -> bossSystem.handleMeleeRetaliation(bossEntity) }
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

        playerEntity = PlayerEntity.create(characterClass = characterClass, spawnX = 0f, spawnY = 0f).apply {
            addComponent(XPComponent())
        }
        gameLoop.addEntity(playerEntity!!)

        aiSystem.setPlayer(playerEntity!!)
        bossSystem.setPlayer(playerEntity!!)

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

                // ─── Wave 3 Gunner Drone Grant ───────────────────────
                if (!droneGrantedWave3 && waveSpawner.elapsedGameTime >= 90f) {
                    droneGrantedWave3 = true
                    playerEntity?.let { droneManager?.grantDrone(DroneType.GUNNER, it) }
                }

                // ─── Boss Spawn Check ──────────────────────────────────
                checkBossSpawn()

                // If a boss fight is active, pause normal zombie spawning
                if (isBossFightActive) {
                    // Still despawn distant zombies during boss fight
                    val pt = playerEntity?.getComponent(TransformComponent::class)
                    if (pt != null) {
                        waveSpawner.despawnDistantZombies(pt)
                    }
                    continue
                }

                // ─── Normal Zombie Spawning ────────────────────────────
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

                    // ─── XP / Level-Up HUD ─────────────────────────────
                    val xpComp = player.getComponent(XPComponent::class)
                    if (xpComp != null) {
                        _xpState.value = XPState(
                            currentXP = xpComp.currentXP,
                            xpToNextLevel = xpComp.xpToNextLevel,
                            currentLevel = xpComp.currentLevel,
                            xpProgress = xpComp.xpProgress
                        )

                        // Check for level-up pending
                        if (xpComp.levelUpPending && !_levelUpState.value.isShowing) {
                            // Pause game and show level-up UI
                            gameLoop.pause()
                            val stats = player.getComponent(StatsComponent::class)
                            val inventory = player.getComponent(WeaponInventoryComponent::class)
                            val options = upgradePool.getRandomUpgrades(
                                count = 3,
                                luck = stats?.luck ?: 0f,
                                droneManager = droneManager,
                                weaponInventory = inventory
                            )
                            _levelUpState.value = LevelUpState(
                                isShowing = true,
                                level = xpComp.currentLevel + 1,
                                upgrades = options
                            )
                        }
                    }
                }
                // Update day/night HUD state as a single atomic snapshot
                // (safe-read — may be null during teardown)
                dayNightCycle?.let { dnc ->
                    _dayNightState.value = DayNightState(
                        phase = dnc.currentPhase,
                        nightIntensity = dnc.nightIntensity,
                        phaseProgress = dnc.phaseProgress,
                        overlayAlpha = dnc.overlayAlpha
                    )
                }

                // Update boss HUD state
                updateBossHudState()

                // Update drone HUD state
                droneManager?.let { dm ->
                    dm.refreshHudState()
                    _droneHudState.value = dm.droneHudState.value
                    _droneUnlockNotification.value = dm.droneUnlockNotification.value
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
        dayNightCycle = null
        activeBossEntity = null
        isBossFightActive = false
        bossesDefeated = 0
        lastBossSpawnTime = 0f
        droneGrantedWave3 = false
        _bossState.value = BossState()
        droneManager?.reset()
        _droneHudState.value = emptyList()
        _droneUnlockNotification.value = null
        _xpState.value = XPState()
        _levelUpState.value = LevelUpState()
        upgradePool.reset()
        activatedSynergies.clear()
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

    fun dismissDroneNotification() {
        droneManager?.dismissDroneNotification()
        _droneUnlockNotification.value = null
    }

    // ─── Level-Up Selection ────────────────────────────────────────────────

    /** Tracks which synergies have been activated this run. */
    private val activatedSynergies = mutableSetOf<String>()

    /**
     * Called when the player selects an upgrade from the level-up screen.
     * Applies the upgrade, records it in the pool, checks synergies, and resumes the game.
     */
    fun selectUpgrade(choice: UpgradeChoice) {
        val upgrade = choice.upgrade
        playerEntity?.let { player ->
            val stats = player.getComponent(StatsComponent::class) ?: return@let
            val health = player.getComponent(HealthComponent::class) ?: return@let
            val inventory = player.getComponent(WeaponInventoryComponent::class)

            // Record the pick in the pool
            upgradePool.recordUpgradePicked(upgrade.id)

            // Build context
            val context = UpgradeContext(
                stats = stats,
                health = health,
                weaponInventory = inventory,
                droneManager = droneManager,
                playerEntity = player,
                currentLevel = choice.nextLevel
            )

            // Apply the upgrade
            upgrade.apply(context)
            Log.d("GameViewModel", "Applied upgrade: ${upgrade.name} Lv${choice.nextLevel}")

            // Handle weapon evolution
            if (upgrade.category == UpgradeCategory.WEAPON_EVOLUTION) {
                handleWeaponEvolution(upgrade.id, player)
            }

            // Sync HP if maxHealth changed
            if (stats.maxHealth > health.maxHealth) {
                val increase = stats.maxHealth - health.maxHealth
                health.maxHealth = stats.maxHealth
                health.heal(increase)
            }

            // Check synergies
            checkSynergies(context)

            // Consume the level-up in the XP component
            val xpComp = player.getComponent(XPComponent::class)
            xpComp?.consumeLevelUp()
        }

        // Hide level-up UI and resume the game
        _levelUpState.value = LevelUpState()
        gameLoop.resume()
    }

    private fun handleWeaponEvolution(upgradeId: String, player: Entity) {
        val recipe = EvolutionRegistry.getRecipeForEvolution(upgradeId) ?: return
        val inventory = player.getComponent(WeaponInventoryComponent::class) ?: return
        val evolvedWeapon = EvolvedWeaponFactory.create(recipe)
        inventory.replaceWeapon(recipe.baseWeaponType, evolvedWeapon)
        Log.d("GameViewModel", "Weapon evolved: ${recipe.displayName}")
    }

    private fun checkSynergies(context: UpgradeContext) {
        for (synergy in SynergyRegistry.ALL) {
            if (synergy.id in activatedSynergies) continue

            val allMet = synergy.requiredUpgradeIds.zip(synergy.requiredMinLevels).all { (id, minLv) ->
                upgradePool.getUpgradeLevel(id) >= minLv
            }

            if (allMet) {
                synergy.apply(context)
                activatedSynergies.add(synergy.id)
                Log.d("GameViewModel", "Synergy activated: ${synergy.name}")
            }
        }
    }

    // ─── Enemy Death Handler ──────────────────────────────────────────────

    private fun handleEnemyDeath(deadEntity: Entity) {
        _killCount.value++
        val kills = _killCount.value

        // Refuel drones on any kill
        droneManager?.refuelAllDrones(DroneManager.REFUEL_ANY_KILL)
        droneManager?.cleanupLostDrones()

        // Check weapon unlocks
        playerEntity?.let { weaponManager.checkWeaponUnlocks(kills, it) }

        // Spawn loot drops
        val inventory = playerEntity?.getComponent(WeaponInventoryComponent::class)
        val healthComp = playerEntity?.getComponent(HealthComponent::class)
        lootDropSystem.tryDropLoot(
            deadEntity = deadEntity,
            elapsedGameTime = waveSpawner.elapsedGameTime,
            unlockedWeaponTypes = inventory?.getUnlockedTypes() ?: emptyList(),
            playerHealthComp = healthComp
        )

        // Bloater explodes on death
        val ai = deadEntity.getComponent(AIComponent::class)
        if (ai?.behavior == AIBehavior.EXPLODE) {
            handleBloaterExplosion(deadEntity)
        }

        // Boss death handling
        val bossComp = deadEntity.getComponent(BossComponent::class)
        if (bossComp != null) {
            handleBossDeath(deadEntity, bossComp)
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

    // ─── Boss Management ───────────────────────────────────────────────

    /**
     * Check if it's time to spawn a boss.
     * First boss at 5 minutes, then every 5 minutes after the previous one is defeated.
     */
    private fun checkBossSpawn() {
        if (isBossFightActive) return

        val elapsed = waveSpawner.elapsedGameTime
        val timeSinceLastBoss = elapsed - lastBossSpawnTime

        // First boss at BOSS_SPAWN_INTERVAL, subsequent bosses also at BOSS_SPAWN_INTERVAL after defeat
        if (timeSinceLastBoss >= BOSS_SPAWN_INTERVAL) {
            spawnBoss()
        }
    }

    private fun spawnBoss() {
        val playerTransform = playerEntity?.getComponent(TransformComponent::class) ?: return

        // Cycle through boss types weakest→strongest: Hive Queen → Tank → Abomination → repeat
        val bossTypes = BossType.entries
        val bossType = bossTypes[bossesDefeated % bossTypes.size]
        val bossNumber = bossesDefeated + 1

        // Spawn boss off-screen at a random angle
        val angle = kotlin.random.Random.nextFloat() * 2f * Math.PI.toFloat()
        val spawnX = playerTransform.x + kotlin.math.cos(angle) * BOSS_SPAWN_DISTANCE
        val spawnY = playerTransform.y + kotlin.math.sin(angle) * BOSS_SPAWN_DISTANCE

        val boss = BossEntity(
            x = spawnX,
            y = spawnY,
            type = bossType,
            bossNumber = bossNumber
        )
        gameLoop.addEntity(boss)
        activeBossEntity = boss
        isBossFightActive = true

        Log.d("GameViewModel", "Boss spawned: ${bossType.displayName} (#$bossNumber)")
    }

    private fun handleBossDeath(deadEntity: Entity, bossComp: BossComponent) {
        bossesDefeated++
        isBossFightActive = false
        activeBossEntity = null
        lastBossSpawnTime = waveSpawner.elapsedGameTime

        // Boss kill grants significant drone fuel
        droneManager?.refuelAllDrones(DroneManager.REFUEL_BOSS_KILL)

        val transform = deadEntity.getComponent(TransformComponent::class)
        if (transform != null) {
            // Big death explosion effect
            HitEffectEntity.burst(
                x = transform.x,
                y = transform.y,
                count = 25,
                baseColor = androidx.compose.ui.graphics.Color(0xFFFF4444),
                speed = 300f,
                lifeTime = 0.8f,
                size = 14f
            ).forEach { gameLoop.addEntity(it) }

            // Secondary green burst
            HitEffectEntity.burst(
                x = transform.x,
                y = transform.y,
                count = 15,
                baseColor = androidx.compose.ui.graphics.Color(0xFF44FF88),
                speed = 200f,
                lifeTime = 0.6f,
                size = 10f
            ).forEach { gameLoop.addEntity(it) }

            // Significant reward drops — bosses guarantee health + ammo in a ring
            spawnBossRewards(transform.x, transform.y)
        }

        Log.d("GameViewModel", "Boss defeated: ${bossComp.bossType.displayName}. Total defeated: $bossesDefeated")
    }

    /**
     * Spawns guaranteed reward pickups in a ring around the boss death position.
     * Half health, half ammo — fulfills the "significant rewards" exit criterion.
     */
    private fun spawnBossRewards(x: Float, y: Float) {
        val inventory = playerEntity?.getComponent(WeaponInventoryComponent::class)
        val ammoTypes = listOf(
            WeaponType.ASSAULT_RIFLE,
            WeaponType.SHOTGUN,
            WeaponType.SMG,
            WeaponType.FLAMETHROWER
        )
        val droppableAmmo = inventory?.getUnlockedTypes()?.filter { it in ammoTypes } ?: emptyList()

        for (i in 0 until BOSS_LOOT_DROP_COUNT) {
            val angle = (i.toFloat() / BOSS_LOOT_DROP_COUNT) * 2f * Math.PI.toFloat()
            val dist = 40f + kotlin.random.Random.nextFloat() * 30f
            val dropX = x + kotlin.math.cos(angle) * dist
            val dropY = y + kotlin.math.sin(angle) * dist

            if (i < BOSS_LOOT_DROP_COUNT / 2) {
                // Health pickups (larger heal than normal drops)
                gameLoop.addEntity(HealthPickup.create(x = dropX, y = dropY, healAmount = 20))
            } else if (droppableAmmo.isNotEmpty()) {
                // Ammo pickups — cycle through unlocked ammo types
                val weaponType = droppableAmmo[i % droppableAmmo.size]
                val amount = getBossAmmoAmount(weaponType)
                gameLoop.addEntity(AmmoPickup.create(x = dropX, y = dropY, amount = amount, weaponType = weaponType))
            } else {
                // Fallback to health if no ammo weapons are unlocked
                gameLoop.addEntity(HealthPickup.create(x = dropX, y = dropY, healAmount = 20))
            }
        }
    }

    private fun getBossAmmoAmount(type: WeaponType): Int {
        return when (type) {
            WeaponType.ASSAULT_RIFLE -> 20
            WeaponType.SHOTGUN -> 6
            WeaponType.SMG -> 25
            WeaponType.FLAMETHROWER -> 40
            else -> 15
        }
    }

    private fun updateBossHudState() {
        val boss = activeBossEntity
        if (boss != null && boss.isActive) {
            val health = boss.getComponent(HealthComponent::class)
            val bossComp = boss.getComponent(BossComponent::class)
            if (health != null && bossComp != null) {
                _bossState.value = BossState(
                    isActive = true,
                    name = bossComp.bossType.displayName,
                    currentHealth = health.currentHealth,
                    maxHealth = health.maxHealth,
                    accentColor = bossComp.bossType.accentColor
                )
                return
            }
        }
        // No active boss
        if (_bossState.value.isActive) {
            _bossState.value = BossState()
        }
    }
}
