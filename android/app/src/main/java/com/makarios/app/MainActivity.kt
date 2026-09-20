package com.makarios.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.makarios.app.ui.screens.AffirmationDetailScreen
import com.makarios.app.ui.screens.CreateScreen
import com.makarios.app.ui.screens.HomeScreen
import com.makarios.app.ui.screens.LibraryScreen
import com.makarios.app.ui.screens.OnboardingScreen
import com.makarios.app.ui.screens.ProfileScreen
import com.makarios.app.ui.screens.SavedScreen
import com.makarios.app.ui.screens.SubscriptionScreen
import com.makarios.app.ui.screens.WidgetStudioScreen
import com.makarios.app.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MakariosTheme {
                MainAppScaffold()
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
    var selectedTab by remember { mutableStateOf(0) }
    var activeAffirmationIdForCreate by remember { mutableStateOf<String?>(null) }
    var viewingAffirmation by remember { mutableStateOf<Affirmation?>(null) }
    var viewingWidgets by remember { mutableStateOf(false) }
    var showOnboarding by remember { mutableStateOf(false) }
    var viewingSubscription by remember { mutableStateOf(false) }

    // If an affirmation is selected for fullscreen contemplation (Page 1 in PDF)
    if (viewingAffirmation != null) {
        BackHandler { viewingAffirmation = null }
        AffirmationDetailScreen(
            affirmation = viewingAffirmation!!,
            onClose = { viewingAffirmation = null },
            onNavigateToCreate = { id ->
                viewingAffirmation = null
                activeAffirmationIdForCreate = id
                selectedTab = 2 // Navigate to Visual Creator
            }
        )
        return
    }

    // If Widget Studio is opened
    if (viewingWidgets) {
        BackHandler { viewingWidgets = false }
        WidgetStudioScreen(
            onBack = { viewingWidgets = false }
        )
        return
    }

    // If Onboarding is opened
    if (showOnboarding) {
        BackHandler { showOnboarding = false }
        OnboardingScreen(
            onComplete = { showOnboarding = false }
        )
        return
    }

    // If Subscription / Makarios+ is opened
    if (viewingSubscription) {
        BackHandler { viewingSubscription = false }
        SubscriptionScreen(
            onClose = { viewingSubscription = false }
        )
        return
    }

    // 5 Bottom Navigation Tabs directly from PDF: Home | Library | Create | Saved | Profile
    val tabs = listOf(
        TabItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        TabItem("Library", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        TabItem("Create", Icons.Filled.Palette, Icons.Outlined.Palette),
        TabItem("Saved", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
        TabItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Porcelain,
        bottomBar = {
            // Sleek 58dp editorial navigation bar matching mockup palette
            Surface(
                color = Surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.5.dp)
                            .background(BorderSubtle)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            val isSelected = selectedTab == index
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(onClick = { selectedTab = index }),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) PorcelainWarm.copy(alpha = 0.6f) else Color.Transparent)
                                        .padding(horizontal = 14.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label,
                                        tint = if (isSelected) Espresso else StoneMuted,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    color = if (isSelected) Espresso else StoneMuted,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                    fontSize = 10.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                onNavigateToDetail = { affirmation -> viewingAffirmation = affirmation },
                onNavigateToCreate = { id ->
                    activeAffirmationIdForCreate = id
                    selectedTab = 2
                },
                onNavigateToLibrary = { selectedTab = 1 },
                onNavigateToWidgets = { viewingWidgets = true },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> LibraryScreen(
                onNavigateToDetail = { affirmation -> viewingAffirmation = affirmation },
                onNavigateToCreate = { id ->
                    activeAffirmationIdForCreate = id
                    selectedTab = 2
                },
                modifier = Modifier.padding(innerPadding)
            )
            2 -> CreateScreen(
                affirmationId = activeAffirmationIdForCreate,
                onBack = { selectedTab = 0 },
                modifier = Modifier.padding(innerPadding)
            )
            3 -> SavedScreen(
                onNavigateToDetail = { affirmation -> viewingAffirmation = affirmation },
                onNavigateToCreate = { id ->
                    activeAffirmationIdForCreate = id
                    selectedTab = 2
                },
                onNavigateToLibrary = { selectedTab = 1 },
                modifier = Modifier.padding(innerPadding)
            )
            4 -> ProfileScreen(
                onNavigateToWidgets = { viewingWidgets = true },
                onRevisitOnboarding = { showOnboarding = true },
                onNavigateToSubscription = { viewingSubscription = true },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
