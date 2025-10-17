@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.appsets.ui.compose.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfSearch
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.apps.SingleApplicationComponent
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.ShowNavBar
import xcj.app.appsets.ui.compose.outside.Screen
import xcj.app.appsets.ui.model.page_state.SearchPageState
import xcj.app.appsets.ui.model.state.SearchResult
import xcj.app.compose_share.components.DesignTextField

@Composable
fun SearchPage(
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {

    ShowNavBar()
    val coroutineScope = rememberCoroutineScope()
    val searchUseCase = LocalUseCaseOfSearch.current
    val searchState by searchUseCase.searchPageState

    DisposableEffect(key1 = true, effect = {
        searchUseCase.attachToSearchFlow(coroutineScope)
        onDispose {
            searchUseCase.detachToSearchFlow()
        }
    })

    SearchPageResults(
        searchPageState = searchState,
        onBioClick = onBioClick,
        onScreenMediaClick = onScreenMediaClick
    )
}

@Composable
fun SearchInputBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    onBackClick: () -> Unit,
    onInputContent: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchUseCase = LocalUseCaseOfSearch.current
    val searchPageState by searchUseCase.searchPageState
    var inputContent by remember {
        mutableStateOf(TextFieldValue())
    }

    val focusRequester = remember {
        FocusRequester()
    }
    var textFiledIsFocused by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = true, block = {

        val searchKeywords = searchPageState.keywords
        if (!searchKeywords.isNullOrEmpty()) {
            inputContent = inputContent.copy(searchKeywords, TextRange(searchKeywords.length))
        }
        if (searchKeywords.isNullOrEmpty()) {
            //focusRequester.requestFocus()
        }
    })

    Box(modifier = modifier) {
        DesignTextField(
            value = inputContent.text,
            onValueChange = {
                inputContent = TextFieldValue(it)
                onInputContent(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    border = searchBorderStroke(searchPageState), shape = CircleShape
                )
                .clip(CircleShape)
                .hazeEffect(
                    hazeState,
                    HazeMaterials.thin()
                )
                .focusRequester(focusRequester)
                .onFocusChanged {
                    textFiledIsFocused = it.hasFocus
                },
            leadingIcon = {
                Icon(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = {
                            if (textFiledIsFocused) {
                                focusRequester.freeFocus()
                                keyboardController?.hide()
                                textFiledIsFocused = false
                                return@clickable
                            }
                            onBackClick()
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
                    contentDescription = stringResource(xcj.app.appsets.R.string.search))
            }, placeholder = {
                Text(text = stringResource(xcj.app.appsets.R.string.search))
            },
            maxLines = 1,
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun searchBorderStroke(searchPageState: SearchPageState): BorderStroke {
    val targetWidth = if (searchPageState is SearchPageState.Searching) {
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
        if (searchPageState is SearchPageState.Searching) {
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
    searchPageState: SearchPageState,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    AnimatedContent(
        targetState = searchPageState,
        contentAlignment = Alignment.TopCenter,
        transitionSpec = {
            if (searchPageState is SearchPageState.None) {
                (fadeIn(
                    animationSpec = snap()
                ) + slideInVertically()).togetherWith(
                    fadeOut(
                        animationSpec = snap()
                    )
                )
            } else {
                (fadeIn(
                    animationSpec = tween(450)
                ) + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(450)
                )).togetherWith(
                    (fadeOut(
                        animationSpec = tween(120)
                    ) + scaleOut(
                        targetScale = 1.12f,
                        animationSpec = tween(120)
                    ))
                )
            }

        }
    ) { targetSearchState ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (targetSearchState) {
                is SearchPageState.None -> {

                }

                is SearchPageState.Searching -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = targetSearchState.tips)
                        )
                        targetSearchState.subTips?.let {
                            Text(
                                text = stringResource(id = it),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                is SearchPageState.SearchPageFailed -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        targetSearchState.tips?.let {
                            Text(
                                text = stringResource(id = it)
                            )
                        }
                        targetSearchState.subTips?.let {
                            Text(
                                text = stringResource(id = it),
                                fontSize = 12.sp
                            )
                        }
                    }

                }

                is SearchPageState.SearchPageSuccess -> {
                    if (targetSearchState.results.isEmpty()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            targetSearchState.tips?.let {
                                Text(
                                    text = stringResource(id = it)
                                )
                            }
                            targetSearchState.subTips?.let {
                                Text(
                                    text = stringResource(id = it),
                                    fontSize = 12.sp
                                )
                            }
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
    searchSuccess: SearchPageState.SearchPageSuccess,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    val pagerState = rememberPagerState { searchSuccess.results.size }
    val coroutineScope = rememberCoroutineScope()
    val hazeState = rememberHazeState()
    LaunchedEffect(pagerState.currentPage) {

    }

    Box {
        HorizontalPager(
            modifier = Modifier.hazeSource(hazeState),
            state = pagerState, verticalAlignment = Alignment.Top
        ) { index ->
            when (val searchResult = searchSuccess.results[index]) {
                is SearchResult.SearchedApplications -> {
                    SearchedApplicationsPage(
                        searchedApplications = searchResult, onBioClick = onBioClick
                    )
                }

                is SearchResult.SearchedUsers -> {
                    SearchedUsersPage(
                        searchedUsers = searchResult, onBioClick = onBioClick
                    )
                }

                is SearchResult.SearchedGroups -> {
                    SearchedGroupsPage(
                        searchedGroups = searchResult, onBioClick = onBioClick
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
        StatusBarAreaGradient(
            modifier = Modifier.align(Alignment.TopCenter)
        )
        NavigationBarAreaGradient(
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        FlowRow(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 12.dp, bottom = 92.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            searchSuccess.results.forEachIndexed { index, searchResult ->

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .hazeEffect(
                            hazeState,
                            HazeMaterials.thin()
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val text = when (searchResult) {
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
fun StatusBarAreaGradient(
    modifier: Modifier = Modifier
) {
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
        modifier = modifier
            .fillMaxWidth()
            .height(WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 52.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors, startY = 0f, endY = Float.POSITIVE_INFINITY
                )
            ),
    ) {}
}

@Composable
fun NavigationBarAreaGradient(
    modifier: Modifier = Modifier
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val gradientColors = listOf(
        Color.Transparent,
        if (isSystemInDarkTheme) {
            Color.Black
        } else {
            Color.White
        },
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(WindowInsets.navigationBars.asPaddingValues().calculateTopPadding() + 52.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors, startY = 0f, endY = Float.POSITIVE_INFINITY
                )
            ),
    ) {}
}

@Composable
fun SearchedGoodsListPage(searchedGoodsList: SearchResult.SearchedGoods) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 120.dp
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
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 120.dp
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
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 120.dp
        )
    ) {
        items(searchedGroups.groups) { groupInfo ->
            SearchedGroupComponent(
                modifier = Modifier.clickable {
                    onBioClick.invoke(groupInfo)
                }, groupInfo = groupInfo
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
        modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 120.dp
        )
    ) {
        items(searchedUsers.users) { userInfo ->
            SearchedUserComponent(
                modifier = Modifier.clickable {
                    onBioClick.invoke(userInfo)
                }, userInfo = userInfo
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
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
            bottom = 120.dp
        )
    ) {
        itemsIndexed(items = searchedApplications.applications) { index, application ->
            SingleApplicationComponent(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                application = application,
                onApplicationClick = {
                    onBioClick(application)
                },
                onApplicationLongClick = {
                    onBioClick(application)
                }
            )
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
                    color = MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.large
                )
                .clip(MaterialTheme.shapes.large), model = userInfo.bioUrl
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = userInfo.bioName ?: "")
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
                    color = MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.large
                )
                .clip(MaterialTheme.shapes.large), model = groupInfo.bioUrl
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = groupInfo.bioName ?: "")
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
            .padding(12.dp)) {
        Screen(
            currentPageRoute = PageRouteNames.SearchPage,
            screenInfo = screenInfo,
            onBioClick = onBioClick,
            onScreenMediaClick = onScreenMediaClick,
            pictureInteractionFlowCollector = { a, b -> },
        )
    }
}