package xcj.app.appsets.ui.compose.settings


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.compose.custom_component.preview_tooling.DesignPreviewCompositionLocalProvider
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer
import xcj.app.compose_share.modifier.hazeSourceIfAvailable
import xcj.app.compose_share.modifier.rememberHazeStateIfAvailable


@Preview(showBackground = true)
@Composable
fun SettingsPagePreView() {
    DesignPreviewCompositionLocalProvider {
        SettingsPage({}, {}, {})
    }
}

@Composable
fun SettingsGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsPage(
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPrivacyAndPermissionClick: () -> Unit
) {
    HideNavBar()
    val hazeState = rememberHazeStateIfAvailable()
    VerticalOverscrollBox {
        Column(
            modifier = Modifier
                .hazeSourceIfAvailable(hazeState)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            StatusBarWithTopActionBarSpacer()

            SessionSettingsComponent()

            SettingsGroup(title = stringResource(xcj.app.appsets.R.string.permissions_and_privacy)) {
                SettingsClickableItem(
                    icon = xcj.app.compose_share.R.drawable.ic_outline_privacy_tip_24,
                    title = stringResource(id = xcj.app.appsets.R.string.check),
                    onClick = onPrivacyAndPermissionClick
                )
            }

            SettingsGroup(title = stringResource(xcj.app.appsets.R.string.about)) {
                SettingsClickableItem(
                    icon = xcj.app.compose_share.R.drawable.ic_info_24,
                    title = stringResource(id = xcj.app.appsets.R.string.check),
                    onClick = onAboutClick
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        BackActionTopBar(
            hazeState = hazeState,
            backButtonText = stringResource(xcj.app.appsets.R.string.settings),
            onBackClick = onBackClick
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_chevron_right_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun SessionSettingsComponent() {
    val coroutineScope = rememberCoroutineScope()
    val appSetsModuleSettings = remember { AppSetsModuleSettings.get() }

    var imMessageReliability by remember { mutableStateOf(appSetsModuleSettings.isBackgroundIMEnable) }
    var imBubbleAlignment by remember { mutableStateOf(appSetsModuleSettings.imBubbleAlignment) }
    var imMessageShowDate by remember { mutableStateOf(appSetsModuleSettings.isImMessageShowDate) }
    var imMessageSendType by remember { mutableStateOf(appSetsModuleSettings.imMessageDeliveryType) }

    SettingsGroup(title = stringResource(xcj.app.appsets.R.string.session_settings)) {
        SingleChoiceInRowComponent(
            icon = xcj.app.compose_share.R.drawable.ic_bubble_chart_24,
            choiceTitle = xcj.app.appsets.R.string.chat_bubble_direction,
            currentChoice = imBubbleAlignment,
            choices = listOf(
                AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_START_END to xcj.app.appsets.R.string.alignment_to_start_end,
                AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_START to xcj.app.appsets.R.string.alignment_all_to_start,
                AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_END to xcj.app.appsets.R.string.alignment_all_to_end
            ),
            onChoiceClick = {
                imBubbleAlignment = it
                coroutineScope.launch { appSetsModuleSettings.onIMBubbleAlignmentChanged(it) }
            }
        )

        SingleChoiceInRowComponent(
            icon = xcj.app.compose_share.R.drawable.ic_round_check_24,
            choiceTitle = xcj.app.appsets.R.string.message_reliability,
            choiceSubTitle = xcj.app.appsets.R.string.message_reliability_tips,
            currentChoice = imMessageReliability,
            choices = listOf(
                false to xcj.app.appsets.R.string.no,
                true to xcj.app.appsets.R.string.yes
            ),
            onChoiceClick = {
                imMessageReliability = it
                coroutineScope.launch { appSetsModuleSettings.onIsIMMessageReliabilityChanged(it) }
            }
        )

        SingleChoiceInRowComponent(
            icon = xcj.app.compose_share.R.drawable.ic_round_access_time_24,
            choiceTitle = xcj.app.appsets.R.string.show_time,
            currentChoice = imMessageShowDate,
            choices = listOf(
                true to xcj.app.appsets.R.string.show,
                false to xcj.app.appsets.R.string.hide
            ),
            onChoiceClick = {
                imMessageShowDate = it
                coroutineScope.launch { appSetsModuleSettings.onIsIMMessageShowDateChanged(it) }
            }
        )

        SingleChoiceInRowComponent(
            icon = xcj.app.compose_share.R.drawable.ic_send_24,
            choiceTitle = xcj.app.appsets.R.string.data_sending_method,
            currentChoice = imMessageSendType,
            choices = listOf(
                AppSetsModuleSettings.IM_MESSAGE_DELIVERY_TYPE_RT to xcj.app.appsets.R.string.relay_delivery,
                AppSetsModuleSettings.IM_MESSAGE_DELIVERY_TYPE_DI to xcj.app.appsets.R.string.send_directly
            ),
            onChoiceClick = {
                imMessageSendType = it
                coroutineScope.launch { appSetsModuleSettings.onIMMessageDeliveryTypeChanged(it) }
            }
        )
    }
}

@Composable
fun <C> SingleChoiceInRowComponent(
    icon: Int,
    choiceTitle: Int,
    choiceSubTitle: Int? = null,
    currentChoice: C,
    choices: List<Pair<C, Int>>,
    onChoiceClick: (C) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(choiceTitle),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (choiceSubTitle != null) {
                    Text(
                        text = stringResource(choiceSubTitle),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp) // 与图标对齐
        ) {
            choices.forEachIndexed { index, choice ->
                SegmentedButton(
                    selected = choice.first == currentChoice,
                    onClick = { onChoiceClick(choice.first) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = choices.size),
                    label = {
                        Text(
                            text = stringResource(id = choice.second),
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.MiddleEllipsis
                        )
                    }
                )
            }
        }
    }
}