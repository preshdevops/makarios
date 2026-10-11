package com.makarios.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
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
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.makarios.app.ui.screens.CreateScreen
import com.makarios.app.ui.screens.HomeScreen
import com.makarios.app.ui.screens.ExploreScreen
import com.makarios.app.ui.screens.OnboardingScreen
import com.makarios.app.ui.screens.ProfileScreen
import com.makarios.app.ui.screens.SavedScreen
import com.makarios.app.ui.screens.TopicDetailScreen
import com.makarios.app.ui.screens.CrashReportScreen
import com.makarios.app.ui.screens.bible.BiblePickerScreen
import com.makarios.app.ui.screens.bible.BibleReaderScreen
import com.makarios.app.ui.screens.bible.ChapterVersePickerScreen
import com.makarios.app.ui.theme.*
import com.makarios.app.ui.wallpaper.WallpaperGallery
import com.makarios.app.util.CrashReporter
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
        runCatching { ReminderManager.init(this) }.onFailure { android.util.Log.e("MainActivity", "Reminder init failed", it) }
        lifecycleScope.launch(Dispatchers.IO) {
            runCatching { com.makarios.app.data.MatchEngine.getInstance(this@MainActivity).initialize() }
                .onFailure { android.util.Log.e("MainActivity", "MatchEngine init failed", it) }
        }
        runCatching { com.makarios.app.widget.WidgetScheduling.schedule(this) }.onFailure { android.util.Log.e("MainActivity", "WidgetScheduling failed", it) }

        val hasCrash = CrashReporter.hasCrashReport(this)
        val crashReportText = if (hasCrash) CrashReporter.getCrashReport(this) else null

        setContent {
            MakariosTheme {
                var showCrashReport by remember { mutableStateOf(crashReportText != null) }

                if (showCrashReport && crashReportText != null) {
                    CrashReportScreen(
                        reportText = crashReportText,
                        onDismiss = {
                            CrashReporter.clearCrashReport(this@MainActivity)
                            showCrashReport = false
                        }
                    )
                } else {
                    val affirmationIdFromIntent = intent?.data?.getQueryParameter("id")
                        ?: intent?.data?.lastPathSegment?.takeIf { intent?.data?.scheme == "makarios" }
                        ?: intent.getStringExtra(ReminderManager.EXTRA_ID)
                    AppNavigation(initialAffirmationId = affirmationIdFromIntent)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(initialAffirmationId: String? = null) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            MainAppScaffold(
                navController = navController,
                initialAffirmationId = initialAffirmationId
            )
        }

        composable(
            route = "bible/books?caller={caller}",
            arguments = listOf(
                navArgument("caller") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            BiblePickerScreen(
                onNavigateBack = { navController.popBackStack() },
                onSelectBookChapter = { book, chapter ->
                    navController.navigate("bible/${Uri.encode(book)}/$chapter/verses")
                },
                onVerseSelected = { selection ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_text", selection.text)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_ref", selection.reference)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "bible/{book}/{chapter}?verse={verse}",
            arguments = listOf(
                navArgument("book") { type = NavType.StringType },
                navArgument("chapter") { type = NavType.IntType },
                navArgument("verse") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val rawBook = backStackEntry.arguments?.getString("book") ?: "Romans"
            val book = Uri.decode(rawBook)
            val chapter = backStackEntry.arguments?.getInt("chapter") ?: 8
            val verseArg = backStackEntry.arguments?.getInt("verse") ?: -1
            val verse = if (verseArg > 0) verseArg else null

            BibleReaderScreen(
                bookName = book,
                chapterNumber = chapter,
                initialVerse = verse,
                onNavigateBack = { navController.popBackStack() },
                onDeclareThis = { selection ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_text", selection.text)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_ref", selection.reference)
                    navController.popBackStack("main", inclusive = false)
                }
            )
        }

        composable(
            route = "bible/{book}/{chapter}/verses",
            arguments = listOf(
                navArgument("book") { type = NavType.StringType },
                navArgument("chapter") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val rawBook = backStackEntry.arguments?.getString("book") ?: "Romans"
            val book = Uri.decode(rawBook)
            val chapter = backStackEntry.arguments?.getInt("chapter") ?: 1

            ChapterVersePickerScreen(
                bookName = book,
                initialChapter = chapter,
                onNavigateBack = { navController.popBackStack() },
                onVerseSelected = { selection ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_text", selection.text)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_ref", selection.reference)
                    navController.popBackStack("main", inclusive = false)
                }
            )
        }

        composable(
            route = "bible/search?q={q}",
            arguments = listOf(
                navArgument("q") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("q") ?: ""
            BiblePickerScreen(
                onNavigateBack = { navController.popBackStack() },
                onSelectBookChapter = { book, chapter ->
                    navController.navigate("bible/${Uri.encode(book)}/$chapter/verses")
                },
                onVerseSelected = { selection ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_text", selection.text)
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_ref", selection.reference)
                    navController.popBackStack()
                },
                initialQuery = query
            )
        }
    }
}

private data class TabItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainAppScaffold(
    navController: NavHostController,
    initialAffirmationId: String? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var showWallpaper by remember { mutableStateOf(false) }

    // Sub-navigation state for Topic
    var activeTopic by remember { mutableStateOf<String?>(null) }
    var createInitialState by remember { mutableStateOf<Triple<String?, String?, String?>?>(null) }

    // Observe verse selection passed back from Bible picker/reader via savedStateHandle
    val currentEntry = navController.currentBackStackEntry
    val savedStateHandle = currentEntry?.savedStateHandle
    val pickedText by savedStateHandle?.getStateFlow<String?>("selected_verse_text", null)?.collectAsState() ?: remember { mutableStateOf(null) }
    val pickedRef by savedStateHandle?.getStateFlow<String?>("selected_verse_ref", null)?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(pickedText, pickedRef) {
        val t = pickedText
        val r = pickedRef
        if (!t.isNullOrBlank() && !r.isNullOrBlank()) {
            createInitialState = Triple(createInitialState?.first, t, r)
            selectedTab = 2
            savedStateHandle?.remove<String>("selected_verse_text")
            savedStateHandle?.remove<String>("selected_verse_ref")
        }
    }

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

    // Back handling for topic and tabs
    if (activeTopic != null) {
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // The bar hides inside the Declare flow (selectedTab == 2) and full-screen topic
            if (selectedTab != 2 && activeTopic == null) {
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
                    val vParam = if (verse != null) "?verse=$verse" else ""
                    navController.navigate("bible/${Uri.encode(book)}/$chapter$vParam")
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
                        val vParam = if (verse != null) "?verse=$verse" else ""
                        navController.navigate("bible/${Uri.encode(book)}/$chapter$vParam")
                    }
                )
                1 -> ExploreScreen(
                    onNavigateToTopic = { topic, _ -> activeTopic = topic },
                    onNavigateToBiblePicker = { navController.navigate("bible/books") },
                    onNavigateToReader = { book, chapter, verse ->
                        val vParam = if (verse != null) "?verse=$verse" else ""
                        navController.navigate("bible/${Uri.encode(book)}/$chapter$vParam")
                    },
                    onNavigateToDeclare = { declaration, verse, reference ->
                        createInitialState = Triple(declaration, verse, reference)
                        selectedTab = 2
                    },
                    modifier = Modifier.fillMaxSize()
                )
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
                        onSearchBible = { currentDeclaration ->
                            createInitialState = Triple(currentDeclaration, null, null)
                            navController.navigate("bible/books?caller=declare")
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                3 -> SavedScreen(
                    onNavigateToCreate = { selectedTab = 2 },
                    modifier = Modifier.fillMaxSize()
                )
                4 -> ProfileScreen(
                    onSignOut = {},
                    onOpenWallpapers = { showWallpaper = true },
                    onResetOnboarding = { showOnboarding = true },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}