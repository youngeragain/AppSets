package xcj.app.appsets.ui.compose.outside

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import xcj.app.appsets.R
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.ui.compose.PageRouteNameProvider
import xcj.app.appsets.ui.compose.win11Snapshot.ComponentAddButton

@Composable
fun OutSidePage(
    onAddButtonClick: () -> Unit,
    onRefreshButtonClick: () -> Unit,
    onScreenAvatarClick: ((UserScreenInfo) -> Unit)?,
    onScreenContentClick: ((UserScreenInfo) -> Unit)?,
    onPictureClick: ((ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit)?,
    onScreenVideoPlayClick: ((ScreenMediaFileUrl) -> Unit)?,
) {
    val paddingHorizontal =
        if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            0.dp
        } else {
            12.dp
        }
    var boxSizeFloat by remember {
        mutableStateOf(Size(0f, 0f))
    }
    Box(
        modifier = Modifier
            .padding(horizontal = paddingHorizontal)
            .fillMaxSize()
            .onPlaced {
                boxSizeFloat = it.size.toSize()
            }
    ) {
        val mainViewModel: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
        LaunchedEffect(key1 = true) {
            mainViewModel.screensUseCase?.loadIndexScreens(false)
        }

        val scrollableState =
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                rememberLazyListState()
            } else {
                rememberLazyGridState()
            }

        val screensState = mainViewModel.screensUseCase?.systemScreensContainer?.screensState

        val requestNewData by remember {
            derivedStateOf {
                var needRequest = false
                if (scrollableState is LazyGridState) {
                    needRequest = scrollableState.layoutInfo.visibleItemsInfo.isNotEmpty() &&
                            scrollableState.firstVisibleItemIndex != 0 &&
                            (scrollableState.layoutInfo.visibleItemsInfo.maxOf { it.index } + 3) >= (screensState?.size
                        ?: 0)
                } else if (scrollableState is LazyListState) {
                    needRequest = scrollableState.layoutInfo.visibleItemsInfo.isNotEmpty() &&
                            scrollableState.firstVisibleItemIndex != 0 &&
                            (scrollableState.layoutInfo.visibleItemsInfo.maxOf { it.index } + 3) >= (screensState?.size
                        ?: 0)
                }
                needRequest
            }
        }
        LaunchedEffect(key1 = requestNewData, block = {
            mainViewModel.screensUseCase?.loadMore()
        })
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
        if (screensState != null)
            ScreensList(
                modifier = Modifier,
                currentDestinationRoute = PageRouteNameProvider.OutSidePage,
                screens = screensState,
                scrollableState = scrollableState,
                onPictureClick = onPictureClick,
                picInteractionFlow = interactionFlow,
                onScreenAvatarClick = onScreenAvatarClick,
                onScreenContentClick = onScreenContentClick,
                onScreenVideoPlayClick = onScreenVideoPlayClick
            )

        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))

        Column(modifier = Modifier.align(Alignment.TopEnd)) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val rotationState = animateFloatAsState(
                    targetValue = if (mainViewModel.screensUseCase?.systemScreensContainer?.requestingState?.value == true) {
                        540f
                    } else {
                        0f
                    },
                    animationSpec = tween(1000),
                    label = "refresh_indicator"
                )
                Icon(
                    painter = painterResource(R.drawable.round_refresh_24),
                    contentDescription = "refresh",
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(14.dp)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .clickable(onClick = onRefreshButtonClick)
                        .padding(6.dp)
                        .rotate(rotationState.value)
                )
                ComponentAddButton(modifier = Modifier, onClick = onAddButtonClick)
            }
        }

        val bigImageTargetSize = if (bigImageAction?.second is PressInteraction.Press) {
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
        /*val c = with(LocalDensity.current){
            vm.pressPosition!!.x.toDp()-((sizeOfItem.width/3).toDp())/2 to vm.pressPosition!!.y.toDp()-picHeight/2
        }*/
        //Log.e("blue", "c:$c")
        LocalOrRemoteImage(
            modifier = Modifier
                .offset(y = 120f.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .size(bigImageSizeState),
            any = bigImageAction?.first?.mediaFileUrl,
            contentScale = ContentScale.FillWidth
        )

    }
}

@Composable
fun ImageHoldShowContainer(){
    Box(modifier = Modifier
        .padding(horizontal = 12.dp)
        .fillMaxSize()){

        var bigImageAction by remember {
            mutableStateOf<Pair<Any, PressInteraction?>?>(null)
        }
        var sizeOfItem by remember {
            mutableStateOf(IntSize.Zero)
        }

        val interactionSource  = remember {
            MutableInteractionSource()
        }
        val url:String = ""
        LaunchedEffect(key1 = interactionSource, block = {
            interactionSource.interactions.collect{ interaction->
                when(interaction){
                    is PressInteraction.Press->{
                        bigImageAction = url to interaction
                    }

                    is PressInteraction.Release -> {
                        bigImageAction = url to null
                    }

                    is PressInteraction.Cancel -> {
                        bigImageAction = url to null
                    }
                }
            }
        })

        val bigImageHeight = if (bigImageAction?.second is PressInteraction.Press) {
            500.dp
        } else {
            0.dp
        }
        val size = animateDpAsState(
            targetValue = bigImageHeight,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
            finishedListener = {
                if (it.value == 0f)
                    bigImageAction = null
            },
            label = "big_image"
        )
        LocalOrRemoteImage(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(size.value)
                .clip(RoundedCornerShape(24.dp)),
            any = bigImageAction?.first,
            contentScale = ContentScale.FillWidth
        )
    }
}