---
name: Dead Zone Phase 1
overview: Build a complete Android zombie horde survival game with Kotlin and Jetpack Compose. Features include turret defense system, day/night cycle, 7 zombie types, 3 bosses, 6 playable characters, 5 maps, ammo scavenging, survivor rescue missions, level-up system with 20+ upgrades, meta progression with permanent upgrades, and full monetization with AdMob and Google Play Billing.
todos:
  - id: foundation
    content: Setup project with Compose, Hilt, game loop architecture, and ECS foundation
    status: pending
  - id: rendering
    content: Implement Compose Canvas rendering, sprite system, and camera
    status: pending
    dependencies:
      - foundation
  - id: physics
    content: Build collision detection, movement system, and spatial partitioning
    status: pending
    dependencies:
      - rendering
  - id: input
    content: Create touch input handler and virtual joystick component
    status: pending
    dependencies:
      - rendering
  - id: player
    content: Implement player entity with health, stats, and movement
    status: pending
    dependencies:
      - physics
      - input
  - id: weapons
    content: Build weapon system with 6 weapon types and ammo mechanics
    status: pending
    dependencies:
      - player
  - id: combat
    content: Implement auto-aim, projectiles, and damage system
    status: pending
    dependencies:
      - weapons
  - id: enemies
    content: Create 7 zombie types with unique AI behaviors
    status: pending
    dependencies:
      - combat
  - id: spawning
    content: Build wave spawner with difficulty scaling
    status: pending
    dependencies:
      - enemies
  - id: turrets
    content: Implement 4 turret types with placement UI and upgrades
    status: pending
    dependencies:
      - combat
      - spawning
  - id: day-night
    content: Create day/night cycle with lighting and spawn modifiers
    status: pending
    dependencies:
      - spawning
  - id: bosses
    content: Implement 3 boss types with unique attack patterns
    status: pending
    dependencies:
      - day-night
  - id: levelup
    content: Build XP system and level-up screen with 20+ upgrades
    status: pending
    dependencies:
      - combat
  - id: characters
    content: Implement 6 playable character classes with unique passives
    status: pending
    dependencies:
      - player
  - id: maps
    content: Create 5 map environments with unlock system
    status: pending
    dependencies:
      - rendering
  - id: rescue
    content: Add survivor rescue mission system
    status: pending
    dependencies:
      - spawning
  - id: menus
    content: Build main menu, character select, and shop screens
    status: pending
    dependencies:
      - characters
      - maps
  - id: progression
    content: Implement permanent upgrades and supplies economy
    status: pending
    dependencies:
      - menus
  - id: save-system
    content: Create encrypted save/load system for progress
    status: pending
    dependencies:
      - progression
  - id: audio
    content: Add sound effects and background music
    status: pending
    dependencies:
      - combat
  - id: admob
    content: Integrate AdMob with rewarded and interstitial ads
    status: pending
    dependencies:
      - menus
  - id: iap
    content: Implement Google Play Billing for in-app purchases
    status: pending
    dependencies:
      - admob
  - id: optimization
    content: Profile and optimize for 60 FPS with 100 enemies
    status: pending
    dependencies:
      - bosses
      - turrets
  - id: polish
    content: Bug fixes, balance tuning, and final UI polish
    status: pending
    dependencies:
      - optimization
      - audio
      - iap
  - id: launch
    content: Closed beta, Play Store assets, and release build
    status: pending
    dependencies:
      - polish
---

# Dead Zone: Endless Onslaught - Phase 1 Implementation Plan

> **Overview**: Build a complete Android zombie horde survival game with Kotlin and Jetpack Compose. Features include turret defense system, day/night cycle, 7 zombie types, 3 bosses, 6 playable characters, 5 maps, ammo scavenging, survivor rescue missions, level-up system with 20+ upgrades, meta progression with permanent upgrades, and full monetization with AdMob and Google Play Billing.

> **Asset Note**: Agents can take 2D image assets from the `Legacy Collection` directory.

## ✅ Project Status & Todos

### 🏗 Phase A: Core Engine
- [x] **A1: Foundation Setup** <!-- id: foundation -->
- [x] **A2: Rendering System** <!-- id: rendering -->
- [x] **A3: Physics & Collision** <!-- id: physics -->
- [x] **A4: Input System** <!-- id: input -->

### 🎮 Phase B: Player & Combat
- [ ] **B1: Player Entity** <!-- id: player -->
- [ ] **B2: Weapon System** <!-- id: weapons -->
- [ ] **B3: Combat Mechanics** <!-- id: combat -->

### 🧟 Phase C: Enemies & Waves
- [ ] **C1: Zombie Types** <!-- id: enemies -->
- [ ] **C2: Wave Spawning** <!-- id: spawning -->
- [ ] **C3: Day/Night Cycle** <!-- id: day-night -->
- [ ] **C4: Boss Battles** <!-- id: bosses -->

