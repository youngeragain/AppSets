package xcj.app.compose_share.dynamic

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Column {
        BackActionTopBar(
            onBackClick = onBackClick,
            backButtonRightText = stringResource(id = xcj.app.compose_share.R.string.compose_plugin),
            endButtonText = "+",
            onEndButtonClick = onAddClick
        )
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
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                ) {
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
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
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
}