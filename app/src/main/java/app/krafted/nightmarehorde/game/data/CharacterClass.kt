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
    val unlockChallenge: UnlockChallenge
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
        unlockChallenge = UnlockChallenge.NONE
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
        unlockChallenge = UnlockChallenge.SURVIVE_5_MINUTES
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
        unlockChallenge = UnlockChallenge.KILL_500_ZOMBIES
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
        unlockChallenge = UnlockChallenge.DEFEAT_3_BOSSES
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
        unlockChallenge = UnlockChallenge.COMPLETE_10_RUNS
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
        unlockChallenge = UnlockChallenge.KILL_2000_ZOMBIES
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
        unlockChallenge = UnlockChallenge.DEFEAT_10_BOSSES
    );

    /** Convert normalized baseSpeed (100 = default 450f) to actual moveSpeed for StatsComponent */
    val actualMoveSpeed: Float get() = 450f * (baseSpeed / 100f)

    /** HP as fraction of max possible (Space Marine = 140), for stat bar rendering */
    val healthPercent: Float get() = baseHp / 140f

    /** Speed as fraction of max possible (Commando = 120), for stat bar rendering */
    val speedPercent: Float get() = baseSpeed / 120f

    /** Whether this class is unlocked by default (no cost) */
    val isDefaultUnlocked: Boolean get() = unlockChallenge == UnlockChallenge.NONE

    /** Human-readable unlock requirement string */
    val unlockRequirement: String get() = unlockChallenge.displayText
}