### 🔧 Phase D: Turret System
- [ ] **D1: Turret Defense** <!-- id: turrets -->

### ⬆️ Phase E: Progression
- [ ] **E1: Level-Up System** <!-- id: levelup -->
- [ ] **E2: Character Classes** <!-- id: characters -->
- [ ] **E3: Map Environments** <!-- id: maps -->
- [ ] **E4: Rescue Missions** <!-- id: rescue -->

### 📱 Phase F: Meta & UI
- [ ] **F1: Menu Screens** <!-- id: menus -->
- [ ] **F2: Permanent Progression** <!-- id: progression -->
- [ ] **F3: Save System** <!-- id: save-system -->
- [ ] **F4: Audio System** <!-- id: audio -->

### 💰 Phase G: Monetization
- [ ] **G1: AdMob Integration** <!-- id: admob -->
- [ ] **G2: In-App Purchases** <!-- id: iap -->

### 🚀 Phase H: Launch
- [ ] **H1: Optimization** <!-- id: optimization -->
- [ ] **H2: Polish & Balance** <!-- id: polish -->
- [ ] **H3: Release** <!-- id: launch -->

---

## 🏗 System Architecture

### 1. High-Level Architecture (Game Engine + Compose)
```
┌─────────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI Layer                  │
│         (Menus, HUD, Shop, Dialogs, Level-up Screen)        │
├─────────────────────────────────────────────────────────────┤
│                  Custom Game Engine Layer                   │
│          (Game Loop, ECS, Collision, AI, Spawning)          │
├─────────────────────────────────────────────────────────────┤
│                     Compose Canvas                          │
│         (Sprite rendering, Effects, Animations)             │
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                             │
│        (Room DB, DataStore, AdMob, Play Billing)            │
└─────────────────────────────────────────────────────────────┘
```

### 2. Entity-Component-System Architecture
```
Entity (ID only)
    ├── PositionComponent (x, y, rotation)
    ├── VelocityComponent (vx, vy)
    ├── SpriteComponent (texture, animation)
    ├── HealthComponent (current, max)
    ├── ColliderComponent (radius, type)
    ├── WeaponComponent (damage, fireRate, ammo)
    ├── AIComponent (state, target, behavior)
    └── TurretComponent (range, targeting)

Systems (process entities with matching components)
    ├── MovementSystem
    ├── CollisionSystem
    ├── RenderSystem
    ├── CombatSystem
    ├── AISystem
    ├── SpawningSystem
    └── TurretSystem
```

### 3. Project File Structure
```
com.nightmare.deadzone/
├── di/                          # Hilt dependency injection
│   ├── AppModule.kt
│   ├── GameModule.kt
│   └── DatabaseModule.kt
├── engine/                      # Custom game engine
│   ├── core/
│   │   ├── GameLoop.kt
│   │   ├── Entity.kt
│   │   ├── Component.kt
│   │   ├── System.kt
│   │   └── ObjectPool.kt
│   ├── physics/
│   │   ├── CollisionSystem.kt
│   │   ├── MovementSystem.kt
│   │   ├── SpatialHashGrid.kt
│   │   └── Collider.kt
│   ├── rendering/
│   │   ├── SpriteRenderer.kt
│   │   ├── AnimationController.kt
│   │   ├── Camera.kt
│   │   └── ParticleSystem.kt
│   ├── input/
│   │   ├── InputManager.kt
│   │   └── VirtualJoystick.kt
│   └── audio/
│       ├── SoundManager.kt
│       └── MusicManager.kt
├── game/                        # Game-specific logic
│   ├── entities/
│   │   ├── PlayerEntity.kt
│   │   ├── ZombieEntity.kt
│   │   ├── TurretEntity.kt
│   │   ├── BossEntity.kt
│   │   └── ProjectileEntity.kt
│   ├── systems/
│   │   ├── CombatSystem.kt
│   │   ├── AISystem.kt
│   │   ├── WaveSpawner.kt
│   │   ├── LootSystem.kt
│   │   └── DayNightCycle.kt
│   ├── weapons/
│   │   ├── WeaponSystem.kt
│   │   ├── PistolWeapon.kt
│   │   ├── ShotgunWeapon.kt
│   │   └── ...
│   └── data/
│       ├── GameConfig.kt
│       ├── ZombieStats.kt
│       ├── WeaponStats.kt
│       └── UpgradeData.kt
├── ui/                          # Jetpack Compose screens
│   ├── screens/
│   │   ├── MainMenuScreen.kt
│   │   ├── GameScreen.kt
│   │   ├── CharacterSelectScreen.kt
│   │   ├── ShopScreen.kt
│   │   ├── LevelUpScreen.kt
│   │   └── SettingsScreen.kt
│   ├── components/
│   │   ├── HUD.kt
│   │   ├── HealthBar.kt
│   │   ├── TurretPlacementMenu.kt
│   │   └── BossHealthBar.kt
│   └── theme/
│       ├── Theme.kt
│       ├── Color.kt
│       └── Typography.kt
├── data/                        # Persistence and services
│   ├── local/
│   │   ├── SaveManager.kt
│   │   ├── PreferencesManager.kt
│   │   └── GameDatabase.kt
│   ├── ads/
│   │   ├── AdManager.kt
│   │   └── RewardedAdHelper.kt
│   └── billing/
│       ├── BillingManager.kt
│       └── IAPProducts.kt
└── MainActivity.kt
```

