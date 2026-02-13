package app.krafted.nightmarehorde.game.weapons

class FlamethrowerWeapon : Weapon(
    name = "Flamethrower",
    damage = 10f,         // 2x close-range bonus — burns fast when up close
    fireRate = 10f,        // 10 ticks/sec — continuous fire stream
    range = 120f,          // Short range — risky close combat
    projectileSpeed = 200f,
    maxAmmo = 100,
    spreadAngle = 25f,     // Wide cone
    penetrating = true,    // Pierces through enemies
    type = WeaponType.FLAMETHROWER
)
