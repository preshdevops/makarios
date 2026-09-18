package com.makarios.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        containerColor = ParchmentBackground,
        bottomBar = {
            Column {
                // Delicate Top Hairline Rule
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.75.dp)
                        .background(HairlineRule)
                )

                NavigationBar(
                    containerColor = PaperSurface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    val tabs = listOf(
                        "TODAY" to Icons.Default.MenuBook,
                        "ATELIER" to Icons.Default.Widgets,
                        "SAVED" to Icons.Default.BookmarkBorder
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
                                    tint = if (isSelected) RubricVermilion else InkMuted,
                                    modifier = Modifier.size(19.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    color = if (isSelected) RubricVermilion else InkMuted,
                                    fontFamily = BodyFontFamily,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 9.5.sp,
                                    letterSpacing = 1.6.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                onNavigateToCreate = { _ -> selectedTab = 1 },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> ExploreScreen(
                modifier = Modifier.padding(innerPadding)
            )
            2 -> HomeScreen(
                onNavigateToCreate = { _ -> selectedTab = 1 },
                modifier = Modifier.padding(innerPadding)
            )
            else -> HomeScreen(
                onNavigateToCreate = { _ -> selectedTab = 1 },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