---

## 🚀 Detailed Implementation Roadmap

---

## Phase A: Core Engine

### A1: Foundation Setup <!-- id: foundation -->
> **Goal**: Set up project with modern Android architecture and game engine foundation.

**Duration**: 1 Week

**Files to create:**
| File | Description |
|------|-------------|
| `build.gradle.kts` (app) | Add Compose, Hilt, Room, Coroutines |
| `di/AppModule.kt` | Hilt application module |
| `di/GameModule.kt` | Game engine dependencies |
| `engine/core/GameLoop.kt` | Fixed timestep game loop |
| `engine/core/Entity.kt` | Base entity class with ID |
| `engine/core/Component.kt` | Component interface |
| `engine/core/System.kt` | System base class |
| `engine/core/ObjectPool.kt` | Generic object pool |
| `engine/core/GameState.kt` | Game state management |

**Key Dependencies:**
```kotlin
// Core
implementation("androidx.core:core-ktx:1.12.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

// Compose
implementation(platform("androidx.compose:compose-bom:2024.01.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.8.2")

// Hilt
implementation("com.google.dagger:hilt-android:2.50")
ksp("com.google.dagger:hilt-compiler:2.50")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

// Room
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
ksp("androidx.room:room-compiler:2.6.1")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")

// Serialization
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
```

**Exit Criteria:**
- [ ] Project builds and runs on emulator
- [ ] Hilt injection working
- [ ] Game loop maintains 60 FPS timing
- [ ] Basic ECS creates/destroys entities

---

### A2: Rendering System <!-- id: rendering -->
> **Goal**: Implement Compose Canvas rendering with sprites and animations.

**Duration**: 1 Week

**Files to create:**
| File | Description |
|------|-------------|
| `ui/screens/GameScreen.kt` | Main game Compose screen |
| `engine/rendering/GameSurface.kt` | Canvas-based game surface |
| `engine/rendering/SpriteRenderer.kt` | Sprite drawing system |
| `engine/rendering/AnimationController.kt` | Frame-based animation |
| `engine/rendering/Camera.kt` | Follow camera with bounds |
| `engine/rendering/SpriteSheet.kt` | Sprite sheet parsing |
| `game/data/AssetManager.kt` | Texture loading and caching |

**Implementation Details:**
```kotlin
// GameSurface using Compose Canvas
@Composable
fun GameSurface(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Apply camera transform
        withTransform({
            translate(-camera.x, -camera.y)
            scale(camera.zoom, camera.zoom)
        }) {
            // Render all sprites
            renderSystem.render(this, gameState.entities)
        }
    }
}
```

**Exit Criteria:**
- [ ] Sprites render on screen correctly
- [ ] Sprite sheet animations play smoothly
- [ ] Camera follows player with smooth lerp
- [ ] Layer ordering works (background → entities → effects)

---

### A3: Physics & Collision <!-- id: physics -->
> **Goal**: Build collision detection and movement system.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `engine/physics/CollisionSystem.kt` | Collision detection loop |
| `engine/physics/SpatialHashGrid.kt` | Spatial partitioning |
| `engine/physics/MovementSystem.kt` | Velocity-based movement |
| `engine/physics/Collider.kt` | Circle/AABB colliders |
| `engine/core/components/ColliderComponent.kt` | Collider component |
| `engine/core/components/VelocityComponent.kt` | Velocity component |

**Spatial Hash Grid:**
```kotlin
class SpatialHashGrid(
    private val cellSize: Float = 100f
) {
    private val cells = mutableMapOf<Long, MutableList<Entity>>()
    
    fun insert(entity: Entity, position: Vector2)
    fun query(bounds: Rect): List<Entity>
    fun clear()
}
```

**Exit Criteria:**
- [ ] Circle-circle collision detection works
- [ ] Spatial grid reduces collision checks by 80%+
- [ ] Movement uses delta time correctly
- [ ] 100+ entities maintain 60 FPS

---

### A4: Input System <!-- id: input -->
> **Goal**: Create touch input handling and virtual joystick.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `engine/input/InputManager.kt` | Touch event processing |
| `engine/input/VirtualJoystick.kt` | On-screen joystick |
| `engine/input/GestureHandler.kt` | Tap, double-tap detection |

