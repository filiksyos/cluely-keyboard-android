package dev.cluely.keyboard.data.screenshot

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun captureScreen(context: Context) {
        val intent = Intent(context, ScreenshotService::class.java)
        context.startService(intent)
    }
}