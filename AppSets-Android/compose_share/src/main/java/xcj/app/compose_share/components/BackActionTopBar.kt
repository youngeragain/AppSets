@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.compose_share.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

sealed interface BackActionModel {
    class Text() : BackActionModel
    class CustomContent() : BackActionModel
}

@Composable
fun BackActionTopBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState?,
    onBackClick: () -> Unit,
    backIcon: Int? = null,
    backButtonText: String? = null,
    endButtonText: String? = null,
    onEndButtonClick: (() -> Unit)? = null,
    customEndContent: (@Composable () -> Unit)? = null,
    hazeForEnd: Boolean = true
) {
    val boxModifier =
        if (
            !endButtonText.isNullOrEmpty() ||
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
                .align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val backIconBoxModifier = if (hazeState == null) {
                Modifier
                    .clip(CircleShape)
            } else {
                Modifier
                    .clip(CircleShape)
                    .hazeEffect(hazeState, HazeMaterials.thin())
            }
            Box(
                modifier = backIconBoxModifier
                    .clickable(onClick = onBackClick)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .padding(12.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = backIcon ?: xcj.app.compose_share.R.drawable.ic_arrow_back_24
                    ),
                    contentDescription = stringResource(xcj.app.compose_share.R.string.back)
                )
            }

            if (!backButtonText.isNullOrEmpty()) {
                Text(
                    text = backButtonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (!endButtonText.isNullOrEmpty() || customEndContent != null) {
            val endModifier = Modifier
                .align(Alignment.CenterEnd)
                .clip(CircleShape)
            if (hazeState != null && hazeForEnd) {
                endModifier

                    .hazeEffect(hazeState, HazeMaterials.thin())
            }
            Row(
                modifier = endModifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            ) {
                if (!endButtonText.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    onEndButtonClick?.invoke()
                                }
                            )
                            .padding(12.dp),
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