**Virtual Joystick Design:**
```kotlin
@Composable
fun VirtualJoystick(
    onDirectionChange: (Vector2) -> Unit,
    modifier: Modifier = Modifier
) {
    // Fixed position in bottom-left
    // Inner knob follows touch within radius
    // Returns normalized direction vector
}
```

**Exit Criteria:**
- [ ] Joystick appears on touch
- [ ] Movement direction is smooth and responsive
- [ ] Double-tap detected for turret menu
- [ ] No input lag perceptible

---

## Phase B: Player & Combat

### B1: Player Entity <!-- id: player -->
> **Goal**: Implement the player character with stats and movement.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/entities/PlayerEntity.kt` | Player entity factory |
| `engine/core/components/HealthComponent.kt` | Health tracking |
| `engine/core/components/StatsComponent.kt` | Player stats |
| `game/systems/PlayerSystem.kt` | Player-specific logic |
| `ui/components/HealthBar.kt` | Player health UI |

**Player Stats:**
```kotlin
data class PlayerStats(
    val maxHealth: Int = 100,
    val moveSpeed: Float = 100f,
    val armor: Int = 0,
    val damageMultiplier: Float = 1f,
    val fireRateMultiplier: Float = 1f
)
```

**Exit Criteria:**
- [ ] Player moves with joystick
- [ ] Health displays and decreases on damage
- [ ] Player death triggers game over
- [ ] Stats affect gameplay

---

### B2: Weapon System <!-- id: weapons -->
> **Goal**: Build the weapon architecture with 6 weapon types.

**Duration**: 1 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/weapons/WeaponSystem.kt` | Weapon manager |
| `game/weapons/Weapon.kt` | Base weapon class |
| `game/weapons/PistolWeapon.kt` | Infinite ammo fallback |
| `game/weapons/AssaultRifleWeapon.kt` | Auto-fire rifle |
| `game/weapons/ShotgunWeapon.kt` | Spread shot |
| `game/weapons/SMGWeapon.kt` | High fire rate |
| `game/weapons/FlamethrowerWeapon.kt` | DoT cone |
| `game/weapons/MeleeWeapon.kt` | Close range |
| `game/systems/AmmoSystem.kt` | Ammo management |
| `game/entities/AmmoPickup.kt` | Ammo crate entity |

**Weapon Stats Reference:**
| Weapon | DMG | Fire Rate | Range | Clip | Ammo? |
|--------|-----|-----------|-------|------|-------|
| Pistol | 10 | 2/sec | 200 | ∞ | No |
| Assault Rifle | 12 | 5/sec | 250 | 30 | Yes |
| Shotgun | 8×6 | 1/sec | 150 | 8 | Yes |
| SMG | 8 | 8/sec | 180 | 40 | Yes |
| Flamethrower | 5/tick | 10/sec | 100 | 100 | Yes |
| Dual Pistols | 8×2 | 4/sec | 200 | ∞ | No |

**Exit Criteria:**
- [ ] 6 weapon types implemented
- [ ] Weapons fire automatically
- [ ] Ammo consumption works
- [ ] Ammo pickups restore ammo

---

### B3: Combat Mechanics <!-- id: combat -->
> **Goal**: Implement auto-aim, projectiles, and damage.

**Duration**: 1 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/systems/CombatSystem.kt` | Damage resolution |
| `game/systems/AutoAimSystem.kt` | Target acquisition |
| `game/systems/ProjectileSystem.kt` | Bullet movement |
| `game/entities/ProjectileEntity.kt` | Bullet entity |
| `ui/components/DamagePopup.kt` | Floating numbers |
| `engine/rendering/ParticleSystem.kt` | Hit effects |

**Auto-Aim Logic:**
```kotlin
fun findNearestTarget(
    position: Vector2,
    range: Float,
    enemies: List<Entity>
): Entity? {
    return enemies
        .filter { it.position.distanceTo(position) <= range }
        .minByOrNull { it.position.distanceTo(position) }
}
```

**Exit Criteria:**
- [ ] Auto-aim locks to nearest enemy
- [ ] Projectiles travel and hit enemies
- [ ] Damage numbers float up
- [ ] Hit effects display

---

## Phase C: Enemies & Waves

### C1: Zombie Types <!-- id: enemies -->
> **Goal**: Create 7 zombie types with unique behaviors.

**Duration**: 1.5 Weeks

**Files to create:**
| File | Description |
|------|-------------|
| `game/entities/ZombieEntity.kt` | Base zombie factory |
| `game/entities/zombies/WalkerZombie.kt` | Basic slow zombie |
| `game/entities/zombies/RunnerZombie.kt` | Fast, fragile |
| `game/entities/zombies/BloaterZombie.kt` | Explodes on death |
| `game/entities/zombies/SpitterZombie.kt` | Ranged attack |
| `game/entities/zombies/BruteZombie.kt` | Tanky charger |
| `game/entities/zombies/CrawlerZombie.kt` | Low profile |
| `game/entities/zombies/ScreamerZombie.kt` | Buffs allies |
| `game/systems/AISystem.kt` | Behavior state machine |
| `game/systems/SteeringSystem.kt` | Movement behaviors |

**Zombie Stats Reference:**
| Zombie | HP | DMG | Speed | XP | Behavior |
|--------|----|----|-------|-----|----------|
| Walker | 15 | 5 | 40 | 1 | Direct chase |
| Runner | 10 | 8 | 90 | 2 | Fast chase |
| Bloater | 50 | 15 | 30 | 5 | Explodes on death |
| Spitter | 20 | 10 | 35 | 4 | Ranged acid |
| Brute | 100 | 25 | 50 | 10 | Charges |
| Crawler | 8 | 12 | 80 | 2 | Low profile |
| Screamer | 30 | 5 | 45 | 8 | Buffs nearby |

**Exit Criteria:**
- [ ] All 7 zombie types spawn correctly
- [ ] Each has unique behavior
- [ ] Bloater explosion works
- [ ] Screamer buff applies to nearby zombies

---

### C2: Wave Spawning <!-- id: spawning -->
> **Goal**: Build progressive wave system with scaling difficulty.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/systems/WaveSpawner.kt` | Wave management |
| `game/systems/DifficultyManager.kt` | Scaling logic |
| `game/systems/LootSystem.kt` | XP and drops |
| `game/data/WaveConfig.kt` | Wave definitions |

