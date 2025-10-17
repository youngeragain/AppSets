@file:OptIn(ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.outside

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreenPost
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionTypes
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.model.ScreenInfoForCreate
import xcj.app.appsets.ui.model.page_state.PostScreenPageState
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField

private const val TAG = "CreateScreenPage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScreenPage(
    quickStepContents: List<QuickStepContent>?,
    onBackClick: (Boolean) -> Unit,
    onConfirmClick: () -> Unit,
    onIsPublicClick: (Boolean) -> Unit,
    onGenerateClick: () -> Unit,
    onInputContent: (String) -> Unit,
    onInputTopics: (String) -> Unit,
    onInputPeoples: (String) -> Unit,
    onAddMediaFallClick: () -> Unit,
    onAddMediaContentClick: (String, String, Int) -> Unit,
    onRemoveMediaContent: (String, UriProvider) -> Unit,
    onVideoPlayClick: (UriProvider) -> Unit,
) {

    HideNavBar()
    val screenPostUseCase = LocalUseCaseOfScreenPost.current
    LaunchedEffect(Unit) {
        screenPostUseCase.updateWithQuickStepContentIfNeeded(quickStepContents)
    }
    DisposableEffect(key1 = true, effect = {
        onDispose {
            screenPostUseCase.onComposeDispose("page dispose")
        }
    })
    val postScreenState by screenPostUseCase.postScreenPageState
    LaunchedEffect(key1 = postScreenState) {
        if (postScreenState is PostScreenPageState.PostSuccessPage) {
            onBackClick(true)
        }
    }
    val hazeState = rememberHazeState()
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
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .hazeSource(hazeState)
                .widthIn(TextFieldDefaults.MinWidth)
                .imePadding()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(
                modifier = Modifier.height(
                    backActionsHeight + 12.dp
                )
            )
            NewPostScreenComponent(
                screenInfoForCreate = postScreenState.screenInfoForCreate,
                onGenerateClick = onGenerateClick,
                onVisibilityClick = onIsPublicClick,
                onInputContent = onInputContent,
                onInputTopics = onInputTopics,
                onInputUsers = onInputPeoples,
                onAddMediaFallClick = onAddMediaFallClick,
                onAddMediaContentClick = onAddMediaContentClick,
                onRemoveMediaContent = onRemoveMediaContent,
                onVideoPlayClick = onVideoPlayClick
            )
        }


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
            onEndButtonClick = onConfirmClick
        )

        CreateScreenIndicator(postScreenPageState = postScreenState)
    }
}

