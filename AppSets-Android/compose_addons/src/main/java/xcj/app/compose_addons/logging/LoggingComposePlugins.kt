package xcj.app.compose_addons.logging

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.compose_share.dynamic.AbstractComposeMethods
import xcj.app.compose_share.dynamic.StatesHolder

class LoggingComposePlugins : AbstractComposeMethods() {
    private val loggingStatesHolder: LoggingStatesHolder = LoggingStatesHolder()
    override fun getStatesHolder(): StatesHolder {
        return loggingStatesHolder
    }

    override fun content(context: Context): ComposeView {
        return ComposeView(context).apply {
            setContent {
                RealContent()
            }
        }
    }

    @Composable
    fun RealContent() {
        Column(
            modifier = Modifier
                .widthIn(240.dp, 360.dp)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = "后台日志输出",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {

                }
            }

        }
    }
}