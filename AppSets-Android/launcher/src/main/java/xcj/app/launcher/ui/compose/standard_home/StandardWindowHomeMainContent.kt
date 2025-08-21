package xcj.app.launcher.ui.compose.standard_home

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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.viewmodel.compose.viewModel
import xcj.app.launcher.ui.model.StyledAppDefinition

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
    val onAppClick: (StyledAppDefinition) -> Unit = remember {
        { styledApp ->
            val launchIntentForPackage =
                styledApp.appDefinition.applicationInfo?.packageName?.let {
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
            .onSizeChanged {
                boxSize = it
            }
    ) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            beyondViewportPageCount = 1
        ) { pagerIndex ->
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