@Composable
fun CreateScreenIndicator(postScreenPageState: PostScreenPageState) {
    AnimatedVisibility(
        visible = postScreenPageState is PostScreenPageState.Posting,
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
                    Text(stringResource(xcj.app.appsets.R.string.processing), fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewPostScreenComponent(
    modifier: Modifier = Modifier,
    screenInfoForCreate: ScreenInfoForCreate,
    onGenerateClick: () -> Unit,
    onVisibilityClick: (Boolean) -> Unit,
    onInputContent: (String) -> Unit,
    onInputTopics: (String) -> Unit,
    onInputUsers: (String) -> Unit,
    onAddMediaFallClick: () -> Unit,
    onAddMediaContentClick: (String, String, Int) -> Unit,
    onRemoveMediaContent: (String, UriProvider) -> Unit,
    onVideoPlayClick: (UriProvider) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.status),
                Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = screenInfoForCreate.isPublic,
                    onClick = {
                        onVisibilityClick(true)

                    },
                    label = {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.public_)
                        )
                    },
                    shape = CircleShape
                )
                FilterChip(
                    selected = !screenInfoForCreate.isPublic,
                    onClick = {
                        onVisibilityClick(false)
                    },
                    label = {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.private_)
                        )
                    },
                    shape = CircleShape
                )
            }
        }
        val statusTip = if (screenInfoForCreate.isPublic) {
            stringResource(xcj.app.appsets.R.string.screen_will_randomly_appear_on_the_homepage_after_passing_the_review)
        } else {
            stringResource(xcj.app.appsets.R.string.screen_is_only_visible_to_you)
        }
        Text(
            text = statusTip, fontSize = 11.sp, modifier = Modifier
                .padding(8.dp)
                .animateContentSize()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.content),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = onGenerateClick)
            ) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(
                        painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_gesture_24),
                        contentDescription = stringResource(xcj.app.appsets.R.string.use_ai_generate)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = stringResource(xcj.app.appsets.R.string.generate))
                }
            }
        }
        DesignTextField(
            modifier = Modifier
                .heightIn(min = 250.dp)
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.example_It_s_raining_today),
                    fontSize = 11.sp
                )
            },
            value = screenInfoForCreate.content ?: "",
            onValueChange = onInputContent
        )
        Text(
            text = stringResource(xcj.app.appsets.R.string.related_topics),
            modifier = Modifier.padding(vertical = 10.dp),
            fontWeight = FontWeight.Bold
        )
        DesignTextField(
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.example_smart_car_huawei),
                    fontSize = 11.sp
                )
            },
            value = screenInfoForCreate.associateTopics ?: "",
            onValueChange = onInputTopics
        )
        Text(
            text = stringResource(xcj.app.appsets.R.string.associated_people),
            modifier = Modifier.padding(vertical = 10.dp),
            fontWeight = FontWeight.Bold
        )

        DesignTextField(
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.example_jiang_kaixin),
                    fontSize = 11.sp
                )
            },
            value = screenInfoForCreate.associatePeoples ?: "",
            onValueChange = onInputUsers
        )
        Text(
            text = stringResource(xcj.app.appsets.R.string.picture),
            modifier = Modifier.padding(vertical = 10.dp),
            fontWeight = FontWeight.Bold
        )
        FlowRow(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraLarge)
                .wrapContentHeight()
                .animateContentSize()
        ) {
            screenInfoForCreate.pictures.forEach { contentUriProvider ->
                Box(
                    Modifier
                        .padding(4.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .size(120.dp, 120.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        AnyImage(
                            modifier = Modifier
                                .fillMaxSize(),
                            model = contentUriProvider.provideUri()
                        )
                    }
                }
            }
            Box(
                Modifier
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp, 120.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable(onClick = {
                            onAddMediaContentClick(
                                "CREATE_SCREEN_CONTENT_SELECT_IMAGE_REQUEST",
                                ContentSelectionTypes.IMAGE,
                                32
                            )
                        }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                        contentDescription = "add_content"
                    )
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.video),
                modifier = Modifier
                    .padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            if (screenInfoForCreate.videos.firstOrNull() != null) {
                Spacer(modifier = Modifier.weight(1f))
                FilterChip(
                    selected = screenInfoForCreate.addToMediaFall,
                    onClick = onAddMediaFallClick,
                    label = {
                        Text(text = stringResource(xcj.app.appsets.R.string.add_to_stream))
                    },
                    shape = CircleShape
                )
            }
        }
        Box(
            modifier = Modifier
                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraLarge)
                .height(220.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraLarge)
                .combinedClickable(
                    onLongClick = {
                        val mediaUriProvider =
                            screenInfoForCreate.videos.firstOrNull()
                        if (mediaUriProvider != null) {
                            onVideoPlayClick(mediaUriProvider)
                        }
                    },
                    onClick = {
                        onAddMediaContentClick(
                            "CREATE_SCREEN_CONTENT_SELECT_VIDEO_REQUEST",
                            ContentSelectionTypes.VIDEO,
                            1
                        )
                    },
                )
        ) {
            val videoUriProvider = screenInfoForCreate.videos.firstOrNull()
            if (videoUriProvider != null) {
                AnyImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.extraLarge),
                    model = videoUriProvider.provideUri()
                )
            }

            IconButton(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.Center),
                onClick = {
                    val mediaUriProvider =
                        screenInfoForCreate.videos.firstOrNull()
                    if (mediaUriProvider != null) {
                        onVideoPlayClick(mediaUriProvider)
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_slow_motion_video_24),
                    contentDescription = null,
                )
            }
        }
        Spacer(modifier = Modifier.height(150.dp))
    }
}
