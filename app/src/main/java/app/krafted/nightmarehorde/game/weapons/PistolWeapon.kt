package app.krafted.nightmarehorde.game.weapons

class PistolWeapon : Weapon(
    name = "Pistol",
    damage = 10f,       // Low DMG, infinite ammo fallback
    fireRate = 2f,       // 2 shots/sec
    range = 400f,        // Increased to match other weapons
    projectileSpeed = 500f,
    maxAmmo = 0,
    infiniteAmmo = true,
    type = WeaponType.PISTOL
)
