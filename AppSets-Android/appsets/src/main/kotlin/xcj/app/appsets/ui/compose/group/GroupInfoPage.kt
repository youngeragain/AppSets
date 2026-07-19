@file:OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3ExpressiveApi::class)

package xcj.app.appsets.ui.compose.group

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.compose.theme.ExtraLarge2
import xcj.app.appsets.ui.model.page_state.GroupInfoPageUIState
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer

@Preview(showBackground = true)
@Composable
fun GroupInfoPagePreview() {
    val groupInfoPageUIState by remember {
        mutableStateOf(
            GroupInfoPageUIState.LoadSuccess(GroupInfo.basic("", "name", ""))
        )
    }
    GroupInfoPage(
        groupInfoPageUIState = groupInfoPageUIState,
        onBackClick = {},
        onBioClick = {},
        onChatClick = {},
        onJoinGroupRequestClick = {}
    )
}

@Composable
fun GroupInfoPage(
    groupInfoPageUIState: GroupInfoPageUIState,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onChatClick: (GroupInfo) -> Unit,
    onJoinGroupRequestClick: (GroupInfo) -> Unit
) {
    HideNavBar()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (groupInfoPageUIState) {
            is GroupInfoPageUIState.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    LoadingIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                    DesignBackButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = onBackClick
                    )
                }
            }

            is GroupInfoPageUIState.NotFound -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    groupInfoPageUIState.tips?.let {
                        Text(
                            text = stringResource(id = it),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    DesignBackButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = onBackClick
                    )
                }
            }

            is GroupInfoPageUIState.LoadSuccess -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                    VerticalOverscrollBox(modifier = Modifier.widthIn(max = TextFieldDefaults.MinWidth * 2)) {
                        val userInfoList =
                            groupInfoPageUIState.groupInfo.userInfoList
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(82.dp),
                            modifier = Modifier,
                            state = rememberLazyGridState(),
                            contentPadding = PaddingValues(
                                top = 12.dp,
                                bottom = 68.dp
                            )
                        )
                        {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column(
                                    modifier = Modifier
                                )
                                {
                                    StatusBarWithTopActionBarSpacer()
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    )
                                    {
                                        AnyImage(
                                            modifier = Modifier
                                                .size(250.dp)
                                                .clip(ExtraLarge2)
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    ExtraLarge2
                                                ),
                                            model = groupInfoPageUIState.groupInfo.bioUrl,
                                            error = groupInfoPageUIState.groupInfo.bioName
                                        )
                                        Column(
                                            modifier = Modifier
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(text = "${(groupInfoPageUIState.groupInfo.bioName ?: "")}(${userInfoList?.size ?: 0})")
                                            Text(
                                                text = groupInfoPageUIState.groupInfo.introduction
                                                    ?: stringResource(xcj.app.appsets.R.string.no_introduction),
                                                fontSize = 12.sp
                                            )
                                        }
                                        if (userInfoList.isNullOrEmpty()) {
                                            Box(
                                                Modifier
                                                    .fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = stringResource(xcj.app.appsets.R.string.no_group_members))
                                            }
                                        }
                                    }
                                }
                            }
                            if (userInfoList != null) {
                                items(
                                    items = userInfoList,
                                    key = { item -> item.uid }
                                )
                                { userInfo ->
                                    Column(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .clip(MaterialTheme.shapes.small)
                                            .clickable {
                                                onBioClick(userInfo)
                                            },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        AnyImage(
                                            model = userInfo.bioUrl,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(MaterialTheme.shapes.extraLarge)
                                                .border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outline,
                                                    MaterialTheme.shapes.extraLarge
                                                ),
                                            error = userInfo.bioName
                                        )
                                        Text(
                                            text = userInfo.bioName ?: "",
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                        BackActionTopBar(
                            onBackClick = onBackClick,
                            endButtonText = if (RelationsUseCase.getInstance()
                                    .hasGroupRelated(groupInfoPageUIState.groupInfo.groupId)
                                || groupInfoPageUIState.groupInfo.public == 1
                            ) {
                                stringResource(xcj.app.appsets.R.string.chat)
                            } else {
                                stringResource(xcj.app.appsets.R.string.apply_to_join)
                            },
                            onEndButtonClick = {
                                if (RelationsUseCase.getInstance()
                                        .hasGroupRelated(groupInfoPageUIState.groupInfo.groupId)
                                    || groupInfoPageUIState.groupInfo.public == 1
                                ) {
                                    onChatClick(groupInfoPageUIState.groupInfo)
                                } else {
                                    onJoinGroupRequestClick(groupInfoPageUIState.groupInfo)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}