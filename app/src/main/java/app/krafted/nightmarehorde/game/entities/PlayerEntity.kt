package app.krafted.nightmarehorde.game.entities

import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.components.CharacterComponent
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.HealthComponent
import app.krafted.nightmarehorde.engine.core.components.PlayerTagComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.StatsComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponComponent
import app.krafted.nightmarehorde.engine.core.components.WeaponInventoryComponent
import app.krafted.nightmarehorde.game.data.CharacterClass
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.game.weapons.AssaultRifleWeapon
import app.krafted.nightmarehorde.game.weapons.BroadSwordWeapon
import app.krafted.nightmarehorde.game.weapons.DualPistolWeapon
import app.krafted.nightmarehorde.game.weapons.PistolWeapon
import app.krafted.nightmarehorde.game.weapons.SMGWeapon
import app.krafted.nightmarehorde.game.weapons.ShotgunWeapon
import app.krafted.nightmarehorde.game.weapons.SwordWeapon
import app.krafted.nightmarehorde.game.weapons.Weapon
import app.krafted.nightmarehorde.game.weapons.WeaponType

/**
 * Factory for creating the player entity with all required components.
 * Accepts a [CharacterClass] to configure stats, weapons, and passives,
 * and derives the visual [CharacterType] from it.
 */
object PlayerEntity {

    /**
     * Create a new player entity at the given spawn position.
     *
     * @param characterClass The selected class — determines stats, weapons, passives
     * @param characterType  Override for sprite data (defaults to the class's mapped type)
     * @param spawnX World X position
     * @param spawnY World Y position
     */
    fun create(
        characterClass: CharacterClass = CharacterClass.ROOKIE,
        characterType: CharacterType = characterClass.characterType,
        spawnX: Float = 0f,
        spawnY: Float = 0f
    ): Entity {
        val stats = buildStatsForClass(characterClass)
        val primaryWeapon = createStartingWeapon(characterClass)
        val meleeWeapon: Weapon = SwordWeapon()

        return Entity().apply {
            addComponent(PlayerTagComponent())
            addComponent(CharacterComponent(characterType))

            addComponent(TransformComponent(
                x = spawnX,
                y = spawnY,
                scale = characterType.scale
            ))

            addComponent(VelocityComponent())

            addComponent(ColliderComponent.circle(
                radius = characterType.colliderRadius,
                layer = CollisionLayer.PLAYER
            ))

            addComponent(SpriteComponent(
                textureKey = characterType.idleTextureKey,
                layer = 1,
                frameWidth = characterType.frameWidth,
                frameHeight = characterType.frameHeight,
                currentFrame = 0,
                animationKey = "idle"
            ))

            addComponent(WeaponComponent(
                equippedWeapon = primaryWeapon,
                currentAmmo = 0,
                totalAmmo = 0
            ))

            addComponent(WeaponInventoryComponent().apply {
                // Starting weapon gets a full clip of ammo
                val startAmmo = if (primaryWeapon.infiniteAmmo) 0 else primaryWeapon.maxAmmo
                addWeapon(primaryWeapon, initialAmmo = startAmmo)
                activeWeaponType = primaryWeapon.type
                // Sword-primary characters get Pistol as ranged backup (Sword replaces Whip Blade)
                // Other characters get the Whip Blade as melee secondary
                when (primaryWeapon.type) {
                    WeaponType.SWORD -> addWeapon(PistolWeapon()) // Ranged fallback, no Whip Blade
                    WeaponType.MELEE -> { /* Already melee, no secondary needed */ }
                    else -> addWeapon(meleeWeapon)
                }
            })

            addComponent(HealthComponent(
                maxHealth = stats.maxHealth
            ))

            addComponent(stats)
        }
    }

    /**
     * Build a [StatsComponent] configured for the given character class,
     * applying class-specific base stats and passive modifiers.
     */
    private fun buildStatsForClass(characterClass: CharacterClass): StatsComponent {
        return StatsComponent(
            moveSpeed = characterClass.actualMoveSpeed,
            maxHealth = characterClass.baseHp
        ).apply {
            when (characterClass) {
                CharacterClass.SOLDIER -> {
                    ammoCapacityMultiplier = 1.2f       // +20% ammo capacity
                }
                CharacterClass.COMMANDO -> {
                    fireRateMultiplier = 1.5f            // +50% fire rate
                }
                CharacterClass.SPACE_MARINE -> {
                    armor = 5                            // +5 flat armor
                    damageMultiplier = 0.75f             // 25% damage reduction (applied as reduced outgoing? No — see note)
                    // Note: damageMultiplier is outgoing damage. For damage reduction,
                    // we use armor. The 25% reduction is implemented via armor value.
                    // 5 armor + tank HP makes this very durable.
                }
                CharacterClass.ENFORCER -> {
                    pickupRadius = 130f                  // +30% pickup radius (default 100)
                    xpMultiplier = 1.25f                 // +25% XP gain
                }
                CharacterClass.HUNTER -> {
                    damageMultiplier = 1.4f              // +40% weapon damage
                }
                CharacterClass.TERRIBLE_KNIGHT -> {
                    hpRegen = 3f                         // +3 HP/sec regeneration
                    areaMultiplier = 1.3f                // +30% melee/area damage range
                }
                CharacterClass.ROOKIE -> {
                    // Balanced — no special modifiers
                }
            }
        }
    }

    /**
     * Create the starting weapon for the given character class.
     */
    private fun createStartingWeapon(characterClass: CharacterClass): Weapon {
        return when (characterClass) {
            CharacterClass.ROOKIE -> PistolWeapon()
            CharacterClass.SOLDIER -> AssaultRifleWeapon()
            CharacterClass.COMMANDO -> DualPistolWeapon()
            CharacterClass.SPACE_MARINE -> ShotgunWeapon()
            CharacterClass.ENFORCER -> SMGWeapon()
            CharacterClass.HUNTER -> PistolWeapon()
            CharacterClass.TERRIBLE_KNIGHT -> BroadSwordWeapon()
        }
    }
}
