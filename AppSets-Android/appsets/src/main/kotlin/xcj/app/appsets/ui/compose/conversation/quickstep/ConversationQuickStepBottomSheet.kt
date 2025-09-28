package xcj.app.appsets.ui.compose.conversation.quickstep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.im.InputSelector
import xcj.app.appsets.im.Session
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.appsets.ui.compose.theme.extShapes
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState

private const val RECENT_SESSIONS_SHOW_COUNT_LIMIT = 8

@Composable
fun ConversationQuickStepBottomSheet(
    quickStepContents: List<QuickStepContent>?
) {
    val context = LocalContext.current
    val conversationUseCase = LocalUseCaseOfConversation.current
    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
    val inputQuickStepContents = remember {
        mutableStateListOf<QuickStepContent>().apply {
            if (quickStepContents != null) {
                addAll(quickStepContents)
            }
        }
    }
    val allSessions = remember {
        conversationUseCase.getAllSessions()
    }
    val recentSessions by remember {
        derivedStateOf {
            val sessions = allSessions.sortedByDescending {
                it.latestIMMessage?.timestamp?.time ?: 0
            }
            if (sessions.size <= RECENT_SESSIONS_SHOW_COUNT_LIMIT) {
                sessions
            } else {
                sessions.subList(0, RECENT_SESSIONS_SHOW_COUNT_LIMIT)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = stringResource(xcj.app.appsets.R.string.send_to_people),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = stringResource(xcj.app.appsets.R.string.recently),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    recentSessions.forEachIndexed { index, session ->
                        SingleRecentSessionComponent(
                            modifier = Modifier
                                .sizeIn(90.dp)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            session = session,
                            onSessionClick = {
                                val textQuickStepContent =
                                    inputQuickStepContents.filterIsInstance<TextQuickStepContent>()
                                        .firstOrNull()
                                if (textQuickStepContent != null) {
                                    conversationUseCase.sendMessage(
                                        context,
                                        InputSelector.TEXT,
                                        textQuickStepContent.text,
                                        session
                                    )
                                    val bottomSheetState =
                                        visibilityComposeStateProvider.bottomSheetState()
                                    bottomSheetState.hide()
                                }
                            }
                        )
                    }
                }
            }
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = stringResource(xcj.app.appsets.R.string.others),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            itemsIndexed(items = allSessions) { index, session ->
                SingleAllSessionComponent(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    session = session,
                    onSessionClick = {
                        val textQuickStepContent =
                            inputQuickStepContents.filterIsInstance<TextQuickStepContent>()
                                .firstOrNull()
                        if (textQuickStepContent != null) {
                            conversationUseCase.sendMessage(
                                context,
                                InputSelector.TEXT,
                                textQuickStepContent.text,
                                session
                            )
                            val bottomSheetState =
                                visibilityComposeStateProvider.bottomSheetState()
                            bottomSheetState.hide()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SingleRecentSessionComponent(
    modifier: Modifier = Modifier,
    session: Session,
    onSessionClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnyImage(
            modifier = Modifier
                .size(68.dp)
                .clip(MaterialTheme.shapes.extShapes.large)
                .background(
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.extShapes.large
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.extShapes.large
                )
                .clickable(
                    enabled = true,
                    onClick = onSessionClick,
                ),
            model = session.imObj.bio.bioUrl
        )
        Text(
            text = session.imObj.bio.bioName ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            modifier = Modifier
                .widthIn(max = 82.dp)
        )
    }
}

@Composable
fun SingleAllSessionComponent(
    modifier: Modifier = Modifier,
    session: Session,
    onSessionClick: () -> Unit,
) {
    Row(
        modifier = modifier.clickable(
            enabled = true,
            onClick = onSessionClick,
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        AnyImage(
            modifier = Modifier
                .size(42.dp)
                .clip(MaterialTheme.shapes.extShapes.large)
                .background(
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.extShapes.large
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.extShapes.large
                ),
            model = session.imObj.bio.bioUrl
        )
        Text(
            text = session.imObj.bio.bioName ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            modifier = Modifier
                .widthIn(max = 82.dp)
        )
    }
}