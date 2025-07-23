@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package xcj.app.screen_share.ui.compose.standard_home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import xcj.app.screen_share.ui.model.StyledAppDefinition

@Composable
fun AppsPrimaryStylePage1(containerSize: IntSize, onAppClick: (StyledAppDefinition) -> Unit) {
    AppsPrimaryStylePageVertical1(containerSize, onAppClick)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppsPrimaryStylePageVertical1(
    containerSize: IntSize,
    onAppClick: (StyledAppDefinition) -> Unit
) {
    val viewModel = viewModel<StandardWindowHomeViewModel>()
    val settings by viewModel.settings
    val space = settings.appCardSpace.dp
    val density = LocalDensity.current
    val boxWidthDp = with(density) {
        (containerSize.width.toDp() - space * (settings.appCountOnLine + 1)) / settings.appCountOnLine
    }
    val cardBackgroundColor =
        (settings.appCardBackgroundState as? WindowHomeBackgroundState.Color)?.color?.let {
            Color(it)
        } ?: Color.Transparent
    val cardColors = CardDefaults.outlinedCardColors(containerColor = cardBackgroundColor)
    val appNameColor = Color(settings.appCardAppNameColor)
    val context = LocalContext.current
    val appDefinitionList = viewModel.apps
    val hazeState = remember {
        HazeState()
    }
    var containerSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPlaced {
                containerSize = it.size
            }
            .hazeSource(hazeState)
    ) {
        FlowRow(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            maxItemsInEachRow = 4
        ) {
            appDefinitionList.forEach { styledApp ->
                AppCard(
                    modifier = Modifier,
                    containerSize = containerSize,
                    styledApp = styledApp
                )
            }
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier,
    containerSize: IntSize,
    styledApp: StyledAppDefinition,
) {
    val viewModel = viewModel<StandardWindowHomeViewModel>()
    val density = LocalDensity.current
    val cardWidth = with(density) {
        val availableWidth =
            containerSize.width
        val singleCardWidthPixel = availableWidth / 4
        val currentAppCardWidthPixel = singleCardWidthPixel * styledApp.style.sizeStyleH
        currentAppCardWidthPixel.toDp()
    }
    val cardHeight = with(density) {
        val availableWidth =
            containerSize.width
        val singleCardWidthPixel = availableWidth / 4
        val currentAppCardWidthPixel = singleCardWidthPixel * styledApp.style.sizeStyleV
        currentAppCardWidthPixel.toDp()
    }
    val cardWidthState by animateDpAsState(cardWidth, animationSpec = tween(450))
    val cardHeightState by animateDpAsState(cardHeight, animationSpec = tween(450))

    Box(
        modifier = modifier.size(width = cardWidthState, height = cardHeightState),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Card(
                modifier = Modifier,
                shape = MaterialTheme.shapes.extraLarge,
                elevation = CardDefaults.outlinedCardElevation(),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = styledApp.appDefinition.icon,
                        contentDescription = styledApp.appDefinition.description,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(52.dp)
                            .clip(MaterialTheme.shapes.large)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                MaterialTheme.shapes.large
                            ),
                    )
                    /*Text(
                        text = app.name ?: "",
                        modifier = Modifier.widthIn(max = 68.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )*/
                }
            }
        }
        Column(modifier = Modifier.align(Alignment.CenterEnd)) {
            Spacer(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount.x < -20) {
                                styledApp.style.previousSizeStyleH()?.let {
                                    val newStyledApp = styledApp.copy(style = it)
                                    viewModel.updateExistApp(newStyledApp)
                                }
                            } else if (dragAmount.x > 20) {
                                styledApp.style.nextSizeStyleH()?.let {
                                    val newStyledApp = styledApp.copy(style = it)
                                    viewModel.updateExistApp(newStyledApp)
                                }
                            }

                        }
                    }
            )
        }

        Row(modifier = Modifier.align(Alignment.BottomCenter)) {
            Spacer(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        CircleShape
                    )
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()

                            if (dragAmount.y < -20) {
                                styledApp.style.previousSizeStyleV()?.let {
                                    val newStyledApp = styledApp.copy(style = it)
                                    viewModel.updateExistApp(newStyledApp)
                                }

                            } else if (dragAmount.y > 20) {
                                styledApp.style.nextSizeStyleV()?.let {
                                    val newStyledApp = styledApp.copy(style = it)
                                    viewModel.updateExistApp(newStyledApp)
                                }
                            }

                        }
                    }
            )
        }

    }
}