package app.krafted.nightmarehorde.engine.input

/**
 * Player-selectable joystick control scheme.
 *
 * @property displayName Label shown in the settings UI
 */
enum class JoystickMode(val displayName: String) {
    /** Joystick appears wherever the player touches (Vampire Survivors style). */
    FLOATING("Floating"),

    /** Joystick fixed at the bottom-left corner of the screen. */
    FIXED_LEFT("Left"),

    /** Joystick fixed at the bottom-right corner of the screen. */
    FIXED_RIGHT("Right");

    companion object {
        /** Parses a persisted value, falling back to [FLOATING] for unknown input. */
        fun fromString(value: String?): JoystickMode =
            entries.firstOrNull { it.name == value } ?: FLOATING
    }
}
