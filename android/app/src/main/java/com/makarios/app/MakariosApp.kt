package com.makarios.app

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.res.Configuration as ResourceConfiguration
import androidx.work.Configuration
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.memory.MemoryCache
import com.makarios.app.data.ShareRepository
import com.makarios.app.ui.wallpaper.StyleTypefaces
import com.makarios.app.util.BitmapMemoryCache
import com.makarios.app.util.CrashReporter
import com.makarios.app.util.PerformanceMetrics
import com.makarios.app.util.PerformanceTier
import com.makarios.app.util.ReminderManager
import com.makarios.app.widget.WidgetScheduling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class MakariosApp : Application(), ComponentCallbacks2, ImageLoaderFactory, Configuration.Provider {
    private val deferredInitializationStarted = AtomicBoolean(false)

    companion object {
        /** Set in onCreate before any composition occurs. Used by KeptRepository for lazy DB access. */
        lateinit var instance: MakariosApp
            private set
    }

    // Required because WorkManagerInitializer is disabled in the manifest.
    // WorkManager.getInstance() would crash without this.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.ERROR)
            .build()

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        CrashReporter.install(this)
        // Initialize WorkManager manually since we disabled the auto-init ContentProvider.
        runCatching { WorkManager.initialize(this, workManagerConfiguration) }
            .onFailure { CrashReporter.logNonFatal("MakariosApp", it) }
        runCatching { PerformanceTier.initialize(this) }
            .onFailure { CrashReporter.logNonFatal("MakariosApp", it) }
        runCatching { PerformanceMetrics.initialize(this) }
            .onFailure { CrashReporter.logNonFatal("MakariosApp", it) }
        runCatching { BitmapMemoryCache.initialize(this) }
            .onFailure { CrashReporter.logNonFatal("MakariosApp", it) }
    }

    /**
     * Configure Coil with memory cache set to 15% of RAM and performance-tier animation aware crossfade.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.15)
                    .build()
            }
            .crossfade(PerformanceTier.animationsEnabled)
            .build()
    }

    /** Invoked from MainActivity after its first frame has been drawn. */
    suspend fun initializeAfterFirstFrame() = withContext(Dispatchers.Default) {
        if (!deferredInitializationStarted.compareAndSet(false, true)) return@withContext

        // Defer MatchEngine init to background coroutine after first frame
        launch(Dispatchers.Default) {
            runCatching { com.makarios.app.data.MatchEngine.getInstance(this@MakariosApp).initialize() }
                .onFailure { CrashReporter.logNonFatal("MakariosApp", it); Timber.e(it, "Deferred MatchEngine initialization failed") }
        }

        // Defer Bible index init to background coroutine after first frame
        launch(Dispatchers.IO) {
            runCatching { com.makarios.app.data.bible.BibleRepository.initialize(this@MakariosApp) }
                .onFailure { CrashReporter.logNonFatal("MakariosApp", it); Timber.e(it, "Deferred Bible initialization failed") }
        }

        // Defer widget init to background coroutine after first frame
        launch(Dispatchers.Default) {
            runCatching {
                WidgetScheduling.initialize(this@MakariosApp)
                WidgetScheduling.schedule(this@MakariosApp)
            }.onFailure { CrashReporter.logNonFatal("MakariosApp", it); Timber.e(it, "Deferred widget initialization failed") }
        }

        runCatching { ReminderManager.init(this@MakariosApp) }
            .onFailure { CrashReporter.logNonFatal("MakariosApp", it); Timber.e(it, "Deferred reminder initialization failed") }

        runCatching { com.makarios.app.util.DailyDeclarationWorker.scheduleAll(this@MakariosApp) }
            .onFailure { CrashReporter.logNonFatal("MakariosApp", it); Timber.e(it, "Deferred daily declaration scheduling failed") }

        runCatching { StyleTypefaces.init(this@MakariosApp) }
            .onFailure { CrashReporter.logNonFatal("MakariosApp", it); Timber.e(it, "Deferred typeface initialization failed") }

        // KeptRepository is now lazy — no init() call needed here.

        withContext(Dispatchers.IO) {
            runCatching { ShareRepository.cleanupOldShareFiles(this@MakariosApp) }
                .onFailure { CrashReporter.logNonFatal("MakariosApp", it); Timber.e(it, "Deferred share cleanup failed") }
        }
        Timber.i("Post-frame deferred initialization launched in background")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        BitmapMemoryCache.clear()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        BitmapMemoryCache.clear()
    }

    override fun onConfigurationChanged(newConfig: ResourceConfiguration) {
        super.onConfigurationChanged(newConfig)
    }
}