@file:OptIn(ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.content_selection

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.ImplementationMode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.camera.CameraComponents
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.LoadMoreHandler
import xcj.app.appsets.ui.compose.custom_component.SwipeContainer
import xcj.app.appsets.util.model.MediaStoreDataUri
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.DesignVDivider
import java.io.File

enum class DragValue { Start, Center, End }

data class ContentSelectionType(
    val name: String,
    val nameStringResource: Int,
)

sealed interface ContentSelectionResults {
    val context: Context
    val requestKey: String
    val contextPageName: String
    val selectType: String

    data class RichMediaContentSelectionResults(
        override val context: Context,
        override val requestKey: String,
        override val contextPageName: String,
        override val selectType: String,
        val selectItems: List<UriProvider>,
    ) : ContentSelectionResults

    data class LocationInfo(
        val coordinate: String,
        val info: String? = null,
        val extras: String? = null,
    )

    data class LocationContentSelectionResults(
        override val context: Context,
        override val requestKey: String,
        override val contextPageName: String,
        override val selectType: String,
        val locationInfo: LocationInfo,
    ) : ContentSelectionResults
}


private val selectionTypes = listOf(
    ContentSelectionType(ContentSelectionVarargs.PICTURE, xcj.app.appsets.R.string.picture),
    ContentSelectionType(ContentSelectionVarargs.VIDEO, xcj.app.appsets.R.string.video),
    ContentSelectionType(ContentSelectionVarargs.AUDIO, xcj.app.appsets.R.string.audio),
    ContentSelectionType(ContentSelectionVarargs.FILE, xcj.app.appsets.R.string.file),
    ContentSelectionType(
        ContentSelectionVarargs.LOCATION,
        xcj.app.appsets.R.string.location
    ),
    ContentSelectionType(ContentSelectionVarargs.CAMERA, xcj.app.appsets.R.string.camera),
)

@Composable
fun ContentSelectDialog(
    contextName: String,
    requestKey: String,
    requestTypes: List<String>? = null,
    onContentSelect: (ContentSelectionResults) -> Unit,
) {

    val pagerState = rememberPagerState { selectionTypes.size }

    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
        ) { index ->
            when (selectionTypes[index].name) {
                ContentSelectionVarargs.CAMERA -> {
                    CameraContentSelection(
                        contextName = contextName,
                        requestKey = requestKey,
                        onContentSelect = onContentSelect
                    )
                }

                ContentSelectionVarargs.LOCATION -> {
                    LocationContentSelection(
                        contextName = contextName,
                        requestKey = requestKey,
                        onContentSelect = onContentSelect
                    )
                }

                ContentSelectionVarargs.PICTURE -> {
                    PictureContentSelection(
                        contextName = contextName,
                        requestKey = requestKey,
                        onContentSelect = onContentSelect
                    )
                }

                ContentSelectionVarargs.VIDEO -> {
                    VideoContentSelection(
                        contextName = contextName,
                        requestKey = requestKey,
                        onContentSelect = onContentSelect
                    )
                }

                ContentSelectionVarargs.AUDIO -> {
                    AudioContentSelection(
                        contextName = contextName,
                        requestKey = requestKey,
                        onContentSelect = onContentSelect
                    )
                }

                ContentSelectionVarargs.FILE -> {
                    FileContentSelection(
                        contextName = contextName,
                        requestKey = requestKey,
                        onContentSelect = onContentSelect
                    )
                }
            }
        }

        val tabsScrollState = rememberScrollState()
        val buttonSize = remember {
            mutableStateOf(IntSize.Zero)
        }
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .padding()
                .horizontalScroll(tabsScrollState)
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            selectionTypes.forEachIndexed { index, selectionType ->
                SegmentedButton(
                    modifier = Modifier.onPlaced {
                        buttonSize.value = it.size
                    },
                    selected = index == pagerState.currentPage,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = selectionTypes.size
                    ),
                    icon = {}
                ) {
                    Text(stringResource(selectionType.nameStringResource))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        LaunchedEffect(pagerState.currentPage) {
            tabsScrollState.animateScrollTo(buttonSize.value.width * pagerState.currentPage)
        }
    }
}


