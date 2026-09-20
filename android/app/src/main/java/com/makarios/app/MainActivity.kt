package com.makarios.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.screens.CreateScreen
import com.makarios.app.ui.screens.ExploreScreen
import com.makarios.app.ui.screens.HomeScreen
import com.makarios.app.ui.screens.ProfileScreen
import com.makarios.app.ui.screens.SavedScreen
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

    val tabs = listOf(
        TabItem("Home", Icons.Filled.Home, Icons.Outlined.Home),
        TabItem("Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
        TabItem("Create", Icons.Filled.Add, Icons.Default.Add),
        TabItem("Saved", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
        TabItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Porcelain,
        bottomBar = {
            NavigationBar(
                containerColor = Surface,
                tonalElevation = 0.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                tint = if (isSelected) Espresso else StoneMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                color = if (isSelected) Espresso else StoneMuted,
                                fontFamily = BodyFontFamily,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                fontSize = 11.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                onNavigateToCreate = { id ->
                    activeAffirmationIdForCreate = id
                    selectedTab = 2
                },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> ExploreScreen(
                modifier = Modifier.padding(innerPadding)
            )
            2 -> CreateScreen(
                affirmationId = activeAffirmationIdForCreate,
                onBack = { selectedTab = 0 },
                modifier = Modifier.padding(innerPadding)
            )
            3 -> SavedScreen(
                onNavigateToCreate = { id ->
                    activeAffirmationIdForCreate = id
                    selectedTab = 2
                },
                modifier = Modifier.padding(innerPadding)
            )
            4 -> ProfileScreen(
                onNavigateToWidgets = { selectedTab = 1 },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
