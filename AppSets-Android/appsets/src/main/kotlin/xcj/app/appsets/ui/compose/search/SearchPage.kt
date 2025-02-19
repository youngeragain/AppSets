package xcj.app.appsets.ui.compose.search

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.AppSetsTextFieldNormal
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.outside.ScreenComponent
import xcj.app.appsets.ui.model.SearchResult
import xcj.app.appsets.ui.model.SearchState

@Composable
fun SearchPage(
    onBackClick: () -> Unit,
    onInputContent: (String) -> Unit,
    onBioClick: (Bio) -> Unit,
    onPictureClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onScreenVideoPlayClick: (ScreenMediaFileUrl) -> Unit
) {

    HideNavBarWhenOnLaunch()

    val searchUseCase = LocalUseCaseOfSearch.current
    val searchState = searchUseCase.searchState.value
    var sizeOfSearchBar by remember {
        mutableStateOf(IntSize.Zero)
    }

    var boxSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    DisposableEffect(key1 = true, effect = {
        searchUseCase.attachToSearchFlow()
        onDispose {
            searchUseCase.detachToSearchFlow()
        }
    })

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        SearchPageTop(
            searchState = searchState,
            sizeOfSearchBar = sizeOfSearchBar,
            onBackClick = onBackClick,
            onInputContent = onInputContent,
            searchBarPlaced = {
                sizeOfSearchBar = it.size
            }
        )
        Spacer(modifier = Modifier.height(1.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .onPlaced {
                    boxSize = it.size
                }
        ) {
            SearchPageResults(
                searchState = searchState,
                containerSize = boxSize,
                sizeOfSearchBar = sizeOfSearchBar,
                onBioClick = onBioClick,
                onPictureClick = onPictureClick,
                onScreenVideoPlayClick = onScreenVideoPlayClick
            )
        }
    }
}

@Composable
fun SearchPageTop(
    searchState: SearchState,
    sizeOfSearchBar: IntSize,
    onBackClick: () -> Unit,
    onInputContent: (String) -> Unit,
    searchBarPlaced: (LayoutCoordinates) -> Unit
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
    val scope = rememberCoroutineScope()
    val cornerShape by rememberUpdatedState(
        RoundedCornerShape(
            topStart = sizeOfSearchBar.height
                .div(2)
                .toFloat(),
            topEnd = sizeOfSearchBar.height
                .div(2)
                .toFloat(),
            bottomStart = 8.dp.value,
            bottomEnd = 8.dp.value
        )
    )
    AppSetsTextFieldNormal(
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
            .onPlaced(searchBarPlaced),
        leadingIcon = {
            Icon(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = {
                        scope.launch {
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
    containerSize: IntSize,
    sizeOfSearchBar: IntSize,
    onBioClick: (Bio) -> Unit,
    onPictureClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onScreenVideoPlayClick: (ScreenMediaFileUrl) -> Unit,
) {
    val density = LocalDensity.current
    val targetValue by rememberUpdatedState(
        if (searchState is SearchState.SearchSuccess) {
            with(density) {
                containerSize.height.toDp() - WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding()
            }
        } else {
            120.dp
        }
    )
    val heightOfBox = animateDpAsState(
        targetValue = targetValue,
        animationSpec = tween(550),
        label = "search_container_height_animate"
    )
    val roundedCornerShape by rememberUpdatedState(
        RoundedCornerShape(
            topStart = 8.dp.value,
            topEnd = 8.dp.value,
            bottomStart = sizeOfSearchBar.height
                .div(2)
                .toFloat(),
            bottomEnd = sizeOfSearchBar.height
                .div(2)
                .toFloat()
        )
    )
    Box(
        Modifier
            .height(heightOfBox.value)
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, roundedCornerShape)
            .border(1.dp, MaterialTheme.colorScheme.outline, roundedCornerShape)
    ) {
        when (searchState) {
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
                    searchState.tips?.let {
                        Text(text = stringResource(id = it))
                    }

                }
            }

            is SearchState.SearchFailed -> {
                searchState.tips?.let {
                    Text(
                        text = stringResource(id = it),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            is SearchState.SearchSuccess -> {
                if (searchState.results.isEmpty()) {
                    searchState.tips?.let {
                        Text(
                            text = stringResource(id = it),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(vertical = 12.dp)) {
                        items(searchState.results) { result ->
                            when (result) {
                                is SearchResult.SplitTitle -> {
                                    Column(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        result.title?.let {
                                            Text(
                                                text = stringResource(id = it),
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )
                                        }

                                        DesignHDivider()
                                    }
                                }

                                is SearchResult.SearchedUser -> {
                                    SearchedUserComponent(
                                        modifier = Modifier.clickable {
                                            onBioClick.invoke(result.userInfo)
                                        },
                                        result.userInfo
                                    )
                                }

                                is SearchResult.SearchedGroup -> {
                                    SearchedGroupComponent(
                                        modifier = Modifier.clickable {
                                            onBioClick.invoke(result.groupInfo)
                                        },
                                        result.groupInfo
                                    )
                                }

                                is SearchResult.SearchedScreen -> {
                                    SearchedScreenComponent(
                                        modifier = Modifier,
                                        screenInfo = result.screenInfo,
                                        onBioClick = onBioClick,
                                        onPictureClick = onPictureClick,
                                        onScreenVideoPlayClick = onScreenVideoPlayClick
                                    )
                                }

                                is SearchResult.SearchedApplications -> {
                                    SearchedApplicationComponent(
                                        modifier = Modifier,
                                        applications = result.applications,
                                        onBioClick = onBioClick
                                    )
                                }

                                else -> Unit
                            }
                        }
                    }
                }
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
                .size(36.dp)
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
                .size(36.dp)
                .clip(MaterialTheme.shapes.large),
            any = groupInfo.bioUrl,
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
    onPictureClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onScreenVideoPlayClick: (ScreenMediaFileUrl) -> Unit
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
            onPictureClick = onPictureClick,
            pictureInteractionFlow = { a, b -> },
            onScreenVideoPlayClick = onScreenVideoPlayClick
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchedApplicationComponent(
    modifier: Modifier,
    applications: List<Application>,
    onBioClick: (Bio) -> Unit
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
                        .clip(MaterialTheme.shapes.large)
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
