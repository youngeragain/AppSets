package xcj.app.compose_share.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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

@Composable
fun BackActionTopBar(
    modifier: Modifier = Modifier,
    @DrawableRes
    backIcon: Int? = null,
    backButtonRightText: String? = null,
    endButtonText: String? = null,
    onBackClick: () -> Unit,
    onEndButtonClick: (() -> Unit)? = null,
    customEndContent: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier.statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        id = backIcon ?: xcj.app.compose_share.R.drawable.ic_arrow_back_24
                    ),
                    contentDescription = "go back",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onBackClick)
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
                    FilledTonalButton(
                        onClick = {
                            onEndButtonClick?.invoke()
                        }
                    ) {
                        Text(text = endButtonText)
                    }
                } else {
                    if (customEndContent != null) {
                        customEndContent()
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}