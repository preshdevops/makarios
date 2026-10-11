package com.makarios.app.util

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import com.makarios.app.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedDeque

object CrashReporter {

    private const val TAG = "CrashReporter"
    private const val MAX_LOGS = 50
    private val logBuffer = ConcurrentLinkedDeque<String>()

    fun log(tag: String, message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        val entry = "[$timestamp] $tag: $message"
        logBuffer.addLast(entry)
        while (logBuffer.size > MAX_LOGS) {
            logBuffer.pollFirst()
        }
        Log.d(tag, message)
    }

    fun install(application: Application) {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val crashDir = File(application.filesDir, "crash")
                if (!crashDir.exists()) {
                    crashDir.mkdirs()
                }
                val crashFile = File(crashDir, "last.txt")

                val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(Date())
                val sb = StringBuilder()
                sb.append("=== Makarios Crash Report ===\n")
                sb.append("Time: $timeStr\n")
                sb.append("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
                sb.append("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})\n")
                sb.append("Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
                sb.append("Thread: ${thread.name} (id: ${thread.id})\n\n")

                sb.append("--- Exception Stack Trace ---\n")
                sb.append(throwable.stackTraceToString())
                sb.append("\n\n")

                sb.append("--- Last 50 Log Lines ---\n")
                logBuffer.forEach { line ->
                    sb.append(line).append("\n")
                }

                crashFile.writeText(sb.toString())

                // Record to Firebase Crashlytics if available
                runCatching {
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(throwable)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to write crash report", e)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    fun hasCrashReport(context: Context): Boolean {
        return File(context.filesDir, "crash/last.txt").exists()
    }

    fun getCrashReport(context: Context): String? {
        val file = File(context.filesDir, "crash/last.txt")
        return if (file.exists()) {
            runCatching { file.readText() }.getOrNull()
        } else null
    }

    fun clearCrashReport(context: Context) {
        runCatching {
            File(context.filesDir, "crash/last.txt").delete()
        }
    }

    fun triggerDebugCrash() {
        throw RuntimeException("Debug crash test: Makarios crash reporter verification.")
    }
}
