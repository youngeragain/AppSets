@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)

package xcj.app.appsets.ui.compose.content_selection

import android.Manifest
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest.Builder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.SingleMimeType
import androidx.camera.view.PreviewView
import androidx.camera.view.PreviewView.ImplementationMode
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.LocalUseCaseOfNowSpaceContent
import xcj.app.appsets.ui.compose.camera.CameraComponents
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionRequest.SelectionTypeParam
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult.RichMediaContentSelectionResult
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DragValue
import xcj.app.appsets.ui.compose.custom_component.LoadMoreHandler
import xcj.app.appsets.ui.compose.custom_component.SwipeContainer
import xcj.app.appsets.ui.compose.theme.extShapes
import xcj.app.appsets.util.ktx.asComponentActivityOrNull
import xcj.app.appsets.util.model.MediaStoreDataUri
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.DesignVDivider
import xcj.app.starter.android.ActivityThemeInterface
import xcj.app.starter.android.SystemContentSelectionCallback
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage
import xcj.app.starter.android.usecase.PlatformUseCase
import java.io.File


typealias CountProvider = (String) -> Int

private val defaultMaxCountProvider: CountProvider
    get() = { 1 }

fun defaultImageSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.IMAGE, countProvider)
    )

fun defaultVideoSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.VIDEO, countProvider)
    )

fun defaultAudioSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.AUDIO, countProvider)
    )

fun defaultFileSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.FILE, countProvider)
    )

fun defaultLocationSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.LOCATION, countProvider)
    )

fun defaultCameraSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    listOf(
        SelectionTypeParam(ContentSelectionTypes.CAMERA, countProvider)
    )

fun defaultAllSelectionTypeParam(
    countProvider: CountProvider = defaultMaxCountProvider
): List<SelectionTypeParam> =
    defaultImageSelectionTypeParam(countProvider) +
            defaultVideoSelectionTypeParam(countProvider) +
            defaultAudioSelectionTypeParam(countProvider) +
            defaultFileSelectionTypeParam(countProvider) +
            defaultLocationSelectionTypeParam(countProvider) +
            defaultCameraSelectionTypeParam(countProvider)


sealed interface ContentSelectionPageState {
    object StartPrompt : ContentSelectionPageState
    object AppImplementation : ContentSelectionPageState
    data class SystemImplConfirm(
        val systemSelectedContents: List<UriProvider>
    ) : ContentSelectionPageState
}

