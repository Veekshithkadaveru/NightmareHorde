package app.krafted.nightmarehorde.game.data

enum class PermanentUpgrade(
    val displayName: String,
    val effect: String,
    val maxLevel: Int,
    val costPerLevel: Int,
) {
    TOUGHNESS("Toughness", "+10 Max HP", 10, 100),
    FIREPOWER("Firepower", "+5% Damage", 10, 150),
    MOBILITY("Mobility", "+5% Speed", 5, 200),
    SCAVENGER("Scavenger", "+10% More Drops", 10, 120),
    DRONE_MASTER("Drone Master", "+10% Drone Damage", 5, 250),
    AMMO_BELT("Ammo Belt", "+10% Ammo", 10, 100),
    SECOND_WIND("Second Wind", "+1 Revive/Run", 3, 500);
}
