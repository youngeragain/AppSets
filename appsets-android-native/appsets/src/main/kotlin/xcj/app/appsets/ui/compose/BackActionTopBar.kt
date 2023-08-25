package xcj.app.appsets.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.R

@Composable
fun BackActionTopBar(
    backButtonRightText: String? = null,
    endButtonText: String? = null,
    onBackAction: () -> Unit,
    onConfirmClick: (() -> Unit)? = null
) {
    Column {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_round_arrow_24),
                    contentDescription = "go back",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onBackAction)
                        .padding(12.dp)
                )
                if (!backButtonRightText.isNullOrEmpty()) {
                    Text(
                        text = backButtonRightText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (!endButtonText.isNullOrEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 12.dp)
                ) {
                    Button(onClick = {
                        onConfirmClick?.invoke()
                    }) {
                        Text(text = "确认")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Divider(modifier = Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
    }
}