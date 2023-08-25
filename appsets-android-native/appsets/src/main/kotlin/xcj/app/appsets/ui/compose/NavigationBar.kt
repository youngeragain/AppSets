package xcj.app.appsets.ui.compose


import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi

@UnstableApi
@Composable
fun NavigationBar(
    onTabClick: (TabItemState) -> Unit
) {
    val viewModel: MainViewModel = viewModel(LocalContext.current as ComponentActivity)

    when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_SQUARE -> Unit
        Configuration.ORIENTATION_UNDEFINED -> Unit
        Configuration.ORIENTATION_LANDSCAPE -> {
            AnimatedVisibility(
                visible = viewModel.bottomMenuUseCase.tabVisibilityState.value,
                enter = expandHorizontally(),
                exit = shrinkHorizontally()
            ) {
                Row {
                    Divider(
                        modifier = Modifier
                            .width(0.5.dp)
                            .fillMaxHeight(),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .widthIn(min = 48.dp, max = 58.dp)
                            .background(
                                brush = SolidColor(value = MaterialTheme.colorScheme.surface),
                                alpha = 0.9f
                            )
                    ) {
                        val modifier = Modifier.weight(1f)
                        viewModel.bottomMenuUseCase.tabItemsState.forEach { tab ->
                            TabItem(
                                modifier = modifier,
                                tabItem = tab,
                                onTabClick = onTabClick
                            )
                        }
                    }
                }
            }

        }

        Configuration.ORIENTATION_PORTRAIT -> {
            AnimatedVisibility(
                visible = viewModel.bottomMenuUseCase.tabVisibilityState.value,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            brush = SolidColor(value = MaterialTheme.colorScheme.surface),
                            alpha = 0.9f
                        )
                        .navigationBarsPadding()
                ) {
                    Divider(
                        modifier = Modifier
                            .height(0.5.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .heightIn(min = 48.dp, max = 58.dp)

                    ) {
                        val modifier = Modifier.weight(1f)
                        viewModel.bottomMenuUseCase.tabItemsState.forEach { tab ->
                            TabItem(
                                modifier = modifier,
                                tabItem = tab,
                                onTabClick = onTabClick
                            )
                        }
                    }
                }
            }
        }
    }

}


@Composable
fun TabItem(
    modifier: Modifier = Modifier,
    tabItem: TabItemState,
    onTabClick: (TabItemState) -> Unit
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(1f)
        ) {
            Icon(
                painter = painterResource(id = tabItem.iconRes),
                contentDescription = tabItem.description ?: tabItem.name,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        onTabClick(tabItem)
                    }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                tint = tabItem.transFormIconTintColor
            )
            /*Spacer(modifier = Modifier.height(4.dp))
            Text(text = tabItem.name,
                fontSize = 10.sp,
                color = tabItem.transFormTextColor,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(12.dp))*/
        }
    }
}

