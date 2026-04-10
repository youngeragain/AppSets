@file:OptIn(ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.apps

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.ShowNavBar
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.compose.theme.extShapes
import xcj.app.appsets.ui.model.page_state.AppCenterPageUIState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppsCenterPage(
    appCenterPageUIState: AppCenterPageUIState,
    onBioClick: (Bio) -> Unit,
    onApplicationLongPress: (Application) -> Unit
) {
    ShowNavBar()
    val allApplications by rememberUpdatedState(appCenterPageUIState.apps.flatMap { it.applications })
    SimpleApplicationList(
        apps = allApplications,
        onBioClick = onBioClick,
        onApplicationLongPress = onApplicationLongPress
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SimpleApplicationList(
    modifier: Modifier = Modifier,
    apps: List<Application>,
    onBioClick: (Bio) -> Unit,
    onApplicationLongPress: (Application) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    VerticalOverscrollBox(
        modifier = modifier
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(90.dp),
            modifier = Modifier,
            state = rememberLazyGridState(),
            contentPadding = PaddingValues(
                top = WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                    .calculateTopPadding() + 12.dp,
                bottom = 150.dp
            )
        ) {
            itemsIndexed(items = apps) { index, application ->
                SingleApplicationComponent(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .animateItem(),
                    application = application,
                    onApplicationClick = {
                        onBioClick(application)
                    },
                    onApplicationLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onApplicationLongPress(application)
                    }
                )
            }
        }
    }
}

@Composable
fun SingleApplicationComponent(
    modifier: Modifier = Modifier,
    application: Application,
    onApplicationClick: () -> Unit,
    onApplicationLongClick: () -> Unit,
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
                .combinedClickable(
                    enabled = true,
                    onClick = onApplicationClick,
                    onLongClick = onApplicationLongClick
                ),
            model = application.bioUrl
        )
        Text(
            text = application.bioName ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            modifier = Modifier
                .widthIn(max = 82.dp)
        )
    }
}