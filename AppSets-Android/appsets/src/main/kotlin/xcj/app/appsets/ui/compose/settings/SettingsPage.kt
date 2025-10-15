package xcj.app.appsets.ui.compose.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
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
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    var backActionBarSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val backActionsHeight by remember {
        derivedStateOf {
            with(density) {
                backActionBarSize.height.toDp()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .hazeSource(hazeState)
                .padding(start = 12.dp, end = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        )
        {
            Spacer(
                modifier = Modifier.height(
                    WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding() + backActionsHeight + 12.dp
                )
            )
            SessionSettingsComponent()

            PermissionAndPrivacyComponent(onPrivacyAndPermissionClick = onPrivacyAndPermissionClick)

            AboutSettingsComponent(onAboutClick = onAboutClick)
        }

        BackActionTopBar(
            modifier = Modifier.onPlaced {
                backActionBarSize = it.size
            },
            hazeState = hazeState,
            centerText = stringResource(xcj.app.appsets.R.string.settings),
            onBackClick = onBackClick
        )
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
    val coroutineScope = rememberCoroutineScope()
    val appSetsModuleSettings = remember {
        AppSetsModuleSettings.get()
    }
    var imMessageReliability by remember {
        mutableStateOf(appSetsModuleSettings.isBackgroundIMEnable)
    }
    val imMessageReliabilityChoices = remember {
        listOf(
            false to xcj.app.appsets.R.string.no,
            true to xcj.app.appsets.R.string.yes
        )
    }

    var imBubbleAlignment by remember {
        mutableStateOf(appSetsModuleSettings.imBubbleAlignment)
    }
    val bubbleAlignmentChoices = remember {
        listOf(
            AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_START_END to xcj.app.appsets.R.string.alignment_to_start_end,
            AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_START to xcj.app.appsets.R.string.alignment_all_to_start,
            AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_END to xcj.app.appsets.R.string.alignment_all_to_end
        )
    }
    var imMessageShowDate by remember {
        mutableStateOf(appSetsModuleSettings.isImMessageShowDate)
    }
    val showDateChoices = remember {
        listOf(
            true to xcj.app.appsets.R.string.show,
            false to xcj.app.appsets.R.string.hide
        )
    }
    var imMessageSendType by remember {
        mutableStateOf(appSetsModuleSettings.imMessageDeliveryType)
    }
    val sendTypeChoices = remember {
        listOf(
            AppSetsModuleSettings.IM_MESSAGE_DELIVERY_TYPE_RT to xcj.app.appsets.R.string.relay_delivery,
            AppSetsModuleSettings.IM_MESSAGE_DELIVERY_TYPE_DI to xcj.app.appsets.R.string.send_directly
        )
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(xcj.app.appsets.R.string.session_settings),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp),
            fontSize = 16.sp
        )
        SingleChoiceInRowComponent(
            choiceTitle = xcj.app.appsets.R.string.message_reliablity,
            choiceSubTitle = xcj.app.appsets.R.string.message_reliablity_tips,
            currentChoice = imMessageReliability,
            choices = imMessageReliabilityChoices,
            onChoiceClick = {
                imMessageReliability = it
                coroutineScope.launch {
                    appSetsModuleSettings.onIsIMMessageReliabilityChanged(it)
                }
            }
        )

        SingleChoiceInRowComponent(
            choiceTitle = xcj.app.appsets.R.string.chat_bubble_direction,
            currentChoice = imBubbleAlignment,
            choices = bubbleAlignmentChoices,
            onChoiceClick = {
                imBubbleAlignment = it
                coroutineScope.launch {
                    appSetsModuleSettings.onIMBubbleAlignmentChanged(it)
                }
            }
        )


        SingleChoiceInRowComponent(
            choiceTitle = xcj.app.appsets.R.string.show_time,
            currentChoice = imMessageShowDate,
            choices = showDateChoices,
            onChoiceClick = {
                imMessageShowDate = it
                coroutineScope.launch {
                    appSetsModuleSettings.onIsIMMessageShowDateChanged(it)
                }
            }
        )


        SingleChoiceInRowComponent(
            choiceTitle = xcj.app.appsets.R.string.data_sending_method,
            currentChoice = imMessageSendType,
            choices = sendTypeChoices,
            onChoiceClick = {
                imMessageSendType = it
                coroutineScope.launch {
                    appSetsModuleSettings.onIMMessageDeliveryTypeChanged(it)
                }
            }
        )
    }
}


@Composable
fun <C> SingleChoiceInRowComponent(
    choiceTitle: Int,
    choiceSubTitle: Int? = null,
    currentChoice: C,
    choices: List<Pair<C, Int>>,
    onChoiceClick: (C) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(choiceTitle),
            fontSize = 12.sp,
        )
        if (choiceSubTitle != null) {
            Text(
                text = stringResource(choiceSubTitle),
                fontSize = 10.sp,
            )
        }

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