@Composable
fun ContentSelectionPromptSheetContent(
    request: ContentSelectionRequest,
    shouldConfirmIfUseSystemImplementation: Boolean = true,
    onContentSelected: (ContentSelectionResult) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val nowSpaceContentUseCase = LocalUseCaseOfNowSpaceContent.current
    val coroutineScope = rememberCoroutineScope()
    var contentSelectionPageState by remember {
        mutableStateOf<ContentSelectionPageState>(ContentSelectionPageState.StartPrompt)
    }
    Box {
        AnimatedContent(
            targetState = contentSelectionPageState,
            contentAlignment = Alignment.TopCenter,
            transitionSpec = {
                fadeIn(animationSpec = tween())
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            }
        ) { targetContentSelectionPageState ->
            when (targetContentSelectionPageState) {
                is ContentSelectionPageState.StartPrompt -> {
                    ContentSelectPromptContent(
                        onUseSystemImplClick = {
                            val activity = context.asComponentActivityOrNull()
                            if (activity == null) {
                                return@ContentSelectPromptContent
                            }
                            if (activity !is ActivityThemeInterface) {
                                return@ContentSelectPromptContent
                            }
                            val systemContentSelectionCallback =
                                object : SystemContentSelectionCallback {
                                    override fun onSystemContentSelected(content: Any?) {
                                        if (content !is Uri) {
                                            return
                                        }
                                        if (shouldConfirmIfUseSystemImplementation) {
                                            val systemSelectedContents = listOf(content)
                                                .map(UriProvider::fromUri)
                                            contentSelectionPageState =
                                                ContentSelectionPageState.SystemImplConfirm(
                                                    systemSelectedContents
                                                )
                                        } else {
                                            val systemSelectedContents = listOf(content)
                                                .map(UriProvider::fromUri)
                                            val results =
                                                RichMediaContentSelectionResult(
                                                    context,
                                                    request,
                                                    request.defaultSelectionType,
                                                    systemSelectedContents
                                                )
                                            onContentSelected(results)
                                            onDismiss()
                                        }

                                    }
                                }
                            activity.setSystemContentSelectionCallback(
                                systemContentSelectionCallback
                            )
                            val activityResultLauncher =
                                activity.getActivityResultLauncher(
                                    ActivityResultContracts.PickVisualMedia::class.java,
                                    null
                                )
                            val singleMimeType =
                                SingleMimeType(request.defaultSelectionType)
                            val pickVisualMediaRequest =
                                Builder().setMediaType(singleMimeType)
                                    .build()
                            activityResultLauncher?.launch(pickVisualMediaRequest)
                        },
                        onUseAppImplClick = {
                            val platformFilePermission =
                                PlatformPermissionsUsage.provideFilePermission(context)
                            if (!platformFilePermission.granted) {
                                onDismiss()
                                coroutineScope.launch {
                                    nowSpaceContentUseCase.showPlatformPermissionUsageTipsIfNeeded(
                                        context = context
                                    )
                                }
                                return@ContentSelectPromptContent
                            }
                            contentSelectionPageState = ContentSelectionPageState.AppImplementation
                        }
                    )
                }

                is ContentSelectionPageState.AppImplementation -> {
                    AppImplContentSelectionContent(
                        request = request,
                        onContentSelected = onContentSelected
                    )
                }

                is ContentSelectionPageState.SystemImplConfirm -> {
                    SystemImplContentSelectionConfirmContent(
                        systemImplConfirm = targetContentSelectionPageState,
                        onConfirm = {
                            val selectedContents =
                                targetContentSelectionPageState.systemSelectedContents
                            val results =
                                RichMediaContentSelectionResult(
                                    context,
                                    request,
                                    request.defaultSelectionType,
                                    selectedContents
                                )
                            onContentSelected(results)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemImplContentSelectionConfirmContent(
    modifier: Modifier = Modifier,
    systemImplConfirm: ContentSelectionPageState.SystemImplConfirm,
    onConfirm: () -> Unit
) {
    val gridState = rememberLazyGridState()
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth(),
            state = gridState,
            contentPadding = PaddingValues(
                start = 4.dp,
                top = 4.dp,
                end = 4.dp,
                bottom = 120.dp
            )
        ) {
            items(
                items = systemImplConfirm.systemSelectedContents
            ) { contentUriProvider ->
                Box(
                    modifier = Modifier.padding(2.dp)
                ) {
                    AnyImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                RectangleShape
                            ),
                        model = contentUriProvider.provideUri()
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.BottomEnd)
        ) {
            FilledTonalButton(
                onClick = onConfirm
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.ok))
            }
        }
    }

}

@Composable
private fun ContentSelectPromptContent(
    modifier: Modifier = Modifier,
    onUseSystemImplClick: () -> Unit,
    onUseAppImplClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(
            start = 12.dp,
            end = 12.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(xcj.app.appsets.R.string.choose_implementation),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onUseSystemImplClick)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(MaterialTheme.shapes.extShapes.large)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extShapes.large
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_settings_24),
                    contentDescription = stringResource(xcj.app.appsets.R.string.system)
                )
            }
            Column {
                Text(text = stringResource(xcj.app.appsets.R.string.system_content_selection))
                Text(
                    text = stringResource(xcj.app.appsets.R.string.system_content_selection_des),
                    fontSize = 12.sp
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onUseAppImplClick)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(68.dp)
                    .clip(MaterialTheme.shapes.extShapes.large)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extShapes.large
                    ),
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                contentDescription = stringResource(xcj.app.appsets.R.string.system)
            )
            Column {
                Text(text = stringResource(xcj.app.appsets.R.string.app_custom_content_selection))
                Text(
                    text = stringResource(xcj.app.appsets.R.string.app_custom_content_selection_des),
                    fontSize = 12.sp
                )
            }
        }

    }
}

