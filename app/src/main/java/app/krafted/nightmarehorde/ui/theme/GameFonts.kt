package app.krafted.nightmarehorde.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import app.krafted.nightmarehorde.R

/**
 * The two display typefaces used across the menus and HUD: [creepster] for the
 * dripping horror titles and [blackOpsOne] for the military-stencil body text.
 */
data class GameFonts(
    val creepster: FontFamily,
    val blackOpsOne: FontFamily,
)

/** Remembers the shared [GameFonts] so each screen stops re-creating the families. */
@Composable
fun rememberGameFonts(): GameFonts = remember {
    GameFonts(
        creepster = FontFamily(Font(R.font.creepster)),
        blackOpsOne = FontFamily(Font(R.font.black_ops_one)),
    )
}
