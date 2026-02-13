package app.krafted.nightmarehorde.game.weapons

class ShotgunWeapon : Weapon(
    name = "Shotgun",
    damage = 20f, // Per pellet
    fireRate = 1.2f,
    range = 300f,
    projectileSpeed = 550f,
    maxAmmo = 40,
    projectileCount = 5,
    spreadAngle = 45f,
    type = WeaponType.SHOTGUN
)
