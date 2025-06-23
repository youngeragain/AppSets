@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.launcher.ui.compose.standard_home

import android.view.ContextThemeWrapper
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import xcj.app.launcher.ui.model.StyledAppDefinition
import kotlin.math.roundToInt

@Composable
fun AppsPrimaryStylePage(containerSize: IntSize, onAppClick: (StyledAppDefinition) -> Unit) {
    AppsPrimaryStylePageVertical(containerSize, onAppClick)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppsPrimaryStylePageVertical(
    containerSize: IntSize,
    onAppClick: (StyledAppDefinition) -> Unit,
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
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize(),
        columns = GridCells.Fixed(settings.appCountOnLine),
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            start = space,
            end = space
        )
    ) {
        items(
            items = appDefinitionList,
            key = { it.appDefinition.id }
        ) { styledApp ->
            Column(
                modifier = Modifier
                    .padding(bottom = space)
                    .animateItem(
                        fadeInSpec = tween(),
                        placementSpec = tween(),
                        fadeOutSpec = tween()
                    )
            ) {
                Card(
                    modifier = Modifier.size(boxWidthDp),
                    shape = RectangleShape,
                    elevation = CardDefaults.outlinedCardElevation(),
                    colors = cardColors,
                    onClick = {
                        onAppClick(styledApp)
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = styledApp.appDefinition.icon,
                            contentDescription = styledApp.appDefinition.description,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(settings.appIconSize.dp)
                                .clip(MaterialTheme.shapes.large)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    MaterialTheme.shapes.large
                                ),
                        )
                        Row(
                            modifier = Modifier
                                .padding(horizontal = space, vertical = space / 2)
                                .align(Alignment.BottomStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = styledApp.appDefinition.name ?: "",
                                modifier = Modifier.widthIn(max = 68.dp),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                color = appNameColor,
                                fontSize = settings.appNameFontSize.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun findActivity(): ComponentActivity {
    var context = LocalContext.current
    do {
        if (context is ComponentActivity) {
            return context
        }
        if (context is ContextThemeWrapper) {
            context = context.baseContext
        }
    } while (true)
}

@Composable
fun SettingsPanelVertical(appCardContainerSize: IntSize) {
    val viewModel = viewModel<StandardWindowHomeViewModel>()
    val context = LocalContext.current
    val settings by viewModel.settings
    val coroutineScope = rememberCoroutineScope()
    Column(Modifier.statusBarsPadding()) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.statusBarsPadding())
                Column {
                    Text(
                        text = "Amounts",
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = FontWeight.Bold
                    )

                    Column {
                        var sliderPositionForFontColumns by remember { mutableFloatStateOf(settings.appCountOnLine.toFloat()) }
                        Text(
                            text = "Columns or Rows (${sliderPositionForFontColumns.roundToInt()})",
                            fontSize = 12.sp
                        )
                        Slider(
                            value = sliderPositionForFontColumns,
                            onValueChange = {
                                sliderPositionForFontColumns = it
                                viewModel.updateSettingsAppCountOnLine(it.roundToInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary,
                                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                            steps = 0,
                            valueRange = 1f..20f
                        )
                    }
                    Column {
                        var sliderPositionForFontSize by remember { mutableFloatStateOf(settings.appNameFontSize.toFloat()) }
                        Text(
                            text = "Font Size (${sliderPositionForFontSize.roundToInt()})",
                            fontSize = 12.sp
                        )
                        Slider(
                            value = sliderPositionForFontSize,
                            onValueChange = {
                                sliderPositionForFontSize = it
                                viewModel.updateSettingsAppNameFontSize(it.roundToInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary,
                                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                            steps = 0,
                            valueRange = 4f..24f
                        )
                    }


                    Column {
                        var sliderPositionForIcon by remember { mutableFloatStateOf(settings.appIconSize.toFloat()) }
                        Text(
                            text = "App Icon Size (${sliderPositionForIcon.roundToInt()})",
                            fontSize = 12.sp
                        )
                        Slider(
                            value = sliderPositionForIcon,
                            onValueChange = {
                                sliderPositionForIcon = it
                                viewModel.updateSettingsAppIconSize(it.roundToInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary,
                                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                            steps = 0,
                            valueRange = 24f..98f
                        )
                    }

                    Column {
                        var sliderPositionForSpace by remember { mutableFloatStateOf(settings.appCardSpace.toFloat()) }
                        Text(
                            text = "Card Space (${sliderPositionForSpace.roundToInt()})",
                            fontSize = 12.sp
                        )
                        Slider(
                            value = sliderPositionForSpace,
                            onValueChange = {
                                sliderPositionForSpace = it
                                viewModel.updateSettingsAppCardSpace(it.roundToInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.secondary,
                                activeTrackColor = MaterialTheme.colorScheme.secondary,
                                inactiveTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                            ),
                            steps = 0,
                            valueRange = 0f..20f
                        )
                    }
                }
                Column {
                    Text(
                        text = "Colors",
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = FontWeight.Bold
                    )
                    var currentColorToChange by remember {
                        mutableIntStateOf(-1)
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val pageBackgroundState = settings.pageBackgroundState
                            if (pageBackgroundState is WindowHomeBackgroundState.Color) {
                                if (pageBackgroundState.color != null) {
                                    Spacer(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(8.dp)
                                            .background(Color(pageBackgroundState.color))
                                    )
                                }

                            }
                            FilterChip(
                                selected = false,
                                onClick = {
                                    currentColorToChange = AppsPageSettings.PAGE_BACKGROUND
                                },
                                label = {
                                    Text(
                                        text = "Background",
                                        modifier = Modifier
                                            .padding(vertical = 16.dp),
                                        fontSize = 10.sp
                                    )
                                },
                                shape = RectangleShape,
                                border = FilterChipDefaults.filterChipBorder(
                                    true,
                                    false,
                                    borderWidth = 2.dp
                                )
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val appCardBackgroundState = settings.appCardBackgroundState
                            if (appCardBackgroundState is WindowHomeBackgroundState.Color) {
                                if (appCardBackgroundState.color != null) {
                                    Spacer(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(8.dp)
                                            .background(Color(appCardBackgroundState.color))
                                    )
                                }
                            }
                            FilterChip(
                                selected = false,
                                onClick = {
                                    currentColorToChange = AppsPageSettings.APP_CARD_BACKGROUND
                                },
                                label = {
                                    Text(
                                        text = "App Card",
                                        modifier = Modifier
                                            .padding(vertical = 16.dp),
                                        fontSize = 10.sp
                                    )
                                },
                                shape = RectangleShape,
                                border = FilterChipDefaults.filterChipBorder(
                                    true,
                                    false,
                                    borderWidth = 2.dp
                                )
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val appCardNameBackgroundState = settings.appCardAppNameColor
                            Spacer(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(8.dp)
                                    .background(Color(appCardNameBackgroundState))
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    currentColorToChange = AppsPageSettings.APP_CARD_APP_NAME_COLOR
                                },
                                label = {
                                    Text(
                                        text = "App Name",
                                        modifier = Modifier
                                            .padding(vertical = 16.dp),
                                        fontSize = 10.sp
                                    )
                                },
                                shape = RectangleShape,
                                border = FilterChipDefaults.filterChipBorder(
                                    true,
                                    false,
                                    borderWidth = 2.dp
                                )
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val searchPageAppNameColor = settings.searchPageAppNameColor
                            Spacer(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(8.dp)
                                    .background(Color(searchPageAppNameColor))
                            )
                            FilterChip(
                                selected = false,
                                onClick = {
                                    currentColorToChange =
                                        AppsPageSettings.SEARCH_PAGE_APP_NAME_COLOR
                                },
                                label = {
                                    Text(
                                        text = "Search Page Content",
                                        modifier = Modifier
                                            .padding(vertical = 16.dp),
                                        fontSize = 10.sp
                                    )
                                },
                                shape = RectangleShape,
                                border = FilterChipDefaults.filterChipBorder(
                                    true,
                                    false,
                                    borderWidth = 2.dp
                                )
                            )
                        }
                    }
                    if (currentColorToChange != -1) {
                        ColorSelector(
                            currentColorToChange = currentColorToChange,
                            onDismissRequest = {
                                currentColorToChange = -1
                            },
                            onConfirmColor = { color ->
                                coroutineScope.launch {
                                    viewModel.updateSettingsColor(color, currentColorToChange)
                                    currentColorToChange = -1
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSelector(
    currentColorToChange: Int,
    onDismissRequest: () -> Unit,
    onConfirmColor: (Color?) -> Unit,
) {
    var colorCurrent by remember {
        mutableStateOf<Color?>(null)
    }
    val viewModel = viewModel<StandardWindowHomeViewModel>()
    val context = LocalContext.current
    val settings by viewModel.settings
    val sliderDefaultColor = when (currentColorToChange) {
        AppsPageSettings.PAGE_BACKGROUND -> {
            (settings.pageBackgroundState as? WindowHomeBackgroundState.Color)?.color?.let {
                Color(it)
            }
        }

        AppsPageSettings.APP_CARD_BACKGROUND -> {
            (settings.appCardBackgroundState as? WindowHomeBackgroundState.Color)?.color?.let {
                Color(it)
            }
        }

        AppsPageSettings.APP_CARD_APP_NAME_COLOR -> {
            Color(settings.appCardAppNameColor)
        }

        else -> null
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .align(Alignment.CenterHorizontally),
        ) {
            ColorSliderVertical(
                modifier = Modifier
                    .fillMaxWidth(),
                defaultColor = sliderDefaultColor,
                onColorChanged = { color ->
                    colorCurrent = color
                }
            )
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = {
                        onConfirmColor(null)
                    }
                ) {
                    Text("Clear color")
                }
                FilledTonalButton(
                    onClick = {
                        onConfirmColor(colorCurrent)
                    }
                ) {
                    Text("OK")
                }
            }
        }
    }
}
