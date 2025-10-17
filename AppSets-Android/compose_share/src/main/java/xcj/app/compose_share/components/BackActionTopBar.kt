@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.compose_share.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@Composable
fun BackActionTopBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    backIcon: Int? = null,
    backButtonText: String? = null,
    customBackButtonContent: (@Composable () -> Unit)? = null,
    centerText: String? = null,
    customCenterContent: (@Composable () -> Unit)? = null,
    endButtonText: String? = null,
    onBackClick: () -> Unit,
    onEndButtonClick: (() -> Unit)? = null,
    customEndContent: (@Composable () -> Unit)? = null,
) {
    val boxModifier =
        if (!centerText.isNullOrEmpty() ||
            !endButtonText.isNullOrEmpty() ||
            customCenterContent != null ||
            customEndContent != null
        ) {
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp)
        } else {
            modifier
                .statusBarsPadding()
                .padding(horizontal = 12.dp)
        }
    Box(
        modifier = boxModifier
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .hazeEffect(hazeState, HazeMaterials.thin())
                .clickable(onClick = onBackClick)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = backIcon ?: xcj.app.compose_share.R.drawable.ic_arrow_back_24
                ),
                contentDescription = stringResource(xcj.app.compose_share.R.string.back)
            )
            if (!backButtonText.isNullOrEmpty() || customBackButtonContent != null) {
                if (!backButtonText.isNullOrEmpty()) {
                    Text(
                        text = backButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else if (customBackButtonContent != null) {
                    customBackButtonContent()
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
        if (!centerText.isNullOrEmpty() || customCenterContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .hazeEffect(hazeState, HazeMaterials.thin())
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)

            ) {
                Row(
                    modifier = Modifier.padding(12.dp)
                ) {
                    if (!centerText.isNullOrEmpty()) {
                        Text(
                            text = centerText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (customCenterContent != null) {
                        customCenterContent()
                    }
                }
            }
        }

        if (!endButtonText.isNullOrEmpty() || customEndContent != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clip(CircleShape)
                    .hazeEffect(hazeState, HazeMaterials.thin())
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            ) {
                if (!endButtonText.isNullOrEmpty()) {
                    TextButton(
                        onClick = {
                            onEndButtonClick?.invoke()
                        }
                    ) {
                        Text(text = endButtonText)
                    }
                } else if (customEndContent != null) {
                    customEndContent()
                }
            }
        }
    }

}