@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.appsets.ui.compose.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfSearch
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.ShowNavBarWhenOnLaunch
import xcj.app.appsets.ui.compose.outside.ScreenComponent
import xcj.app.appsets.ui.compose.theme.AppSetsShapes
import xcj.app.appsets.ui.model.SearchResult
import xcj.app.appsets.ui.model.SearchState
import xcj.app.compose_share.components.DesignTextField

@Composable
fun SearchPage(
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {

    ShowNavBarWhenOnLaunch()

    val searchUseCase = LocalUseCaseOfSearch.current
    val searchState by searchUseCase.searchState

    DisposableEffect(key1 = true, effect = {
        searchUseCase.attachToSearchFlow()
        onDispose {
            searchUseCase.detachToSearchFlow()
        }
    })

    SearchPageResults(
        searchState = searchState,
        onBioClick = onBioClick,
        onScreenMediaClick = onScreenMediaClick
    )
}

@Composable
fun SearchInputBar(
    searchState: SearchState,
    sizeOfSearchBar: IntSize,
    onBackClick: () -> Unit,
    onInputContent: (String) -> Unit,
    onSearchBarSizeChanged: (IntSize) -> Unit,
) {
    var inputContent by remember {
        mutableStateOf(TextFieldValue())
    }

    val requester = remember {
        FocusRequester()
    }
    LaunchedEffect(key1 = true, block = {
        val searchKeywords = searchState.keywords
        if (!searchKeywords.isNullOrEmpty()) {
            inputContent = inputContent.copy(searchKeywords, TextRange(searchKeywords.length))
        }
        if (searchKeywords.isNullOrEmpty()) {
            delay(450)
            requester.requestFocus()
        }
    })
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()
    val corner = sizeOfSearchBar.height
        .div(2)
        .toFloat()
    val cornerShape by rememberUpdatedState(
        RoundedCornerShape(
            topStart = corner,
            topEnd = corner,
            bottomStart = corner,
            bottomEnd = corner
        )
    )
    DesignTextField(
        value = inputContent.text,
        onValueChange = {
            inputContent = TextFieldValue(it)
            onInputContent(it)
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(requester)
            .border(
                border = searchBorderStroke(searchState),
                shape = cornerShape
            )
            .onSizeChanged(onSearchBarSizeChanged),
        leadingIcon = {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = {
                        coroutineScope.launch {
                            keyboardController?.hide()
                            delay(350)
                            onBackClick()
                        }
                    })
                    .padding(4.dp),
                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_arrow_back_24),
                contentDescription = stringResource(id = xcj.app.appsets.R.string.return_)
            )
        },
        trailingIcon = {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onInputContent(inputContent.text)
                    }
                    .padding(4.dp),
                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_search_24),
                contentDescription = stringResource(xcj.app.appsets.R.string.search)
            )
        },
        placeholder = {
            Text(text = stringResource(xcj.app.appsets.R.string.search))
        },
        maxLines = 1,
        shape = cornerShape,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            focusedPlaceholderColor = Color.Companion.Transparent,
            unfocusedPlaceholderColor = Color.Transparent
        )
    )
}

@Composable
fun searchBorderStroke(searchState: SearchState): BorderStroke {
    val targetWidth = if (searchState is SearchState.Searching) {
        2.dp
    } else {
        1.dp
    }
    val targetWidthState = animateDpAsState(
        targetValue = targetWidth,
        label = "search_border_stroke_animate",
        animationSpec = tween(450)
    )
    val outlineColor = MaterialTheme.colorScheme.outline
    val stroke by rememberUpdatedState(
        if (searchState is SearchState.Searching) {
            BorderStroke(
                targetWidthState.value, linearGradient(
                    0.0f to Color.Red,
                    0.2f to Color.Green,
                    0.4f to Color.Yellow,
                    0.6f to Color.Cyan,
                    0.8f to Color.Magenta,
                    1.0f to Color.Blue
                )
            )
        } else {
            BorderStroke(targetWidthState.value, SolidColor(outlineColor))
        }
    )
    return stroke
}