**Wave Progression:**
| Time | HP Mult | Spawn Rate | Max Enemies | New Types |
|------|---------|------------|-------------|-----------|
| 0-1 min | ×1.0 | 1/sec | 15 | Walker |
| 1-3 min | ×1.2 | 1.5/sec | 25 | +Runner |
| 3-5 min | ×1.4 | 2/sec | 35 | +Bloater, Spitter |
| 5-10 min | ×1.8 | 3/sec | 50 | +Brute, Crawler |
| 10-15 min | ×2.5 | 4/sec | 75 | +Screamer |
| 15+ min | ×3.5 | 5/sec | 100 | All + elites |

**Exit Criteria:**
- [ ] Waves spawn at correct rates
- [ ] New zombie types unlock over time
- [ ] Difficulty scales properly
- [ ] Loot drops on kills

---

### C3: Day/Night Cycle <!-- id: day-night -->
> **Goal**: Implement time cycle affecting gameplay.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/systems/DayNightCycle.kt` | Time tracking |
| `engine/rendering/LightingSystem.kt` | Screen tinting |
| `ui/components/TimeIndicator.kt` | Day/night UI |

**Cycle Timing:**
| Phase | Duration | Zombie Behavior |
|-------|----------|-----------------|
| Day | 2 minutes | Slow, fewer spawns |
| Night | 1 minute | Fast, +50% spawns, +25% DMG |

**Exit Criteria:**
- [ ] Screen tints based on time
- [ ] Night zombies are faster/stronger
- [ ] Spawn rates change correctly
- [ ] UI shows current phase

---

### C4: Boss Battles <!-- id: bosses -->
> **Goal**: Implement 3 boss types with unique attacks.

**Duration**: 1 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/entities/BossEntity.kt` | Base boss class |
| `game/entities/bosses/TankBoss.kt` | Ground slam, charge |
| `game/entities/bosses/HiveQueenBoss.kt` | Spawns minions |
| `game/entities/bosses/AbominationBoss.kt` | Multi-arm, regen |
| `game/systems/BossSystem.kt` | Boss AI controller |
| `ui/components/BossHealthBar.kt` | Large HP display |

**Boss Stats:**
| Boss | Base HP | DMG | Special Attacks |
|------|---------|-----|-----------------|
| The Tank | 800 | 40 | Ground slam, Rock throw, Charge |
| Hive Queen | 600 | 25 | Spawn minions, Acid spray, Burrow |
| Abomination | 1200 | 50 | Multi-arm swipe, Regen, Enrage |

**HP Scaling:** `Base × (1 + 0.5 × BossNumber)`

**Exit Criteria:**
- [ ] Boss spawns every 5 minutes
- [ ] Each boss has 3 unique attacks
- [ ] Boss health bar displays
- [ ] Boss drops significant rewards

---

## Phase D: Turret System

### D1: Turret Defense <!-- id: turrets -->
> **Goal**: Implement 4 turret types with placement and upgrades.

**Duration**: 1.5 Weeks

**Files to create:**
| File | Description |
|------|-------------|
| `game/entities/TurretEntity.kt` | Base turret |
| `game/entities/turrets/MachineGunTurret.kt` | High fire rate |
| `game/entities/turrets/ShotgunTurret.kt` | Cone spread |
| `game/entities/turrets/FlamethrowerTurret.kt` | DoT |
| `game/entities/turrets/TeslaCoilTurret.kt` | Chain lightning |
| `game/systems/TurretSystem.kt` | Turret AI |
| `game/systems/ScrapSystem.kt` | Resource management |
| `ui/components/TurretPlacementMenu.kt` | Placement UI |
| `ui/components/TurretUpgradePanel.kt` | Upgrade UI |

