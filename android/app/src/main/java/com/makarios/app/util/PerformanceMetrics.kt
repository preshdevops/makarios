package com.makarios.app.util

import android.app.ActivityManager
import android.content.Context
import android.os.SystemClock
import android.os.Debug
import java.util.concurrent.atomic.AtomicBoolean

object PerformanceTier {
    @Volatile var isLowRamDevice: Boolean = false
        private set

    @Volatile var animationsEnabled: Boolean = true
        private set

    val blurEnabled: Boolean get() = !isLowRamDevice
    val grainEnabled: Boolean get() = !isLowRamDevice

    fun initialize(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        isLowRamDevice = activityManager.isLowRamDevice || activityManager.memoryClass <= 256
        animationsEnabled = !isLowRamDevice
        BitmapMemoryCache.initialize(context)
    }
}

object PerformanceMetrics {
    private val startedAt = SystemClock.elapsedRealtime()
    private val firstFrameRecorded = AtomicBoolean(false)

    @Volatile private var memoryClassMb: Int = 0
    @Volatile private var firstFrameMs: Long = -1L
    @Volatile private var engineInitMs: Long = -1L

    fun initialize(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        memoryClassMb = activityManager.memoryClass
    }

    fun recordFirstFrame() {
        if (firstFrameRecorded.compareAndSet(false, true)) {
            firstFrameMs = SystemClock.elapsedRealtime() - startedAt
        }
    }

    fun recordEngineInitialization(durationMs: Long) {
        engineInitMs = durationMs
    }

    fun reportLine(): String {
        val runtime = Runtime.getRuntime()
        val usedHeapBytes = runtime.totalMemory() - runtime.freeMemory()
        return "Performance: memoryClass=${memoryClassMb}MB, heapUsed=${usedHeapBytes / (1024L * 1024L)}MB ($usedHeapBytes bytes), engineInitMs=$engineInitMs, firstFrameMs=$firstFrameMs"
    }
}