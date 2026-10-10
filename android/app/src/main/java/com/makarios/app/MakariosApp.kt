package com.makarios.app

import android.app.Application
import android.util.Log
import com.makarios.app.data.ShareRepository
import com.makarios.app.ui.wallpaper.StyleTypefaces
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

class MakariosApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Clean old share files (>24 hours) on startup
        ShareRepository.cleanupOldShareFiles(this)

        // Initialize fonts
        StyleTypefaces.init(this)

        // Debug uncaught exception handler writes to crash_log.txt
        if (BuildConfig.DEBUG) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                runCatching {
                    val logFile = File(filesDir, "crash_log.txt")
                    val sw = StringWriter()
                    throwable.printStackTrace(PrintWriter(sw))
                    logFile.writeText("Thread: ${thread.name}\nTime: ${System.currentTimeMillis()}\n$sw")
                }
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}