**Turret Stats:**
| Turret | Cost | Damage | Special |
|--------|------|--------|---------|
| Machine Gun | 50 | Low, Fast | High fire rate |
| Shotgun | 75 | High, Slow | Cone spread |
| Flamethrower | 100 | DoT | Burn effect |
| Tesla Coil | 125 | Medium | Chain lightning |

**Exit Criteria:**
- [ ] Double-tap opens placement menu
- [ ] 4 turret types work correctly
- [ ] Turrets auto-target enemies
- [ ] 2 upgrade levels each
- [ ] Max 4 turrets enforced

---

## Phase E: Progression

### E1: Level-Up System <!-- id: levelup -->
> **Goal**: Build XP progression and upgrade selection.

**Duration**: 1 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/systems/XPSystem.kt` | Experience tracking |
| `game/systems/LevelUpSystem.kt` | Level progression |
| `game/data/UpgradePool.kt` | Available upgrades |
| `game/data/Upgrades.kt` | Upgrade definitions |
| `ui/screens/LevelUpScreen.kt` | Upgrade selection |
| `game/systems/WeaponEvolution.kt` | Max level transforms |

**XP Formula:** `XP = 10 + (Level × 8) + (Level^1.4)`

**Sample Upgrades:**
- Damage +10%
- Fire Rate +15%
- Max HP +20
- Move Speed +10%
- Armor +5
- Ammo Capacity +25%
- XP Magnet Range +50%
- Turret Damage +20%

**Exit Criteria:**
- [ ] XP collection works
- [ ] Level-up pauses game
- [ ] 3 random upgrades shown
- [ ] Upgrades apply correctly
- [ ] Weapon evolutions trigger

---

### E2: Character Classes <!-- id: characters -->
> **Goal**: Implement 6 playable characters with unique passives.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/data/Characters.kt` | Character definitions |
| `game/entities/CharacterFactory.kt` | Character creation |

**Character Stats:**
| Character | HP | Speed | Starting Weapon | Passive |
|-----------|-----|-------|-----------------|---------|
| Rookie | 100 | 100 | Pistol | Balanced |
| Soldier | 120 | 90 | Assault Rifle | +20% Ammo |
| Mechanic | 80 | 100 | Wrench | -25% Turret Cost |
| Medic | 90 | 110 | SMG | Heal 1 HP/10 kills |
| Pyro | 85 | 95 | Flamethrower | Fire immune |
| Commando | 70 | 120 | Dual Pistols | +50% Fire Rate |

**Unlock Requirements:**
- Soldier: 500 supplies
- Mechanic: 1000 supplies
- Medic: 1500 supplies
- Pyro: 2000 supplies
- Commando: Rescue 50 survivors

**Exit Criteria:**
- [ ] All 6 characters playable
- [ ] Passives apply correctly
- [ ] Starting weapons differ
- [ ] Unlock system works

---

### E3: Map Environments <!-- id: maps -->
> **Goal**: Create 5 unique map environments.

**Duration**: 1 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/maps/MapSystem.kt` | Map loading |
| `game/maps/MapData.kt` | Map definitions |
| `game/maps/Suburbs.kt` | Default map |
| `game/maps/Mall.kt` | Shopping center |
| `game/maps/Hospital.kt` | Dark corridors |
| `game/maps/MilitaryBase.kt` | Open compound |
| `game/maps/Lab.kt` | Sci-fi bunker |

**Map Unlocks:**
| Map | Unlock Cost | Special Feature |
|-----|-------------|-----------------|
| Suburbs | Default | Cars as cover |
| Mall | 500 supplies | Ammo rooms |
| Hospital | 1000 supplies | Health spawns |
| Military Base | 1500 supplies | Pre-placed turrets |
| Lab | Kill 5 bosses | Laser traps |

**Exit Criteria:**
- [ ] 5 maps with unique visuals
- [ ] Map-specific features work
- [ ] Unlock system functions
- [ ] Collision with obstacles

---

### E4: Rescue Missions <!-- id: rescue -->
> **Goal**: Add survivor rescue system.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/systems/SurvivorRescue.kt` | Rescue logic |
| `game/entities/SurvivorEntity.kt` | Survivor NPC |
| `ui/components/RescueIndicator.kt` | UI marker |

**Mechanics:**
- Survivors spawn every 2-3 minutes at map edge
- Get near to make them follow
- Lead to evac point for 25 supplies
- They die in 2 hits - protect them!

**Exit Criteria:**
- [ ] Survivors spawn at edge
- [ ] Follow player when touched
- [ ] Can be killed by zombies
- [ ] Evac point grants rewards

