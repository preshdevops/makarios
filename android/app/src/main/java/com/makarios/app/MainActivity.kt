package com.makarios.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.data.Affirmation
import com.makarios.app.ui.screens.CreateScreen
import com.makarios.app.ui.screens.HomeScreen
import com.makarios.app.ui.screens.ExploreScreen
import com.makarios.app.ui.screens.OnboardingScreen
import com.makarios.app.ui.screens.ProfileScreen
import com.makarios.app.ui.screens.SavedScreen
import com.makarios.app.ui.theme.*
import com.makarios.app.ui.wallpaper.WallpaperGallery
import com.makarios.app.util.ReminderManager

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            ReminderManager.rescheduleAll(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ReminderManager.init(this)
        com.makarios.app.widget.WidgetScheduling.schedule(this)
        requestNotificationPermission()

        setContent {
            MakariosTheme {
                MainAppScaffold()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

private data class TabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainAppScaffold() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("makarios_prefs", Context.MODE_PRIVATE) }
    var selectedTab by remember { mutableStateOf(0) }
    var activeAffirmationIdForCreate by remember { mutableStateOf<String?>(null) }
    var showWallpaper by remember { mutableStateOf(false) }
    var showOnboarding by remember {
        mutableStateOf(!prefs.getBoolean("onboarding_completed", false))
    }

    // If Onboarding is opened
    if (showOnboarding) {
        BackHandler {
            prefs.edit().putBoolean("onboarding_completed", true).apply()
            showOnboarding = false
        }
        OnboardingScreen(
            onComplete = {
                prefs.edit().putBoolean("onboarding_completed", true).apply()
                showOnboarding = false
            },
            onOpenWallpapers = { showWallpaper = true }
        )
        return
    }


    if (showWallpaper) {
        WallpaperGallery(onBack = { showWallpaper = false })
        return
    }

    val currentLight = remember { getCurrentLightForTime() }

    // Five tabs: Today, Explore, Declare, Kept, You
    val tabs = listOf(
        TabItem("Today", Icons.Filled.Home, Icons.Outlined.Home),
        TabItem("Explore", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        TabItem("Declare", Icons.Filled.Palette, Icons.Outlined.Palette),
        TabItem("Kept", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
        TabItem("You", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = currentLight.bottom,
        bottomBar = {
            // The bar hides inside the Declare flow (selectedTab == 2)
            if (selectedTab != 2) {
                Surface(
                    color = currentLight.bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedTab == index
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(onClick = {
                                        if (index == 2 && selectedTab != 2) {
                                            activeAffirmationIdForCreate = null
                                        }
                                        selectedTab = index
                                    }),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) currentLight.text else currentLight.text.copy(alpha = 0.55f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    color = if (isSelected) currentLight.text else currentLight.text.copy(alpha = 0.55f),
                                    fontFamily = HankenGroteskFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(modifier = Modifier.padding(innerPadding))
            1 -> ExploreScreen(
                onNavigateToTopic = { _, _ -> },
                onNavigateToCreate = { selectedTab = 2 },
                modifier = Modifier.padding(innerPadding)
            )
            2 -> CreateScreen(
                onNavigateBack = { selectedTab = 0 },
                modifier = Modifier.padding(innerPadding)
            )
            3 -> SavedScreen(
                onNavigateToCreate = { selectedTab = 2 },
                modifier = Modifier.padding(innerPadding)
            )
            4 -> ProfileScreen(
                onSignOut = {
                    // Sign out callbacks if any
                },
                onOpenWallpapers = { showWallpaper = true },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}







