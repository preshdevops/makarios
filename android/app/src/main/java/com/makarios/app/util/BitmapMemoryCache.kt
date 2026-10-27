package com.makarios.app.util

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import java.util.Collections
import java.util.IdentityHashMap

/**
 * Shared preview cache backed by an [LruCache] sized by BYTES (about 1/8 of memoryClass),
 * capped at 6 entries, with memory-aware eviction and recycling.
 */
object BitmapMemoryCache {
    private const val MAX_ENTRIES = 6
    private val lock = Any()
    private val pinCounts = IdentityHashMap<Bitmap, Int>()
    private val evictedWhilePinned = Collections.newSetFromMap(IdentityHashMap<Bitmap, Boolean>())

    // Default to 1/8 of a standard 256MB memoryClass = 32MB
    private var maxSizeBytes: Int = 32 * 1024 * 1024
    private var cache = createCache(maxSizeBytes)

    private fun createCache(maxBytes: Int) = object : LruCache<String, Bitmap>(maxBytes) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount.coerceAtLeast(1)

        override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            if (oldValue !== newValue) recycleWhenUnused(oldValue)
        }
    }

    fun initialize(context: Context) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // 1/8 of device memoryClass in bytes
        val memoryClassBudget = (activityManager.memoryClass.toLong() * 1024L * 1024L / 8L)
            .coerceAtMost(Int.MAX_VALUE.toLong()).toInt().coerceAtLeast(1)
        synchronized(lock) {
            if (memoryClassBudget != maxSizeBytes) {
                cache.evictAll()
                maxSizeBytes = memoryClassBudget
                cache = createCache(memoryClassBudget)
            }
        }
    }

    private fun putIntoCache(key: String, bitmap: Bitmap) {
        val snapshot = cache.snapshot()
        // Cap at 6 entries: if full, evict oldest
        if (!snapshot.containsKey(key) && snapshot.size >= MAX_ENTRIES) {
            snapshot.keys.firstOrNull()?.let(cache::remove)
        }
        cache.put(key, bitmap)
    }

    operator fun get(key: String): Bitmap? = synchronized(lock) {
        cache.get(key)?.takeUnless { it.isRecycled }
    }

    operator fun set(key: String, bitmap: Bitmap) {
        put(key, bitmap)
    }

    fun getOrPut(context: Context, key: String, create: () -> Bitmap): Bitmap {
        synchronized(lock) { cache.get(key)?.takeUnless { it.isRecycled } }?.let { return it }
        val created = create()
        synchronized(lock) {
            val existing = cache.get(key)?.takeUnless { it.isRecycled }
            if (existing != null) {
                if (created !== existing && !created.isRecycled) created.recycle()
                return existing
            }
            if (created.byteCount <= maxSizeBytes) putIntoCache(key, created)
            return created
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        synchronized(lock) {
            if (bitmap.byteCount <= maxSizeBytes) putIntoCache(key, bitmap)
        }
    }

    fun pin(bitmap: Bitmap?) {
        if (bitmap == null || bitmap.isRecycled) return
        synchronized(lock) { pinCounts[bitmap] = (pinCounts[bitmap] ?: 0) + 1 }
    }

    fun unpin(bitmap: Bitmap?) {
        if (bitmap == null) return
        synchronized(lock) {
            val pins = pinCounts[bitmap] ?: return
            if (pins > 1) pinCounts[bitmap] = pins - 1
            else {
                pinCounts.remove(bitmap)
                if (evictedWhilePinned.remove(bitmap)) recycle(bitmap)
            }
        }
    }

    fun clear() {
        synchronized(lock) { cache.evictAll() }
    }

    private fun recycleWhenUnused(bitmap: Bitmap) {
        if ((pinCounts[bitmap] ?: 0) > 0) evictedWhilePinned.add(bitmap) else recycle(bitmap)
    }

    private fun recycle(bitmap: Bitmap) {
        if (!bitmap.isRecycled) bitmap.recycle()
    }
}