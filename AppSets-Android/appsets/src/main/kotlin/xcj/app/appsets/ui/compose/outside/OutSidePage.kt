package xcj.app.appsets.ui.compose.outside

import android.content.res.Configuration
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.toSize
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.LoadMoreHandler
import xcj.app.appsets.ui.compose.custom_component.ShowNavBarWhenOnLaunch
import xcj.app.appsets.ui.model.ScreenState

@Composable
fun OutSidePage(
    screens: List<ScreenState>,
    onBioClick: (Bio) -> Unit,
    onLoadMore: () -> Unit,
    onPictureClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onScreenVideoPlayClick: (ScreenMediaFileUrl) -> Unit,
) {
    ShowNavBarWhenOnLaunch()
    var boxSizeFloat by remember {
        mutableStateOf(Size(0f, 0f))
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPlaced {
                boxSizeFloat = it.size.toSize()
            }
    ) {
        var bigImageAction by remember {
            mutableStateOf<Pair<ScreenMediaFileUrl?, PressInteraction?>?>(null)
        }
        val interactionFlow: (Interaction, ScreenMediaFileUrl) -> Unit = remember {
            { interaction, mediaFileUrl ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        bigImageAction = mediaFileUrl to interaction
                    }

                    is PressInteraction.Release -> {
                        bigImageAction = mediaFileUrl to null
                    }

                    is PressInteraction.Cancel -> {
                        bigImageAction = mediaFileUrl to null
                    }
                }
            }
        }
        val scrollableState =
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                rememberLazyListState()
            } else {
                rememberLazyStaggeredGridState()
            }
        LoadMoreHandler(scrollableState = scrollableState) {
            onLoadMore()
        }
        ScreensList(
            modifier = Modifier,
            scrollableState = scrollableState,
            currentDestinationRoute = PageRouteNames.OutSidePage,
            screens = screens,
            onBioClick = onBioClick,
            pictureInteractionFlow = interactionFlow,
            onPictureClick = onPictureClick,
            onScreenVideoPlayClick = onScreenVideoPlayClick
        )

        /*val bigImageTargetSize = if (bigImageAction?.second is PressInteraction.Press) {
            boxSizeFloat * 0.8f
        } else {
            Size.Zero
        }
        val bigImageSizeState = with(LocalDensity.current) {
            val animateSize = animateSizeAsState(
                targetValue = bigImageTargetSize,
                label = "big_image",
                animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
                finishedListener = {
                    if (it.width == 0f)
                        bigImageAction = null
                })
            animateSize.value.toDpSize()
        }
        LocalOrRemoteImage(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.extraLarge)
                .clip(MaterialTheme.shapes.extraLarge)
                .size(bigImageSizeState),
            any = bigImageAction?.first?.mediaFileUrl,
            contentScale = ContentScale.FillWidth
        )*/
    }
}