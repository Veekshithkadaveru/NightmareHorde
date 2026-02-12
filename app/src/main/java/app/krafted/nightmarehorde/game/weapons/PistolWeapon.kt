package app.krafted.nightmarehorde.game.weapons

class PistolWeapon : Weapon(
    type = WeaponType.PISTOL,
    name = "Pistol",
    damage = 10f,
    fireRate = 2f,
    range = 1000f,
    maxAmmo = 0,
    infiniteAmmo = true,
    projectileSpeed = 600f
)
