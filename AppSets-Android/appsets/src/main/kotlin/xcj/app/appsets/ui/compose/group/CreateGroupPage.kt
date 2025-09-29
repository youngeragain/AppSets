package xcj.app.appsets.ui.compose.group

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.model.GroupInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateGroupPageState
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField

private const val TAG = "CreateGroupPage"

@Composable
fun CreateGroupPage(
    createGroupPageState: CreateGroupPageState,
    onBackClick: () -> Unit,
    onConfirmAction: () -> Unit,
    onSelectGroupIconClick: (String) -> Unit
) {
    HideNavBar()
    val systemUseCase = LocalUseCaseOfSystem.current
    DisposableEffect(Unit) {
        onDispose {
            systemUseCase.onComposeDispose("page dispose")
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            BackActionTopBar(
                backButtonRightText = stringResource(id = xcj.app.appsets.R.string.create_group),
                endButtonText = stringResource(id = xcj.app.appsets.R.string.ok),
                onBackClick = onBackClick,
                onEndButtonClick = onConfirmAction
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column {
                        Text(
                            text = String.format(
                                stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                                stringResource(id = xcj.app.appsets.R.string.group_logo),
                                stringResource(id = xcj.app.appsets.R.string.required)
                            ),
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                        Box(
                            modifier = Modifier
                                .clip(MaterialTheme.shapes.extraLarge)
                                .border(
                                    1.dp, MaterialTheme.colorScheme.outline,
                                    MaterialTheme.shapes.extraLarge
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            val groupIconUri =
                                createGroupPageState.groupInfoForCreate.icon?.provideUri()
                                    ?: xcj.app.compose_share.R.drawable.ic_emoji_nature_24
                            AnyImage(
                                model = groupIconUri,
                                modifier = Modifier
                                    .size(98.dp)
                                    .clip(MaterialTheme.shapes.extraLarge)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable(onClick = {
                                onSelectGroupIconClick("CREATE_GROUP_IMAGE_SELECT_REQUEST")
                            })
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(text = stringResource(id = xcj.app.appsets.R.string.choose))
                        }
                    }
                }
                Text(text = stringResource(id = xcj.app.appsets.R.string.status))
                Row() {
                    FilterChip(
                        selected = createGroupPageState.groupInfoForCreate.isPublic,
                        onClick = {
                            GroupInfoForCreate.updateGroupCreatePublicStatus(
                                systemUseCase.createGroupPageState,
                                true
                            )
                        },
                        label = {
                            Text(text = stringResource(id = xcj.app.appsets.R.string.public_))
                        },
                        shape = CircleShape
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    FilterChip(
                        selected = !createGroupPageState.groupInfoForCreate.isPublic,
                        onClick = {
                            GroupInfoForCreate.updateGroupCreatePublicStatus(
                                systemUseCase.createGroupPageState,
                                false
                            )
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
                        stringResource(id = xcj.app.appsets.R.string.required)
                    ),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    value = createGroupPageState.groupInfoForCreate.name,
                    onValueChange = {
                        val name = if (it.length > 30) {
                            it.substring(0, 30)
                        } else {
                            it
                        }
                        GroupInfoForCreate.updateGroupCreateName(
                            systemUseCase.createGroupPageState,
                            name
                        )
                    }, placeholder = {
                        Text(text = stringResource(xcj.app.appsets.R.string.group_name))
                    })
                Text(
                    text = stringResource(xcj.app.appsets.R.string.maximum_number_of_members),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    value = createGroupPageState.groupInfoForCreate.membersCount,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    onValueChange = {
                        GroupInfoForCreate.updateGroupCreateMembersCount(
                            systemUseCase.createGroupPageState,
                            it.toIntOrNull()?.toString() ?: "100"
                        )
                    },
                    placeholder = {
                        Text(text = stringResource(xcj.app.appsets.R.string.quantity))
                    }
                )
                Text(
                    text = stringResource(id = xcj.app.appsets.R.string.introduction),
                    modifier = Modifier.padding(vertical = 10.dp)
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = createGroupPageState.groupInfoForCreate.introduction,
                    onValueChange = {
                        GroupInfoForCreate.updateGroupCreateDescription(
                            systemUseCase.createGroupPageState,
                            it
                        )
                    },
                    placeholder = {
                        Text(text = stringResource(xcj.app.appsets.R.string.group_description))
                    }
                )
                Spacer(modifier = Modifier.height(68.dp))
            }
        }

        CreateGroupIndicator(createGroupPageState = createGroupPageState)
    }

}

@Composable
private fun CreateGroupIndicator(createGroupPageState: CreateGroupPageState) {
    AnimatedVisibility(
        visible = createGroupPageState is CreateGroupPageState.Creating,
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