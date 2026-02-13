package app.krafted.nightmarehorde.game.weapons

class PistolWeapon : Weapon(
    name = "Pistol",
    damage = 15f,
    fireRate = 2f,
    range = 400f,
    projectileSpeed = 600f,
    maxAmmo = 0,
    infiniteAmmo = true,
    type = WeaponType.PISTOL
)
