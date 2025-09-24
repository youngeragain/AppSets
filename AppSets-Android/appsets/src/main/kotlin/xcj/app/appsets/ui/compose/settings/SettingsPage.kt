package xcj.app.appsets.ui.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignHDivider


@Preview(showBackground = true)
@Composable
fun SettingsPagePreView() {
    SettingsPage({}, {}, {})
}

@Composable
fun SettingsPage(
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPrivacyAndPermissionClick: () -> Unit
) {
    HideNavBar()
    Column {
        BackActionTopBar(
            backButtonRightText = stringResource(xcj.app.appsets.R.string.settings),
            onBackClick = onBackClick
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        )
        {
            SessionSettingsComponent()

            PermissionAndPrivacyComponent(onPrivacyAndPermissionClick = onPrivacyAndPermissionClick)

            AboutSettingsComponent(onAboutClick = onAboutClick)
        }
    }
}

@Composable
fun AboutSettingsComponent(onAboutClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(xcj.app.appsets.R.string.about),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
            fontSize = 16.sp
        )

        Column(
            modifier = Modifier
                .clickable(onClick = onAboutClick),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DesignHDivider()
            Text(
                text = stringResource(id = xcj.app.appsets.R.string.check),
                modifier = Modifier.padding(vertical = 12.dp)
            )

        }
    }
}

@Composable
fun PermissionAndPrivacyComponent(onPrivacyAndPermissionClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(xcj.app.appsets.R.string.permissions_and_privacy),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
            fontSize = 16.sp
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPrivacyAndPermissionClick),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DesignHDivider()
            Text(
                text = stringResource(id = xcj.app.appsets.R.string.check),
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
fun SessionSettingsComponent() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(xcj.app.appsets.R.string.session_settings),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
            fontSize = 16.sp
        )

        var imBubbleAlignment by remember {
            mutableStateOf(AppSetsModuleSettings.get().imBubbleAlignment)
        }
        val bubbleAlignmentChoices = remember {
            listOf(
                AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_START_END to xcj.app.appsets.R.string.alignment_to_start_end,
                AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_START to xcj.app.appsets.R.string.alignment_all_to_start,
                AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_END to xcj.app.appsets.R.string.alignment_all_to_end
            )
        }
        SingleChoiceInRowComponent(
            choiceTitle = xcj.app.appsets.R.string.chat_bubble_direction,
            currentChoice = imBubbleAlignment,
            choices = bubbleAlignmentChoices,
            onChoiceClick = {
                AppSetsModuleSettings.get().onIMBubbleAlignmentChanged(it)
                imBubbleAlignment = it
            }
        )

        var imMessageShowDate by remember {
            mutableStateOf(AppSetsModuleSettings.get().isImMessageShowDate)
        }
        val showDateChoices = remember {
            listOf(
                true to xcj.app.appsets.R.string.show,
                false to xcj.app.appsets.R.string.hide
            )
        }
        SingleChoiceInRowComponent(
            choiceTitle = xcj.app.appsets.R.string.show_time,
            currentChoice = imMessageShowDate,
            choices = showDateChoices,
            onChoiceClick = {
                AppSetsModuleSettings.get().onIsIMMessageShowDateChanged(it)
                imMessageShowDate = it
            }
        )

        var imMessageSendType by remember {
            mutableStateOf(AppSetsModuleSettings.get().imMessageDeliveryType)
        }
        val sendTypeChoices = remember {
            listOf(
                AppSetsModuleSettings.IM_MESSAGE_DELIVERY_TYPE_RT to xcj.app.appsets.R.string.relay_delivery,
                AppSetsModuleSettings.IM_MESSAGE_DELIVERY_TYPE_DI to xcj.app.appsets.R.string.send_directly
            )
        }
        SingleChoiceInRowComponent(
            choiceTitle = xcj.app.appsets.R.string.data_sending_method,
            currentChoice = imMessageSendType,
            choices = sendTypeChoices,
            onChoiceClick = {
                AppSetsModuleSettings.get().onIMMessageDeliveryTypeChanged(it)
                imMessageSendType = it
            }
        )
    }
}


@Composable
fun <C> SingleChoiceInRowComponent(
    choiceTitle: Int,
    currentChoice: C,
    choices: List<Pair<C, Int>>,
    onChoiceClick: (C) -> Unit
) {
    Column {
        Text(
            text = stringResource(choiceTitle),
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        val widthDp = 120.dp * choices.size
        SingleChoiceSegmentedButtonRow(modifier = Modifier.width(widthDp)) {
            choices.forEachIndexed { index, choice ->
                SegmentedButton(
                    selected = choice.first == currentChoice,
                    onClick = {
                        onChoiceClick(choice.first)
                    },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = choices.size
                    ),
                ) {
                    Text(text = stringResource(id = choice.second))
                }
            }
        }
    }
}