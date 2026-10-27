package com.makarios.app

import android.content.Context
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.makarios.app.data.AffirmationRepository
import com.makarios.app.data.local.KeptEntity
import com.makarios.app.data.local.KeptRepository
import com.makarios.app.ui.screens.*
import com.makarios.app.ui.screens.bible.BiblePickerScreen
import com.makarios.app.ui.screens.bible.BibleReaderScreen
import com.makarios.app.ui.theme.*
import com.makarios.app.ui.wallpaper.WallpaperGallery
import com.makarios.app.util.CrashReporter
import com.makarios.app.util.PerformanceMetrics
import com.makarios.app.util.ReminderManager
import com.makarios.app.util.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : ComponentActivity() {
    private val launchIntentFlow = MutableStateFlow<Intent?>(null)
    private val firstFrameHandled = AtomicBoolean(false)

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

        launchIntentFlow.value = intent
        val hasCrash = CrashReporter.hasCrashReport(this)
        val crashReportText = if (hasCrash) CrashReporter.getCrashReport(this) else null

        setContent {
            MakariosTheme(light = com.makarios.app.ui.theme.Light.forNow()) {
                var showCrashReport by remember { mutableStateOf(crashReportText != null) }

                val currentIntent by launchIntentFlow.collectAsState()
                if (showCrashReport && crashReportText != null) {
                    CrashReportScreen(
                        reportText = crashReportText,
                        onDismiss = {
                            CrashReporter.clearCrashReport(this@MainActivity)
                            showCrashReport = false
                        }
                    )
                } else {
                    val launch = currentIntent ?: intent
                    val affirmationIdFromIntent = launch?.data?.getQueryParameter("id")
                        ?: launch?.data?.lastPathSegment?.takeIf { launch.data?.scheme == "makarios" }
                        ?: launch?.getStringExtra("id")
                        ?: launch?.getStringExtra(ReminderManager.EXTRA_ID)
                    val affirmationOrigin = launch?.getStringExtra("source")
                        ?: launch?.data?.getQueryParameter("source")
                        ?: if (launch?.action == Intent.ACTION_VIEW) "widget" else "reminder"

                    val isSettingsIntent = currentIntent?.action == "android.settings.APP_NOTIFICATION_SETTINGS" ||
                        currentIntent?.action == "android.intent.action.APPLICATION_PREFERENCES" ||
                        currentIntent?.getStringExtra("open_settings") == "reminders"
                    val initialSettingsTab = if (isSettingsIntent) 1 else null

                    AppNavigation(
                        initialAffirmationId = affirmationIdFromIntent,
                        initialSettingsTab = initialSettingsTab,
                        initialAffirmationSource = affirmationOrigin
                    )
                }
            }
        }
        schedulePostFrameInitialization()
    }

    private fun schedulePostFrameInitialization() {
        val decor = window.decorView
        val listener = object : ViewTreeObserver.OnDrawListener {
            override fun onDraw() {
                if (!firstFrameHandled.compareAndSet(false, true)) return
                decor.postOnAnimation {
                    if (decor.viewTreeObserver.isAlive) decor.viewTreeObserver.removeOnDrawListener(this)
                    PerformanceMetrics.recordFirstFrame()
                    lifecycleScope.launch(Dispatchers.Default) {
                        try {
                            (application as MakariosApp).initializeAfterFirstFrame()
                        } catch (t: Throwable) {
                            CrashReporter.logNonFatal("MainActivity", t)
                        }
                    }
                }
            }
        }
        decor.viewTreeObserver.addOnDrawListener(listener)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchIntentFlow.value = intent
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                com.makarios.app.data.MatchEngine.retryPendingInstanceOnForeground()
            } catch (t: Throwable) {
                CrashReporter.logNonFatal("MainActivity", t)
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") // Edge-to-edge: content draws under the bar by design; the bottom bar is the only inset consumer.
@Composable
fun AppNavigation(
    initialAffirmationId: String? = null,
    initialSettingsTab: Int? = null,
    initialAffirmationSource: String = "reminder"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userLight by com.makarios.app.data.UserLightStore.light(context).collectAsState(initial = Light.forNow())

    ProvideUserLight(
        light = userLight,
        setLight = { chosen ->
            scope.launch {
                try {
                    com.makarios.app.data.UserLightStore.set(context, chosen)
                } catch (t: Throwable) {
                    CrashReporter.logNonFatal("AppNavigation", t)
                }
            }
        }
    ) {
      MakariosTheme(light = userLight) {
        val navController = rememberNavController()

        var activeLightState by remember { mutableStateOf(userLight) }
        LaunchedEffect(userLight) { activeLightState = userLight }
        var activeRouteName by remember { mutableStateOf("Today") }
        var selectedTab by rememberSaveable { mutableStateOf(0) }
        var createStep by rememberSaveable { mutableStateOf(1) }
        var isFullScreenVerse by remember { mutableStateOf(initialAffirmationId != null) }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentNavRoute = navBackStackEntry?.destination?.route ?: "main"
        val isBibleRoute = currentNavRoute.startsWith("bible")

        // The bar hides only inside the Create flow after step 1 and in full-screen verse view.
        val hideBar = (currentNavRoute == "main" && selectedTab == 2 && createStep > 1) || isFullScreenVerse

        CompositionLocalProvider(
            LocalActiveLightUpdater provides { newLight, routeName ->
                activeLightState = newLight
                activeRouteName = routeName
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (!hideBar) {
                        MakariosBottomBar(
                            selectedTab = if (isBibleRoute) -1 else selectedTab,
                            activeLight = activeLightState,
                            currentRoute = activeRouteName,
                            onTabSelected = { index ->
                                if (isBibleRoute) {
                                    navController.popBackStack("main", inclusive = false)
                                }
                                selectedTab = index
                            }
                        )
                    }
                }
            ) { _ ->
                NavHost(
                    navController = navController,
                    startDestination = "main"
                ) {
                    composable("main") {
                        MainAppContent(
                            navController = navController,
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it },
                            createStep = createStep,
                            onCreateStepChange = { createStep = it },
                            onFullScreenVerseChange = { isFullScreenVerse = it },
                            initialAffirmationId = initialAffirmationId,
                            initialSettingsTab = initialSettingsTab,
                            initialAffirmationSource = initialAffirmationSource
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
                                navController.navigate("bible/${Uri.encode(book)}/$chapter")
                            },
                            onVerseSelected = { selection ->
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_text", selection.text)
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_verse_ref", selection.reference)
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(
                        route = "bible/{book}/{chapter}?verse={verse}&pick={pick}",
                        arguments = listOf(
                            navArgument("book") { type = NavType.StringType },
                            navArgument("chapter") { type = NavType.IntType },
                            navArgument("verse") {
                                type = NavType.IntType
                                defaultValue = -1
                            },
                            navArgument("pick") {
                                type = NavType.IntType
                                defaultValue = 0
                            }
                        )
                    ) { backStackEntry ->
                        val rawBook = backStackEntry.arguments?.getString("book") ?: "Romans"
                        val book = Uri.decode(rawBook)
                        val chapter = backStackEntry.arguments?.getInt("chapter") ?: 8
                        val verseArg = backStackEntry.arguments?.getInt("verse") ?: -1
                        val verse = if (verseArg > 0) verseArg else null
                        val pickMode = (backStackEntry.arguments?.getInt("pick") ?: 0) == 1

                        BibleReaderScreen(
                            bookName = book,
                            chapterNumber = chapter,
                            initialVerse = verse,
                            pickMode = pickMode,
                            onNavigateBack = { navController.popBackStack() },
                            onOpenPicker = { navController.navigate("bible/books") },
                            onDeclareThis = { selection ->
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
                                navController.navigate("bible/${Uri.encode(book)}/$chapter?pick=1")
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
        }
      }
    }
}

@Composable
fun MainAppContent(
    navController: NavHostController,
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    createStep: Int,
    onCreateStepChange: (Int) -> Unit,
    onFullScreenVerseChange: (Boolean) -> Unit,
    initialAffirmationId: String? = null,
    initialSettingsTab: Int? = null,
    initialAffirmationSource: String = "reminder"
) {
    val context = LocalContext.current
    val setUserLight = LocalSetLight.current
    var showWallpaper by remember { mutableStateOf(false) }
    var deepLinkId by remember(initialAffirmationId) { mutableStateOf(initialAffirmationId) }
    var deepLinkOrigin by remember(initialAffirmationSource) { mutableStateOf(initialAffirmationSource) }
    var settingsTab by remember(initialSettingsTab) { mutableStateOf(initialSettingsTab) }

    LaunchedEffect(deepLinkId) {
        onFullScreenVerseChange(deepLinkId != null)
    }

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
            onSelectTab(2) // Switch to Declare tab
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
    var deepLinkShareContent by remember { mutableStateOf<com.makarios.app.ui.components.ShareContent?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_START || event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    try {
                        com.makarios.app.data.AuthManager.reloadUser()
                    } catch (e: com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                        showAuthChoose = true
                    } catch (t: Throwable) {
                        CrashReporter.logNonFatal("MainActivity", t)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showAuthChoose) {
        AuthFlowHost(
            onContinueToApp = { showAuthChoose = false }
        )
        return
    }

    if (showWallpaper) {
        WallpaperGallery(onBack = { showWallpaper = false })
        return
    }

    if (settingsTab != null) {
        BackHandler { settingsTab = null }
        WidgetRemindersSettingsScreen(
            initialTab = settingsTab ?: 0,
            onNavigateBack = { settingsTab = null }
        )
        return
    }

    // Back handling for topic and tabs
    if (activeTopic != null) {
        BackHandler { activeTopic = null }
    } else if (selectedTab != 0) {
        BackHandler {
            if (selectedTab == 2) {
                createInitialState = null
                onCreateStepChange(1)
            }
            onSelectTab(0)
        }
    }

    if (activeTopic != null) {
        TopicDetailScreen(
            topicName = activeTopic ?: "",
            onNavigateBack = { activeTopic = null },
            onDeclareThis = { declaration, verseText, reference ->
                activeTopic = null
                createInitialState = Triple(declaration, verseText, reference)
                onSelectTab(2)
            },
            onReadChapter = { book, chapter, verse ->
                val vParam = "?verse=$verse"
                navController.navigate("bible/${Uri.encode(book)}/$chapter$vParam")
            },
            modifier = Modifier.fillMaxSize()
        )

    } else {
        when (selectedTab) {
            0 -> HomeScreen(
                modifier = Modifier.fillMaxSize(),
                initialAffirmationId = initialAffirmationId,
                onLightChanged = { chosen -> setUserLight(chosen) },
                onNavigateToReader = { book, chapter, verse ->
                    val vParam = if (verse != null) "?verse=$verse" else ""
                    navController.navigate("bible/${Uri.encode(book)}/$chapter$vParam")
                }
            )
            1 -> ExploreScreen(
                onNavigateToTopic = { topic, _ -> activeTopic = topic },
                onNavigateToBiblePicker = { val b = com.makarios.app.data.bible.BibleReaderPrefs.getLastBook(context); val c = com.makarios.app.data.bible.BibleReaderPrefs.getLastChapter(context); navController.navigate("bible/${Uri.encode(b)}/$c") },
                onNavigateToReader = { book, chapter, verse ->
                    val vParam = if (verse != null) "?verse=$verse" else ""
                    navController.navigate("bible/${Uri.encode(book)}/$chapter$vParam")
                },
                onNavigateToDeclare = { declaration, verse, reference ->
                    createInitialState = Triple(declaration, verse, reference)
                    onCreateStepChange(1)
                    onSelectTab(2)
                },
                modifier = Modifier.fillMaxSize()
            )
            2 -> CreateScreen(
                onNavigateBack = {
                    onSelectTab(0)
                    createInitialState = null
                    onCreateStepChange(1)
                },
                initialDeclaration = createInitialState?.first,
                initialVerseText = createInitialState?.second,
                initialReference = createInitialState?.third,
                initialStep = 1,
                onStepChange = { onCreateStepChange(it) },
                onSearchBible = { q -> navController.navigate("bible/search?q=${Uri.encode(q)}") },
                onOpenWidgetSettings = { settingsTab = 0 },
                onOpenReminderSettings = { settingsTab = 1 },
                modifier = Modifier.fillMaxSize()
            )
            3 -> SavedScreen(
                onNavigateToCreate = {
                    createInitialState = null
                    onCreateStepChange(1)
                    onSelectTab(2)
                },
                onOpenWidgetSettings = { settingsTab = 0 },
                onOpenReminderSettings = { settingsTab = 1 },
                modifier = Modifier.fillMaxSize()
            )
            4 -> ProfileScreen(
                onSignOut = {},
                onOpenWallpapers = { showWallpaper = true },
                onResetOnboarding = { showOnboarding = true },
                onOpenWidgetSettings = { settingsTab = 0 },
                onOpenReminderSettings = { settingsTab = 1 },
                onOpenMakeItYours = { settingsTab = 0 },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (deepLinkId != null) {
        BackHandler { deepLinkId = null; onSelectTab(0) }
        DeclarationViewScreen(
            id = deepLinkId ?: "",
            origin = deepLinkOrigin,
            onClose = { deepLinkId = null; onSelectTab(0) },
            onNext = {
                val pool = AffirmationRepository.curatedAffirmations
                deepLinkId = (pool.filter { it.id != deepLinkId }.randomOrNull()
                    ?: AffirmationRepository.affirmationOfTheDay).id
            },
            onShare = { affirmation, light ->
                deepLinkShareContent = com.makarios.app.ui.components.ShareContent(
                    declaration = affirmation.declaration,
                    verse = affirmation.scriptureText,
                    reference = affirmation.reference,
                    initialLightName = light.name,
                    contentType = "curated",
                    declarationId = affirmation.id
                )
            },
            onKeep = { affirmation, light ->
                coroutineScope.launch {
                    try {
                        val existing = KeptRepository.getById(affirmation.id)
                        if (existing?.deletedAt == null) {
                            if (existing != null) KeptRepository.softDelete(affirmation.id)
                        } else {
                            KeptRepository.restore(affirmation.id)
                        }
                        if (existing == null) KeptRepository.save(
                            KeptEntity(
                                id = affirmation.id,
                                declaration = affirmation.declaration,
                                verseText = affirmation.scriptureText,
                                reference = affirmation.reference,
                                lightName = light.name,
                                styleId = "scene",
                                sourceType = "curated"
                            )
                        )
                    } catch (t: Throwable) {
                        CrashReporter.logNonFatal("MainActivity", t)
                    }
                }
            },
            onDeclare = { affirmation ->
                createInitialState = Triple(affirmation.declaration, affirmation.scriptureText, affirmation.reference)
                deepLinkId = null
                onCreateStepChange(1)
                onSelectTab(2)
            },
            onWallpaper = { deepLinkId = null; showWallpaper = true },
            modifier = Modifier.fillMaxSize()
        )

        deepLinkShareContent?.let { shareContent ->
            com.makarios.app.ui.components.ShareSheet(
                content = shareContent,
                onDismiss = { deepLinkShareContent = null }
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
private fun MakariosBottomBar(
    selectedTab: Int,
    activeLight: Light,
    currentRoute: String,
    onTabSelected: (Int) -> Unit
) {
    var showDebugInspector by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            TabItem("Today", Icons.Filled.Home, Icons.Outlined.Home),
            TabItem("Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
            TabItem("Declare", Icons.Filled.Add, Icons.Filled.Add),
            TabItem("Saved", Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder),
            TabItem("You", Icons.Filled.Person, Icons.Outlined.Person)
        )
    }

    val animatedBottomColor by animateColorAsState(
        targetValue = activeLight.bottom,
        animationSpec = tween(200),
        label = "navBottomColor"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = activeLight.text,
        animationSpec = tween(200),
        label = "navTextColor"
    )
    val animatedUnselectedTextColor by animateColorAsState(
        targetValue = activeLight.text.copy(alpha = 0.60f),
        animationSpec = tween(200),
        label = "navUnselectedColor"
    )

    val declareFill = if (activeLight.isDark) Color(0xFFC9964A) else Color(0xFF8A5A14)
    val declareIconColor = activeLight.top

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(animatedBottomColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        if (BuildConfig.DEBUG) {
                            showDebugInspector = true
                        }
                    }
                )
            }
    ) {
        // Hairline on top of the bar: 1dp, light.text at 10% alpha
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(animatedTextColor.copy(alpha = 0.10f))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isDeclare = index == 2
                val isSelected = selectedTab == index

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(onClick = { onTabSelected(index) }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isDeclare) {
                        // Declare: centre button, plus icon, slightly raised, the one filled accent
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .offset(y = (-4).dp)
                                .clip(CircleShape)
                                .background(declareFill),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.selectedIcon,
                                contentDescription = tab.label,
                                tint = declareIconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = tab.label,
                            color = animatedTextColor,
                            fontFamily = HankenGroteskFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            modifier = Modifier.offset(y = (-2).dp)
                        )
                    } else {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = if (isSelected) animatedTextColor else animatedUnselectedTextColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            color = if (isSelected) animatedTextColor else animatedUnselectedTextColor,
                            fontFamily = HankenGroteskFontFamily,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }

    if (showDebugInspector && BuildConfig.DEBUG) {
        val pageBottomHex = activeLight.bottom.toHex()
        AlertDialog(
            onDismissRequest = { showDebugInspector = false },
            confirmButton = {
                TextButton(onClick = { showDebugInspector = false }) {
                    Text("Dismiss", color = activeLight.text)
                }
            },
            title = {
                Text("Bottom Nav / Light Inspector", style = MakariosTypography.titleMedium, color = activeLight.text)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Route: $currentRoute", color = activeLight.text)
                    Text("Light: ${activeLight.name}", color = activeLight.text)
                    Text("Page Bottom Hex: $pageBottomHex", color = activeLight.text)
                    Text("Bar Hex: $pageBottomHex", color = activeLight.text)
                }
            },
            containerColor = activeLight.top,
            textContentColor = activeLight.text
        )
    }
}

private fun Color.toHex(): String {
    val a = (alpha * 255).toInt()
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format(java.util.Locale.US, "#%02X%02X%02X%02X", a, r, g, b)
}
