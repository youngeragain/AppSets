package xcj.app.appsets.ui.compose.win11Snapshot

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.ui.compose.PageRouteNameProvider


@UnstableApi
@Composable
fun Win11SnapShotPage(
    onSearchBarClick: () -> Unit,
    onSettingsUserNameClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsLoginClick: () -> Unit,
    onSearchBarAddButtonClick: () -> Unit,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_SQUARE -> Unit
        Configuration.ORIENTATION_UNDEFINED -> Unit
        Configuration.ORIENTATION_LANDSCAPE ->
            LandscapeSnapShotPage(
                onSearchBarClick = onSearchBarClick,
                onSettingsUserNameClick = onSettingsUserNameClick,
                onSettingsClick = onSettingsClick,
                onSettingsLoginClick = onSettingsLoginClick,
                onSearchBarAddButtonClick = onSearchBarAddButtonClick,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )

        Configuration.ORIENTATION_PORTRAIT ->
            PortraitSnapShotPage(
                onSearchBarClick = onSearchBarClick,
                onSettingsUserNameClick = onSettingsUserNameClick,
                onSettingsClick = onSettingsClick,
                onSettingsLoginClick = onSettingsLoginClick,
                onSearchBarAddButtonClick = onSearchBarAddButtonClick,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
    }
}

@UnstableApi
@Composable
fun LandscapeSnapShotPage(
    onSearchBarClick: () -> Unit,
    onSettingsUserNameClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsLoginClick: () -> Unit,
    onSearchBarAddButtonClick: () -> Unit,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    Row {
        val mainViewModel: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
        LeftOrRight(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 6.dp),
            dataList = listOf(
                SpotLightState.Bar,
                mainViewModel.win11SnapShotUseCase.pinnedApps.value,
                mainViewModel.win11SnapShotUseCase.recommendItems
            ),
            onSearchBarClick = onSearchBarClick,
            onSettingsUserNameClick = onSettingsUserNameClick,
            onSettingsClick = onSettingsClick,
            onSettingsLoginClick = onSettingsLoginClick,
            onAddButtonClick = onSearchBarAddButtonClick,
            onWin11SnapShotStateClick = onWin11SnapShotStateClick
        )
        val win11SearchSpotLight = mainViewModel.appSetsUseCase.spotLightsState
        LeftOrRight(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, end = 12.dp),
            dataList = win11SearchSpotLight,
            onSearchBarClick = onSearchBarClick,
            onSettingsUserNameClick = onSettingsUserNameClick,
            onSettingsClick = onSettingsClick,
            onSettingsLoginClick = onSettingsLoginClick,
            onAddButtonClick = onSearchBarAddButtonClick,
            onWin11SnapShotStateClick = onWin11SnapShotStateClick
        )
    }
}

