package xcj.app.appsets.ui.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.compose.custom_component.preview_tooling.DesignPreviewCompositionLocalProvider
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer

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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        VerticalOverscrollBox(modifier = Modifier.widthIn(max = TextFieldDefaults.MinWidth * 2)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                StatusBarWithTopActionBarSpacer()

                // 操作项列表 - 移除卡片背景，使用列表式布局
                Column(modifier = Modifier.fillMaxWidth()) {
                    AboutActionItem(
                        icon = xcj.app.compose_share.R.drawable.ic_language_24,
                        title = stringResource(xcj.app.appsets.R.string.website),
                        onClick = onWebsiteClick
                    )

                    var historyExpandState by remember { mutableStateOf(false) }

                    Column {
                        AboutActionItem(
                            icon = xcj.app.compose_share.R.drawable.ic_round_refresh_24,
                            title = stringResource(xcj.app.appsets.R.string.version_update_history),
                            trailingIcon = {
                                val rotateState by animateFloatAsState(if (historyExpandState) -180f else 0f)
                                Icon(
                                    modifier = Modifier.rotate(rotateState),
                                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_keyboard_arrow_down_24),
                                    contentDescription = "expand"
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
                                    .fillMaxWidth()
                                    .animateContentSize()
                            ) {
                                if (updateHistory.isEmpty()) {
                                    Text(
                                        text = stringResource(xcj.app.appsets.R.string.no_version_information),
                                        modifier = Modifier.padding(
                                            horizontal = 24.dp,
                                            vertical = 16.dp
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    updateHistory.forEach { result ->
                                        UpdateHistoryItem(result)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            BackActionTopBar(
                onBackClick = onBackClick,
                backButtonText = stringResource(xcj.app.appsets.R.string.about)
            )
        }
    }
}

@Composable
fun AboutActionItem(
    icon: Int,
    title: String,
    onClick: () -> Unit,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        if (trailingIcon != null) {
            trailingIcon()
        } else {
            Icon(
                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_chevron_right_24),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun UpdateHistoryItem(result: UpdateCheckResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = result.newestVersion ?: "Unknown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = result.publishDateTime ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        if (!result.updateChangesHtml.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = result.updateChangesHtml,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    }
}
