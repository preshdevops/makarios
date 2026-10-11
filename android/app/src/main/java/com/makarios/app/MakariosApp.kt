package com.makarios.app

import android.app.Application
import android.util.Log
import com.makarios.app.data.ShareRepository
import com.makarios.app.ui.wallpaper.StyleTypefaces
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

import com.makarios.app.data.bible.BibleRepository
import com.makarios.app.util.CrashReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MakariosApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Install global crash reporter catching all uncaught exceptions
        CrashReporter.install(this)

        // Clean old share files (>24 hours) on startup
        ShareRepository.cleanupOldShareFiles(this)

        // Initialize fonts and Room database
        StyleTypefaces.init(this)
        com.makarios.app.data.local.KeptRepository.init(this)

        // Initialize Bible repository and warm up MatchEngine once in background
        CoroutineScope(Dispatchers.IO).launch {
            BibleRepository.initialize(this@MakariosApp)
            com.makarios.app.data.MatchEngine.getInstance(this@MakariosApp).initialize()
        }
    }
}
