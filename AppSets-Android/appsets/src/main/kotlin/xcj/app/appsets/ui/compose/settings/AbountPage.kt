package xcj.app.appsets.ui.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.ui.compose.custom_component.preview_tooling.DesignPreviewCompositionLocalProvider
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer
import xcj.app.compose_share.modifier.hazeSourceIfAvailable
import xcj.app.compose_share.modifier.rememberHazeStateIfAvailable

@Preview(showBackground = true)
@Composable
fun AboutPagePreview() {
    DesignPreviewCompositionLocalProvider {
        AboutPage(
            updateHistory = listOf(
                UpdateCheckResult(
                    versionCode = 1,
                    newestVersionCode = 2,
                    newestVersion = "1.0.1",
                    updateChangesHtml = "• 修复了一些已知问题\n• 优化了用户体验",
                    forceUpdate = false,
                    downloadUrl = "",
                    publishDateTime = "2023-10-27",
                    canUpdate = true
                )
            ),
            onBackClick = {},
            onWebsiteClick = {},
            onHistoryExpandStateChanged = {}
        )
    }
}

@Composable
fun AboutPage(
    updateHistory: List<UpdateCheckResult>,
    onBackClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    onHistoryExpandStateChanged: (Boolean) -> Unit,
) {
    val hazeState = rememberHazeStateIfAvailable()
    val context = LocalContext.current
    val versionName = remember {
        SystemUseCase.getAppSetsPackageVersionName(context) ?: ""
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .hazeSourceIfAvailable(hazeState)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            StatusBarWithTopActionBarSpacer()
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                // 头部展示区
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                )
                {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                    ) {
                        Image(
                            modifier = Modifier
                                .padding(2.dp)
                                .fillMaxSize(),
                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                            contentDescription = "App Icon"
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 操作卡片区
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    AboutActionCard(
                        icon = xcj.app.compose_share.R.drawable.ic_language_24,
                        title = stringResource(xcj.app.appsets.R.string.website),
                        onClick = onWebsiteClick
                    )

                    var historyExpandState by remember { mutableStateOf(false) }

                    Column {
                        AboutActionCard(
                            icon = xcj.app.compose_share.R.drawable.ic_round_refresh_24,
                            title = stringResource(xcj.app.appsets.R.string.version_update_history),
                            trailingIcon = {
                                val rotateState by animateFloatAsState(if (historyExpandState) -180f else 0f)
                                Icon(
                                    modifier = Modifier.rotate(rotateState),
                                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_keyboard_arrow_down_24),
                                    contentDescription = "expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            onClick = {
                                historyExpandState = !historyExpandState
                                onHistoryExpandStateChanged(historyExpandState)
                            }
                        )

                        AnimatedVisibility(
                            visible = historyExpandState,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                if (updateHistory.isEmpty()) {
                                    Text(
                                        text = stringResource(xcj.app.appsets.R.string.no_version_information),
                                        modifier = Modifier.padding(24.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    updateHistory.forEach { result ->
                                        UpdateHistoryItem(result)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(150.dp))
            }
        }

        BackActionTopBar(
            hazeState = hazeState,
            onBackClick = onBackClick,
            backButtonText = stringResource(xcj.app.appsets.R.string.about)
        )
    }
}

@Composable
fun AboutActionCard(
    icon: Int,
    title: String,
    onClick: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            if (trailingIcon != null) {
                trailingIcon()
            } else {
                Icon(
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_chevron_right_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun UpdateHistoryItem(result: UpdateCheckResult) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = result.newestVersion ?: "Unknown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = result.publishDateTime ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = result.updateChangesHtml ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))
        DesignHDivider(modifier = Modifier.alpha(0.1f))
    }
}