@file:OptIn(ExperimentalHazeMaterialsApi::class)

package xcj.app.appsets.ui.compose.group

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBottomBackButton
import xcj.app.appsets.ui.compose.theme.BigAvatarShape
import xcj.app.appsets.ui.model.page_state.GroupInfoPageState
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.compose_share.components.DesignHDivider
import kotlin.math.roundToInt

@Preview(showBackground = true)
@Composable
fun GroupInfoPagePreview() {
    //GroupInfoPage(null)
}

@Composable
fun GroupInfoPage(
    groupInfoPageState: GroupInfoPageState,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onChatClick: (GroupInfo) -> Unit,
    onJoinGroupRequestClick: (GroupInfo) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (groupInfoPageState) {
            is GroupInfoPageState.Loading -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center)
                    )
                    DesignBottomBackButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = onBackClick
                    )
                }
            }

            is GroupInfoPageState.NotFound -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    groupInfoPageState.tipsIntRes?.let {
                        Text(
                            text = stringResource(id = it),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    DesignBottomBackButton(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onClick = onBackClick
                    )
                }
            }

            is GroupInfoPageState.LoadSuccess -> {
                var sizeOfGroupAvatar by remember {
                    mutableStateOf(IntSize.Zero)
                }
                val groupAvatarOffsetHeightPx = remember { mutableFloatStateOf(0f) }
                // now, let's create connection to the nested scroll system and listen to the scroll
                // happening inside child LazyColumn
                val nestedScrollConnection = remember {
                    object : NestedScrollConnection {
                        override fun onPreScroll(
                            available: Offset,
                            source: NestedScrollSource
                        ): Offset {
                            // try to consume before LazyColumn to collapse toolbar if needed, hence pre-scroll
                            val delta = available.y
                            val newOffset = groupAvatarOffsetHeightPx.floatValue + delta
                            groupAvatarOffsetHeightPx.floatValue =
                                newOffset.coerceIn(
                                    -sizeOfGroupAvatar.height.toFloat(),
                                    0f
                                )
                            // here's the catch: let's pretend we consumed 0 in any case, since we want
                            // LazyColumn to scroll anyway for good UX
                            // We're basically watching scroll without taking it
                            return Offset.Zero
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(nestedScrollConnection)
                ) {
                    val userInfoList = groupInfoPageState.groupInfo.userInfoList
                    if (userInfoList.isNullOrEmpty()) {
                        Box(
                            Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stringResource(xcj.app.appsets.R.string.no_group_members))
                        }
                    } else {
                        val density = LocalDensity.current
                        val paddingValues = with(density) {
                            PaddingValues(
                                top = sizeOfGroupAvatar.height.toDp(),
                                bottom = 68.dp
                            )
                        }
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(82.dp),
                            modifier = Modifier,
                            state = rememberLazyGridState(),
                            contentPadding = paddingValues
                        ) {
                            items(userInfoList) { userInfo ->
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
                                        error = userInfo.name
                                    )
                                    Text(
                                        text = userInfo.name ?: "",
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier
                        .onSizeChanged {
                            sizeOfGroupAvatar = it
                        }
                    ) {
                        Column(
                            modifier = Modifier
                                .zIndex(1f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                            ) {
                                Icon(
                                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_arrow_back_24),
                                    contentDescription = stringResource(id = xcj.app.appsets.R.string.return_),
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable(onClick = onBackClick)
                                        .padding(12.dp)
                                )
                                if (RelationsUseCase.getInstance()
                                        .hasGroupRelated(groupInfoPageState.groupInfo.groupId)
                                    || groupInfoPageState.groupInfo.public == 1
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        TextButton(
                                            onClick = {
                                                onChatClick(groupInfoPageState.groupInfo)
                                            }
                                        ) {
                                            Text(text = stringResource(xcj.app.appsets.R.string.chat))
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(horizontal = 12.dp)
                                    ) {
                                        FilledTonalButton(
                                            onClick = {
                                                onJoinGroupRequestClick(groupInfoPageState.groupInfo)
                                            }
                                        ) {
                                            Text(text = stringResource(xcj.app.appsets.R.string.apply_to_join))
                                        }
                                    }
                                }
                            }
                            DesignHDivider()
                        }
                        Column(
                            modifier = Modifier
                                .zIndex(0f)
                                .fillMaxWidth()
                                .padding(12.dp)
                                .offset {
                                    IntOffset(
                                        x = 0,
                                        y = groupAvatarOffsetHeightPx.floatValue.roundToInt()
                                    )
                                },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AnyImage(
                                modifier = Modifier
                                    .size(250.dp)
                                    .clip(BigAvatarShape)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        BigAvatarShape
                                    ),
                                model = groupInfoPageState.groupInfo.bioUrl,
                                error = groupInfoPageState.groupInfo.name
                            )
                            Column(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.shapes.extraLarge
                                    )
                                    .clip(
                                        MaterialTheme.shapes.extraLarge
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "${(groupInfoPageState.groupInfo.name ?: "")}(${userInfoList?.size ?: 0})")
                                Text(
                                    text = groupInfoPageState.groupInfo.introduction
                                        ?: stringResource(xcj.app.appsets.R.string.no_introduction),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}