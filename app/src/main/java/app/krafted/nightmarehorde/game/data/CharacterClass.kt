package app.krafted.nightmarehorde.game.data

import app.krafted.nightmarehorde.game.weapons.WeaponType

/**
 * Defines the playable character classes with gameplay stats, passives,
 * starting weapons, and unlock requirements.
 *
 * Each class references a [CharacterType] for its sprite/visual data,
 * keeping a clean separation between gameplay and rendering concerns.
 */
enum class CharacterClass(
    val displayName: String,
    val characterType: CharacterType,
    val baseHp: Int,
    val baseSpeed: Int,
    val startingWeaponType: WeaponType,
    val startingWeaponDisplayName: String,
    val passiveName: String,
    val passiveDescription: String,
    val unlocksAtSupplies: Int,
    val unlocksAtRescues: Int
) {
    ROOKIE(
        displayName = "Rookie",
        characterType = CharacterType.CYBERPUNK_DETECTIVE,
        baseHp = 100,
        baseSpeed = 100,
        startingWeaponType = WeaponType.PISTOL,
        startingWeaponDisplayName = "Pistol",
        passiveName = "Balanced",
        passiveDescription = "No special advantages or weaknesses. A solid all-rounder for any situation.",
        unlocksAtSupplies = 0,
        unlocksAtRescues = 0
    ),

    SOLDIER(
        displayName = "Soldier",
        characterType = CharacterType.SOLDIER,
        baseHp = 120,
        baseSpeed = 90,
        startingWeaponType = WeaponType.ASSAULT_RIFLE,
        startingWeaponDisplayName = "Assault Rifle",
        passiveName = "+20% Ammo",
        passiveDescription = "Military training grants 20% more ammo capacity for all weapons.",
        unlocksAtSupplies = 0,    // TODO: restore to 500 after testing
        unlocksAtRescues = 0
    ),

    COMMANDO(
        displayName = "Commando",
        characterType = CharacterType.COMMANDO,
        baseHp = 70,
        baseSpeed = 120,
        startingWeaponType = WeaponType.PISTOL,
        startingWeaponDisplayName = "Dual Pistols",
        passiveName = "Trigger Happy",
        passiveDescription = "+50% fire rate for all weapons. Glass cannon supreme.",
        unlocksAtSupplies = 0,    // TODO: restore to 0 supplies, 50 rescues after testing
        unlocksAtRescues = 0      // TODO: restore to 50 after testing
    ),

    SPACE_MARINE(
        displayName = "Space Marine",
        characterType = CharacterType.SPACE_MARINE,
        baseHp = 140,
        baseSpeed = 80,
        startingWeaponType = WeaponType.SHOTGUN,
        startingWeaponDisplayName = "Shotgun",
        passiveName = "Heavy Armor",
        passiveDescription = "+5 armor and 25% damage reduction. Built to endure the horde.",
        unlocksAtSupplies = 0,
        unlocksAtRescues = 0
    ),

    ENFORCER(
        displayName = "Enforcer",
        characterType = CharacterType.ENFORCER,
        baseHp = 100,
        baseSpeed = 105,
        startingWeaponType = WeaponType.SMG,
        startingWeaponDisplayName = "SMG",
        passiveName = "Scavenger",
        passiveDescription = "+30% pickup radius and +25% XP gain. Gear up faster than anyone.",
        unlocksAtSupplies = 0,
        unlocksAtRescues = 0
    ),

    HUNTER(
        displayName = "Hunter",
        characterType = CharacterType.HUNTER,
        baseHp = 80,
        baseSpeed = 110,
        startingWeaponType = WeaponType.PISTOL,
        startingWeaponDisplayName = "Pistol",
        passiveName = "Dead Eye",
        passiveDescription = "+40% weapon damage and +25% range. Every shot counts.",
        unlocksAtSupplies = 0,
        unlocksAtRescues = 0
    ),

    TERRIBLE_KNIGHT(
        displayName = "Terrible Knight",
        characterType = CharacterType.TERRIBLE_KNIGHT,
        baseHp = 130,
        baseSpeed = 85,
        startingWeaponType = WeaponType.SWORD,
        startingWeaponDisplayName = "Broad Sword",
        passiveName = "Undying Fury",
        passiveDescription = "+3 HP regen per second and +30% melee/area damage. A relentless close-combat warrior.",
        unlocksAtSupplies = 0,
        unlocksAtRescues = 0
    );

    /** Convert normalized baseSpeed (100 = default 450f) to actual moveSpeed for StatsComponent */
    val actualMoveSpeed: Float get() = 450f * (baseSpeed / 100f)

    /** HP as fraction of max possible (Space Marine = 140), for stat bar rendering */
    val healthPercent: Float get() = baseHp / 140f

    /** Speed as fraction of max possible (Commando = 120), for stat bar rendering */
    val speedPercent: Float get() = baseSpeed / 120f

    /** Whether this class is unlocked by default (no cost) */
    val isDefaultUnlocked: Boolean get() = unlocksAtSupplies == 0 && unlocksAtRescues == 0

    /** Human-readable unlock requirement string */
    val unlockRequirement: String
        get() = when {
            isDefaultUnlocked -> ""
            unlocksAtRescues > 0 -> "Rescue $unlocksAtRescues Survivors"
            else -> "Collect $unlocksAtSupplies Supplies"
        }
}
