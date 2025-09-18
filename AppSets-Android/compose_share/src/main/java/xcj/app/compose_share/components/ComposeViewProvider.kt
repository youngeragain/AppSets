package xcj.app.compose_share.components

import android.content.Context
import androidx.compose.ui.platform.ComposeView

interface ComposeViewProvider {
    fun provideComposeView(context: Context): ComposeView
}