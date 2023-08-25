package xcj.app.appsets.ui.compose.web

import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import xcj.app.appsets.ui.compose.MainViewModel

@Composable
fun WebComponent() {
    val viewModel = viewModel<MainViewModel>(LocalContext.current as AppCompatActivity)
    /*val heightState = animateDpAsState(
        targetValue = if(viewModel.url.value.isNullOrEmpty()){0.dp}else{610.dp}
    )*/
    Box(modifier = Modifier.padding(horizontal = 12.dp)) {
        Card(
            Modifier
                .height(610.dp)
                .fillMaxWidth()
        ) {
            Text(text = "网页浏览器")
            AndroidView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                factory = {
                    val webView = WebView(it)
                    webView.settings.apply {
                        javaScriptEnabled = true
                        //userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Mobile Safari/537.36"
                        useWideViewPort = true
                        loadWithOverviewMode = false
                        setSupportZoom(false)
                    }
                    webView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webView
                }
            ) {

                //it.loadUrl(viewModel.url.value ?: "")
            }
        }
    }
}