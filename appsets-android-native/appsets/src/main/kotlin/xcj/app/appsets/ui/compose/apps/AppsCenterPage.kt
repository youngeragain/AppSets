package xcj.app.appsets.ui.compose.apps

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.server.model.AppsWithCategory
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.PageRouteNameProvider
import xcj.app.appsets.ui.compose.win11Snapshot.ComponentSearchBar
import xcj.app.appsets.usecase.models.Application

@UnstableApi
@Composable
fun AppsCenterPage(
    recommendApplication: State<Application?>,
    applications: List<AppsWithCategory>,
    onRequestLoadApplication: () -> Unit,
    onAppClick: (Application) -> Unit,
    onSearchBarClick: () -> Unit,
    onSearchBarAddButtonClick: () -> Unit,
    onSettingsUserNameClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsLoginClick: () -> Unit
) {
    LaunchedEffect(key1 = true) {
        onRequestLoadApplication()
    }
    Box(Modifier.fillMaxWidth()) {
        val rememberScrollState = rememberScrollState()
        val showRecommendTips by remember {
            derivedStateOf {
                rememberScrollState.value == 0
            }
        }
        Column(modifier = Modifier.verticalScroll(rememberScrollState)) {
            val recApplication = recommendApplication.value
            if (recApplication != null) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAppClick(recApplication)
                        }
                ) {
                    Column {
                        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                        Spacer(modifier = Modifier.height(68.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            LocalOrRemoteImage(
                                modifier = Modifier
                                    .width(450.dp)
                                    .height(260.dp)
                                    .border(
                                        0.5.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clip(RoundedCornerShape(12.dp)),
                                any = recApplication.bannerUrl
                            )
                        }

                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showRecommendTips,
                        enter = fadeIn(animationSpec = tween(450)),
                        exit = slideOutVertically(targetOffsetY = { -100 }) + fadeOut(
                            animationSpec = tween(
                                450,
                                90
                            )
                        ),
                        label = "recommend_tips_animate"
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(24.dp)
                            ) {
                                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
                                Spacer(modifier = Modifier.height(68.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LocalOrRemoteImage(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .size(36.dp),
                                        any = recApplication.iconUrl
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = recApplication.name ?: "",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
            applications.forEach { appCategory ->
                Column(
                    Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = appCategory.category,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                    val rememberScrollStateHorizontal = rememberScrollState()
                    Row(modifier = Modifier.horizontalScroll(rememberScrollStateHorizontal)) {
                        appCategory.applications.forEachIndexed { index, app ->
                            if (index == 0) {
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            Column(modifier = Modifier.padding(16.dp)) {
                                val modifier = Modifier
                                    .size(78.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        onAppClick(app)
                                    }
                                Box(
                                    modifier = modifier,
                                    contentAlignment = Alignment.Center,
                                ) {
                                    LocalOrRemoteImage(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .size(78.dp),
                                        any = app.iconUrl
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = app.name ?: "",
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .widthIn(max = 78.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                            }
                            if (index == appCategory.applications.size - 1) {
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(300.dp))
        }

        Column(Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            ComponentSearchBar(
                modifier = Modifier.padding(horizontal = 12.dp),
                currentDestinationRoute = PageRouteNameProvider.AppSetsCenterPage,
                onSearchBarClick = onSearchBarClick,
                onSettingsUserNameClick = onSettingsUserNameClick,
                onSettingsClick = onSettingsClick,
                onSettingsLoginClick = onSettingsLoginClick,
                onAddButtonClick = onSearchBarAddButtonClick
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}