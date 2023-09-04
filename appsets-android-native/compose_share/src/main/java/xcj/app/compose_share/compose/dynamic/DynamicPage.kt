package xcj.app.compose_share.compose.dynamic

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.compose_share.compose.BackActionTopBar

@SuppressLint("UnrememberedMutableState")
@Preview(showBackground = true)
@Composable
fun DynamicPagePreview() {
    DynamicPage(mutableStateOf(false), {}, {}, {}, {}, emptyList())
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DynamicPage(
    tabVisibilityState: MutableState<Boolean>,
    onBackAction: () -> Unit,
    onAddClick: () -> Unit,
    onDeleteClick: (IComposeMethods) -> Unit,
    onDispose: () -> Unit,
    composeMethods: List<Pair<IComposeMethods, @Composable () -> Unit>>? = null,
) {
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
            onDispose()
        }
    })
    SideEffect {
        Log.e("DynamicPage", "composeMethods hash:${composeMethods?.map { it.second.hashCode() }}")
        tabVisibilityState.value = false
    }
    Column {
        BackActionTopBar(
            onBackAction = onBackAction,
            backButtonRightText = "加载项",
            endButtonText = "+",
            onEndButtonClick = onAddClick
        )
        val update by remember {
            derivedStateOf {
                composeMethods?.size ?: 0
            }
        }
        if (update == 0) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(250.dp), contentAlignment = Alignment.Center
            ) {
                Text(text = "暂无加载项，点击右上角添加")
            }
        } else {
            AnimatedContent(
                targetState = update,
                label = "dynamic_compose_animation"
            ) {
                Column(
                    Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp)
                ) {
                    composeMethods?.forEach {
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
                                    onDeleteClick(it.first)
                                }, label = {
                                    Text(text = "删除", fontSize = 12.sp)
                                })
                            }
                            Divider(
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.height(0.5.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            it.second.invoke()
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                }
            }
        }
    }
}