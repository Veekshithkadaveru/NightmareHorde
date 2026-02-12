package app.krafted.nightmarehorde.game.weapons

class ShotgunWeapon : Weapon(
    type = WeaponType.SHOTGUN,
    name = "Shotgun",
    damage = 12f, // Per pellet — high damage to compensate for short range
    fireRate = 1.2f,
    range = 120f, // Short range — shotgun is close-quarters
    maxAmmo = 8,
    infiniteAmmo = false,
    projectileSpeed = 500f, // Slightly slower pellets
    projectileCount = 3,
    spreadAngle = 30f
)
