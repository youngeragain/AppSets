package xcj.app.appsets.ui.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsPage(
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPrivacyAndPermissionClick: () -> Unit
) {
    HideNavBar()
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        VerticalOverscrollBox(modifier = Modifier.widthIn(max = 600.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                StatusBarWithTopActionBarSpacer()

                SessionSettingsComponent()

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                HomePageSettingsComponent()

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                SettingsGroup(title = stringResource(xcj.app.appsets.R.string.permissions_and_privacy)) {
                    SettingsClickableItem(
                        icon = xcj.app.compose_share.R.drawable.ic_outline_privacy_tip_24,
                        title = stringResource(id = xcj.app.appsets.R.string.check),
                        onClick = onPrivacyAndPermissionClick
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

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
                backButtonText = stringResource(xcj.app.appsets.R.string.settings),
                onBackClick = onBackClick
            )
        }
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
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_chevron_right_24),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.outlineVariant
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

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

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
fun HomePageSettingsComponent() {
    val coroutineScope = rememberCoroutineScope()
    val appSetsModuleSettings = remember { AppSetsModuleSettings.get() }

    var isEnableAppCentral by remember { mutableStateOf(appSetsModuleSettings.appFeatures.homePageFeatures.isEnableAppCentral) }
    var isEnableOutSide by remember { mutableStateOf(appSetsModuleSettings.appFeatures.homePageFeatures.isEnableOutSide) }
    var isEnableConversation by remember { mutableStateOf(appSetsModuleSettings.appFeatures.homePageFeatures.isEnableConversation) }

    SettingsGroup(title = stringResource(xcj.app.appsets.R.string.appsets_launcher)) {
        SingleChoiceInRowComponent(
            icon = xcj.app.compose_share.R.drawable.ic_extension_24,
            choiceTitle = xcj.app.appsets.R.string.all_apps,
            currentChoice = isEnableAppCentral,
            choices = listOf(
                true to xcj.app.appsets.R.string.show,
                false to xcj.app.appsets.R.string.hide
            ),
            onChoiceClick = {
                isEnableAppCentral = it
                coroutineScope.launch { appSetsModuleSettings.onIsEnableAppCentralChanged(it) }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceInRowComponent(
            icon = xcj.app.compose_share.R.drawable.ic_language_24,
            choiceTitle = xcj.app.appsets.R.string.out_side,
            currentChoice = isEnableOutSide,
            choices = listOf(
                true to xcj.app.appsets.R.string.show,
                false to xcj.app.appsets.R.string.hide
            ),
            onChoiceClick = {
                isEnableOutSide = it
                coroutineScope.launch { appSetsModuleSettings.onIsEnableOutsideChanged(it) }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceInRowComponent(
            icon = xcj.app.compose_share.R.drawable.ic_bubble_chart_24,
            choiceTitle = xcj.app.appsets.R.string.conversation,
            currentChoice = isEnableConversation,
            choices = listOf(
                true to xcj.app.appsets.R.string.show,
                false to xcj.app.appsets.R.string.hide
            ),
            onChoiceClick = {
                isEnableConversation = it
                coroutineScope.launch { appSetsModuleSettings.onIsEnableConversationChanged(it) }
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
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(choiceTitle),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (choiceSubTitle != null) {
                    Text(
                        text = stringResource(choiceSubTitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
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