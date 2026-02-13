package app.krafted.nightmarehorde.game.weapons

class FlamethrowerWeapon : Weapon(
    name = "Flamethrower",
    damage = 8f, // Low per tick, hits frequently
    fireRate = 20f,
    range = 250f,
    projectileSpeed = 200f,
    maxAmmo = 300,
    spreadAngle = 25f, // Wide cone
    penetrating = true,
    type = WeaponType.FLAMETHROWER
)
