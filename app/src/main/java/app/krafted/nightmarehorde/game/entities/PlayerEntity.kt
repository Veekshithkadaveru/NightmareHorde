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
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.game.weapons.PistolWeapon

/**
 * Factory for creating the player entity with all required components.
 * Now accepts a CharacterType to configure sprites and collider per-character.
 */
object PlayerEntity {

    /**
     * Create a new player entity at the given spawn position.
     *
     * @param characterType The selected character — determines sprites, frame sizes, scale
     * @param spawnX World X position
     * @param spawnY World Y position
     * @param stats Player stat block
     */
    fun create(
        characterType: CharacterType = CharacterType.CYBERPUNK_DETECTIVE,
        spawnX: Float = 0f,
        spawnY: Float = 0f,
        stats: StatsComponent = StatsComponent()
    ): Entity {
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

            // Start with Pistol (Phase B2)
            addComponent(WeaponComponent(
                equippedWeapon = PistolWeapon(),
                currentAmmo = 0,
                totalAmmo = 0
            ))

            addComponent(HealthComponent(
                maxHealth = stats.maxHealth
            ))

            addComponent(stats)
        }
    }
}
