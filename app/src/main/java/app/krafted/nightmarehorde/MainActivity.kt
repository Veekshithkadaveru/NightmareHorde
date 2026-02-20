package app.krafted.nightmarehorde

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.krafted.nightmarehorde.ui.navigation.NightmareHordeNavHost
import app.krafted.nightmarehorde.ui.theme.NightmareHordeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            NightmareHordeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NightmareHordeNavHost()
                }
            }
        }
    }
}
