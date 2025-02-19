package xcj.app.appsets.ui.compose.media.video

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp

@SuppressLint("UnsafeOptInUsageError")
class DesignBufferView(context: Context) : FrameLayout(context) {
    init {
        val composeView = ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            setContent {
                BufferViewComposeContent()
            }
        }
        addView(composeView)
    }

    @Composable
    fun BufferViewComposeContent() {
        LinearProgressIndicator(
            modifier = Modifier.size(width = 98.dp, height = 6.dp),
            color = MaterialTheme.colorScheme.surface,
            trackColor = MaterialTheme.colorScheme.onSurface
        )
    }
}