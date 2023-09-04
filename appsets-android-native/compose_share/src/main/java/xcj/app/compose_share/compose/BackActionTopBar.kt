package xcj.app.compose_share.compose

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
import androidx.compose.material3.Icon
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
import xcj.app.compose_share.R

@Composable
fun BackActionTopBar(
    backButtonRightText: String? = null,
    endButtonText: String? = null,
    onBackAction: () -> Unit,
    onEndButtonClick: (() -> Unit)? = null,
    customEndContent: (@Composable () -> Unit)? = null,
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
                Icon(
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 12.dp)
            ) {
                if (customEndContent == null && !endButtonText.isNullOrEmpty()) {
                    Button(onClick = {
                        onEndButtonClick?.invoke()
                    }) {
                        Text(text = endButtonText)
                    }
                } else {
                    if (customEndContent != null) {
                        customEndContent()
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Divider(modifier = Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
    }
}