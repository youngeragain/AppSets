@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.compose_share.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import xcj.app.compose_share.modifier.hazeEffectIfAvailable

private const val TAG = "BackActionTopBar"

data class BackActionTopBarInfo(
    val barIsShowing: MutableState<Boolean> = mutableStateOf(false),
    val barSize: MutableState<IntSize> = mutableStateOf(IntSize.Zero)
)


val LocalBackActionTopBarInfo = staticCompositionLocalOf { BackActionTopBarInfo() }

sealed interface BackActionModel {
    class Text() : BackActionModel
    class CustomContent() : BackActionModel
}


@Preview(showBackground = true)
@Composable
fun BackActionTopBarPreview() {
    BackActionTopBar(
        hazeState = null, onBackClick = {

        }, backButtonText = "Back", endButtonText = "OK"
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
    val context = LocalContext.current
    val backActionTopBarInfo = LocalBackActionTopBarInfo.current

    DisposableEffect(true) {
        onDispose {
            backActionTopBarInfo.barIsShowing.value = false
            backActionTopBarInfo.barSize.value = IntSize.Zero
        }
    }
    val boxModifier = if (!endButtonText.isNullOrEmpty() || customEndContent != null) {
        modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                top = 12.dp + WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                    .calculateTopPadding(),
                end = 12.dp,
                bottom = 12.dp
            )
    } else {
        modifier.padding(
            start = 12.dp,
            top = 12.dp + WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                .calculateTopPadding(),
            end = 12.dp,
            bottom = 12.dp
        )
    }
    Box(
        modifier = boxModifier.onPlaced {
            backActionTopBarInfo.barIsShowing.value = true
            backActionTopBarInfo.barSize.value = it.size
        }) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .hazeEffectIfAvailable(hazeState, HazeMaterials.thin())
                    .clickable(onClick = onBackClick)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .padding(12.dp)
            ) {
                Icon(
                    painter = painterResource(
                        id = backIcon ?: xcj.app.compose_share.R.drawable.ic_arrow_back_24
                    ), contentDescription = stringResource(xcj.app.compose_share.R.string.back)
                )
            }

            if (!backButtonText.isNullOrEmpty()) {
                Text(
                    text = backButtonText, fontSize = 16.sp, fontWeight = FontWeight.Bold
                )
            }
        }

        if (!endButtonText.isNullOrEmpty()) {
            val endModifier = if (hazeState != null && hazeForEnd) {
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
                modifier = endModifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                onEndButtonClick?.invoke()
                            })
                        .padding(12.dp),
                ) {
                    Text(text = endButtonText)
                }
            }
        } else if (customEndContent != null) {
            Box(
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                customEndContent()
            }
        }
    }

}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatusBarWithTopActionBarSpacer() {
    val statusBarHeight =
        WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
    val finalTopPadding = 12.dp + statusBarHeight + 68.dp
    Spacer(
        modifier = Modifier
            .height(finalTopPadding)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun statusBarWithTopActionBarPaddingValues(
    start: Dp = 12.dp,
    top: Dp = 12.dp,
    end: Dp = 12.dp,
    bottom: Dp = 12.dp,
    containsTopBarHeight: Boolean = true,
): PaddingValues {
    val statusBarHeight =
        WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
    var finalTopPadding = top + statusBarHeight
    if (containsTopBarHeight) {
        finalTopPadding += 68.dp
    }
    return PaddingValues(
        start = start, top = finalTopPadding, end = end, bottom = bottom
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun backActionsBarHeightDp(): Dp {
    return 68.dp
}