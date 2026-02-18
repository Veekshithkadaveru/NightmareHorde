package app.krafted.nightmarehorde.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import app.krafted.nightmarehorde.engine.input.GestureHandler
import app.krafted.nightmarehorde.engine.input.VirtualJoystick
import app.krafted.nightmarehorde.engine.input.detectGameGestures
import app.krafted.nightmarehorde.engine.rendering.GameSurface
import app.krafted.nightmarehorde.game.data.CharacterType
import app.krafted.nightmarehorde.game.systems.DayNightCycle
import app.krafted.nightmarehorde.game.weapons.WeaponType
import app.krafted.nightmarehorde.ui.components.BossHealthBar
import app.krafted.nightmarehorde.ui.components.HealthBar
import app.krafted.nightmarehorde.ui.components.TimeIndicator
import app.krafted.nightmarehorde.ui.components.XPBar
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    characterType: CharacterType = CharacterType.CYBERPUNK_DETECTIVE,
    viewModel: GameViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val playerHealth by viewModel.playerHealth.collectAsState()
    val kills by viewModel.killCount.collectAsState()
    val gameTimeSec by viewModel.gameTime.collectAsState()
    val unlockedWeapons by viewModel.unlockedWeapons.collectAsState()
    val activeWeaponType by viewModel.activeWeaponType.collectAsState()
    val currentAmmo by viewModel.currentAmmo.collectAsState()
    val weaponUnlockNotification by viewModel.weaponUnlockNotification.collectAsState()
    val dayNight by viewModel.dayNightState.collectAsState()
    val bossState by viewModel.bossState.collectAsState()
    val droneUnlockNotification by viewModel.droneUnlockNotification.collectAsState()
    val xpState by viewModel.xpState.collectAsState()
    val levelUpState by viewModel.levelUpState.collectAsState()
    val scope = rememberCoroutineScope()

    var frameTick by remember { mutableIntStateOf(0) }

    val gestureHandler = remember(viewModel.inputManager, scope) {
        GestureHandler(viewModel.inputManager, scope)
    }

    // Auto-dismiss weapon unlock notification after 2 seconds
    var showUnlockBanner by remember { mutableStateOf(false) }
    var unlockBannerText by remember { mutableStateOf("") }

    LaunchedEffect(weaponUnlockNotification) {
        val notification = weaponUnlockNotification
        if (notification != null) {
            unlockBannerText = "${getWeaponDisplayName(notification)} Unlocked!"
            showUnlockBanner = true
            delay(2000)
            showUnlockBanner = false
            viewModel.dismissWeaponNotification()
        }
    }

    LaunchedEffect(droneUnlockNotification) {
        val notification = droneUnlockNotification
        if (notification != null) {
            unlockBannerText = "${notification.displayName} Online!"
            showUnlockBanner = true
            delay(2000)
            showUnlockBanner = false
            viewModel.dismissDroneNotification()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startGame(characterType)
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { }
            frameTick++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopGame()
            gestureHandler.reset()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Game surface
        GameSurface(
            entitiesProvider = {
                frameTick
                viewModel.gameLoop.getEntitiesSnapshot()
            },
            camera = viewModel.camera,
            spriteRenderer = viewModel.spriteRenderer,
            damageNumberRenderer = viewModel.damageNumberRenderer,
            particleRenderer = viewModel.particleRenderer,
            droneRenderer = viewModel.droneRenderer,
            backgroundColor = Color(0xFF1a1a2e),
            modifier = Modifier.detectGameGestures(gestureHandler, scope)
        )

        // Day/Night lighting overlay — only composed when there is a tint to draw.
        // Skipping the Canvas entirely during day avoids GPU overdraw on every frame.
        if (dayNight.nightIntensity > 0f) {
            val lightingSystemRef = remember { viewModel.lightingSystem }
            Canvas(modifier = Modifier.fillMaxSize()) {
                lightingSystemRef.render(
                    drawScope = this,
                    phase = dayNight.phase,
                    nightIntensity = dayNight.nightIntensity,
                    phaseProgress = dayNight.phaseProgress,
                    overlayAlpha = dayNight.overlayAlpha
                )
            }
        }

        // HUD overlay — Health Bar (top-left)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            HealthBar(
                currentHealth = playerHealth.first,
                maxHealth = playerHealth.second
            )
            XPBar(
                xpProgress = xpState.xpProgress,
                currentLevel = xpState.currentLevel,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Game Timer (top-center, VS-style)
        val minutes = (gameTimeSec / 60f).toInt()
        val seconds = (gameTimeSec % 60f).toInt()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        // Kill Counter + Time Indicator (top-right, VS-style)
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "Kills: $kills",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            TimeIndicator(
                phase = dayNight.phase,
                phaseProgress = dayNight.phaseProgress,
                nightIntensity = dayNight.nightIntensity,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Weapon Bar (top center, below timer)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                unlockedWeapons.forEach { weaponType ->
                    val isActive = weaponType == activeWeaponType
                    val bgColor = if (isActive) Color(0xFF444444) else Color(0xFF222222)
                    val borderColor = if (isActive) Color(0xFFFFCC00) else Color(0xFF666666)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(width = 44.dp, height = 28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(bgColor)
                            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
                            .clickable { viewModel.switchWeapon(weaponType) }
                    ) {
                        Text(
                            text = getWeaponAbbreviation(weaponType),
                            color = if (isActive) Color(0xFFFFCC00) else Color.White,
                            fontSize = 10.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Ammo display
            Text(
                text = if (currentAmmo == -1) "\u221E" else "$currentAmmo",
                color = if (currentAmmo in 1..5) Color(0xFFFF4444) else Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Weapon unlock notification banner
        AnimatedVisibility(
            visible = showUnlockBanner,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 100.dp)
        ) {
            Text(
                text = unlockBannerText,
                color = Color(0xFFFFCC00),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        Color(0xCC000000),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }

        // Boss Health Bar (bottom-center, above joystick)
        BossHealthBar(
            bossName = bossState.name,
            currentHealth = bossState.currentHealth,
            maxHealth = bossState.maxHealth,
            isVisible = bossState.isActive,
            accentColor = bossState.accentColor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        // Virtual Joystick - bottom left corner
        VirtualJoystick(
            inputManager = viewModel.inputManager,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
        )

        // Level-Up Screen overlay
        LevelUpScreen(
            isVisible = levelUpState.isShowing,
            level = levelUpState.level,
            upgrades = levelUpState.upgrades,
            onUpgradeSelected = { choice -> viewModel.selectUpgrade(choice) }
        )
    }
}

private fun getWeaponAbbreviation(type: WeaponType): String {
    return when (type) {
        WeaponType.PISTOL -> "PST"
        WeaponType.MELEE -> "WHP"
        WeaponType.SHOTGUN -> "SHG"
        WeaponType.ASSAULT_RIFLE -> "AR"
        WeaponType.SMG -> "SMG"
        WeaponType.FLAMETHROWER -> "FLM"
    }
}

private fun getWeaponDisplayName(type: WeaponType): String {
    return when (type) {
        WeaponType.PISTOL -> "Pistol"
        WeaponType.MELEE -> "Whip Blade"
        WeaponType.SHOTGUN -> "Shotgun"
        WeaponType.ASSAULT_RIFLE -> "Assault Rifle"
        WeaponType.SMG -> "SMG"
        WeaponType.FLAMETHROWER -> "Flamethrower"
    }
}
