package com.makarios.app.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.makarios.app.BuildConfig
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.ArrayDeque
import java.util.Date
import java.util.Locale

object CrashReporter {

    private const val TAG = "CrashReporter"
    private const val MAX_LOGS = 50
    private val logLock = Any()
    private val logBuffer = ArrayDeque<String>(MAX_LOGS)

    fun log(tag: String, message: String) {
        Timber.tag(tag).d("%s", message)
    }

    /** Record a non-fatal throwable into the crash-log buffer and Firebase Crashlytics. */
    fun logNonFatal(tag: String, throwable: Throwable) {
        val msg = "Non-fatal: "
        appendLines(tag, "\n")
        Timber.tag(tag).e(throwable, "%s", msg)
        runCatching {
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        val logText = if (throwable == null) message else "\n"
        appendLines(tag, logText)
        Log.e(tag, logText)
        if (throwable != null) {
            runCatching {
                com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(throwable)
            }
        }
    }

    fun install(application: Application) {
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                val actualTag = tag ?: TAG
                val logText = if (t == null) message else "$message\n${Log.getStackTraceString(t)}"
                appendLines(actualTag, logText)
                Log.println(priority, actualTag, logText)
            }
        })
        Timber.i("Crash log capture installed")

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val crashDir = File(application.filesDir, "crash")
                if (!crashDir.exists()) crashDir.mkdirs()
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
                sb.append(throwable.stackTraceToString()).append("\n\n")
                sb.append(PerformanceMetrics.reportLine()).append("\n")
                sb.append("--- Last 50 Log Lines ---\n")
                synchronized(logLock) {
                    logBuffer.forEach { line -> sb.append(line).append("\n") }
                }
                crashFile.writeText(sb.toString())
                runCatching {
                    com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance().recordException(throwable)
                }
            } catch (failure: Throwable) {
                Log.e(TAG, "Failed to write crash report", failure)
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }
    }

    private fun appendLines(tag: String, message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
        synchronized(logLock) {
            message.lineSequence().forEach { line ->
                if (logBuffer.size == MAX_LOGS) logBuffer.removeFirst()
                logBuffer.addLast("[$timestamp] $tag: $line")
            }
        }
    }

    fun hasCrashReport(context: Context): Boolean = File(context.filesDir, "crash/last.txt").exists()

    fun getCrashReport(context: Context): String? {
        val file = File(context.filesDir, "crash/last.txt")
        return if (file.exists()) runCatching { file.readText() }.getOrNull() else null
    }

    fun clearCrashReport(context: Context) {
        runCatching { File(context.filesDir, "crash/last.txt").delete() }
    }

    fun generateReportText(context: Context): String {
        val existing = getCrashReport(context)
        if (!existing.isNullOrBlank()) return existing

        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(Date())
        val sb = StringBuilder()
        sb.append("=== Makarios Diagnostic Report ===\n")
        sb.append("Time: $timeStr\n")
        sb.append("App Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})\n")
        sb.append("Device: ${Build.MANUFACTURER} ${Build.MODEL} (${Build.DEVICE})\n")
        sb.append("Android OS: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n\n")
        sb.append(PerformanceMetrics.reportLine()).append("\n\n")
        sb.append("--- Last 50 Log Lines ---\n")
        synchronized(logLock) {
            logBuffer.forEach { line -> sb.append(line).append("\n") }
        }
        return sb.toString()
    }

    fun sendCrashReportEmail(context: Context) {
        val report = generateReportText(context)
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@makarios.app"))
            putExtra(Intent.EXTRA_SUBJECT, "Makarios Diagnostic / Crash Report (${BuildConfig.VERSION_NAME})")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Send crash report"))
        } catch (_: Exception) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@makarios.app"))
                putExtra(Intent.EXTRA_SUBJECT, "Makarios Diagnostic / Crash Report (${BuildConfig.VERSION_NAME})")
                putExtra(Intent.EXTRA_TEXT, report)
            }
            try {
                context.startActivity(Intent.createChooser(sendIntent, "Send crash report"))
            } catch (_: Exception) {
                Toast.makeText(context, "No email app found to send report.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun triggerDebugCrash() {
        throw RuntimeException("Debug crash test: Makarios crash reporter verification.")
    }
}
