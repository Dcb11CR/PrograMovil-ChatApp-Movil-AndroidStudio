package com.ulatina.chatapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         val webUrl = "https://programovil.net"        // PRODUCCIÓN (Azure Static Web Apps)

        setContent {
            var webViewRef by remember { mutableStateOf<WebView?>(null) }

            // Botón atrás: navegar dentro del WebView
            BackHandler(enabled = (webViewRef?.canGoBack() == true)) {
                webViewRef?.goBack()
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        WebView.setWebContentsDebuggingEnabled(true)
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.userAgentString = settings.userAgentString + " AndroidWebView"


                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                // Mantener navegación dentro del WebView
                                return false
                            }
                        }

                        loadUrl(webUrl)
                        webViewRef = this
                    }
                }
            )
        }
    }
}