@UnstableApi
@Composable
fun LeftOrRight(
    modifier: Modifier,
    dataList: List<SpotLightState>?,
    onSearchBarClick: () -> Unit,
    onSettingsUserNameClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsLoginClick: () -> Unit,
    onAddButtonClick: () -> Unit,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {

    if (dataList.isNullOrEmpty())
        return
    if (dataList[0] is SpotLightState.Bar) {
        Spacer(modifier = Modifier.height(32.dp))
        val mainViewModel: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
        if (mainViewModel.mediaUseCase.audioPlayerState.value != null) {
            ArrangeComponent(
                item = mainViewModel.mediaUseCase.audioPlayerState.value!!,
                onSearchBarClick = onSearchBarClick,
                onSettingsUserNameClick = onSettingsUserNameClick,
                onSettingsClick = onSettingsClick,
                onSettingsLoginClick = onSettingsLoginClick,
                onSearchBarAddButtonClick = onAddButtonClick,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }
    }
    val scrollableState = rememberScrollState()
    Column(
        modifier = modifier
            .safeContentPadding()
            .verticalScroll(scrollableState)
    ) {
        dataList.forEach { item ->
            ArrangeComponent(
                item = item,
                onSearchBarClick = onSearchBarClick,
                onSettingsUserNameClick = onSettingsUserNameClick,
                onSettingsClick = onSettingsClick,
                onSettingsLoginClick = onSettingsLoginClick,
                onSearchBarAddButtonClick = onAddButtonClick,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }
    }
}


@UnstableApi
@Composable
fun ArrangeComponent(
    item: SpotLightState,
    onSearchBarClick: () -> Unit,
    onSettingsUserNameClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsLoginClick: () -> Unit,
    onSearchBarAddButtonClick: () -> Unit,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    when (item) {
        is SpotLightState.Bar -> {
            ComponentSearchBar(
                modifier = Modifier.padding(bottom = 12.dp),
                currentDestinationRoute = PageRouteNameProvider.Win11SnapShotPage,
                onSearchBarClick = onSearchBarClick,
                onSettingsUserNameClick = onSettingsUserNameClick,
                onSettingsClick = onSettingsClick,
                onSettingsLoginClick = onSettingsLoginClick,
                onAddButtonClick = onSearchBarAddButtonClick
            )
        }

        is SpotLightState.AudioPlayer -> {
            ComponentAudioPlayer(
                item = item,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }

        is SpotLightState.PinnedApps -> {
            ComponentPinnedAppsOrRecommendedItems(
                item = item,
                leftTopText = "已固定",
                rightTopText = "所有应用",
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }

        is SpotLightState.RecommendedItems -> {
            ComponentPinnedAppsOrRecommendedItems(
                item = item,
                leftTopText = "推荐的项目",
                rightTopText = "更多",
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }

        is SpotLightState.HeaderTitle -> {
            ComponentHeaderTitle(
                item = item,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }

        is SpotLightState.QuestionOfTheDay -> {
            ComponentQuestionOfTheDay(
                item = item,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }

        is SpotLightState.WordOfTheDayAndTodayInHistory -> {
            ComponentWordOfTheDayAndTodayInHistory(
                item = item,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }

        is SpotLightState.PopularSearches -> {
            ComponentPopularSearches(
                item = item,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }

        is SpotLightState.HotWordsWrapper -> {
            ComponentHotWordsWrapper(
                item = item,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }

        else -> Unit
    }
}

@Composable
fun ComponentAudioPlayer(
    item: SpotLightState.AudioPlayer,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    Box(modifier = Modifier.padding(10.dp)) {
        val shape18 = RoundedCornerShape(18.dp)
        Card(shape = shape18) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    Modifier
                        .fillMaxSize()
                ) {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(shape18),
                        any = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Now playing")
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = item.title, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = item.art)
                    }
                }
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                ) {
                    Text(text = item.currentDuration)
                    Box(modifier = Modifier.weight(1f)) {

                    }
                    Text(text = item.duration)
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@UnstableApi
@Composable
fun ComponentHotWordsWrapper(
    item: SpotLightState.HotWordsWrapper,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = item.from,
                Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
            item.words.forEach { hotsearch ->
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = hotsearch.cardTitle,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                //mainViewModel.url.value = URLDecoder.decode(it?.linkurl)
                            },
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = hotsearch.heatScore ?: "",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
    
}

@UnstableApi
@Composable
fun PortraitSnapShotPage(
    onSearchBarClick: () -> Unit,
    onSettingsUserNameClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsLoginClick: () -> Unit,
    onSearchBarAddButtonClick: () -> Unit,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    val scrollableState = rememberScrollState()
    Column(
        Modifier
            .padding(horizontal = 12.dp)
            .verticalScroll(scrollableState)
    ) {
        val mainViewModel: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        if (mainViewModel.mediaUseCase.audioPlayerState.value != null) {
            ArrangeComponent(
                item = mainViewModel.mediaUseCase.audioPlayerState.value!!,
                onSearchBarClick = onSearchBarClick,
                onSettingsUserNameClick = onSettingsUserNameClick,
                onSettingsClick = onSettingsClick,
                onSettingsLoginClick = onSettingsLoginClick,
                onSearchBarAddButtonClick = onSearchBarAddButtonClick,
                onWin11SnapShotStateClick = onWin11SnapShotStateClick
            )
        }
        ArrangeComponent(
            item = SpotLightState.Bar,
            onSearchBarClick = onSearchBarClick,
            onSettingsUserNameClick = onSettingsUserNameClick,
            onSettingsClick = onSettingsClick,
            onSettingsLoginClick = onSettingsLoginClick,
            onSearchBarAddButtonClick = onSearchBarAddButtonClick,
            onWin11SnapShotStateClick = onWin11SnapShotStateClick
        )
        ArrangeComponent(
            item = mainViewModel.win11SnapShotUseCase.pinnedApps.value,
            onSearchBarClick = onSearchBarClick,
            onSettingsUserNameClick = onSettingsUserNameClick,
            onSettingsClick = onSettingsClick,
            onSettingsLoginClick = onSettingsLoginClick,
            onSearchBarAddButtonClick = onSearchBarAddButtonClick,
            onWin11SnapShotStateClick = onWin11SnapShotStateClick
        )
        ArrangeComponent(
            item = mainViewModel.win11SnapShotUseCase.recommendItems,
            onSearchBarClick = onSearchBarClick,
            onSettingsUserNameClick = onSettingsUserNameClick,
            onSettingsClick = onSettingsClick,
            onSettingsLoginClick = onSettingsLoginClick,
            onSearchBarAddButtonClick = onSearchBarAddButtonClick,
            onWin11SnapShotStateClick = onWin11SnapShotStateClick
        )
        val win11SearchSpotLight = mainViewModel.appSetsUseCase.spotLightsState
        if (win11SearchSpotLight.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp), contentAlignment = Alignment.Center
            ) {
                Text(text = "加载Win11搜索亮点", color = MaterialTheme.colorScheme.tertiary)
            }
        } else {
            win11SearchSpotLight.forEach { item ->
                ArrangeComponent(
                    item = item,
                    onSearchBarClick = onSearchBarClick,
                    onSettingsUserNameClick = onSettingsUserNameClick,
                    onSettingsClick = onSettingsClick,
                    onSettingsLoginClick = onSettingsLoginClick,
                    onSearchBarAddButtonClick = onSearchBarAddButtonClick,
                    onWin11SnapShotStateClick = onWin11SnapShotStateClick
                )
            }
        }
        Spacer(modifier = Modifier.height(98.dp))
        /*WebComponent()*/
    }

}


@OptIn(ExperimentalLayoutApi::class)
@UnstableApi
@Composable
fun ComponentPinnedAppsOrRecommendedItems(
    item: SpotLightState,
    leftTopText: String,
    rightTopText: String,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    val size = when (item) {
        is SpotLightState.PinnedApps -> {
            item.apps.size
        }

        is SpotLightState.RecommendedItems -> {
            item.items.size
        }

        else -> 0
    }

    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = leftTopText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        onWin11SnapShotStateClick(item, "more_action")
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    text = rightTopText,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_chevron_right_24),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
        if (size == 0){
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Text(
                    text = "将喜欢的应用固定在此",
                    fontSize = 14.sp
                )
            }
        }else {
            var isShowUnPinAppDialog by remember {
                mutableStateOf(false)
            }
            var unPinAppPackageName by remember {
                val str: String? = null
                mutableStateOf(str)
            }
            when (item) {
                is SpotLightState.PinnedApps -> {
                    FlowRow(maxItemsInEachRow = 5, modifier = Modifier.heightIn(min = 180.dp)) {
                        item.apps.forEachIndexed { index, app ->
                            ComponentAppWithName(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                unPinAppPackageName = app.packageName
                                                isShowUnPinAppDialog = true
                                            },
                                            onTap = {
                                                onWin11SnapShotStateClick(item, app)
                                            }
                                        )
                                    },
                                app = app,
                            )
                        }
                    }
                }

                is SpotLightState.RecommendedItems -> {
                    FlowRow(maxItemsInEachRow = 2) {
                        item.items.forEachIndexed { index, item2 ->
                            ComponentItemWithName(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onWin11SnapShotStateClick(item, item2)
                                    },
                                item = item2
                            )
                        }
                    }
                }

                else -> Unit
            }
            if (isShowUnPinAppDialog) {
                val vm: MainViewModel = viewModel(context as AppCompatActivity)
                AlertDialog(
                    onDismissRequest = {
                        unPinAppPackageName = null
                        if (isShowUnPinAppDialog)
                            isShowUnPinAppDialog = false
                    },
                    confirmButton = {
                        Text(text = "是", modifier = Modifier.clickable {
                            isShowUnPinAppDialog = false
                            vm.unPinApp(unPinAppPackageName)
                        })
                    },
                    text = {
                        Text(text = "从主屏幕取消固定?")
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }

}
@Composable
fun ComponentAppWithName(modifier: Modifier, app: AppDefinition) {
    Column(
        modifier = modifier
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .widthIn(min = 58.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val painter = anyToPainter(any = app.icon)
        val roundedCornerShape = RoundedCornerShape(12.dp)
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.inverseOnSurface, roundedCornerShape)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    roundedCornerShape
                )
                .padding(6.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = app.name,
            fontSize = 12.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 58.dp),
            lineHeight = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ComponentItemWithName(modifier: Modifier, item: ItemDefinition) {
    Row(modifier = modifier.padding(vertical = 8.dp)) {
        val painter = anyToPainter(any = item.icon)
        val roundedCornerShape = RoundedCornerShape(12.dp)
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.inverseOnSurface, roundedCornerShape)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    roundedCornerShape
                )
                .padding(6.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier
                .widthIn(max = 120.dp)
                .align(Alignment.Top)
        ) {
            Text(text = item.name, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description ?: "",
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComponentPopularSearches(
    item: SpotLightState.PopularSearches,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    var sizeOfThis by remember {
        mutableStateOf(IntSize.Zero)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged {
                sizeOfThis = it
            }
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp, top = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_call_missed_outgoing_24),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        item.title,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow() {
                    val textModifier = Modifier
                        .background(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        .padding(8.dp, 4.dp)
                    val boxModifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    item.words.forEach {
                        Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
                            Text(
                                text = it,
                                modifier = textModifier,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

        }
    }
}

@Composable
fun MeasureUnconstrainedViewWidth(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (measuredWidth: Dp) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measuredWidth = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints()).width.toDp()

        val contentPlaceable = subcompose("content") {
            content(measuredWidth)
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentWordOfTheDayAndTodayInHistory(
    item: SpotLightState.WordOfTheDayAndTodayInHistory,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    Column {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        {
            val context = LocalContext.current
            if (item.wordOfTheDay != null) {
                Card(modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onWin11SnapShotStateClick(item.wordOfTheDay, null)
                    })
                {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text(
                            item.wordOfTheDay.content,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp),
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 16.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            Text(
                                "每日一言：",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                item.wordOfTheDay.author,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            if(item.todayInHistory!=null){
                Spacer(modifier = Modifier.width(12.dp))
                Card(modifier = Modifier
                    .weight(1f)
                    .clickable {
                        onWin11SnapShotStateClick(item.todayInHistory, null)
                    }) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LocalOrRemoteImage(
                            modifier = Modifier.fillMaxSize(),
                            any = item.todayInHistory.bgImg
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 4.dp, bottom = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.onSecondary
                                    )
                                    .padding(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = item.todayInHistory.title ?: "",
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold, fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.todayInHistory.content,
                                    color = MaterialTheme.colorScheme.secondary,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun ComponentQuestionOfTheDay(
    item: SpotLightState.QuestionOfTheDay,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    Column() {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clickable {
                    onWin11SnapShotStateClick(item, null)
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LocalOrRemoteImage(any = item.img)
                Column(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_banner_microsoft),
                        contentDescription = ""
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        .padding(4.dp, 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_outline_location_on_24),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Text(
                        item.where,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.size(6.dp))
                Text(
                    item.whereBelowText,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .background(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        .padding(6.dp, 2.dp)
                        .widthIn(max = 150.dp),
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

    }
}

@Composable
fun ComponentHeaderTitle(
    item: SpotLightState.HeaderTitle,
    onWin11SnapShotStateClick: (Any, Any?) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = item.time,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(1f)
                .clickable {
                    onWin11SnapShotStateClick(item, 0)
                }
        )
    }
}

@Composable
fun anyToPainter(any:Any?, defaultColor:Color = MaterialTheme.colorScheme.primary):Painter {
    val painter = when (any) {
        is Int -> {
            BitmapPainter(ImageBitmap.imageResource(id = any))
        }

        is ImageBitmap -> {
            BitmapPainter(any)
        }

        is Bitmap -> {
            BitmapPainter(any.asImageBitmap())
        }
        else -> {
            ColorPainter(defaultColor)
        }
    }
    return painter
}

