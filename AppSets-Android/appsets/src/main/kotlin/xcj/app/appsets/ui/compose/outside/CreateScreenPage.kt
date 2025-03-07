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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreenPost
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionVarargs
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.model.PostScreen
import xcj.app.appsets.ui.model.PostScreenState
import xcj.app.appsets.util.model.MediaStoreDataUri
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
    onAddMediaContentClick: (String, String) -> Unit,
    onRemoveMediaContent: (String, UriProvider) -> Unit,
    onVideoPlayClick: (UriProvider) -> Unit,
) {

    HideNavBarWhenOnLaunch()
    val screenPostUseCase = LocalUseCaseOfScreenPost.current
    LaunchedEffect(Unit) {
        screenPostUseCase.updateWithQuickStepContentIfNeeded(quickStepContents)
    }
    DisposableEffect(key1 = true, effect = {
        onDispose {
            screenPostUseCase.onComposeDispose("page dispose")
        }
    })
    val postScreenState by screenPostUseCase.postScreenState
    LaunchedEffect(key1 = postScreenState) {
        if (postScreenState is PostScreenState.PostSuccess) {
            onBackClick(true)
        }
    }
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            BackActionTopBar(
                backButtonRightText = stringResource(xcj.app.appsets.R.string.create_screen),
                endButtonText = stringResource(id = xcj.app.appsets.R.string.sure),
                onBackClick = {
                    onBackClick(false)
                },
                onEndButtonClick = onConfirmClick
            )
            NewPostScreenComponent(
                postScreenState.postScreen,
                onGenerateClick,
                onIsPublicClick,
                onInputContent,
                onInputTopics,
                onInputPeoples,
                onAddMediaFallClick,
                onAddMediaContentClick,
                onRemoveMediaContent,
                onVideoPlayClick
            )
        }
        CreateScreenIndicator(postScreenState = postScreenState)
    }
}

@Composable
fun CreateScreenIndicator(postScreenState: PostScreenState) {
    AnimatedVisibility(
        visible = postScreenState is PostScreenState.Posting,
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
    postScreen: PostScreen,
    onGenerateClick: () -> Unit,
    onVisibilityClick: (Boolean) -> Unit,
    onInputContent: (String) -> Unit,
    onInputTopics: (String) -> Unit,
    onInputUsers: (String) -> Unit,
    onAddMediaFallClick: () -> Unit,
    onAddMediaContentClick: (String, String) -> Unit,
    onRemoveMediaContent: (String, UriProvider) -> Unit,
    onVideoPlayClick: (UriProvider) -> Unit
) {
    Column(
        modifier = Modifier
            .widthIn(TextFieldDefaults.MinWidth)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
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
                    selected = postScreen.isPublic,
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
                    selected = !postScreen.isPublic,
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
        val statusTip = if (postScreen.isPublic) {
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
            value = postScreen.content ?: "",
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
            value = postScreen.associateTopics ?: "",
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
                    text = stringResource(xcj.app.appsets.R.string.example_jiang_kaixin_li_wenyi),
                    fontSize = 11.sp
                )
            },
            value = postScreen.associatePeoples ?: "",
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
            postScreen.pictures.forEach { contentUriProvider ->
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
                            any = contentUriProvider.provideUri()
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
                                ContentSelectionVarargs.PICTURE
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
            if (postScreen.videos.firstOrNull() != null) {
                Spacer(modifier = Modifier.weight(1f))
                FilterChip(
                    selected = postScreen.addToMediaFall,
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
                            postScreen.videos.firstOrNull()
                        if (mediaUriProvider != null) {
                            onVideoPlayClick(mediaUriProvider)
                        }
                    },
                    onClick = {
                        onAddMediaContentClick(
                            "CREATE_SCREEN_CONTENT_SELECT_VIDEO_REQUEST",
                            ContentSelectionVarargs.VIDEO
                        )
                    },
                )
        ) {
            val videoUriProvider = postScreen.videos.firstOrNull()
            val mediaStoreDataUri =
                videoUriProvider as? MediaStoreDataUri
            if (mediaStoreDataUri != null) {
                AnyImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(MaterialTheme.shapes.extraLarge),
                    any = mediaStoreDataUri.provideUri()
                )
            }

            IconButton(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.Center),
                onClick = {
                    val mediaUriProvider =
                        postScreen.videos.firstOrNull()
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
