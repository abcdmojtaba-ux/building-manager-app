package com.mojtaba.building

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingBackupContent: String? = null

    // انتخاب فایل برای بازیابی پشتیبان
    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uris = if (result.resultCode == RESULT_OK) {
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
            } else null
            filePathCallback?.onReceiveValue(uris)
            filePathCallback = null
        }

    // ذخیره فایل پشتیبان (کاربر خودش محل ذخیره را انتخاب می‌کند)
    private val createDocLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            val content = pendingBackupContent
            if (result.resultCode == RESULT_OK && uri != null && content != null) {
                try {
                    contentResolver.openOutputStream(uri)?.use {
                        it.write(content.toByteArray(Charsets.UTF_8))
                    }
                    Toast.makeText(this, "فایل پشتیبان ذخیره شد", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "خطا در ذخیره فایل", Toast.LENGTH_LONG).show()
                }
            }
            pendingBackupContent = null
        }

    inner class AndroidBridge {
        @JavascriptInterface
        fun saveBackup(filename: String, content: String) {
            runOnUiThread {
                pendingBackupContent = content
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/json"
                    putExtra(Intent.EXTRA_TITLE, filename)
                }
                try {
                    createDocLauncher.launch(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@MainActivity, "برنامه‌ای برای ذخیره فایل پیدا نشد", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

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

        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                return handleExternalLink(url)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                callback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                return try {
                    fileChooserLauncher.launch(Intent.createChooser(intent, "انتخاب فایل پشتیبان"))
                    true
                } catch (e: Exception) {
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = null
                    false
                }
            }
        }

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