---

## Phase F: Meta & UI

### F1: Menu Screens <!-- id: menus -->
> **Goal**: Build all menu screens with Compose.

**Duration**: 1 Week

**Files to create:**
| File | Description |
|------|-------------|
| `ui/screens/MainMenuScreen.kt` | Start screen |
| `ui/screens/CharacterSelectScreen.kt` | Character picker |
| `ui/screens/MapSelectScreen.kt` | Map picker |
| `ui/screens/ShopScreen.kt` | Supplies shop |
| `ui/screens/SettingsScreen.kt` | Options |
| `ui/screens/GameOverScreen.kt` | End run screen |
| `ui/navigation/NavGraph.kt` | Navigation setup |

**Exit Criteria:**
- [ ] All screens navigate correctly
- [ ] Character/map selection works
- [ ] Shop displays all upgrades
- [ ] Settings persist

---

### F2: Permanent Progression <!-- id: progression -->
> **Goal**: Implement supplies shop and permanent upgrades.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `game/data/PermanentUpgrades.kt` | Upgrade definitions |
| `game/systems/SuppliesManager.kt` | Currency management |

**Permanent Upgrades:**
| Upgrade | Max Lv | Cost/Lv | Effect |
|---------|--------|---------|--------|
| Toughness | 10 | 100 | +10 Max HP |
| Firepower | 10 | 150 | +5% Damage |
| Mobility | 5 | 200 | +5% Speed |
| Scavenger | 10 | 120 | +10% Drops |
| Engineer | 5 | 250 | -10% Turret Cost |
| Ammo Belt | 10 | 100 | +10% Ammo |
| Second Wind | 3 | 500 | +1 Revive |

**Exit Criteria:**
- [ ] Supplies earned per run
- [ ] Upgrades purchasable
- [ ] Upgrades apply to gameplay
- [ ] Progress persists

---

### F3: Save System <!-- id: save-system -->
> **Goal**: Create encrypted save/load for progress.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `data/local/SaveManager.kt` | Save/load logic |
| `data/local/SaveData.kt` | Save data model |
| `data/local/Encryption.kt` | AES encryption |

**Saved Data:**
- Supplies currency
- Permanent upgrade levels
- Character unlocks
- Map unlocks
- Statistics (total kills, time, etc.)
- Achievements progress

**Exit Criteria:**
- [ ] Progress saves on app close
- [ ] Progress loads on app start
- [ ] Data is encrypted
- [ ] Corruption handling works

---

### F4: Audio System <!-- id: audio -->
> **Goal**: Add sound effects and music.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `engine/audio/SoundManager.kt` | SFX playback |
| `engine/audio/MusicManager.kt` | BGM playback |
| `engine/audio/AudioPool.kt` | Sound pooling |

**Sound Effects:**
- Weapon fire (per weapon type)
- Zombie death
- Player damage
- Pickup collection
- Level up
- Boss spawn
- Turret fire

**Exit Criteria:**
- [ ] All weapon sounds play
- [ ] Background music loops
- [ ] Volume settings work
- [ ] No audio lag/overlap issues

---

## Phase G: Monetization

### G1: AdMob Integration <!-- id: admob -->
> **Goal**: Implement rewarded and interstitial ads.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `data/ads/AdManager.kt` | AdMob initialization |
| `data/ads/RewardedAdHelper.kt` | Rewarded video |
| `data/ads/InterstitialAdHelper.kt` | Interstitial |
| `data/ads/AdFrequencyManager.kt` | Fatigue rules |

**Ad Placements:**
| Placement | Type | Reward | Limit |
|-----------|------|--------|-------|
| Revive | Rewarded | Continue 50% HP | 1/run |
| Double Supplies | Rewarded | 2× end supplies | 1/run |
| Daily Crate | Rewarded | Random reward | 1/day |
| Free Turret | Rewarded | Start with turret | 2/day |
| Between Runs | Interstitial | - | 1 per 3 runs |

**Anti-Fatigue Rules:**
- No ads on first 3 runs
- No interstitial after rewarded
- Max 5 ads per session
- If ad fails, grant reward anyway

**Exit Criteria:**
- [ ] Rewarded ads grant rewards
- [ ] Interstitials respect limits
- [ ] Frequency rules enforced
- [ ] Fallback on ad failure

---

### G2: In-App Purchases <!-- id: iap -->
> **Goal**: Implement Google Play Billing.

**Duration**: 0.5 Week

**Files to create:**
| File | Description |
|------|-------------|
| `data/billing/BillingManager.kt` | Billing client |
| `data/billing/IAPProducts.kt` | Product definitions |
| `data/billing/PurchaseHandler.kt` | Purchase flow |

