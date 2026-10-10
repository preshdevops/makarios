package com.makarios.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import com.makarios.app.ui.screens.TopicDetailScreen
import com.makarios.app.ui.screens.bible.BiblePickerScreen
import com.makarios.app.ui.screens.bible.BibleReaderScreen
import com.makarios.app.ui.screens.bible.ChapterVersePickerScreen
import com.makarios.app.ui.theme.*
import com.makarios.app.ui.wallpaper.WallpaperGallery
import com.makarios.app.util.ReminderManager

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        ReminderManager.init(this)
        lifecycleScope.launch(Dispatchers.IO) { com.makarios.app.data.MatchEngine.getInstance(this@MainActivity).initialize() }
        com.makarios.app.widget.WidgetScheduling.schedule(this)

        setContent {
            MakariosTheme {
                MainAppScaffold(initialAffirmationId = intent.getStringExtra(ReminderManager.EXTRA_ID))
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
fun MainAppScaffold(initialAffirmationId: String? = null) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("makarios_prefs", Context.MODE_PRIVATE) }
    var selectedTab by remember { mutableStateOf(0) }
    var activeAffirmationIdForCreate by remember { mutableStateOf<String?>(null) }
    var showWallpaper by remember { mutableStateOf(false) }

    // Sub-navigation states
    var activeTopic by remember { mutableStateOf<String?>(null) }
    var activeReaderTarget by remember { mutableStateOf<Triple<String, Int, Int?>?>(null) }
    var activeBiblePicker by remember { mutableStateOf(false) }
    var activeChapterVersePickerTarget by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var createInitialState by remember { mutableStateOf<Triple<String?, String?, String?>?>(null) }

    var showOnboarding by remember {
        mutableStateOf(
            !com.makarios.app.data.OnboardingStore.isOnboardingDone(context) &&
            !(com.makarios.app.data.AuthManager.isLoggedIn && !com.makarios.app.data.AuthManager.isAnonymous)
        )
    }

    // If Onboarding is opened
    if (showOnboarding) {
        BackHandler {
            com.makarios.app.data.OnboardingStore.setOnboardingDone(context, true)
            showOnboarding = false
        }
        OnboardingScreen(
            onComplete = {
                com.makarios.app.data.OnboardingStore.setOnboardingDone(context, true)
                showOnboarding = false
            }
        )
        return
    }


    var showAuthChoose by remember { mutableStateOf(false) }
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_START || event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    try {
                        com.makarios.app.data.AuthManager.reloadUser()
                    } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                        showAuthChoose = true
                    } catch (_: Exception) {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showAuthChoose) {
        com.makarios.app.ui.screens.AuthFlowHost(
            onContinueToApp = { showAuthChoose = false }
        )
        return
    }

    if (showWallpaper) {
        WallpaperGallery(onBack = { showWallpaper = false })
        return
    }

    // Back handling for sub-flows and tabs
    if (activeChapterVersePickerTarget != null) {
        BackHandler { activeChapterVersePickerTarget = null }
    } else if (activeReaderTarget != null) {
        BackHandler { activeReaderTarget = null }
    } else if (activeBiblePicker) {
        BackHandler { activeBiblePicker = false }
    } else if (activeTopic != null) {
        BackHandler { activeTopic = null }
    } else if (selectedTab != 0) {
        BackHandler {
            if (selectedTab == 2) {
                createInitialState = null
            }
            selectedTab = 0
        }
    }

    var activeLight by remember { mutableStateOf(getCurrentLightForTime()) }
    SystemBarsController(activeLight)

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            1 -> activeLight = Light.Dawn
            3 -> activeLight = Light.Dawn
            4 -> activeLight = Light.Midday
            else -> {}
        }
    }

    // Five tabs: Today, Explore, Declare, Kept, You
    val tabs = listOf(
        TabItem("Today", Icons.Filled.Home, Icons.Outlined.Home),
        TabItem("Explore", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        TabItem("Declare", Icons.Filled.Palette, Icons.Outlined.Palette),
        TabItem("Kept", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
        TabItem("You", Icons.Filled.Person, Icons.Outlined.Person)
    )

    val isSubScreenOpen = activeChapterVersePickerTarget != null || activeReaderTarget != null || activeBiblePicker || activeTopic != null

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // The bar hides inside the Declare flow (selectedTab == 2) and full-screen sub-flows
            if (selectedTab != 2 && !isSubScreenOpen) {
                Surface(
                    color = activeLight.bottom,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
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
                                            createInitialState = null
                                        }
                                        selectedTab = index
                                    }),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) activeLight.text else activeLight.text.copy(alpha = 0.55f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    color = if (isSelected) activeLight.text else activeLight.text.copy(alpha = 0.55f),
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
    ) { _ ->
        if (activeChapterVersePickerTarget != null) {
            val target = activeChapterVersePickerTarget!!
            ChapterVersePickerScreen(
                bookName = target.first,
                initialChapter = target.second,
                onNavigateBack = { activeChapterVersePickerTarget = null },
                onVerseSelected = { selection ->
                    activeChapterVersePickerTarget = null
                    activeBiblePicker = false
                    createInitialState = Triple(null, selection.text, selection.reference)
                    selectedTab = 2
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (activeReaderTarget != null) {
            val target = activeReaderTarget!!
            BibleReaderScreen(
                bookName = target.first,
                chapterNumber = target.second,
                initialVerse = target.third,
                onNavigateBack = { activeReaderTarget = null },
                onDeclareThis = { selection ->
                    activeReaderTarget = null
                    createInitialState = Triple(null, selection.text, selection.reference)
                    selectedTab = 2
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (activeBiblePicker) {
            BiblePickerScreen(
                onNavigateBack = { activeBiblePicker = false },
                onSelectBookChapter = { book, chapter ->
                    activeChapterVersePickerTarget = Pair(book, chapter)
                },
                onVerseSelected = { selection ->
                    activeBiblePicker = false
                    createInitialState = Triple(null, selection.text, selection.reference)
                    selectedTab = 2
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            when (selectedTab) {
                0 -> HomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    initialAffirmationId = initialAffirmationId,
                    onLightChanged = { activeLight = it },
                    onNavigateToReader = { book, chapter, verse ->
                        activeReaderTarget = Triple(book, chapter, verse)
                    }
                )
                1 -> {
                    if (activeTopic != null) {
                        TopicDetailScreen(
                            topicName = activeTopic!!,
                            onNavigateBack = { activeTopic = null },
                            onDeclareThis = { declaration, verseText, reference ->
                                activeTopic = null
                                createInitialState = Triple(declaration, verseText, reference)
                                selectedTab = 2
                            },
                            onReadChapter = { book, chapter, verse ->
                                activeReaderTarget = Triple(book, chapter, verse)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ExploreScreen(
                            onNavigateToTopic = { topic, _ -> activeTopic = topic },
                            onNavigateToBiblePicker = { activeBiblePicker = true },
                            onNavigateToReader = { book, chapter, verse ->
                                activeReaderTarget = Triple(book, chapter, verse)
                            },
                            onNavigateToDeclare = { declaration, verse, reference ->
                                createInitialState = Triple(declaration, verse, reference)
                                selectedTab = 2
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                2 -> key(createInitialState) {
                    CreateScreen(
                        onNavigateBack = {
                            createInitialState = null
                            selectedTab = 0
                        },
                        initialDeclaration = createInitialState?.first,
                        initialVerseText = createInitialState?.second,
                        initialReference = createInitialState?.third,
                        initialStep = if (createInitialState?.first != null) 3 else 1,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                3 -> SavedScreen(
                    onNavigateToCreate = { selectedTab = 2 },
                    modifier = Modifier.fillMaxSize()
                )
                4 -> ProfileScreen(
                    onSignOut = {
                        // Sign out callbacks if any
                    },
                    onOpenWallpapers = { showWallpaper = true },
                    onResetOnboarding = { showOnboarding = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}