package app.krafted.nightmarehorde.game.weapons

class FlamethrowerWeapon : Weapon(
    type = WeaponType.FLAMETHROWER,
    name = "Flamethrower",
    damage = 5f, // Higher damage per tick — flame burns hard
    fireRate = 15f, // Rapid stream of flames
    range = 80f, // Very short range — must be close to enemies
    maxAmmo = 200,
    infiniteAmmo = false,
    penetrating = true,
    projectileSpeed = 200f, // Slow-moving flame particles
    projectileCount = 1, // One flame per tick, but fires very fast
    spreadAngle = 25f, // Random spread applied per particle in WeaponSystem
    isFlame = true // Flag for special flame rendering
)
