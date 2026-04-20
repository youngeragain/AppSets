package xcj.app.appsets.ui.compose.group

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.compose.custom_component.preview_tooling.DesignPreviewCompositionLocalProvider
import xcj.app.appsets.ui.compose.theme.ExtraLarge2
import xcj.app.appsets.ui.model.GroupInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateGroupPageUIState
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.RuntimeSingleStateUpdater
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.LocalHazedStateProvider
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer
import xcj.app.compose_share.modifier.hazeSourceIfAvailable

private const val TAG = "CreateGroupPage"


@Preview(showBackground = true)
@Composable
fun CreateGroupPagePreview() {
    val createGroupPageUIState by remember {
        mutableStateOf<CreateGroupPageUIState>(CreateGroupPageUIState.CreateStart())
    }
    val groupInfoForCreate by remember {
        mutableStateOf(GroupInfoForCreate())
    }

    DesignPreviewCompositionLocalProvider {
        CreateGroupPage(
            createGroupPageUIState = createGroupPageUIState,
            groupInfoForCreate = groupInfoForCreate,
            onBackClick = {

            },
            onConfirmClick = { groupInfoForCreate ->

            },
            onSelectGroupIconClick = { requestKey, composeStateUpdater ->

            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateGroupPage(
    createGroupPageUIState: CreateGroupPageUIState,
    groupInfoForCreate: GroupInfoForCreate,
    onBackClick: () -> Unit,
    onConfirmClick: (GroupInfoForCreate) -> Unit,
    onSelectGroupIconClick: (String, ComposeStateUpdater<*>) -> Unit
) {
    HideNavBar()
    val hazeState = LocalHazedStateProvider.current
    val systemUseCase = LocalUseCaseOfSystem.current
    DisposableEffect(Unit) {
        onDispose {
            systemUseCase.onComposeDispose("page dispose")
        }
    }
    var startAnimation by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
    }

    VerticalOverscrollBox {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .widthIn(max = TextFieldDefaults.MinWidth)
                    .align(Alignment.CenterHorizontally)
                    .hazeSourceIfAvailable(hazeState)
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            )
            {
                StatusBarWithTopActionBarSpacer()

                // Logo Section with Animation
                AnimatedVisibility(
                    visible = startAnimation,
                    enter = slideInVertically { -20 } + fadeIn()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(
                                stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                                stringResource(id = xcj.app.appsets.R.string.logo),
                                "*"
                            ),
                            modifier = Modifier.padding(vertical = 12.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            if (isPressed) 0.95f else 1f,
                            label = "logoScale"
                        )

                        val infiniteTransition = rememberInfiniteTransition(label = "logoPulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.03f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse"
                        )

                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .scale(scale * if (groupInfoForCreate.iconUriProvider.value == null) pulseScale else 1f)
                                .clip(ExtraLarge2)
                                .border(
                                    2.dp,
                                    if (groupInfoForCreate.iconUriProvider.value != null)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    ExtraLarge2
                                )
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = {
                                        val composeStateUpdater =
                                            RuntimeSingleStateUpdater.fromState(groupInfoForCreate.iconUriProvider) { markKey, input ->
                                                if (input !is ContentSelectionResult.RichMediaContentSelectionResult) {
                                                    return@fromState
                                                }
                                                val uriProviders = input.selectedProvider.provide()
                                                if (uriProviders.isEmpty()) {
                                                    return@fromState
                                                }
                                                update(uriProviders.first())
                                            }
                                        onSelectGroupIconClick(
                                            "CREATE_GROUP_IMAGE_SELECT_REQUEST",
                                            composeStateUpdater
                                        )
                                    }),
                            contentAlignment = Alignment.Center
                        ) {
                            val groupIconUri =
                                groupInfoForCreate.iconUriProvider.value?.provideUri()

                            AnimatedContent(
                                targetState = groupIconUri != null,
                                transitionSpec = {
                                    (fadeIn() + scaleIn()).togetherWith(fadeOut() + scaleOut())
                                },
                                label = "iconTransition"
                            ) { hasUri ->
                                if (hasUri) {
                                    AnyImage(
                                        modifier = Modifier.fillMaxSize(),
                                        model = groupIconUri,
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Staggered Items
                val itemModifiers = List(5) { index ->
                    val delayTime = 150 + (index * 50)
                    val alpha by animateFloatAsState(
                        if (startAnimation) 1f else 0f,
                        animationSpec = tween(500, delayMillis = delayTime),
                        label = "itemAlpha$index"
                    )
                    val translationY by animateFloatAsState(
                        if (startAnimation) 0f else 30f,
                        animationSpec = tween(500, delayMillis = delayTime),
                        label = "itemSlide$index"
                    )
                    Modifier.graphicsLayer {
                        this.alpha = alpha
                        this.translationY = translationY
                    }
                }

                Column(
                    modifier = itemModifiers[0],
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.status),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = groupInfoForCreate.isPublic.value,
                            onClick = { groupInfoForCreate.isPublic.value = true },
                            label = { Text(text = stringResource(id = xcj.app.appsets.R.string.public_)) },
                            shape = CircleShape,
                            leadingIcon = if (groupInfoForCreate.isPublic.value) {
                                {
                                    Icon(
                                        painterResource(xcj.app.compose_share.R.drawable.ic_round_check_24),
                                        null
                                    )
                                }
                            } else null
                        )
                        FilterChip(
                            selected = !groupInfoForCreate.isPublic.value,
                            onClick = { groupInfoForCreate.isPublic.value = false },
                            label = { Text(text = stringResource(id = xcj.app.appsets.R.string.private_)) },
                            shape = CircleShape,
                            leadingIcon = if (!groupInfoForCreate.isPublic.value) {
                                {
                                    Icon(
                                        painterResource(xcj.app.compose_share.R.drawable.ic_round_check_24),
                                        null
                                    )
                                }
                            } else null
                        )
                    }
                }

                Column(
                    modifier = itemModifiers[1],
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = String.format(
                            stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                            stringResource(id = xcj.app.appsets.R.string.name),
                            "*"
                        ),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    DesignTextField(
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        value = groupInfoForCreate.name.value,
                        onValueChange = {
                            groupInfoForCreate.name.value =
                                if (it.length > 30) it.substring(0, 30) else it
                        },
                        placeholder = {
                            Text(
                                text = stringResource(xcj.app.appsets.R.string.group_name),
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                Column(
                    modifier = itemModifiers[2],
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.maximum_number_of_members),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    DesignTextField(
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        value = groupInfoForCreate.membersCount.value,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        onValueChange = {
                            groupInfoForCreate.membersCount.value =
                                it.toIntOrNull()?.toString() ?: "1000"
                        },
                        placeholder = {
                            Text(
                                text = stringResource(xcj.app.appsets.R.string.quantity),
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                Column(
                    modifier = itemModifiers[3],
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.introduction),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    DesignTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = groupInfoForCreate.introduction.value,
                        onValueChange = { groupInfoForCreate.introduction.value = it },
                        placeholder = {
                            Text(
                                text = stringResource(xcj.app.appsets.R.string.group_description),
                                fontSize = 12.sp
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(150.dp))
            }
        }

        BackActionTopBar(
            backButtonText = stringResource(xcj.app.appsets.R.string.create_group),
            endButtonText = stringResource(xcj.app.appsets.R.string.ok),
            onBackClick = onBackClick,
            onEndButtonClick = {
                onConfirmClick(groupInfoForCreate)
            }
        )

        CreateGroupIndicator(createGroupPageUIState = createGroupPageUIState)
    }

}

@Composable
private fun CreateGroupIndicator(createGroupPageUIState: CreateGroupPageUIState) {
    AnimatedVisibility(
        visible = createGroupPageUIState is CreateGroupPageUIState.Creating,
        enter = fadeIn(tween(400)) + scaleIn(tween(400), initialScale = 0.8f),
        exit = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 1.2f),
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.2f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(72.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            strokeWidth = 2.dp,
                            progress = { 1f }
                        )
                        Image(
                            modifier = Modifier
                                .size(48.dp)
                                .rotate(rotation),
                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                            contentDescription = null
                        )
                    }
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.processing),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