@Composable
fun SearchPageResults(
    searchState: SearchState,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    AnimatedContent(
        targetState = searchState,
        contentAlignment = Alignment.TopCenter,
        transitionSpec = {
            (fadeIn(animationSpec = tween(450)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(450)))
                .togetherWith(
                    fadeOut(animationSpec = tween(120)) + scaleOut(
                        targetScale = 1.12f,
                        animationSpec = tween(
                            120
                        )
                    )
                )
        }
    ) { targetSearchState ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (targetSearchState) {
                is SearchState.None -> {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.no_content),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is SearchState.Searching -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        targetSearchState.tips?.let {
                            Text(text = stringResource(id = it))
                        }

                    }
                }

                is SearchState.SearchFailed -> {
                    targetSearchState.tips?.let {
                        Text(
                            text = stringResource(id = it),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                is SearchState.SearchSuccess -> {
                    if (targetSearchState.results.isEmpty()) {
                        targetSearchState.tips?.let {
                            Text(
                                text = stringResource(id = it),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        SearchSuccessPages(
                            searchSuccess = targetSearchState,
                            onBioClick = onBioClick,
                            onScreenMediaClick = onScreenMediaClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchSuccessPages(
    searchSuccess: SearchState.SearchSuccess,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    val pagerState = rememberPagerState { searchSuccess.results.size }
    val coroutineScope = rememberCoroutineScope()
    val tabsScrollState = rememberScrollState()
    var buttonSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    LaunchedEffect(pagerState.currentPage) {
        tabsScrollState.animateScrollTo(buttonSize.width * pagerState.currentPage)
    }

    Box {
        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { index ->
            val searchResult = searchSuccess.results[index]
            when (searchResult) {
                is SearchResult.SearchedApplications -> {
                    SearchedApplicationsPage(
                        searchedApplications = searchResult,
                        onBioClick = onBioClick
                    )
                }

                is SearchResult.SearchedUsers -> {
                    SearchedUsersPage(
                        searchedUsers = searchResult,
                        onBioClick = onBioClick
                    )
                }

                is SearchResult.SearchedGroups -> {
                    SearchedGroupsPage(
                        searchedGroups = searchResult,
                        onBioClick = onBioClick
                    )
                }

                is SearchResult.SearchedScreens -> {
                    SearchedScreensPage(
                        searchedScreens = searchResult,
                        onBioClick = onBioClick,
                        onScreenMediaClick = onScreenMediaClick
                    )
                }

                is SearchResult.SearchedGoods -> {
                    SearchedGoodsListPage(searchResult)
                }
            }

        }
        val isSystemInDarkTheme = isSystemInDarkTheme()
        val gradientColors = listOf(
            if (isSystemInDarkTheme) {
                Color.Black
            } else {
                Color.White
            },
            Color.Transparent,
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 52.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors,
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                ),
        ) {
        }
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 12.dp)
                .horizontalScroll(tabsScrollState)
        ) {

            searchSuccess.results.forEachIndexed { index, selectionType ->
                SegmentedButton(
                    modifier = Modifier
                        .onSizeChanged {
                            buttonSize = it
                        },
                    colors = SegmentedButtonDefaults.colors()
                        .copy(inactiveContainerColor = MaterialTheme.colorScheme.surface),
                    selected = index == pagerState.currentPage,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = searchSuccess.results.size
                    ),
                    icon = {}
                ) {
                    val text = when (selectionType) {
                        is SearchResult.SearchedApplications -> {
                            stringResource(xcj.app.appsets.R.string.application)
                        }

                        is SearchResult.SearchedUsers -> {
                            stringResource(xcj.app.appsets.R.string.user)
                        }

                        is SearchResult.SearchedGroups -> {
                            stringResource(xcj.app.appsets.R.string.group)
                        }

                        is SearchResult.SearchedScreens -> {
                            "Screen"
                        }

                        is SearchResult.SearchedGoods -> {
                            stringResource(xcj.app.appsets.R.string.goods)
                        }
                    }
                    Text(text = text)
                }
            }
        }
    }
}

@Composable
fun SearchedGoodsListPage(searchedGoodsList: SearchResult.SearchedGoods) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 52.dp + WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 68.dp
        )
    ) {
        items(searchedGoodsList.goodsList) { screenInfo ->

        }
    }
}

@Composable
fun SearchedScreensPage(
    searchedScreens: SearchResult.SearchedScreens,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 52.dp + WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 68.dp
        )
    ) {
        items(searchedScreens.screens) { screenInfo ->
            SearchedScreenComponent(
                modifier = Modifier,
                screenInfo = screenInfo,
                onBioClick = onBioClick,
                onScreenMediaClick = onScreenMediaClick
            )
        }
    }
}

