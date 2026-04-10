@file:OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)

package xcj.app.appsets.ui.compose.outside

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreenPost
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionTypes
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.model.ScreenInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateScreenPageUIState
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.RuntimeListStateUpdater
import xcj.app.appsets.util.model.isVideoType
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.foundation_extension.ProjectPreviewWrapperProviderImpl
import xcj.app.compose_share.modifier.hazeSourceIfAvailable
import xcj.app.compose_share.modifier.rememberHazeStateIfAvailable
import xcj.app.starter.android.util.UriProvider

@PreviewWrapper(wrapper = ProjectPreviewWrapperProviderImpl::class)
@Preview(showBackground = true)
@Composable
fun CreateScreenPagePreview() {
    val createScreenPageUIState by remember {
        mutableStateOf<CreateScreenPageUIState>(CreateScreenPageUIState.CreateStart())
    }
    val screenInfoForCreate by remember {
        mutableStateOf(ScreenInfoForCreate())
    }

    val mainViewModel = remember {
        MainViewModel()
    }

    CompositionLocalProvider(
        LocalUseCaseOfScreenPost provides mainViewModel.screenPostUseCase,
        LocalUseCaseOfNavigation provides mainViewModel.navigationUseCase
    ) {
        CreateScreenPage(
            quickStepContents = null,
            createScreenPageUIState = createScreenPageUIState,
            screenInfoForCreate = screenInfoForCreate,
            onBackClick = {},
            onConfirmClick = { },
            onGenerateClick = {},
            onAddMediaContentClick = { _, _, _, _ ->

            },
            onRemoveMediaContent = { _, _ ->

            },
            onMediaClick = {

            }
        )
    }
}

@Composable
fun CreateScreenPage(
    quickStepContents: List<QuickStepContent>?,
    createScreenPageUIState: CreateScreenPageUIState,
    screenInfoForCreate: ScreenInfoForCreate,
    onBackClick: (Boolean) -> Unit,
    onConfirmClick: (ScreenInfoForCreate) -> Unit,
    onGenerateClick: () -> Unit,
    onAddMediaContentClick: (String, String, (String) -> Int, ComposeStateUpdater<*>) -> Unit,
    onRemoveMediaContent: (String, UriProvider) -> Unit,
    onMediaClick: (UriProvider) -> Unit,
) {

    HideNavBar()
    val screenPostUseCase = LocalUseCaseOfScreenPost.current
    LaunchedEffect(Unit) {
        screenPostUseCase.updateWithQuickStepContentIfNeeded(quickStepContents, screenInfoForCreate)
    }
    DisposableEffect(key1 = true, effect = {
        onDispose {
            screenPostUseCase.onComposeDispose("page dispose")
        }
    })

    LaunchedEffect(key1 = createScreenPageUIState) {
        if (createScreenPageUIState is CreateScreenPageUIState.CreateSuccess) {
            onBackClick(true)
        }
    }
    val hazeState = rememberHazeStateIfAvailable()
    val density = LocalDensity.current
    var backActionBarSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val backActionsHeight by remember {
        derivedStateOf {
            with(density) {
                backActionBarSize.height.toDp()
            }
        }
    }

    VerticalOverscrollBox {
        Column(
            modifier = Modifier
                .hazeSourceIfAvailable(hazeState)
                .widthIn(TextFieldDefaults.MinWidth)
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            val statusBarHeight =
                WindowInsets.statusBarsIgnoringVisibility.asPaddingValues().calculateTopPadding()
            val finalTopPadding = remember(backActionsHeight, statusBarHeight) {
                if (backActionsHeight > 0.dp) backActionsHeight + 12.dp else statusBarHeight + 84.dp
            }
            Spacer(modifier = Modifier.height(finalTopPadding))
            NewPostScreenComponent(
                screenInfoForCreate = screenInfoForCreate,
                onGenerateClick = onGenerateClick,
                onAddMediaContentClick = onAddMediaContentClick,
                onRemoveMediaContent = onRemoveMediaContent,
                onMediaClick = onMediaClick
            )
        }

        CreateScreenIndicator(createScreenPageUIState = createScreenPageUIState)

        BackActionTopBar(
            modifier = Modifier.onPlaced {
                backActionBarSize = it.size
            },
            hazeState = hazeState,
            backButtonText = stringResource(xcj.app.appsets.R.string.create_screen),
            endButtonText = stringResource(id = xcj.app.appsets.R.string.ok),
            onBackClick = {
                onBackClick(false)
            },
            onEndButtonClick = {
                onConfirmClick(screenInfoForCreate)
            }
        )
    }
}

