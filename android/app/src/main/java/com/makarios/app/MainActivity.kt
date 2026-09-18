package com.makarios.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.makarios.app.ui.screens.ExploreScreen
import com.makarios.app.ui.screens.HomeScreen
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

@Composable
fun MainAppScaffold() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = PorcelainBackground,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceWhite,
                tonalElevation = 0.dp,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                val tabs = listOf(
                    "Home" to Icons.Default.LocalFireDepartment,
                    "Explore" to Icons.Default.Explore,
                    "Create" to Icons.Default.AutoAwesome,
                    "Saved" to Icons.Default.BookmarkBorder,
                    "Profile" to Icons.Default.Eco
                )

                tabs.forEachIndexed { index, (label, icon) ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) BrandPlum else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (isSelected) BrandPlum else TextMuted,
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
                onNavigateToCreate = { selectedTab = 2 },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> ExploreScreen(
                modifier = Modifier.padding(innerPadding)
            )
            else -> HomeScreen(
                onNavigateToCreate = { selectedTab = 2 },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