**Products:**
| Product | Price | Contents |
|---------|-------|----------|
| Remove Ads | $2.99 | No interstitials |
| Starter Pack | $4.99 | 1000 supplies + Soldier + No ads |
| Supplies S | $0.99 | 500 supplies |
| Supplies M | $2.99 | 2000 supplies |
| Supplies L | $4.99 | 5000 supplies |
| Ultimate | $9.99 | All characters + No ads + 3000 |

**Exit Criteria:**
- [ ] All 6 products purchasable
- [ ] Remove Ads persists
- [ ] Supplies add correctly
- [ ] Purchase restoration works

---

## Phase H: Launch

### H1: Optimization <!-- id: optimization -->
> **Goal**: Achieve 60 FPS with 100 enemies on mid-range devices.

**Duration**: 1 Week

**Tasks:**
| Task | Description |
|------|-------------|
| Profiling | Identify rendering/update bottlenecks |
| Object Pooling | Pool all frequent entities |
| Draw Call Batching | Batch sprite rendering |
| Spatial Grid Tuning | Optimize cell size |
| Quality Settings | 30/60 FPS toggle |
| Memory Optimization | Reduce allocations |

**Target Devices:**
- **High-end**: 60 FPS, 100 enemies, full effects
- **Mid-range**: 60 FPS, 75 enemies, reduced particles
- **Low-end**: 30 FPS, 50 enemies, minimal effects

**Exit Criteria:**
- [ ] 60 FPS on mid-range devices
- [ ] 30 FPS on low-end devices
- [ ] No memory leaks
- [ ] < 100 MB APK size

---

### H2: Polish & Balance <!-- id: polish -->
> **Goal**: Bug fixes, balance tuning, and UI polish.

**Duration**: 1 Week

**Tasks:**
- Fix all critical bugs from testing
- Tune weapon damage values
- Tune zombie HP/spawn rates
- Tune economy (supplies, XP)
- Add UI animations
- Add screen transitions
- Add haptic feedback
- Final UI polish pass

**Exit Criteria:**
- [ ] No critical bugs
- [ ] Gameplay feels balanced
- [ ] UI is polished
- [ ] All features complete

---

### H3: Release <!-- id: launch -->
> **Goal**: Closed beta, store assets, and release.

**Duration**: 1 Week

**Tasks:**
| Task | Description |
|------|-------------|
| Firebase | Analytics and Crashlytics setup |
| Closed Beta | Limited release testing |
| Store Assets | Screenshots, description, video |
| Privacy Policy | Create and host |
| Release Build | Signed APK/AAB |
| Store Submission | Play Store listing |

**Exit Criteria:**
- [ ] Firebase tracking working
- [ ] Beta feedback addressed
- [ ] Store listing approved
- [ ] v1.0 released

---

## 📊 Timeline Summary

```
Week 1      ████████████  Phase A1: Foundation
Week 2      ████████████  Phase A2: Rendering
Week 3      ██████        Phase A3: Physics
            ██████        Phase A4: Input
Week 4      ██████        Phase B1: Player
            ██████        Phase B2: Weapons (start)
Week 5      ████████████  Phase B2: Weapons + B3: Combat
Week 6      ████████████  Phase C1: Enemies
Week 7      ██████        Phase C1: Enemies (finish)
            ██████        Phase C2: Spawning + C3: Day/Night
Week 8      ████████████  Phase C4: Bosses
Week 9      ████████████  Phase D1: Turrets
Week 10     ██████        Phase D1: Turrets (finish)
            ██████        Phase E1: Level-Up (start)
Week 11     ██████        Phase E1: Level-Up (finish)
            ██████        Phase E2: Characters + E3: Maps (start)
Week 12     ████████████  Phase E3: Maps + E4: Rescue + F1: Menus
Week 13     ████████████  Phase F2-F4: Progression, Save, Audio
Week 14     ████████████  Phase G1-G2: Monetization
Week 15     ████████████  Phase H1: Optimization
Week 16     ████████████  Phase H2-H3: Polish & Launch
```

**Total Duration: 16 Weeks**

---

## 🎯 Success Metrics

| Metric | Target |
|--------|--------|
| Frame Rate | 60 FPS (mid-range) |
| Load Time | < 3 seconds |
| APK Size | < 100 MB |
| Crash Rate | < 0.5% |
| Session Length | > 5 minutes avg |
| Day 1 Retention | > 40% |
| Day 7 Retention | > 15% |

---

## ⚠️ Risk Mitigation

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| 100 enemies lag | High | High | Object pooling, spatial hash, batching |
| Pathfinding lag | Medium | Medium | Use steering behaviors only |
| Touch input delay | Low | High | Process input first in frame |
| Memory pressure | Medium | High | Aggressive pooling, texture atlas |
| Compose Canvas limits | Medium | High | Fallback to SurfaceView |

---

*Document Version: 1.1*  
*Last Updated: February 2026*  
*Project: Dead Zone - Kotlin + Jetpack Compose*
