package com.app.hoverrobot

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.app.hoverrobot.ui.MainScreen
import com.app.hoverrobot.ui.theme.MyAppTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyAppTheme {
                val navController = rememberNavController()
                MainScreen(navController)
            }
        }
//        webViewSetup()
    }

    /* TODO: migrar a compose
    private fun webViewSetup(){

        val webView = findViewById<WebView>(R.id.webView)
        webView.webViewClient = WebViewClient()

        webView.apply {
            loadUrl("https://cdn.flowplayer.com/a30bd6bc-f98b-47bc-abf5-97633d4faea0/hls/de3f6ca7-2db3-4689-8160-0f574a5996ad/playlist.m3u8")
//            loadUrl("http://192.168.1.55:8090/stream/webrtc")
            settings.javaScriptEnabled = true
            settings.safeBrowsingEnabled = true
        }
    }
     */

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}