package app.krafted.nightmarehorde.game.weapons

class SwordWeapon : Weapon(
    type = WeaponType.MELEE,
    name = "Sword",
    damage = 25f,
    fireRate = 1.0f, // Once per second
    range = 90f, // Offset from player
    maxAmmo = 0,
    infiniteAmmo = true,
    penetrating = true,
    projectileSpeed = 0f,
    projectileCount = 1, // Single large slash entity
    spreadAngle = 0f,
    isMelee = true
)