@Composable
private fun getTabName(selectionType: String): String {
    return when (selectionType) {
        ContentSelectionTypes.IMAGE -> stringResource(xcj.app.appsets.R.string.picture)
        ContentSelectionTypes.VIDEO -> stringResource(xcj.app.appsets.R.string.video)
        ContentSelectionTypes.AUDIO -> stringResource(xcj.app.appsets.R.string.audio)
        ContentSelectionTypes.FILE -> stringResource(xcj.app.appsets.R.string.file)
        ContentSelectionTypes.LOCATION -> stringResource(xcj.app.appsets.R.string.location)
        ContentSelectionTypes.CAMERA -> stringResource(xcj.app.appsets.R.string.camera)
        else -> ""
    }
}

@Composable
private fun AppImplContentSelectionContent(
    modifier: Modifier = Modifier,
    request: ContentSelectionRequest,
    onContentSelected: (ContentSelectionResult) -> Unit,
) {

    val selectionTypes = remember {
        val contentSelectionTabs = mutableListOf(
            ContentSelectionTypes.IMAGE,
            ContentSelectionTypes.VIDEO,
            ContentSelectionTypes.AUDIO,
            ContentSelectionTypes.FILE,
            ContentSelectionTypes.LOCATION,
            ContentSelectionTypes.CAMERA,
        )
        contentSelectionTabs.removeIf { selectionType ->
            request.selectionTypeParams.firstOrNull {
                it.selectionType == selectionType
            } == null
        }
        contentSelectionTabs
    }
    var defaultSelectionTypeIndex =
        selectionTypes.indexOfFirst { selectionType -> selectionType == request.defaultSelectionType }
    if (defaultSelectionTypeIndex == -1) {
        defaultSelectionTypeIndex = 0
    }
    val pagerState = rememberPagerState(defaultSelectionTypeIndex) { selectionTypes.size }

    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState,
        ) { index ->
            val tabSelectionType = selectionTypes[index]
            when (tabSelectionType) {
                ContentSelectionTypes.CAMERA -> {
                    CameraContentSelection(
                        request = request,
                        onContentSelected = onContentSelected
                    )
                }

                ContentSelectionTypes.LOCATION -> {
                    LocationContentSelection(
                        request = request,
                        onContentSelected = onContentSelected
                    )
                }

                ContentSelectionTypes.IMAGE -> {
                    PictureContentSelection(
                        request = request,
                        onContentSelected = onContentSelected
                    )
                }

                ContentSelectionTypes.VIDEO -> {
                    VideoContentSelection(
                        request = request,
                        onContentSelected = onContentSelected
                    )
                }

                ContentSelectionTypes.AUDIO -> {
                    AudioContentSelection(
                        request = request,
                        onContentSelected = onContentSelected
                    )
                }

                ContentSelectionTypes.FILE -> {
                    FileContentSelection(
                        request = request,
                        onContentSelected = onContentSelected
                    )
                }
            }
        }
        if (selectionTypes.size > 1) {
            val tabsScrollState = rememberScrollState()

            val buttonSize = remember {
                mutableStateOf(IntSize.Zero)
            }

            LaunchedEffect(pagerState.currentPage) {
                tabsScrollState.animateScrollTo(buttonSize.value.width * pagerState.currentPage)
            }

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .horizontalScroll(tabsScrollState)
            ) {
                Spacer(modifier = Modifier.width(12.dp))
                selectionTypes.forEachIndexed { index, selectionType ->
                    SegmentedButton(
                        modifier = Modifier.onSizeChanged {
                            buttonSize.value = it
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
                        val tabName = getTabName(selectionType)
                        Text(text = tabName)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
        }
    }
}


@Composable
private fun CameraContentSelection(
    request: ContentSelectionRequest,
    onContentSelected: (ContentSelectionResult) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val cameraComponents = remember {
        CameraComponents().apply {
            prepare(context)
        }
    }
    var capturedPictureFile by remember {
        mutableStateOf<File?>(null)
    }
    var capturedPictureIsSelect by remember {
        mutableStateOf(false)
    }
    var swipeableState by remember {
        mutableStateOf<DragValue>(DragValue.Start)
    }
    val alphaAnimation = remember {
        AnimationState(1f)
    }
    var hasCameraPermission by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(true) {
        hasCameraPermission = PlatformUseCase.hasPlatformPermissions(
            context,
            listOf(Manifest.permission.CAMERA)
        )
    }
    DisposableEffect(Unit) {
        onDispose {
            cameraComponents.close()
            if (capturedPictureIsSelect) {
                val pictureFile = capturedPictureFile
                if (pictureFile != null && pictureFile.exists()) {
                    pictureFile.delete()
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
                    cameraComponents.bindToLifecycle(lifecycleOwner)
                    cameraComponents.attachPreview(it)
                }
                if (swipeableState != DragValue.End) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = alphaAnimation.value
                            }
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {

                        Column(
                            modifier = Modifier.animateContentSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(xcj.app.appsets.R.string.slide_to_enable_camera_preview))
                            if (!hasCameraPermission) {
                                Text(
                                    text = stringResource(
                                        xcj.app.appsets.R.string.no_x_permission,
                                        stringResource(xcj.app.appsets.R.string.camera)
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable(
                                        onClick = {

                                        }
                                    )
                                )
                            }

                        }
                    }
                }
            }
        }

        Box(Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.align(Alignment.TopStart)) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = capturedPictureFile != null,
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
                                    val pictureFile = capturedPictureFile
                                    if (pictureFile == null) {
                                        return@clickable
                                    }
                                    val uriProvider = UriProvider.fromFile(pictureFile)
                                    if (uriProvider == null) {
                                        return@clickable
                                    }
                                    capturedPictureIsSelect = true
                                    val selectedContents = listOf(uriProvider)
                                    val results =
                                        ContentSelectionResult.RichMediaContentSelectionResult(
                                            context,
                                            request,
                                            ContentSelectionTypes.IMAGE,
                                            selectedContents
                                        )
                                    onContentSelected(results)
                                },
                            model = capturedPictureFile
                        )
                        DesignVDivider(modifier = Modifier.height(32.dp))
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.delete),
                            modifier = Modifier.clickable(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        capturedPictureFile?.delete()
                                        capturedPictureFile = null
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
                        if (swipeableState == dragValue) {
                            return@SwipeContainer
                        }
                        swipeableState = dragValue
                        if (dragValue == DragValue.End) {
                            cameraComponents.startCamera()
                        } else {
                            cameraComponents.stopCamera()
                        }
                        coroutineScope.launch {
                            alphaAnimation.animateTo(
                                if (swipeableState == DragValue.Start) {
                                    1f
                                } else {
                                    0f
                                }, tween(350)
                            )
                        }
                    },
                    dragContent = {
                        IconButton(
                            onClick = {
                                if (swipeableState != DragValue.End) {
                                    return@IconButton
                                }
                                cameraComponents.takePicture { pictureFile ->
                                    capturedPictureFile = pictureFile
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
private fun LocationContentSelection(
    request: ContentSelectionRequest,
    onContentSelected: (ContentSelectionResult) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var swipeableState by remember {
        mutableStateOf<DragValue>(DragValue.Start)
    }
    val alphaAnimation = remember {
        AnimationState(1f)
    }
    var hasLocationPermission by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(true) {
        hasLocationPermission = PlatformUseCase.hasPlatformPermissions(
            context,
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                if (swipeableState != DragValue.End) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = alphaAnimation.value
                            }
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.animateContentSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = stringResource(xcj.app.appsets.R.string.slide_to_enable_location_preview))
                            if (!hasLocationPermission) {
                                Text(
                                    text = stringResource(
                                        xcj.app.appsets.R.string.no_x_permission,
                                        stringResource(xcj.app.appsets.R.string.location)
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable(
                                        onClick = {

                                        }
                                    ),

                                    )
                            }

                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth()) {
            Box(Modifier.align(Alignment.TopCenter)) {
                SwipeContainer(
                    onDragValueChanged = { dragValue ->
                        if (swipeableState == dragValue) {
                            return@SwipeContainer
                        }
                        swipeableState = dragValue
                        if (dragValue == DragValue.End) {

                        } else {

                        }
                        coroutineScope.launch {
                            alphaAnimation.animateTo(
                                if (swipeableState == DragValue.Start) {
                                    1f
                                } else {
                                    0f
                                }, tween(350)
                            )
                        }
                    },
                    dragContent = {
                        IconButton(onClick = {
                            if (swipeableState != DragValue.End) {
                                return@IconButton
                            }
                            val locationInfo =
                                ContentSelectionResult.LocationContentSelectionResult.LocationInfo(
                                    coordinate = "104.066284,30.572938",
                                    info = "四川省成都市武侯区锦悦西路2"
                                )
                            val results = ContentSelectionResult.LocationContentSelectionResult(
                                context,
                                request,
                                ContentSelectionTypes.LOCATION,
                                locationInfo
                            )
                            onContentSelected(results)
                        }) {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_location_on_24),
                                contentDescription = "location"
                            )
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PictureContentSelection(
    request: ContentSelectionRequest,
    onContentSelected: (ContentSelectionResult) -> Unit,
) {

    val context = LocalContext.current
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val contentSelectionResultsProvider = remember {
        ContentSelectionResultsProvider(MediaStore.Images::class.java)
    }
    val contentUris = contentSelectionResultsProvider.contentUris
    LaunchedEffect(true) {
        contentSelectionResultsProvider.load(context, true)
    }
    val gridState = rememberLazyGridState()
    LoadMoreHandler(scrollableState = gridState) {
        contentSelectionResultsProvider.load(context, false)
    }

    val selectedContents = remember {
        mutableStateListOf<UriProvider>()
    }
    var searchText by remember {
        mutableStateOf("")
    }
    var isSearchMode by remember {
        mutableStateOf(false)
    }
    val filteredContentUrls by remember {
        derivedStateOf {
            contentUris.filter {
                if (it is MediaStoreDataUri) {
                    it.displayName?.contains(searchText, true) == true
                } else {
                    true
                }
            }
        }
    }
    var boxSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    val itemHeightDp by remember {
        derivedStateOf {
            with(density) {
                (boxSize.width.toDp() - 2.dp * 6) / 3
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged {
                boxSize = it
            },
        contentAlignment = Alignment.Center
    )
    {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 120.dp)
        ) {
            items(
                items = filteredContentUrls
            ) { contentUriProvider ->
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .animateItem()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        val itemClipShape = calculateItemClipShape(
                            itemHeightDp,
                            selectedContents,
                            contentUriProvider
                        )
                        AnyImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeightDp)
                                .clip(itemClipShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, itemClipShape)
                                .clickable(
                                    onClick = {
                                        val selectionTypeMaxCount =
                                            request.selectionTypeMaxCount(ContentSelectionTypes.IMAGE)
                                        val isSelected =
                                            selectedContents.firstOrNull { it == contentUriProvider } != null
                                        if (isSelected) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                                            selectedContents.removeIf { it == contentUriProvider }
                                        } else if (selectionTypeMaxCount == 1) {
                                            selectedContents.clear()
                                            selectedContents.add(contentUriProvider)
                                        } else if (selectedContents.size >= selectionTypeMaxCount) {
                                            return@clickable
                                        } else {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                            selectedContents.add(contentUriProvider)
                                        }
                                    }
                                ),
                            model = contentUriProvider.provideUri()
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
            contentUris.isEmpty()
        ) {
            Text(stringResource(xcj.app.appsets.R.string.there_is_nothing_here))
        }

        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            request = request,
            contentSelectionType = ContentSelectionTypes.IMAGE,
            devicesContents = contentUris,
            selectedContents = selectedContents,
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

            },
            onContentSelected = onContentSelected,
        )
    }
}

@Composable
private fun calculateItemClipShape(
    itemHeightDp: Dp,
    selectedContents: List<UriProvider>,
    contentUriProvider: UriProvider
): Shape {
    val density = LocalDensity.current
    val isSelected by remember {
        derivedStateOf {
            selectedContents.firstOrNull { it == contentUriProvider } != null
        }
    }

    var clipShapeCornerSize by remember {
        mutableStateOf(0.dp)
    }
    LaunchedEffect(isSelected) {
        clipShapeCornerSize = if (isSelected) {
            with(density) {
                CircleShape.topStart.toPx(DpSize(itemHeightDp, itemHeightDp).toSize(), density)
                    .toDp()
            }
        } else {
            0.dp
        }
    }
    val clipShapeCornerSizeState by animateDpAsState(
        targetValue = clipShapeCornerSize,
        animationSpec = tween(350)
    )
    val clipShape by remember {
        derivedStateOf {
            RoundedCornerShape(clipShapeCornerSizeState)
        }
    }
    return clipShape
}

@Composable
private fun VideoContentSelection(
    request: ContentSelectionRequest,
    onContentSelected: (ContentSelectionResult) -> Unit,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val contentSelectionResultsProvider = remember {
        ContentSelectionResultsProvider(MediaStore.Video::class.java)
    }
    val contentUris = contentSelectionResultsProvider.contentUris

    val columnState = rememberLazyListState()

    LaunchedEffect(true) {
        contentSelectionResultsProvider.load(context, true)
    }

    LoadMoreHandler(scrollableState = columnState) {
        contentSelectionResultsProvider.load(context, false)
    }
    val selectedContents = remember {
        mutableStateListOf<UriProvider>()
    }
    var searchText by remember {
        mutableStateOf("")
    }
    var isSearchMode by remember {
        mutableStateOf(false)
    }
    val filteredContentUrls by remember {
        derivedStateOf {
            contentUris.filter {
                if (it is MediaStoreDataUri) {
                    it.displayName?.contains(searchText, true) == true
                } else {
                    true
                }
            }
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items = filteredContentUrls) { contentUriProvider ->
                Column(
                    modifier = Modifier
                        .animateItem()
                        .animateContentSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val itemClipShape = calculateItemClipShape(
                            250.dp,
                            selectedContents,
                            contentUriProvider
                        )
                        AnyImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(250.dp)
                                .clip(itemClipShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, itemClipShape)
                                .clickable(
                                    onClick = {
                                        val selectionTypeMaxCount =
                                            request.selectionTypeMaxCount(ContentSelectionTypes.VIDEO)
                                        val isSelected =
                                            selectedContents.firstOrNull { it == contentUriProvider } != null
                                        if (isSelected) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                                            selectedContents.removeIf { it == contentUriProvider }
                                        } else if (selectionTypeMaxCount == 1) {
                                            selectedContents.clear()
                                            selectedContents.add(contentUriProvider)
                                        } else if (selectedContents.size >= selectionTypeMaxCount) {
                                            return@clickable
                                        } else {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                            selectedContents.add(contentUriProvider)
                                        }
                                    }
                                ),
                            model = contentUriProvider.provideUri()
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
            contentUris.isEmpty()
        ) {
            Text(stringResource(xcj.app.appsets.R.string.there_is_nothing_here))
        }
        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            request = request,
            contentSelectionType = ContentSelectionTypes.VIDEO,
            devicesContents = contentUris,
            selectedContents = selectedContents,
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

            },
            onContentSelected = onContentSelected
        )
    }
}

@Composable
private fun AudioContentSelection(
    request: ContentSelectionRequest,
    onContentSelected: (ContentSelectionResult) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val contentSelectionResultsProvider = remember {
        ContentSelectionResultsProvider(MediaStore.Audio::class.java)
    }
    val contentUris = contentSelectionResultsProvider.contentUris

    val columnState = rememberLazyListState()

    LaunchedEffect(true) {
        contentSelectionResultsProvider.load(context, true)
    }

    LoadMoreHandler(scrollableState = columnState) {
        contentSelectionResultsProvider.load(context, false)
    }

    val selectedContents = remember {
        mutableStateListOf<UriProvider>()
    }

    var searchText by remember {
        mutableStateOf("")
    }
    var isSearchMode by remember {
        mutableStateOf(false)
    }
    val filteredContentUrls by remember {
        derivedStateOf {
            contentUris.filter {
                if (it is MediaStoreDataUri) {
                    it.displayName?.contains(searchText, true) == true
                } else {
                    true
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items = filteredContentUrls) { contentUriProvider ->
                var itemSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                val itemHeightDp by remember {
                    derivedStateOf {
                        with(density) { itemSize.height.toDp() }
                    }
                }
                val itemClipShape = calculateItemClipShape(
                    itemHeightDp,
                    selectedContents,
                    contentUriProvider
                )
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged {
                            itemSize = it
                        }
                        .clip(itemClipShape)
                        .clickable {
                            val selectionTypeMaxCount =
                                request.selectionTypeMaxCount(ContentSelectionTypes.AUDIO)
                            val isSelected =
                                selectedContents.firstOrNull { it == contentUriProvider } != null
                            if (isSelected) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                                selectedContents.removeIf { it == contentUriProvider }
                            } else if (selectionTypeMaxCount == 1) {
                                selectedContents.clear()
                                selectedContents.add(contentUriProvider)
                            } else if (selectedContents.size >= selectionTypeMaxCount) {
                                return@clickable
                            } else {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                selectedContents.add(contentUriProvider)
                            }
                        },
                    shape = itemClipShape,
                )
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
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
            contentUris.isEmpty()
        ) {
            Text(stringResource(xcj.app.appsets.R.string.there_is_nothing_here))
        }

        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            request = request,
            contentSelectionType = ContentSelectionTypes.AUDIO,
            devicesContents = contentUris,
            selectedContents = selectedContents,
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

            },
            onContentSelected = onContentSelected
        )
    }

}

@Composable
private fun FileContentSelection(
    request: ContentSelectionRequest,
    onContentSelected: (ContentSelectionResult) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val contentSelectionResultsProvider = remember {
        ContentSelectionResultsProvider(MediaStore.Files::class.java)
    }
    val contentUris = contentSelectionResultsProvider.contentUris

    val columnState = rememberLazyListState()

    LaunchedEffect(true) {
        contentSelectionResultsProvider.load(context, true)
    }

    LoadMoreHandler(scrollableState = columnState) {
        contentSelectionResultsProvider.load(context, false)
    }

    val selectedContents = remember {
        mutableStateListOf<UriProvider>()
    }

    var searchText by remember {
        mutableStateOf("")
    }
    var isSearchMode by remember {
        mutableStateOf(false)
    }
    val filteredContentUrls by remember {
        derivedStateOf {
            contentUris.filter {
                if (it is MediaStoreDataUri) {
                    it.displayName?.contains(searchText, true) == true
                } else {
                    true
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = columnState,
            contentPadding = PaddingValues(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(items = filteredContentUrls) { contentUriProvider ->
                var itemSize by remember {
                    mutableStateOf(IntSize.Zero)
                }
                val itemHeightDp by remember {
                    derivedStateOf {
                        with(density) { itemSize.height.toDp() }
                    }
                }
                val itemClipShape = calculateItemClipShape(
                    itemHeightDp,
                    selectedContents,
                    contentUriProvider
                )
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged {
                            itemSize = it
                        }
                        .clip(itemClipShape)
                        .clickable {
                            val selectionTypeMaxCount =
                                request.selectionTypeMaxCount(ContentSelectionTypes.FILE)
                            val isSelected =
                                selectedContents.firstOrNull { it == contentUriProvider } != null
                            if (isSelected) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOff)
                                selectedContents.removeIf { it == contentUriProvider }
                            } else if (selectionTypeMaxCount == 1) {
                                selectedContents.clear()
                                selectedContents.add(contentUriProvider)
                            } else if (selectedContents.size >= selectionTypeMaxCount) {
                                return@clickable
                            } else {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                selectedContents.add(contentUriProvider)
                            }
                        },
                    shape = itemClipShape,
                )
                {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
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
            contentUris.isEmpty()
        ) {
            Text(stringResource(xcj.app.appsets.R.string.there_is_nothing_here))
        }

        BottomActions(
            modifier = Modifier.align(Alignment.BottomCenter),
            request = request,
            contentSelectionType = ContentSelectionTypes.FILE,
            devicesContents = contentUris,
            selectedContents = selectedContents,
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

            },
            onContentSelected = onContentSelected
        )
    }
}

@Composable
private fun BottomActions(
    modifier: Modifier,
    request: ContentSelectionRequest,
    contentSelectionType: String,
    devicesContents: List<UriProvider>,
    selectedContents: List<UriProvider>,
    isSearchMode: Boolean,
    searchText: String,
    actionIcon: Int,
    onSearchModeChanged: (Boolean) -> Unit,
    onSearchContentChanged: (String) -> Unit,
    onActionClick: () -> Unit,
    onContentSelected: (ContentSelectionResult) -> Unit,
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val isReachMinCount by rememberUpdatedState(selectedContents.isNotEmpty())
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val gradientColors = listOf(
        androidx.compose.ui.graphics.Color.Transparent,
        if (isSystemInDarkTheme) {
            androidx.compose.ui.graphics.Color.Black
        } else {
            androidx.compose.ui.graphics.Color.White
        }
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(
                        alignment = Alignment.BottomCenter
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = isSearchMode,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    Box(modifier = Modifier.padding(vertical = 6.dp)) {
                        DesignTextField(
                            modifier = Modifier,
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
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (devicesContents.isNotEmpty()) {
                        FilledTonalIconButton(
                            modifier = Modifier
                                .size(TextFieldDefaults.MinHeight),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                                onSearchModeChanged(!isSearchMode)
                            }
                        ) {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_search_24),
                                contentDescription = null
                            )
                        }
                    }
                    FilledTonalIconButton(
                        modifier = Modifier
                            .size(TextFieldDefaults.MinHeight),
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onActionClick()
                        }
                    ) {
                        Icon(
                            painter = painterResource(actionIcon),
                            contentDescription = null
                        )
                    }
                }
                LazyRow(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .widthIn(max = TextFieldDefaults.MinWidth / 2)
                        .animateContentSize(alignment = Alignment.CenterStart),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        if (devicesContents.isNotEmpty()) {
                            Surface(
                                modifier
                                    .clip(CircleShape)
                            ) {
                                Text(
                                    "${selectedContents.size}/${
                                        request.selectionTypeMaxCount(
                                            contentSelectionType
                                        )
                                    }",
                                    modifier = Modifier.padding(horizontal = 2.dp),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    items(selectedContents) { uriProvider ->
                        AnyImage(
                            modifier = modifier
                                .animateItem()
                                .size(25.dp),
                            model = uriProvider.provideUri()
                        )
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    AnimatedVisibility(
                        visible = isReachMinCount,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        FilledTonalButton(
                            modifier = Modifier
                                .height(TextFieldDefaults.MinHeight),
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                                val results =
                                    ContentSelectionResult.RichMediaContentSelectionResult(
                                        context,
                                        request,
                                        contentSelectionType,
                                        selectedContents
                                    )
                                onContentSelected(results)
                            }
                        ) {
                            Text(text = stringResource(xcj.app.appsets.R.string.ok))
                        }
                    }
                }
            }
        }
    }
}