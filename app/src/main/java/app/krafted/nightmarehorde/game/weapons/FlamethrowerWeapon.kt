package app.krafted.nightmarehorde.game.weapons

class FlamethrowerWeapon : Weapon(
    name = "Flamethrower",
    damage = 15f,           // High damage — late-game devastation
    fireRate = 15f,         // 15 particles/sec — dense flame stream
    range = 300f,           // Medium range — VS-style fire reach
    projectileSpeed = 350f, // Fast flames travel further before dying
    maxAmmo = 200,          // Large ammo pool to sustain high fire rate
    spreadAngle = 20f,      // Focused cone of destruction
    penetrating = true,     // Pierces through enemies
    type = WeaponType.FLAMETHROWER,
    isFlame = true
)
