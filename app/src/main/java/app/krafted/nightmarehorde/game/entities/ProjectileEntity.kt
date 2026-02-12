package app.krafted.nightmarehorde.game.entities

import androidx.compose.ui.graphics.Color
import app.krafted.nightmarehorde.engine.core.Entity
import app.krafted.nightmarehorde.engine.core.Vector2
import app.krafted.nightmarehorde.engine.core.components.ColliderComponent
import app.krafted.nightmarehorde.engine.core.components.CollisionLayer
import app.krafted.nightmarehorde.engine.core.components.ProjectileComponent
import app.krafted.nightmarehorde.engine.core.components.SpriteComponent
import app.krafted.nightmarehorde.engine.core.components.TransformComponent
import app.krafted.nightmarehorde.engine.core.components.VelocityComponent

object ProjectileEntity {
    private val random = java.util.Random()

    fun create(
        x: Float,
        y: Float,
        direction: Vector2,
        speed: Float = 500f,
        damage: Float = 10f,
        range: Float = 1000f,
        ownerId: String = "",
        penetrating: Boolean = false,
        texture: String = "projectile_standard"
    ): Entity {
        return Entity().apply {
            addComponent(TransformComponent(x = x, y = y, rotation = direction.angle(), scale = 0.5f))
            addComponent(VelocityComponent(vx = direction.x * speed, vy = direction.y * speed))
            val collider = ColliderComponent.circle(
                radius = 5f,
                layer = CollisionLayer.PROJECTILE
            )
            collider.isTrigger = true
            addComponent(collider)
            addComponent(SpriteComponent(
                textureKey = texture,
                layer = 2
            ))
            addComponent(ProjectileComponent(
                damage = damage,
                maxDistance = range,
                ownerId = ownerId,
                penetrating = penetrating
            ))
        }
    }

    fun createFlame(
        x: Float,
        y: Float,
        direction: Vector2,
        speed: Float = 200f,
        damage: Float = 5f,
        range: Float = 80f,
        ownerId: String = ""
    ): Entity {
        // Randomize initial scale slightly
        val startScale = 0.5f + random.nextFloat() * 0.5f // 0.5 to 1.0
        
        // Randomize rotation for visual variety
        val rotation = direction.angle() + (random.nextFloat() - 0.5f) * 360f 
        
        // Color variation: Yellow -> Orange -> Red
        val hue = random.nextFloat()
        val color = when {
            hue > 0.6f -> Color(0xFFFFCC00) // Yellow
            hue > 0.3f -> Color(0xFFFF6600) // Orange
            else -> Color(0xFFFF3300) // Red
        }

        return Entity().apply {
            addComponent(TransformComponent(x = x, y = y, rotation = rotation, scale = startScale))
            addComponent(VelocityComponent(vx = direction.x * speed, vy = direction.y * speed))
            // Larger collider — flame hits a wider area
            val collider = ColliderComponent.circle(
                radius = 12f,
                layer = CollisionLayer.PROJECTILE
            )
            collider.isTrigger = true
            addComponent(collider)
            
            addComponent(SpriteComponent(
                textureKey = "projectile_standard", // Reusing standard circle/blob for now
                layer = 2,
                tint = color,
                alpha = 0.9f,
                width = 20f,
                height = 20f
            ))
            addComponent(ProjectileComponent(
                damage = damage,
                maxDistance = range,
                ownerId = ownerId,
                penetrating = true,
                maxLifetime = 0.6f, // Short life
                growthRate = 2.0f, // Grows 2x per second
                fadeRate = 1.5f // Fades out completely in ~0.66s
            ))
        }
    }

    fun createSlash(
        x: Float,
        y: Float,
        direction: Vector2,
        damage: Float = 25f,
        range: Float = 120f,
        ownerId: String = ""
    ): Entity {
        return Entity().apply {
            // Rotation aligns the long axis of the sprite with the facing direction
            addComponent(TransformComponent(x = x, y = y, rotation = direction.angle(), scale = 1f))
            // Stationary — appears in place and vanishes
            addComponent(VelocityComponent(vx = 0f, vy = 0f))
            // Large collider covering the full slash area
            val collider = ColliderComponent.circle(
                radius = 60f, // Big hitbox
                layer = CollisionLayer.PROJECTILE
            )
            collider.isTrigger = true
            addComponent(collider)
            // VS slash visual: one big wide horizontal bar
            addComponent(SpriteComponent(
                textureKey = "projectile_standard",
                layer = 3,
                tint = Color(0xFFDDEEFF), // Bright white-blue slash
                alpha = 0.85f,
                width = 160f, // Very wide
                height = 16f // Thin
            ))
            addComponent(ProjectileComponent(
                damage = damage,
                maxDistance = Float.MAX_VALUE,
                ownerId = ownerId,
                penetrating = true, // Sword slash penetrates enemies
                maxLifetime = 0.25f // Brief flash
            ))
        }
    }
}
