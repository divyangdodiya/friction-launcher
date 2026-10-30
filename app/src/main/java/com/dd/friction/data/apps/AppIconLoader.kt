package com.dd.friction.data.apps

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.util.LruCache
import androidx.core.graphics.drawable.toBitmap
import com.dd.friction.di.IoDispatcher
import com.dd.friction.domain.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppIconLoader @Inject constructor(
    @ApplicationContext context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val packageManager = context.packageManager

    private val cache = object : LruCache<String, Bitmap>(MAX_CACHE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    suspend fun load(app: InstalledApp, sizePx: Int): Bitmap? {
        cache.get(app.key)?.let { return it }
        val bitmap = withContext(ioDispatcher) {
            runCatching {
                packageManager
                    .getActivityIcon(ComponentName(app.packageName, app.activityName))
                    .toBitmap(sizePx, sizePx)
            }.getOrNull()
        }
        bitmap?.let { cache.put(app.key, it) }
        return bitmap
    }

    private companion object {
        const val MAX_CACHE_BYTES = 8 * 1024 * 1024
    }
}
