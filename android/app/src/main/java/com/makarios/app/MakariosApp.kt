package com.makarios.app

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.makarios.app.data.ShareRepository
import com.makarios.app.data.bible.BibleRepository
import com.makarios.app.ui.wallpaper.StyleTypefaces
import com.makarios.app.util.CrashReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MakariosApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()

        // Install global crash reporter catching all uncaught exceptions
        CrashReporter.install(this)

        // Initialize WorkManager defensively
        runCatching {
            WorkManager.initialize(this, workManagerConfiguration)
        }.onFailure { Log.e("MakariosApp", "WorkManager manual init skipped or failed", it) }

        // Clean old share files (>24 hours) on startup
        runCatching {
            ShareRepository.cleanupOldShareFiles(this)
        }.onFailure { Log.e("MakariosApp", "Share cleanup failed", it) }

        // Initialize fonts and Room database
        runCatching {
            StyleTypefaces.init(this)
        }.onFailure { Log.e("MakariosApp", "StyleTypefaces init failed", it) }

        runCatching {
            com.makarios.app.data.local.KeptRepository.init(this)
        }.onFailure { Log.e("MakariosApp", "KeptRepository init failed", it) }

        // Initialize Bible repository and warm up MatchEngine once in background
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                BibleRepository.initialize(this@MakariosApp)
            }.onFailure { Log.e("MakariosApp", "BibleRepository background init failed", it) }

            runCatching {
                com.makarios.app.data.MatchEngine.getInstance(this@MakariosApp).initialize()
            }.onFailure { Log.e("MakariosApp", "MatchEngine background init failed", it) }
        }
    }
}
