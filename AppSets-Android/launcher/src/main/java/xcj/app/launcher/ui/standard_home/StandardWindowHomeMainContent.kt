package xcj.app.launcher.ui.standard_home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import xcj.app.starter.android.AppDefinition

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandardWindowHomeMainContent() {
    val viewModel = viewModel<StandardWindowHomeViewModel>()
    val context = LocalContext.current
    var boxSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val settings by viewModel.settings
    settings.pageBackgroundState.Content()
    val pagerState = rememberPagerState(1) {
        3
    }
    val onAppClick: (AppDefinition) -> Unit = remember {
        { app ->
            val launchIntentForPackage =
                app.applicationInfo?.packageName?.let {
                    context.packageManager.getLaunchIntentForPackage(it)
                }
            if (launchIntentForPackage != null) {
                runCatching {
                    context.startActivity(launchIntentForPackage)
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onPlaced {
                boxSize = it.size
            }
    ) {
        HorizontalPager(modifier = Modifier.fillMaxSize(), state = pagerState) { pagerIndex ->
            when (pagerIndex) {
                0 -> {
                    SettingsPanelVertical(boxSize)
                }

                1 -> {
                    AppsPrimaryStylePage(
                        containerSize = boxSize,
                        onAppClick = onAppClick
                    )
                }

                2 -> {
                    AppsSecondaryStylePage(
                        onAppClick = onAppClick
                    )
                }
            }
        }
    }

}