package com.mojtaba.building

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.displayZoomControls = false
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.textZoom = 100

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleExternalLink(url)
            }
        }
        webView.webChromeClient = WebChromeClient()

        // Load the HTML from assets (offline)
        webView.loadUrl("file:///android_asset/index.html")
    }

    // لینک‌های sms: و tel: و ... را به برنامه‌های گوشی می‌دهد
    private fun handleExternalLink(url: String): Boolean {
        if (url.startsWith("sms:") || url.startsWith("smsto:") ||
            url.startsWith("tel:") || url.startsWith("mailto:")
        ) {
            try {
                if (url.startsWith("sms:") || url.startsWith("smsto:")) {
                    // جدا کردن شماره و متن پیامک
                    val withoutScheme = url.substringAfter(":")
                    val number = withoutScheme.substringBefore("?")
                    val body = if (withoutScheme.contains("body="))
                        Uri.decode(withoutScheme.substringAfter("body=").substringBefore("&"))
                    else ""

                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:$number")
                        putExtra("sms_body", body)
                    }
                    startActivity(intent)
                } else {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "برنامه‌ای برای این کار پیدا نشد", Toast.LENGTH_SHORT).show()
            }
            return true
        }
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
