package xcj.app.appsets.ui.compose.web

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

private const val TAG = "WebComponent"

private const val USER_AGENT_MOBILE =
    "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36"

@SuppressLint("ClickableViewAccessibility")
@Composable
fun WebComponent(title: String?, url: String) {
    Box(
        modifier = Modifier
            .padding()
            .fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = title ?: "",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            AndroidView(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                factory = ::webViewProvider
            ) {
                val overrideUrl = urlOverrider(title, url)
                it.loadUrl(overrideUrl)
            }
        }
    }
}

fun urlOverrider(title: String?, url: String): String {
    return if (url.contains("baidu.com") == true) {
        "https://www.baidu.com/s?word=$title"
    } else {
        url
    }
}

@SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
fun webViewProvider(context: Context): WebView {
    val webView = WebView(context)
    webView.settings.apply {
        javaScriptEnabled = true
        useWideViewPort = false
        userAgentString = USER_AGENT_MOBILE
        loadWithOverviewMode = false
        setSupportZoom(false)
    }
    webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            view?.loadUrl(request?.url.toString())
            return true
        }
    }
    webView.requestFocus()
    webView.layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    webView.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val canScrollVerticallyUp = webView.canScrollVertically(-1)
                if (!canScrollVerticallyUp) {
                    webView.parent.requestDisallowInterceptTouchEvent(false)
                }
                val canScrollVerticallyDown = webView.canScrollVertically(1)

                if (canScrollVerticallyUp or canScrollVerticallyDown) {
                    webView.parent.requestDisallowInterceptTouchEvent(true)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                webView.parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        return@setOnTouchListener false
    }
    return webView
}