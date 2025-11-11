package dev.cluely.keyboard.ui.overlay

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import dev.cluely.keyboard.data.api.OpenRouterClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatOverlayService : Service() {

    @Inject
    lateinit var openRouterClient: OpenRouterClient

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val screenshotBase64 = intent?.getStringExtra("screenshot_base64") ?: ""

        if (screenshotBase64.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        createChatOverlay(screenshotBase64)
        return START_STICKY
    }

    private fun createChatOverlay(screenshotBase64: String) {
        try {
            val analysisState = mutableStateOf("Analyzing screenshot...")
            val chatState = mutableStateOf("")

            composeView = ComposeView(this).apply {
                setContent {
                    ChatOverlay(
                        initialAnalysis = analysisState.value,
                        chatResponse = chatState.value
                    )
                }
            }

            val layoutParams = WindowManager.LayoutParams().apply {
                type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
                }
                format = PixelFormat.TRANSLUCENT
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
                gravity = Gravity.TOP or Gravity.START
            }

            windowManager?.addView(composeView, layoutParams)

            // Analyze screenshot
            analysisState.value = "Analyzing..."
            serviceScope.launch {
                openRouterClient.analyzeScreenshot(screenshotBase64, null).onSuccess { analysis ->
                    analysisState.value = analysis
                    // Update compose view
                    composeView?.setContent {
                        ChatOverlay(
                            initialAnalysis = analysis,
                            chatResponse = chatState.value
                        )
                    }
                }.onFailure { error ->
                    analysisState.value = "Error: ${error.message}"
                    composeView?.setContent {
                        ChatOverlay(
                            initialAnalysis = "Error analyzing screenshot: ${error.message}",
                            chatResponse = ""
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ChatOverlayService", "Error creating overlay", e)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            if (composeView != null) {
                windowManager?.removeView(composeView)
            }
        } catch (e: Exception) {
            Log.e("ChatOverlayService", "Error removing overlay", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

@Composable
fun ChatOverlay(
    initialAnalysis: String,
    chatResponse: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, shape = MaterialTheme.shapes.medium)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Screen Analysis",
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                text = initialAnalysis,
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (chatResponse.isNotEmpty()) {
                Text(
                    text = "\nResponse:",
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = chatResponse,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}