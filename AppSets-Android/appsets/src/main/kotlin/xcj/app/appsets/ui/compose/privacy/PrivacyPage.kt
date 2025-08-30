package xcj.app.appsets.ui.compose.privacy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.apps.tools.PageIndicator
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrivacyPage(
    privacy: String?,
    platformPermissionsUsageList: List<PlatformPermissionsUsage>,
    onBackClick: () -> Unit,
    onRequest: (PlatformPermissionsUsage, Int) -> Unit,
) {
    HideNavBarWhenOnLaunch()
    val pagerState = rememberPagerState { 2 }
    Box(modifier = Modifier.fillMaxSize()) {
        PageIndicator(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 2.dp
                ),
            pagerState = pagerState
        )
        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { pageIndex ->
            if (pageIndex == 0) {
                PlatformPermissionsComponent(platformPermissionsUsageList, onRequest)
            } else if (pageIndex == 1) {
                PrivacyComponent(privacy)
            }
        }

        DesignBackButton(
            modifier = Modifier
                .align(Alignment.BottomCenter), onClick = onBackClick
        )
    }
}

@Composable
fun PrivacyComponent(privacy: String?) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Text(
            text = stringResource(xcj.app.appsets.R.string.user_content_privacy_and_notice),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = privacy ?: stringResource(xcj.app.appsets.R.string.not_offered),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun PlatformPermissionsComponent(
    platformPermissionsUsageList: List<PlatformPermissionsUsage>,
    onRequest: (PlatformPermissionsUsage, Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Text(
            text = stringResource(id = xcj.app.appsets.R.string.platform_permission),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        platformPermissionsUsageList.forEach {
            PermissionCard(platformPermissionsUsage = it, onRequest = onRequest)
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun PermissionCard(
    platformPermissionsUsage: PlatformPermissionsUsage,
    onRequest: (PlatformPermissionsUsage, Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.extraLarge
            )
            .padding(12.dp)
    ) {
        Text(
            text = String.format(
                "%s: %s",
                stringResource(id = xcj.app.appsets.R.string.name),
                platformPermissionsUsage.name
            ), fontSize = 16.sp
        )
        platformPermissionsUsage.androidDefinitionNames.forEach {
            Text(text = it, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = stringResource(xcj.app.appsets.R.string.explanation), fontSize = 16.sp)
        Text(text = platformPermissionsUsage.description, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(xcj.app.appsets.R.string.intent), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = platformPermissionsUsage.usage, fontSize = 12.sp)
        Text(text = stringResource(xcj.app.appsets.R.string.link), fontSize = 16.sp)
        if (platformPermissionsUsage.usageUri == null) {
            Text(text = stringResource(xcj.app.appsets.R.string.not_provide), fontSize = 12.sp)
        } else {
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            if (!platformPermissionsUsage.granted) {
                FilledTonalButton(
                    onClick = {
                        onRequest(platformPermissionsUsage, 1)
                    },
                    shape = CircleShape
                ) {
                    Text(text = stringResource(xcj.app.appsets.R.string.request))
                }

            } else {
                Text(text = stringResource(xcj.app.appsets.R.string.granted))
            }
            FilledTonalButton(
                onClick = {
                    onRequest(platformPermissionsUsage, 0)
                },
                shape = CircleShape,
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.go_to_settings))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PrivacyAndPermissionsPagePreview() {
    PrivacyPage(null, emptyList(), {}, { a, b -> })
}