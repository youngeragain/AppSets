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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreenPost
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionTypes
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.compose.custom_component.preview_tooling.DesignPreviewCompositionLocalProvider
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.model.ScreenInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateScreenPageUIState
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.RuntimeListStateUpdater
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer
import xcj.app.compose_share.foundation_extension.ProjectPreviewWrapperProviderImpl
import xcj.app.starter.android.util.UriProvider
import xcj.app.starter.android.util.model.isAudioType
import xcj.app.starter.android.util.model.isImageType
import xcj.app.starter.android.util.model.isVideoType

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

    DesignPreviewCompositionLocalProvider {
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
    val context = LocalContext.current
    val screenPostUseCase = LocalUseCaseOfScreenPost.current
    LaunchedEffect(Unit) {
        screenPostUseCase.updateWithQuickStepContentIfNeeded(
            context,
            quickStepContents,
            screenInfoForCreate
        )
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

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        VerticalOverscrollBox(modifier = Modifier.widthIn(max = TextFieldDefaults.MinWidth * 2)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                StatusBarWithTopActionBarSpacer()
                NewPostScreenComponent(
                    screenInfoForCreate = screenInfoForCreate,
                    onGenerateClick = onGenerateClick,
                    onAddMediaContentClick = onAddMediaContentClick,
                    onRemoveMediaContent = onRemoveMediaContent,
                    onMediaClick = onMediaClick
                )

                Spacer(modifier = Modifier.height(150.dp))
            }

            CreateScreenIndicator(createScreenPageUIState = createScreenPageUIState)

            BackActionTopBar(
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
            .padding(top = 28.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
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
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
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
    ) {
        // Status section
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionHeader(
                icon = xcj.app.compose_share.R.drawable.ic_info_24,
                title = stringResource(xcj.app.appsets.R.string.status)
            )
            val statusTip = if (screenInfoForCreate.isPublic.value) {
                stringResource(xcj.app.appsets.R.string.screen_will_randomly_appear_on_the_homepage_after_passing_the_review)
            } else {
                stringResource(xcj.app.appsets.R.string.screen_is_only_visible_to_you)
            }
            Text(
                text = statusTip,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .animateContentSize()
            )

            val statusChoices = listOf(
                true to stringResource(id = xcj.app.appsets.R.string.public_),
                false to stringResource(id = xcj.app.appsets.R.string.private_)
            )

            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                statusChoices.forEachIndexed { index, (isPublic, label) ->
                    FilterChip(
                        shape = CircleShape,
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = if (screenInfoForCreate.isPublic.value == isPublic) {
                            {
                                Icon(
                                    painterResource(xcj.app.compose_share.R.drawable.ic_round_check_24),
                                    null
                                )
                            }
                        } else null,
                        selected = screenInfoForCreate.isPublic.value == isPublic,
                        onClick = { screenInfoForCreate.isPublic.value = isPublic },
                    )
                }
            }
        }

        // Content section
        Column {
            SectionHeader(
                icon = xcj.app.compose_share.R.drawable.ic_notes_24,
                title = stringResource(xcj.app.appsets.R.string.content)
            ) {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.generate),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable(onClick = onGenerateClick)
                        .padding(4.dp)
                )
            }
            DesignTextField(
                modifier = Modifier
                    .heightIn(min = 120.dp)
                    .fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.example_It_s_raining_today),
                        fontSize = 12.sp
                    )
                },
                value = screenInfoForCreate.content.value,
                onValueChange = {
                    screenInfoForCreate.content.value = it
                }
            )
        }

        // Topics & People section
        Column {
            SectionHeader(
                icon = xcj.app.compose_share.R.drawable.ic_square_foot_24,
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

        // Media section
        Column {
            SectionHeader(
                icon = xcj.app.compose_share.R.drawable.ic_photo_24,
                title = stringResource(xcj.app.appsets.R.string.rich_media)
            )
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                screenInfoForCreate.mediaUriProviders.forEach { contentUriProvider ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable(onClick = {
                                onMediaClick(contentUriProvider)
                            })
                    ) {
                        if (contentUriProvider.isImageType() || contentUriProvider.isVideoType()) {
                            AnyImage(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.extraLarge),
                                model = contentUriProvider.provideUri()
                            )
                        }
                        if (contentUriProvider.isVideoType()) {
                            Icon(
                                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_slow_motion_video_24),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp),
                                tint = Color.White
                            )
                        } else if (contentUriProvider.isAudioType()) {
                            Icon(
                                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_audiotrack_24),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(24.dp)
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
                                .padding(6.dp)
                                .size(20.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.2f)
                        ) {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_close_24),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                AddMediaButton(
                    modifier = Modifier.size(100.dp),
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
    }
}
