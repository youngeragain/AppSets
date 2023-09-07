package xcj.app.appsets.ui.compose.outside

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun X18ContentConfirmDialog(
    isShowState: MutableState<Boolean>,
    onConfirmClick: (() -> Unit)?
) {
    if (isShowState.value) {
        Dialog(onDismissRequest = {
            isShowState.value = false
        }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 250.dp, max = 450.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(22.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = "提示")
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "内容可能受限,继续查看?")
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = {
                        isShowState.value = false
                        onConfirmClick?.invoke()
                    }) {
                        Text(text = "确定")
                    }
                }
            }
        }
    }
}