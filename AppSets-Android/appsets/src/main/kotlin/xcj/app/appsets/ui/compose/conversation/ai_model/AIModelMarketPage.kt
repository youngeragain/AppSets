@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.conversation.ai_model

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.im.GenerativeAISessions
import xcj.app.appsets.im.Session
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.apps.tools.PageIndicator
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.preview_tooling.DesignPreviewCompositionLocalProvider

data class AIGCSessionTemplate(
    val session: Session,
    val addState: MutableState<Int> = mutableIntStateOf(ADD_STATE_NONE),
) {
    companion object {
        const val ADD_STATE_NONE = 0
        const val ADD_STATE_ADDING = 1
        const val ADD_STATE_ADDED = 2
    }
}

@Preview(showBackground = true)
@Composable
fun AIGCMarketPagePreview() {
    DesignPreviewCompositionLocalProvider {
        AIGCMarketPage(onBackClick = {})
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AIGCMarketPage(
    onBackClick: () -> Unit,
) {
    HideNavBar()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        val pagerState = rememberPagerState { 3 }
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            pageSpacing = 16.dp,
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { pageIndex ->
            when (pageIndex) {
                0 -> AIModelListPage(
                    title = stringResource(xcj.app.appsets.R.string.online_ai_models),
                    sessions = GenerativeAISessions.onlineSessions
                )

                1 -> AIModelListPage(
                    title = stringResource(xcj.app.appsets.R.string.device_local_ai_models),
                    sessions = GenerativeAISessions.onDeviceSessions
                )

                2 -> AIModelListPage(
                    title = stringResource(xcj.app.appsets.R.string.mixed_ai_models),
                    sessions = GenerativeAISessions.mixedSessions
                )
            }
        }

        PageIndicator(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    top = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                        .calculateTopPadding() + 12.dp,
                    end = 12.dp
                ),
            pagerState = pagerState
        )

        DesignBackButton(
            modifier = Modifier
                .align(Alignment.BottomCenter),
            onClick = onBackClick
        )
    }
}

@Composable
fun AIModelListPage(
    title: String,
    sessions: List<Session>
) {
    val sessionsTemplates = remember(sessions) {
        val list = mutableStateListOf<AIGCSessionTemplate>()
        list.addAll(sessions.map { AIGCSessionTemplate(it) })
        list
    }

    val conversationUseCase = LocalUseCaseOfConversation.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    top = 4.dp,
                    bottom = with(density) {
                        WindowInsets.systemBars.getBottom(density).toDp()
                    } + 80.dp
                )
            ) {
                itemsIndexed(
                    items = sessionsTemplates,
                    key = { _, item -> item.session.imObj.id }
                ) { index, sessionTemplate ->
                    // Animated entry for each item
                    androidx.compose.animation.AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) +
                                slideInVertically(spring(stiffness = Spring.StiffnessLow)) { it / 2 }
                    ) {
                        AIModelItem(
                            modifier = Modifier.animateItem(),
                            sessionTemplate = sessionTemplate,
                            onAddClick = {
                                coroutineScope.launch {
                                    sessionTemplate.addState.value =
                                        AIGCSessionTemplate.ADD_STATE_ADDING
                                    conversationUseCase.addAIGCSessionIfAbsent(sessionTemplate.session)
                                    delay(800)
                                    sessionTemplate.addState.value =
                                        AIGCSessionTemplate.ADD_STATE_ADDED
                                }
                            }
                        )
                    }
                }
            }

            if (sessionsTemplates.isEmpty()) {
                Text(
                    text = stringResource(xcj.app.appsets.R.string.no_models_available),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AIModelItem(
    modifier: Modifier = Modifier,
    sessionTemplate: AIGCSessionTemplate,
    onAddClick: () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnyImage(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = MaterialTheme.shapes.large
                    )
                    .clip(MaterialTheme.shapes.large),
                model = sessionTemplate.session.imObj.avatarUrl
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = sessionTemplate.session.imObj.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val bio = sessionTemplate.session.imObj.bio
                if (bio is GenerativeAISessions.AIBio) {
                    Text(
                        text = bio.description ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }

            AnimatedContent(
                targetState = sessionTemplate.addState.value,
                label = "AddState"
            ) { state ->
                if (state == AIGCSessionTemplate.ADD_STATE_ADDING) {
                    LoadingIndicator(modifier = Modifier.size(32.dp))
                } else {
                    val added = state == AIGCSessionTemplate.ADD_STATE_ADDED
                    FilledTonalButton(
                        onClick = onAddClick,
                        enabled = !added,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = if (added) "✓" else stringResource(xcj.app.appsets.R.string.add),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}