@Composable
fun SearchedGroupsPage(searchedGroups: SearchResult.SearchedGroups, onBioClick: (Bio) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 52.dp + WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 68.dp
        )
    ) {
        items(searchedGroups.groups) { groupInfo ->
            SearchedGroupComponent(
                modifier = Modifier.clickable {
                    onBioClick.invoke(groupInfo)
                },
                groupInfo = groupInfo
            )
        }
    }
}

@Composable
fun SearchedUsersPage(
    searchedUsers: SearchResult.SearchedUsers,
    onBioClick: (Bio) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 52.dp + WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 68.dp
        )
    ) {
        items(searchedUsers.users) { userInfo ->
            SearchedUserComponent(
                modifier = Modifier.clickable {
                    onBioClick.invoke(userInfo)
                },
                userInfo = userInfo
            )
        }
    }
}

@Composable
fun SearchedApplicationsPage(
    searchedApplications: SearchResult.SearchedApplications,
    onBioClick: (Bio) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(90.dp),
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyGridState(),
        contentPadding = PaddingValues(
            top = 52.dp + WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 68.dp
        )
    ) {
        itemsIndexed(items = searchedApplications.applications) { index, application ->
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnyImage(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(AppSetsShapes.large)
                        .background(MaterialTheme.colorScheme.outline, AppSetsShapes.large)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            AppSetsShapes.large
                        )
                        .clickable(
                            onClick = {
                                onBioClick(application)
                            }
                        ),
                    any = application.bioUrl
                )
                Text(
                    text = application.name ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .widthIn(max = 82.dp)
                )
            }
        }
    }
}


@Composable
fun SearchedUserComponent(modifier: Modifier, userInfo: UserInfo) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnyImage(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.large
                )
                .clip(MaterialTheme.shapes.large),
            any = userInfo.bioUrl
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = userInfo.name ?: "")
            Text(text = userInfo.introduction ?: "", fontSize = 10.sp)
        }
    }
}

@Composable
fun SearchedGroupComponent(modifier: Modifier, groupInfo: GroupInfo) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnyImage(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.large
                )
                .clip(MaterialTheme.shapes.large),
            any = groupInfo.bioUrl
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = groupInfo.name ?: "")
            Text(text = groupInfo.introduction ?: "", fontSize = 10.sp)
        }
    }
}

@Composable
fun SearchedScreenComponent(
    modifier: Modifier,
    screenInfo: ScreenInfo,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onBioClick(screenInfo)
            }
            .padding(12.dp)
    ) {
        ScreenComponent(
            currentDestinationRoute = PageRouteNames.SearchPage,
            screenInfo = screenInfo,
            onBioClick = onBioClick,
            onScreenMediaClick = onScreenMediaClick,
            pictureInteractionFlowCollector = { a, b -> },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchedApplicationComponent(
    modifier: Modifier,
    applications: List<Application>,
    onBioClick: (Bio) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        applications.forEach { application ->
            Column(
                modifier = modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnyImage(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(AppSetsShapes.large)
                        .background(MaterialTheme.colorScheme.outline, AppSetsShapes.large)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            AppSetsShapes.large
                        )
                        .clickable {
                            onBioClick(application)
                        },
                    any = application.bioUrl
                )
                Text(
                    text = application.name ?: "",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .widthIn(max = 68.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

    }

}