@Composable
fun CameraContentSelection(
    contextName: String,
    requestKey: String,
    onContentSelect: (ContentSelectionResults) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraComponents = remember {
        CameraComponents().apply {
            create(context)
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val capturedPicture = remember {
        mutableStateOf<File?>(null)
    }
    val capturedPictureIsSelect = remember {
        mutableStateOf(false)
    }
    DisposableEffect(Unit) {
        onDispose {
            if (capturedPictureIsSelect.value) {
                val pictureFile = capturedPicture.value
                if (pictureFile != null && pictureFile.exists()) {
                    pictureFile.delete()
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 4.dp, top = 60.dp, end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val swipeableState = remember {
            mutableStateOf(DragValue.Start)
        }
        val alphaAnimation = remember {
            AnimationState(1f)
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxSize(),
                    factory = {
                        val preview = PreviewView(it)
                        preview.setImplementationMode(ImplementationMode.COMPATIBLE)
                        //change to texture view
                        preview.setBackgroundColor(Color.TRANSPARENT)
                        preview
                    }
                ) {
                    cameraComponents.bindToLifecycle(lifecycleOwner, it)
                }
                if (alphaAnimation.value > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = alphaAnimation.value
                            }
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(xcj.app.appsets.R.string.slide_to_enable_camera_preview))
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.align(Alignment.TopStart)) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = capturedPicture.value != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AnyImage(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.shapes.extraLarge
                                )
                                .clip(MaterialTheme.shapes.extraLarge)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    MaterialTheme.shapes.extraLarge
                                )
                                .clickable {
                                    val pictureFile = capturedPicture.value
                                    if (pictureFile == null) {
                                        return@clickable
                                    }
                                    val pictureFileUri = object : UriProvider {
                                        override fun provideUri(): Uri? {
                                            return pictureFile.toUri()
                                        }
                                    }
                                    capturedPictureIsSelect.value = true
                                    val selectContents = listOf(pictureFileUri)
                                    val results =
                                        ContentSelectionResults.RichMediaContentSelectionResults(
                                            context,
                                            requestKey,
                                            contextName,
                                            ContentSelectionVarargs.PICTURE,
                                            selectContents
                                        )
                                    onContentSelect(results)
                                },
                            any = capturedPicture.value
                        )
                        DesignVDivider(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.delete),
                            modifier = Modifier.clickable(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val capturedPictureFile = capturedPicture.value
                                        capturedPictureFile?.delete()
                                        capturedPicture.value = null
                                    }
                                }
                            )
                        )
                    }
                }
            }
            Box(Modifier.align(Alignment.TopCenter)) {
                SwipeContainer(
                    onDragValueChanged = { dragValue ->
                        if (swipeableState.value == dragValue) {
                            return@SwipeContainer
                        }
                        swipeableState.value = dragValue
                        if (dragValue == DragValue.End) {
                            cameraComponents.startCamera()
                        } else {
                            cameraComponents.stopCamera()
                        }
                        coroutineScope.launch {
                            alphaAnimation.animateTo(
                                if (swipeableState.value == DragValue.Start) {
                                    1f
                                } else {
                                    0f
                                }, tween(450)
                            )
                        }
                    },
                    dragContent = {
                        IconButton(
                            onClick = {
                                if (swipeableState.value != DragValue.End) {
                                    return@IconButton
                                }
                                coroutineScope.launch {
                                    cameraComponents.takePicture { pictureFile ->
                                        capturedPicture.value = pictureFile
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_camera_24),
                                contentDescription = "camera_capture"
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LocationContentSelection(
    contextName: String,
    requestKey: String,
    onContentSelect: (ContentSelectionResults) -> Unit,
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 4.dp, top = 60.dp, end = 4.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = MaterialTheme.shapes.extraLarge
        ) {

        }
        IconButton(onClick = {
            val locationInfo = ContentSelectionResults.LocationInfo(
                coordinate = "104.066284,30.572938",
                info = "四川省成都市武侯区锦悦西路2"
            )
            val results = ContentSelectionResults.LocationContentSelectionResults(
                context,
                requestKey,
                contextName,
                ContentSelectionVarargs.LOCATION,
                locationInfo
            )
            onContentSelect(results)
        }) {
            Icon(
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_location_on_24),
                contentDescription = "location"
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PictureContentSelection(
    contextName: String,
    requestKey: String,
    onContentSelect: (ContentSelectionResults) -> Unit,
) {
    val contentSelectionResultsProvider = remember {
        ContentSelectionResultsProvider().apply {
            mediaStoreType = MediaStore.Images::class.java
        }
    }
    val contentUrls = contentSelectionResultsProvider.contentUris
    val context = LocalContext.current
    LaunchedEffect(true) {
        contentSelectionResultsProvider.load(context, true)
    }
    val gridState = rememberLazyGridState()
    LoadMoreHandler(scrollableState = gridState) {
        contentSelectionResultsProvider.load(context, false)
    }

    val selectContents = remember {
        mutableListOf<UriProvider>()
    }
    var searchText by remember {
        mutableStateOf("")
    }
    var isSearchMode by remember {
        mutableStateOf(false)
    }
    val filteredContentUrls by remember {
        derivedStateOf {
            contentUrls.filter {
                if (it is MediaStoreDataUri) {
                    it.displayName?.contains(searchText, true) == true
                } else {
                    true
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    )
    {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(start = 4.dp, top = 60.dp, end = 4.dp, bottom = 120.dp)
        ) {
            items(
                items = filteredContentUrls
            ) { contentUriProvider ->
                Box(
                    Modifier
                        .padding(2.dp)
                        .animateItem()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .clickable(
                                onClick = {
                                    selectContents.add(contentUriProvider)
                                    val results =
                                        ContentSelectionResults.RichMediaContentSelectionResults(
                                            context,
                                            requestKey,
                                            contextName,
                                            ContentSelectionVarargs.PICTURE,
                                            selectContents
                                        )
                                    onContentSelect(results)
                                }
                            )
                    ) {
                        AnyImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            any = contentUriProvider.provideUri()
                        )
                        if (isSearchMode) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val mediaStoreWrapper =
                                    (contentUriProvider as? MediaStoreDataUri)
                                Text(
                                    text = mediaStoreWrapper?.displayName ?: "",
                                    fontSize = 12.sp,
                                    modifier = Modifier,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = mediaStoreWrapper?.sizeReadable ?: "",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
        if (
            contentUrls.isEmpty()
        ) {
            Text(stringResource(xcj.app.appsets.R.string.there_is_nothing_here))
        }

        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            isSearchMode = isSearchMode,
            searchText = searchText,
            actionIcon = xcj.app.compose_share.R.drawable.ic_camera_24,
            onSearchModeChanged = {
                isSearchMode = it
            },
            onSearchContentChanged = {
                searchText = it
            },
            onActionClick = {

            }
        )
    }
}

@Composable
fun BottomActions(
    modifier: Modifier,
    isSearchMode: Boolean,
    searchText: String,
    actionIcon: Int,
    onSearchModeChanged: (Boolean) -> Unit,
    onSearchContentChanged: (String) -> Unit,
    onActionClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            modifier = Modifier.size(TextFieldDefaults.MinHeight),
            onClick = {
                onSearchModeChanged(!isSearchMode)
            }
        ) {
            Icon(
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_search_24),
                contentDescription = null
            )
        }

        AnimatedVisibility(
            visible = isSearchMode,
            modifier = Modifier.clipToBounds()
        ) {
            DesignTextField(
                modifier = Modifier
                    .width(200.dp)
                    .defaultMinSize(minHeight = 42.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                value = searchText,
                onValueChange = {
                    onSearchContentChanged(it)
                },
                placeholder = {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.search),
                        fontSize = 12.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
        Spacer(modifier = Modifier.weight(1f))

        FilledTonalIconButton(
            modifier = Modifier.size(TextFieldDefaults.MinHeight),
            onClick = onActionClick
        ) {
            Icon(
                painter = painterResource(actionIcon),
                contentDescription = null
            )
        }
    }
}


@Composable
fun VideoContentSelection(
    contextName: String,
    requestKey: String,
    onContentSelect: (ContentSelectionResults) -> Unit,
) {
    val contentSelectionResultsProvider = remember {
        ContentSelectionResultsProvider().apply {
            mediaStoreType = MediaStore.Video::class.java
        }
    }
    val contentUrls = contentSelectionResultsProvider.contentUris
    val context = LocalContext.current
    val columnState = rememberLazyListState()

    LaunchedEffect(true) {
        contentSelectionResultsProvider.load(context, true)
    }

    LoadMoreHandler(scrollableState = columnState) {
        contentSelectionResultsProvider.load(context, false)
    }
    val selectContents = remember {
        mutableListOf<UriProvider>()
    }
    var searchText by remember {
        mutableStateOf("")
    }
    var isSearchMode by remember {
        mutableStateOf(false)
    }
    val filteredContentUrls by remember {
        derivedStateOf {
            contentUrls.filter {
                if (it is MediaStoreDataUri) {
                    it.displayName?.contains(searchText, true) == true
                } else {
                    true
                }
            }
        }
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = PaddingValues(start = 4.dp, top = 60.dp, end = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(items = filteredContentUrls) { contentUriProvider ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .animateItem()
                        .animateContentSize()
                        .clickable(onClick = {
                            selectContents.add(contentUriProvider)
                            val results =
                                ContentSelectionResults.RichMediaContentSelectionResults(
                                    context,
                                    requestKey,
                                    contextName,
                                    ContentSelectionVarargs.VIDEO,
                                    selectContents
                                )
                            onContentSelect(results)
                        })
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AnyImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(250.dp),
                            any = contentUriProvider.provideUri()
                        )
                        FilledTonalIconButton(
                            modifier = Modifier.align(Alignment.Center),
                            onClick = {}
                        ) {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_slow_motion_video_24),
                                contentDescription = "play"
                            )
                        }
                    }
                    if (isSearchMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            val mediaStoreWrapper =
                                (contentUriProvider as? MediaStoreDataUri)
                            Text(
                                text = mediaStoreWrapper?.displayName ?: "",
                                fontSize = 12.sp,
                                modifier = Modifier,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = mediaStoreWrapper?.sizeReadable ?: "",
                                fontSize = 12.sp
                            )
                        }
                    }

                }
            }
        }
        if (
            contentUrls.isEmpty()
        ) {
            Text(stringResource(xcj.app.appsets.R.string.there_is_nothing_here))
        }
        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            isSearchMode = isSearchMode,
            searchText = searchText,
            actionIcon = xcj.app.compose_share.R.drawable.ic_camera_24,
            onSearchModeChanged = {
                isSearchMode = it
            },
            onSearchContentChanged = {
                searchText = it
            },
            onActionClick = {

            }
        )
    }
}

@Composable
fun AudioContentSelection(
    contextName: String,
    requestKey: String,
    onContentSelect: (ContentSelectionResults) -> Unit,
) {
    val contentSelectionResultsProvider = remember {
        ContentSelectionResultsProvider().apply {
            mediaStoreType = MediaStore.Audio::class.java
        }
    }
    val contentUrls = contentSelectionResultsProvider.contentUris
    val context = LocalContext.current
    val columnState = rememberLazyListState()

    LaunchedEffect(true) {
        contentSelectionResultsProvider.load(context, true)
    }

    LoadMoreHandler(scrollableState = columnState) {
        contentSelectionResultsProvider.load(context, false)
    }

    val selectContents = remember {
        mutableListOf<UriProvider>()
    }

    var searchText by remember {
        mutableStateOf("")
    }
    var isSearchMode by remember {
        mutableStateOf(false)
    }
    val filteredContentUrls by remember {
        derivedStateOf {
            contentUrls.filter {
                if (it is MediaStoreDataUri) {
                    it.displayName?.contains(searchText, true) == true
                } else {
                    true
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = PaddingValues(start = 4.dp, top = 60.dp, end = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items = filteredContentUrls) { contentUriProvider ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectContents.add(contentUriProvider)
                            val results = ContentSelectionResults.RichMediaContentSelectionResults(
                                context,
                                requestKey,
                                contextName,
                                ContentSelectionVarargs.AUDIO,
                                selectContents
                            )
                            onContentSelect(results)
                        },
                    shape = MaterialTheme.shapes.large,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_audiotrack_24),
                            contentDescription = "file_icon"
                        )

                        val mediaStoreWrapper =
                            (contentUriProvider as? MediaStoreDataUri)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = mediaStoreWrapper?.displayName ?: "")
                            Text(text = mediaStoreWrapper?.sizeReadable ?: "", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        if (
            contentUrls.isEmpty()
        ) {
            Text(stringResource(xcj.app.appsets.R.string.there_is_nothing_here))
        }

        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            isSearchMode = isSearchMode,
            searchText = searchText,
            actionIcon = xcj.app.compose_share.R.drawable.ic_outline_keyboard_voice_24,
            onSearchModeChanged = {
                isSearchMode = it
            },
            onSearchContentChanged = {
                searchText = it
            },
            onActionClick = {

            }
        )
    }

}

@Composable
fun FileContentSelection(
    contextName: String,
    requestKey: String,
    onContentSelect: (ContentSelectionResults) -> Unit,
) {
    val contentSelectionResultsProvider = remember {
        ContentSelectionResultsProvider().apply {
            mediaStoreType = MediaStore.Files::class.java
        }
    }
    val contentUrls = contentSelectionResultsProvider.contentUris
    val context = LocalContext.current
    val columnState = rememberLazyListState()

    LaunchedEffect(true) {
        contentSelectionResultsProvider.load(context, true)
    }

    LoadMoreHandler(scrollableState = columnState) {
        contentSelectionResultsProvider.load(context, false)
    }

    val selectContents = remember {
        mutableListOf<UriProvider>()
    }

    var searchText by remember {
        mutableStateOf("")
    }
    var isSearchMode by remember {
        mutableStateOf(false)
    }
    val filteredContentUrls by remember {
        derivedStateOf {
            contentUrls.filter {
                if (it is MediaStoreDataUri) {
                    it.displayName?.contains(searchText, true) == true
                } else {
                    true
                }
            }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = PaddingValues(start = 4.dp, top = 60.dp, end = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items = filteredContentUrls) { contentUriProvider ->
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectContents.add(contentUriProvider)
                            val results = ContentSelectionResults.RichMediaContentSelectionResults(
                                context,
                                requestKey,
                                contextName,
                                ContentSelectionVarargs.FILE,
                                selectContents
                            )
                            onContentSelect(results)
                        },
                    shape = MaterialTheme.shapes.large,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                            contentDescription = "file_icon"
                        )
                        val mediaStoreWrapper =
                            contentUriProvider as? MediaStoreDataUri
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = mediaStoreWrapper?.displayName ?: "")
                            Text(text = mediaStoreWrapper?.sizeReadable ?: "", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (
            contentUrls.isEmpty()
        ) {
            Text(stringResource(xcj.app.appsets.R.string.there_is_nothing_here))
        }

        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            isSearchMode = isSearchMode,
            searchText = searchText,
            actionIcon = xcj.app.compose_share.R.drawable.ic_round_add_24,
            onSearchModeChanged = {
                isSearchMode = it
            },
            onSearchContentChanged = {
                searchText = it
            },
            onActionClick = {

            }
        )
    }
}