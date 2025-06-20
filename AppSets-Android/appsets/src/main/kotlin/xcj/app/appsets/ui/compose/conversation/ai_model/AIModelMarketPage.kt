package xcj.app.appsets.ui.compose.conversation.ai_model

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.im.Session
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.apps.tools.PageIndicator
import xcj.app.appsets.ui.compose.conversation.GenerativeAISession
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBottomBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch

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

@Composable
fun AIGCMarketPage(
    onBackClick: () -> Unit,
) {
    HideNavBarWhenOnLaunch()
    val pagerState = rememberPagerState { 3 }
    Box(modifier = Modifier.fillMaxSize()) {
        PageIndicator(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 2.dp
                ),
            pagerState = pagerState
        )
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState
        ) { pageIndex ->
            if (pageIndex == 0) {
                OnlineAIModelsPage()
            } else if (pageIndex == 1) {
                DeviceLocalAIModelsPage()
            } else if (pageIndex == 2) {
                MixedAIModelsPage()
            }
        }

        DesignBottomBackButton(
            modifier = Modifier
                .align(Alignment.BottomCenter), onClick = onBackClick
        )
    }
}

@Composable
fun MixedAIModelsPage() {
    val sessionsTemplates = remember {
        val sessions = mutableStateListOf<AIGCSessionTemplate>()
        val aiSessionTemplates = GenerativeAISession.mixedSessions.map {
            AIGCSessionTemplate(it)
        }
        sessions.addAll(aiSessionTemplates)
        sessions
    }
    val conversationUseCase = LocalUseCaseOfConversation.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(xcj.app.appsets.R.string.mixed_ai_models),
            style = MaterialTheme.typography.titleSmall
        )
        Box(
            modifier = Modifier.fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    bottom = with(density) {
                        WindowInsets.systemBars.getBottom(density).toDp()
                    } + 68.dp
                )
            ) {
                items(
                    items = sessionsTemplates
                ) { sessionTemplate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnyImage(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.large
                                )
                                .clip(MaterialTheme.shapes.large),
                            any = sessionTemplate.session.imObj.avatarUrl,
                            defaultColor = MaterialTheme.colorScheme.outline
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            //名称
                            Text(
                                text = sessionTemplate.session.imObj.name,
                                fontSize = 14.sp
                            )
                            val bio = sessionTemplate.session.imObj.bio
                            if (bio is GenerativeAISession.AIBio) {
                                Text(
                                    text = bio.description ?: "",
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                        AnimatedContent(
                            targetState = sessionTemplate.addState.value,
                            contentAlignment = Alignment.CenterEnd
                        ) { targetAddState ->
                            if (targetAddState == AIGCSessionTemplate.ADD_STATE_ADDING) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                FilledTonalButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            sessionTemplate.addState.value =
                                                AIGCSessionTemplate.ADD_STATE_ADDING

                                            conversationUseCase.addAIGCSessionIfAbsent(
                                                sessionTemplate.session
                                            )
                                            delay(1000)
                                            sessionTemplate.addState.value =
                                                AIGCSessionTemplate.ADD_STATE_ADDED
                                        }
                                    }
                                ) {
                                    Text(text = stringResource(xcj.app.appsets.R.string.add))
                                }
                            }
                        }

                    }
                }
            }
            if (sessionsTemplates.isEmpty()) {
                Text(text = stringResource(xcj.app.appsets.R.string.no_models_available), modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun DeviceLocalAIModelsPage() {
    val sessionsTemplates = remember {
        val sessions = mutableStateListOf<AIGCSessionTemplate>()
        val aiSessionTemplates = GenerativeAISession.onDeviceSessions.map {
            AIGCSessionTemplate(it)
        }
        sessions.addAll(aiSessionTemplates)
        sessions
    }
    val conversationUseCase = LocalUseCaseOfConversation.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(xcj.app.appsets.R.string.device_local_ai_models),
            style = MaterialTheme.typography.titleSmall
        )
        Box(
            modifier = Modifier.fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    bottom = with(density) {
                        WindowInsets.systemBars.getBottom(density).toDp()
                    } + 68.dp
                )
            ) {
                items(
                    items = sessionsTemplates
                ) { sessionTemplate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnyImage(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.large
                                )
                                .clip(MaterialTheme.shapes.large),
                            any = sessionTemplate.session.imObj.avatarUrl,
                            defaultColor = MaterialTheme.colorScheme.outline
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            //名称
                            Text(
                                text = sessionTemplate.session.imObj.name,
                                fontSize = 14.sp
                            )
                            val bio = sessionTemplate.session.imObj.bio
                            if (bio is GenerativeAISession.AIBio) {
                                Text(
                                    text = bio.description ?: "",
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                        AnimatedContent(
                            targetState = sessionTemplate.addState.value,
                            contentAlignment = Alignment.CenterEnd
                        ) { targetAddState ->
                            if (targetAddState == AIGCSessionTemplate.ADD_STATE_ADDING) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                FilledTonalButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            sessionTemplate.addState.value =
                                                AIGCSessionTemplate.ADD_STATE_ADDING

                                            conversationUseCase.addAIGCSessionIfAbsent(
                                                sessionTemplate.session
                                            )
                                            delay(1000)
                                            sessionTemplate.addState.value =
                                                AIGCSessionTemplate.ADD_STATE_ADDED
                                        }
                                    }
                                ) {
                                    Text(text = stringResource(xcj.app.appsets.R.string.add))
                                }
                            }
                        }

                    }
                }
            }
            if (sessionsTemplates.isEmpty()) {
                Text(text = stringResource(xcj.app.appsets.R.string.no_models_available), modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
fun OnlineAIModelsPage() {
    val sessionsTemplates = remember {
        val sessions = mutableStateListOf<AIGCSessionTemplate>()
        val aiSessionTemplates = GenerativeAISession.onlineSessions.map {
            AIGCSessionTemplate(it)
        }
        sessions.addAll(aiSessionTemplates)
        sessions
    }
    val conversationUseCase = LocalUseCaseOfConversation.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(xcj.app.appsets.R.string.online_ai_models),
            style = MaterialTheme.typography.titleSmall
        )
        Box(
            modifier = Modifier
                .weight(1f)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    bottom = with(density) {
                        WindowInsets.systemBars.getBottom(density).toDp()
                    } + 68.dp
                )
            ) {
                items(
                    items = sessionsTemplates
                ) { sessionTemplate ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnyImage(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = MaterialTheme.shapes.large
                                )
                                .clip(MaterialTheme.shapes.large),
                            any = sessionTemplate.session.imObj.avatarUrl,
                            defaultColor = MaterialTheme.colorScheme.outline
                        )
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            //名称
                            Text(
                                text = sessionTemplate.session.imObj.name,
                                fontSize = 14.sp
                            )
                            val bio = sessionTemplate.session.imObj.bio
                            if (bio is GenerativeAISession.AIBio) {
                                Text(
                                    text = bio.description ?: "",
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                        AnimatedContent(
                            targetState = sessionTemplate.addState.value,
                            contentAlignment = Alignment.CenterEnd
                        ) { targetAddState ->
                            if (targetAddState == AIGCSessionTemplate.ADD_STATE_ADDING) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                FilledTonalButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            sessionTemplate.addState.value =
                                                AIGCSessionTemplate.ADD_STATE_ADDING

                                            conversationUseCase.addAIGCSessionIfAbsent(
                                                sessionTemplate.session
                                            )
                                            delay(1000)
                                            sessionTemplate.addState.value =
                                                AIGCSessionTemplate.ADD_STATE_ADDED
                                        }
                                    }
                                ) {
                                    Text(text = stringResource(xcj.app.appsets.R.string.add))
                                }
                            }
                        }

                    }
                }
            }
            if (sessionsTemplates.isEmpty()) {
                Text(text = stringResource(xcj.app.appsets.R.string.no_models_available), modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}