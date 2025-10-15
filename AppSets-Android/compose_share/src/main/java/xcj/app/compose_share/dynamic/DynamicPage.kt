package xcj.app.compose_share.dynamic

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import xcj.app.compose_share.components.BackActionTopBar

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun DynamicPagePreview() {
    DynamicPage({}, {}, {}, emptyList())
}

private const val TAG = "DynamicPage"

@Composable
fun DynamicPage(
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: (ComposeMethodsWrapper) -> Unit,
    composeMethods: List<ComposeMethodsWrapper>,
) {
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    var backActionBarSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val backActionsHeight by remember {
        derivedStateOf {
            with(density) {
                backActionBarSize.height.toDp()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .hazeSource(hazeState)
                .padding(top = backActionsHeight + 12.dp)
        ) {
            if (composeMethods.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(xcj.app.compose_share.R.string.there_are_no_add_ons_yet_click_on_the_upper_right_corner_to_add),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                AnimatedContent(
                    targetState = composeMethods,
                    label = "dynamic_compose_animation"
                ) { targetComposeMethods ->
                    Column(
                        Modifier
                            .padding(horizontal = 12.dp)
                            .verticalScroll(rememberScrollState())

                    ) {
                        Spacer(
                            modifier = Modifier.height(
                                WindowInsets.statusBars.asPaddingValues()
                                    .calculateTopPadding() + backActionsHeight + 12.dp
                            )
                        )
                        targetComposeMethods.forEach { wrapper ->
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        0.5.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(
                                        horizontal = 12.dp,
                                        vertical = 4.dp
                                    )
                                ) {
                                    AssistChip(onClick = {
                                        onDeleteClick(wrapper)
                                    }, label = {
                                        Text(
                                            text = stringResource(xcj.app.compose_share.R.string.delete),
                                            fontSize = 12.sp
                                        )
                                    })
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                                Spacer(modifier = Modifier.height(4.dp))
                                wrapper.content()
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                    }
                }
            }
        }

        BackActionTopBar(
            modifier = Modifier.onPlaced {
                backActionBarSize = it.size
            },
            hazeState = hazeState,
            onBackClick = onBackClick,
            centerText = stringResource(id = xcj.app.compose_share.R.string.compose_plugin),
            endButtonText = "+",
            onEndButtonClick = onAddClick
        )
    }
}