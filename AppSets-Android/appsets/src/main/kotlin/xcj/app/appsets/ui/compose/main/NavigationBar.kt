@file:kotlin.OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalSharedTransitionApi::class)

package xcj.app.appsets.ui.compose.main

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.rememberHazeState
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreen
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.ImageButtonComponent
import xcj.app.appsets.ui.compose.search.NavigationSearchBar
import xcj.app.appsets.ui.model.TabAction
import xcj.app.appsets.ui.model.TabItem
import xcj.app.appsets.usecase.NavigationUseCase
import xcj.app.compose_share.components.DesignHDivider

private const val TAG = "NavigationBar"

@Composable
fun NavigationBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    visible: Boolean,
    enable: Boolean,
    inSearchModel: Boolean,
    tabItems: List<TabItem>,
    onTabClick: (TabItem, TabAction?) -> Unit,
    onBackClick: () -> Unit,
    onInputContent: (String) -> Unit,
    onSearchBarClick: () -> Unit,
    onBioClick: () -> Unit,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it + it / 20 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it + it / 20 }),
    ) {
        StandardNavigationBar(
            hazeState = hazeState,
            enable = enable,
            hostVisible = visible,
            inSearchModel = inSearchModel,
            tabItems = tabItems,
            onBackClick = onBackClick,
            onInputContent = onInputContent,
            onTabClick = onTabClick,
            onSearchBarClick = onSearchBarClick,
            onBioClick = onBioClick
        )
    }
}

@Composable
private fun StandardNavigationBar(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    enable: Boolean,
    hostVisible: Boolean,
    inSearchModel: Boolean,
    tabItems: List<TabItem>,
    onTabClick: (TabItem, TabAction?) -> Unit,
    onBackClick: () -> Unit,
    onInputContent: (String) -> Unit,
    onSearchBarClick: () -> Unit,
    onBioClick: () -> Unit,
) {
    val modifierOverride = if (!inSearchModel) {
        modifier
            .hazeEffect(
                hazeState,
                HazeMaterials.thin()
            )
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    } else {
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    }
    Column(
        modifier = modifierOverride
    ) {
        if (!inSearchModel) {
            DesignHDivider()
            Spacer(modifier = Modifier.height(6.dp))
        }
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .heightIn(min = 48.dp)
                .horizontalScroll(state = scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                !inSearchModel
            ) {
                Row {
                    tabItems.forEach { tab ->
                        TabItemContainer(
                            modifier = Modifier,
                            hostVisible = hostVisible,
                            naviTabItem = tab,
                            onTabClick = onTabClick
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            NavigationSearchBar(
                enable = enable,
                inSearchModel = inSearchModel,
                onBackClick = onBackClick,
                onInputContent = onInputContent,
                onSearchBarClick = onSearchBarClick,
                onBioClick = onBioClick
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun TabItemContainer(
    modifier: Modifier = Modifier,
    hostVisible: Boolean,
    naviTabItem: TabItem,
    onTabClick: (TabItem, TabAction?) -> Unit,
) {
    //todo bug
    val itemIsSelectOverride = naviTabItem.isSelect
    Row(
        modifier = modifier.animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        if (naviTabItem.isShow()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TabItemMain(naviTabItem, onTabClick)
                if (!naviTabItem.isSelect) {
                    Spacer(modifier = Modifier.height(4.dp))
                } else {
                    Spacer(modifier = Modifier.height(2.dp))
                    Spacer(
                        modifier = Modifier
                            .width(8.dp)
                            .height(2.dp)
                            .background(
                                MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(1.dp)
                            )
                    )
                }
            }
        }

        if (itemIsSelectOverride) {
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(
                    modifier = Modifier.animateContentSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    naviTabItem.actions?.forEach {
                        if (it.isShow()) {
                            TabItemAction(
                                naviTabItem = naviTabItem,
                                tabAction = it,
                                onTabClick = onTabClick
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
private fun TabItemAction(
    naviTabItem: TabItem,
    tabAction: TabAction,
    onTabClick: (TabItem, TabAction?) -> Unit,
) {
    val conversationUseCase = LocalUseCaseOfConversation.current
    val screenUseCase = LocalUseCaseOfScreen.current
    if (naviTabItem.routeName == PageRouteNames.ConversationOverviewPage &&
        tabAction.action == TabAction.ACTION_ADD
    ) {
        val iconRotationState by animateFloatAsState(
            targetValue = if (conversationUseCase.isShowActions.value) {
                135f
            } else {
                0f
            },
            label = "conversation_overview_add_button_animate"
        )
        ImageButtonComponent(
            resource = tabAction.icon,
            resRotate = iconRotationState,
            onClick = {
                onTabClick(naviTabItem, tabAction)
            }
        )
    } else if (naviTabItem.routeName == PageRouteNames.OutSidePage &&
        tabAction.action == TabAction.ACTION_REFRESH
    ) {
        val iconRotationState by animateFloatAsState(
            targetValue = if (screenUseCase.systemScreensContainer.isRequesting.value) {
                135f
            } else {
                0f
            },
            label = "outside_refresh_button_animate_state"
        )
        ImageButtonComponent(
            resource = tabAction.icon,
            resRotate = iconRotationState,
            useImage = false,
            onClick = {
                onTabClick(naviTabItem, tabAction)
            }
        )
    } else {
        ImageButtonComponent(
            resource = tabAction.icon,
            useImage = tabAction.action == TabAction.ACTION_ADD,
            onClick = {
                onTabClick(naviTabItem, tabAction)
            }
        )
    }
}

@Composable
private fun TabItemMain(naviTabItem: TabItem, onTabClick: (TabItem, TabAction?) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (naviTabItem is TabItem.SampleTabItem) {
            ImageButtonComponent(
                resource = naviTabItem.icon,
                useImage = false,
                onClick = {
                    onTabClick(naviTabItem, null)
                }
            )
        } else if (naviTabItem is TabItem.PlaybackTabItem) {
            val infiniteTransition =
                rememberInfiniteTransition(label = "playback_tab_infinite_transition")
            val rotation = infiniteTransition.animateFloat(
                initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 2000
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "playback_tab_animate"
            )
            ImageButtonComponent(
                resource = naviTabItem.icon,
                useImage = false,
                resRotate = rotation.value,
                onClick = {
                    onTabClick(naviTabItem, null)
                }
            )
        }
    }
}

@Preview
@UnstableApi
@Composable
fun NavigationBarPreview() {
    val hazeState = rememberHazeState()
    val navigationUseCase = remember {
        NavigationUseCase().apply {
            initTabItems()
        }
    }
    NavigationBar(
        hazeState = hazeState,
        visible = true,
        enable = true,
        inSearchModel = false,
        tabItems = navigationUseCase.tabItems.value,
        onTabClick = { tab, tabAction -> },
        onBackClick = {},
        onInputContent = {},
        onSearchBarClick = {},
        onBioClick = {}
    )
}