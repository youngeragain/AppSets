@file:OptIn(ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.content_selection

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.ImplementationMode
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.camera.CameraComponents
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.LoadMoreHandler
import xcj.app.appsets.ui.compose.custom_component.SwipeContainer
import xcj.app.appsets.util.model.MediaStoreDataUri
import xcj.app.appsets.util.model.UriProvider
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

    val pageState = rememberPagerState { selectionTypes.size }

    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        val tabsScrollState = rememberScrollState()
        val buttonSize = remember {
            mutableStateOf(IntSize.Zero)
        }
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .horizontalScroll(tabsScrollState)
        ) {

            selectionTypes.forEachIndexed { index, selectionType ->
                SegmentedButton(
                    modifier = Modifier.onPlaced {
                        buttonSize.value = it.size
                    },
                    selected = index == pageState.currentPage,
                    onClick = {
                        scope.launch {
                            pageState.animateScrollToPage(index)
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
        }
        HorizontalPager(
            state = pageState, modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { index ->
            LaunchedEffect(index) {
                scope.launch {
                    tabsScrollState.animateScrollTo(buttonSize.value.width * index)
                }
            }
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
    val scope = rememberCoroutineScope()
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
            .padding(12.dp),
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
                                    scope.launch(Dispatchers.IO) {
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
                        scope.launch {
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
                                scope.launch {
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
            .padding(12.dp),
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
    val picsState = contentSelectionResultsProvider.contentUris
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
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(2.dp)
        ) {
            items(
                items = picsState
            ) { contentUriProvider ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .height(220.dp)
                        .padding(4.dp)
                ) {
                    AnyImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.extraLarge)
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
                            ),
                        any = contentUriProvider.provideUri()
                    )
                }
            }
        }
        if (
            picsState.isEmpty()
        ) {
            Text("There is nothing here")
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
    val picsState = contentSelectionResultsProvider.contentUris
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
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = picsState) { contentUriProvider ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Box(
                        Modifier
                            .fillMaxSize()
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
                        AnyImage(
                            modifier = Modifier.fillMaxSize(),
                            any = contentUriProvider.provideUri()
                        )
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.shapes.extraLarge
                                    )
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Image(
                                    modifier = Modifier,
                                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_play_circle_filled_24),
                                    contentDescription = "play"
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
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
        }
        if (
            picsState.isEmpty()
        ) {
            Text("There is nothing here")
        }
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
    val picsState = contentSelectionResultsProvider.contentUris
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

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = picsState) { contentUriProvider ->
                Card(
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
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
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
            picsState.isEmpty()
        ) {
            Text("There is nothing here")
        }
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
    val picsState = contentSelectionResultsProvider.contentUris
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

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = picsState) { contentUriProvider ->
                Card(
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
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
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
            picsState.isEmpty()
        ) {
            Text("There is nothing here")
        }
    }
}