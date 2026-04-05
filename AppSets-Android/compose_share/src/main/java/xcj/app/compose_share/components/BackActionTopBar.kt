@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.compose_share.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import xcj.app.compose_share.modifier.hazeEffectIfAvailable

sealed interface BackActionModel {
    class Text() : BackActionModel
    class CustomContent() : BackActionModel
}


@Preview(showBackground = true)
@Composable
fun BackActionTopBarPreview() {
    BackActionTopBar(
        hazeState = null,
        onBackClick = {

        },
        backButtonText = "Back",
        endButtonText = "OK"
    )
}

@OptIn(ExperimentalLayoutApi::class)
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
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                        .calculateTopPadding()
                )
        } else {
            modifier
                .statusBarsPadding()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                        .calculateTopPadding()
                )
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

            val backIconBoxModifier = Modifier
                .clip(CircleShape)
                .hazeEffectIfAvailable(hazeState, HazeMaterials.thin())
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

        if (!endButtonText.isNullOrEmpty()) {
            val endModifier =
                if (hazeState != null && hazeForEnd) {
                    Modifier
                        .align(Alignment.CenterEnd)
                        .clip(CircleShape)
                        .hazeEffectIfAvailable(hazeState, HazeMaterials.thin())
                } else {
                    Modifier
                        .align(Alignment.CenterEnd)
                        .clip(CircleShape)
                }
            Row(
                modifier = endModifier
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            ) {
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
            }
        } else if (customEndContent != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            ) {
                customEndContent()
            }
        }
    }

}