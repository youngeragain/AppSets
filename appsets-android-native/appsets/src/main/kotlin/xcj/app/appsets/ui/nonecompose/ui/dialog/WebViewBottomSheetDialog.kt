package xcj.app.appsets.ui.nonecompose.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import xcj.app.appsets.ui.compose.theme.AppSetsTheme

class WebViewBottomSheetDialog(
    private val url:String,
    val onClick:(DialogFragment, Boolean)->Unit={ _, _->}
): BottomSheetDialogFragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext())
        composeView.setContent {
            AppSetsTheme {
                WebViewPopupPage(url)
            }
        }
        return composeView
    }
}

@Composable
fun WebViewPopupPage(url:String) {
    AndroidView(factory = {
        val webView = WebView(it)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView
    }, Modifier.fillMaxSize()){
        it.loadUrl(url)
    }
}