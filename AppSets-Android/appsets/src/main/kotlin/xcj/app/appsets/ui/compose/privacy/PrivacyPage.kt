package xcj.app.appsets.ui.compose.privacy

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.preview_tooling.DesignPreviewCompositionLocalProvider
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage


@Preview(showBackground = true)
@Composable
fun PrivacyAndPermissionsPagePreview() {
    DesignPreviewCompositionLocalProvider {
        val context = LocalContext.current
        val androidPermissionsUsageList = remember {
            PlatformPermissionsUsage.provideAll(context)
        }
        PrivacyPage(
            "privacy",
            androidPermissionsUsageList,
            {

            },
            { a, b ->

            }
        )
    }
}

@Composable
fun ExpressivePageIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val isSelected = pagerState.currentPage == iteration
            val width by animateDpAsState(
                targetValue = if (isSelected) 32.dp else 8.dp,
                animationSpec = tween(),
                label = "width"
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = 0.5f
                ),
                animationSpec = tween(),
                label = "color"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(width = width, height = 6.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun PrivacyPage(
    privacy: String?,
    platformPermissionsUsageList: List<PlatformPermissionsUsage>,
    onBackClick: () -> Unit,
    onRequest: (PlatformPermissionsUsage, Int) -> Unit,
) {
    HideNavBar()
    val pagerState = rememberPagerState { 2 }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxSize()
            ) { pageIndex ->
                if (pageIndex == 0) {
                    PlatformPermissionsComponent(platformPermissionsUsageList, onRequest)
                } else if (pageIndex == 1) {
                    PrivacyComponent(privacy)
                }
            }

            ExpressivePageIndicator(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp + WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                            .calculateTopPadding()
                    ),
                pagerState = pagerState
            )

            DesignBackButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter),
                onClick = onBackClick
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrivacyComponent(privacy: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(
            modifier = Modifier.height(
                24.dp + WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                    .calculateTopPadding()
            )
        )
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.user_content_privacy_and_notice),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                lineHeight = 36.sp
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Text(
                text = privacy ?: stringResource(xcj.app.appsets.R.string.not_offered),
                fontSize = 14.sp,
                modifier = Modifier.padding(20.dp),
                lineHeight = 22.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(150.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlatformPermissionsComponent(
    platformPermissionsUsageList: List<PlatformPermissionsUsage>,
    onRequest: (PlatformPermissionsUsage, Int) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = 24.dp + WindowInsets.statusBarsIgnoringVisibility.asPaddingValues()
                .calculateTopPadding(),
            end = 12.dp,
            bottom = 150.dp
        )
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = stringResource(id = xcj.app.appsets.R.string.platform_permission),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "了解应用如何使用您的设备权限",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
        items(
            items = platformPermissionsUsageList,
            key = { item -> item.name }
        ) { platformPermissionsUsage ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
            ) {
                PermissionCard(
                    modifier = Modifier.animateItem(),
                    platformPermissionsUsage = platformPermissionsUsage,
                    onRequest = onRequest
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    modifier: Modifier = Modifier,
    platformPermissionsUsage: PlatformPermissionsUsage,
    onRequest: (PlatformPermissionsUsage, Int) -> Unit,
) {
    Surface(
        modifier = modifier.padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(id = platformPermissionsUsage.name),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            platformPermissionsUsage.androidDefinitionNames.forEach {
                Text(
                    text = it,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(xcj.app.appsets.R.string.explanation),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(platformPermissionsUsage.description),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(xcj.app.appsets.R.string.intent),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(platformPermissionsUsage.usage),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (platformPermissionsUsage.usageUri != null) {
                AssistChip(
                    onClick = {
                        onRequest(platformPermissionsUsage, 1)
                    },
                    label = {
                        Text(text = stringResource(id = xcj.app.appsets.R.string.check))
                    },
                    shape = CircleShape,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!platformPermissionsUsage.granted) {
                    FilledTonalButton(
                        onClick = {
                            onRequest(platformPermissionsUsage, 1)
                        },
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(xcj.app.appsets.R.string.request))
                    }

                } else {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.granted),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                FilledTonalButton(
                    onClick = {
                        onRequest(platformPermissionsUsage, 0)
                    },
                    shape = CircleShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(xcj.app.appsets.R.string.go_to_settings))
                }
            }
        }
    }
}
