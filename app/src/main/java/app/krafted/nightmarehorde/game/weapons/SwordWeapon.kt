package app.krafted.nightmarehorde.game.weapons

class SwordWeapon : Weapon(
    name = "Sword",
    damage = 35f,
    fireRate = 2.5f,
    range = 100f,
    projectileSpeed = 0f, // Instant/Stationary slash
    maxAmmo = 0,
    infiniteAmmo = true,
    penetrating = true,
    type = WeaponType.MELEE
)
