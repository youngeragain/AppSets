package xcj.app.compose_addons.sage

import android.content.Context
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

class SageComposePlugins : AbstractComposeMethods() {

    private val mStateHolder: SageStatesHolder = SageStatesHolder()

    override fun getStatesHolder(): StatesHolder {
        return mStateHolder
    }

    override fun content(context: Context): ComposeView {
        val composeView = ComposeView(context)
        composeView.setContent {
            RealContent(
                isCheckToday = mStateHolder.isCheckToday.value,
                checkedDays = mStateHolder.checkedDays.value,
                maxContinuousInterval = mStateHolder.maxContinuousInterval.value
            )
        }
        return composeView
    }

    @Composable
    fun RealContent(
        isCheckToday: Boolean,
        checkedDays: Int,
        maxContinuousInterval: Int
    ) {
        Column(
            modifier = Modifier
                .widthIn(240.dp, 360.dp)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = "贤者时间",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (isCheckToday)
                            return@clickable
                        mStateHolder.setChecked()
                    },
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    Text(
                        text = "\uD83E\uDDD8 已打卡${checkedDays}次 | 最长连续${maxContinuousInterval}次",
                        Modifier.padding(12.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val text = if (isCheckToday) {
                            "今日已打卡"
                        } else {
                            "打卡"
                        }
                        Text(text = text, fontWeight = FontWeight.Bold)
                    }
                }
            }

        }
    }
}