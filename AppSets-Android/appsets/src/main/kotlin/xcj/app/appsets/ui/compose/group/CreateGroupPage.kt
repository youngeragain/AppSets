package xcj.app.appsets.ui.compose.group

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.theme.ExtraLarge2
import xcj.app.appsets.ui.model.GroupInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateGroupPageUIState
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.RuntimeSingleStateUpdater
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "CreateGroupPage"


@Preview
@Composable
fun CreateGroupPagePreview(
) {
    val createGroupPageUIState by remember {
        mutableStateOf<CreateGroupPageUIState>(CreateGroupPageUIState.CreateStart())
    }
    val groupInfoForCreate by remember {
        mutableStateOf(GroupInfoForCreate())
    }

    val mainViewModel = remember {
        MainViewModel()
    }

    CompositionLocalProvider(
        LocalUseCaseOfSystem provides mainViewModel.systemUseCase,
        LocalUseCaseOfNavigation provides mainViewModel.navigationUseCase
    ) {
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

@Composable
fun CreateGroupPage(
    createGroupPageUIState: CreateGroupPageUIState,
    groupInfoForCreate: GroupInfoForCreate,
    onBackClick: () -> Unit,
    onConfirmClick: (GroupInfoForCreate) -> Unit,
    onSelectGroupIconClick: (String, ComposeStateUpdater<*>) -> Unit
) {
    HideNavBar()
    val systemUseCase = LocalUseCaseOfSystem.current
    DisposableEffect(Unit) {
        onDispose {
            systemUseCase.onComposeDispose("page dispose")
        }
    }
    val isInspectionMode = LocalInspectionMode.current
    val hazeState = if (isInspectionMode) {
        null
    } else {
        rememberHazeState()
    }
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
        val rootColumnModifier = if (isInspectionMode) {
            Modifier
        } else {
            Modifier.hazeSource(hazeState!!)
        }
        Column(
            modifier = rootColumnModifier
                .padding(start = 12.dp, end = 12.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        )
        {
            Spacer(
                modifier = Modifier.height(
                    backActionsHeight + 12.dp
                )
            )
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
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .clip(ExtraLarge2)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            ExtraLarge2
                        )
                        .clickable(onClick = {
                            val composeStateUpdater =
                                RuntimeSingleStateUpdater.fromState(groupInfoForCreate.iconUriProvider) { markKey, input ->
                                    PurpleLogger.current.d(
                                        TAG,
                                        "groupInfoForCreate.iconUriProvider, inputHandleDSL:\nmarkKey:$markKey\ninput:$input"
                                    )
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

                    AnimatedContent(groupIconUri != null) { hasUri ->
                        if (hasUri) {
                            AnyImage(
                                model = groupIconUri,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                                contentDescription = stringResource(xcj.app.appsets.R.string.add)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = xcj.app.appsets.R.string.status),
                fontWeight = FontWeight.Bold
            )
            Row {
                FilterChip(
                    selected = groupInfoForCreate.isPublic.value,
                    onClick = {
                        groupInfoForCreate.isPublic.value = true
                    },
                    label = {
                        Text(text = stringResource(id = xcj.app.appsets.R.string.public_))
                    },
                    shape = CircleShape
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilterChip(
                    selected = !groupInfoForCreate.isPublic.value,
                    onClick = {
                        groupInfoForCreate.isPublic.value = false
                    },
                    label = {
                        Text(text = stringResource(id = xcj.app.appsets.R.string.private_))
                    },
                    shape = CircleShape
                )
            }
            Text(
                text = String.format(
                    stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                    stringResource(id = xcj.app.appsets.R.string.name),
                    "*"
                ),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                value = groupInfoForCreate.name.value,
                onValueChange = {
                    val name = if (it.length > 30) {
                        it.substring(0, 30)
                    } else {
                        it
                    }
                    groupInfoForCreate.name.value = name
                }, placeholder = {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.group_name),
                        fontSize = 12.sp
                    )
                })
            Text(
                text = stringResource(xcj.app.appsets.R.string.maximum_number_of_members),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                value = groupInfoForCreate.membersCount.value,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                onValueChange = {
                    groupInfoForCreate.membersCount.value = it.toIntOrNull()?.toString() ?: "1000"
                },
                placeholder = {
                    Text(text = stringResource(xcj.app.appsets.R.string.quantity), fontSize = 12.sp)
                }
            )
            Text(
                text = stringResource(id = xcj.app.appsets.R.string.introduction),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = groupInfoForCreate.introduction.value,
                onValueChange = {
                    groupInfoForCreate.introduction.value = it
                },
                placeholder = {
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.group_description),
                        fontSize = 12.sp
                    )
                }
            )
            Spacer(modifier = Modifier.height(150.dp))
        }

        BackActionTopBar(
            modifier = Modifier.onPlaced {
                backActionBarSize = it.size
            },
            hazeState = hazeState,
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