@Composable
fun CreateScreenIndicator(createScreenPageUIState: CreateScreenPageUIState) {
    AnimatedVisibility(
        visible = createScreenPageUIState is CreateScreenPageUIState.Posting,
        enter = fadeIn(tween()) + scaleIn(
            tween(),
            2f
        ),
        exit = fadeOut(tween()) + scaleOut(
            tween(),
            0.2f
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .padding(vertical = 12.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        modifier = Modifier.size(68.dp),
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.processing),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: Int,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            shape = CircleShape,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (content != null) {
            Spacer(modifier = Modifier.weight(1f))
            content()
        }
    }
}

@Composable
fun AddMediaButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: Int = xcj.app.compose_share.R.drawable.ic_round_add_24
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * @param onAddMediaContentClick param1:requestKey, param2:requestType, param3:requestMaxCountProvider, param4: composeStateUpdater
 */
@Composable
fun NewPostScreenComponent(
    modifier: Modifier = Modifier,
    screenInfoForCreate: ScreenInfoForCreate,
    onGenerateClick: () -> Unit,
    onAddMediaContentClick: (String, String, (String) -> Int, ComposeStateUpdater<*>) -> Unit,
    onRemoveMediaContent: (String, UriProvider) -> Unit,
    onMediaClick: (UriProvider) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // Status section
        Column {
            SectionHeader(
                icon = xcj.app.compose_share.R.drawable.ic_info_24,
                title = stringResource(xcj.app.appsets.R.string.status)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    true to stringResource(id = xcj.app.appsets.R.string.public_),
                    false to stringResource(id = xcj.app.appsets.R.string.private_)
                ).forEach { (isPublic, label) ->
                    val selected = screenInfoForCreate.isPublic.value == isPublic
                    Surface(
                        onClick = { screenInfoForCreate.isPublic.value = isPublic },
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        tonalElevation = if (selected) 2.dp else 0.dp
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier.padding(vertical = 10.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            val statusTip = if (screenInfoForCreate.isPublic.value) {
                stringResource(xcj.app.appsets.R.string.screen_will_randomly_appear_on_the_homepage_after_passing_the_review)
            } else {
                stringResource(xcj.app.appsets.R.string.screen_is_only_visible_to_you)
            }
            Text(
                text = statusTip,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp)
                    .animateContentSize()
            )
        }

        // Content section
        Column {
            SectionHeader(
                icon = xcj.app.compose_share.R.drawable.ic_notes_24,
                title = stringResource(xcj.app.appsets.R.string.content)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onGenerateClick)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_gesture_24),
                            contentDescription = stringResource(xcj.app.appsets.R.string.use_ai_generate),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.generate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            DesignTextField(
                modifier = Modifier
                    .heightIn(min = 160.dp)
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.example_It_s_raining_today),
                        fontSize = 14.sp
                    )
                },
                value = screenInfoForCreate.content.value,
                onValueChange = {
                    screenInfoForCreate.content.value = it
                }
            )
        }

        // Topics & People section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionHeader(
                    icon = xcj.app.compose_share.R.drawable.ic_genres_24,
                    title = stringResource(xcj.app.appsets.R.string.related_topics)
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.example_smart_car_huawei),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    value = screenInfoForCreate.associateTopics.value,
                    onValueChange = {
                        screenInfoForCreate.associateTopics.value = it
                    }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                SectionHeader(
                    icon = xcj.app.compose_share.R.drawable.ic_face_24,
                    title = stringResource(xcj.app.appsets.R.string.associated_people)
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.example_jiang_kaixin),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
                    value = screenInfoForCreate.associatePeoples.value,
                    onValueChange = {
                        screenInfoForCreate.associatePeoples.value = it
                    }
                )
            }
        }

        // Media section
        Column {
            SectionHeader(
                icon = xcj.app.compose_share.R.drawable.ic_photo_24,
                title = stringResource(xcj.app.appsets.R.string.rich_media)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    .padding(8.dp)
                    .wrapContentHeight()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                screenInfoForCreate.mediaUriProviders.forEach { contentUriProvider ->
                    Box(modifier = Modifier.size(150.dp)) {
                        AnyImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.extraLarge)
                                .clickable(
                                    onClick = {
                                        onMediaClick(contentUriProvider)
                                    },
                                ),
                            model = contentUriProvider.provideUri()
                        )
                        if (contentUriProvider.isVideoType()) {
                            Icon(
                                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_slow_motion_video_24),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp),
                                tint = Color.White
                            )
                        }
                        Surface(
                            onClick = {
                                onRemoveMediaContent(
                                    ContentSelectionTypes.ANY,
                                    contentUriProvider
                                )
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(24.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_close_24),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                AddMediaButton(
                    modifier = Modifier.size(150.dp),
                    onClick = {
                        val composeStateUpdater =
                            RuntimeListStateUpdater.fromState<Void>(null) { _, input ->
                                if (input !is ContentSelectionResult.RichMediaContentSelectionResult) {
                                    return@fromState
                                }

                                val uriProviders = input.selectedProvider.provide()
                                screenInfoForCreate.mediaUriProviders.addAll(uriProviders)
                            }
                        onAddMediaContentClick(
                            "CREATE_SCREEN_CONTENT_SELECT_MEDIA_REQUEST",
                            ContentSelectionTypes.ANY,
                            { type ->
                                when (type) {
                                    ContentSelectionTypes.IMAGE -> {
                                        return@onAddMediaContentClick 32
                                    }

                                    ContentSelectionTypes.VIDEO -> {
                                        return@onAddMediaContentClick 1
                                    }

                                    ContentSelectionTypes.AUDIO -> {
                                        return@onAddMediaContentClick 1
                                    }

                                    else -> {
                                        1
                                    }
                                }
                            },
                            composeStateUpdater
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(150.dp))
    }
}
