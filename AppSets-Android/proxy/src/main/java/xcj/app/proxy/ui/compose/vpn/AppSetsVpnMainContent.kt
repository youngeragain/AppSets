@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package xcj.app.proxy.ui.compose.vpn

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xcj.app.compose_share.components.DesignTextField

@Composable
fun AppSetsVpnMainContent(
    onConnectButtonClick: (Boolean) -> Unit,
) {
    val pagerState = rememberPagerState(0) { 2 }
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState
        ) { index ->
            when (index) {
                0 -> {
                    VpnSwitcherPage(
                        onConnectButtonClick = onConnectButtonClick
                    )
                }

                else -> {
                    VpnConfigurationPage()
                }
            }
        }
        SingleChoiceSegmentedButtonRow() {
            SegmentedButton(
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 4.dp),
                    text = "Switch",
                    maxLines = 1
                )
            }
            SegmentedButton(
                selected = pagerState.currentPage == 1,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 4.dp),
                    text = "Configuration",
                    maxLines = 1
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
fun VpnSwitcherPage(onConnectButtonClick: (Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        var showHelp by remember {
            mutableStateOf(false)
        }
        FilledTonalButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = {
                showHelp = !showHelp
            }
        ) {
            Image(
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_help_outline_24),
                contentDescription = null
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var isChecked by remember {
                mutableStateOf(false)
            }
            AnimatedContent(targetState = isChecked) { tagetIsChecked ->
                if (tagetIsChecked) {
                    Text("Disconnect", fontSize = 52.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("Connect", fontSize = 52.sp, fontWeight = FontWeight.Bold)
                }
            }

            Switch(
                checked = isChecked,
                onCheckedChange = {
                    isChecked = it
                    onConnectButtonClick(it)
                }
            )
        }

        if (showHelp) {
            ModalBottomSheet(
                onDismissRequest = {
                    showHelp = false
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Build from ToyVpn, more information see:\nhttps://android.googlesource.com/platform/development/+/master/samples/ToyVpn",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

}

@Composable
fun VpnConfigurationPage() {
    val viewModel = viewModel<AppSetsVpnViewModel>()
    val appSetsVpnData by viewModel.appsetsVpnData
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(horizontal = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Server Address")
            DesignTextField(
                value = appSetsVpnData.serverAddress,
                onValueChange = {
                    val newAppSetsVpnData = appSetsVpnData.copy(serverAddress = it)
                    viewModel.updateAppsetsVpnData(newAppSetsVpnData)
                }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Server Post")
            DesignTextField(
                value = appSetsVpnData.serverPort,
                onValueChange = {
                    val newAppSetsVpnData = appSetsVpnData.copy(serverPort = it)
                    viewModel.updateAppsetsVpnData(newAppSetsVpnData)
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Shared Secret")
            DesignTextField(
                value = appSetsVpnData.sharedSecret,
                onValueChange = {
                    val newAppSetsVpnData = appSetsVpnData.copy(sharedSecret = it)
                    viewModel.updateAppsetsVpnData(newAppSetsVpnData)
                }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("HTTP proxy hostname")
            DesignTextField(
                value = appSetsVpnData.httpProxyHostname,
                onValueChange = {
                    val newAppSetsVpnData = appSetsVpnData.copy(httpProxyHostname = it)
                    viewModel.updateAppsetsVpnData(newAppSetsVpnData)
                }
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("HTTP proxy port")
            DesignTextField(
                value = appSetsVpnData.httpProxyPort,
                onValueChange = {
                    val newAppSetsVpnData = appSetsVpnData.copy(httpProxyPort = it)
                    viewModel.updateAppsetsVpnData(newAppSetsVpnData)
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Packages(comma separated)")
            DesignTextField(
                value = appSetsVpnData.packagesCommaSeparated,
                onValueChange = {
                    val newAppSetsVpnData = appSetsVpnData.copy(packagesCommaSeparated = it)
                    viewModel.updateAppsetsVpnData(newAppSetsVpnData)
                }
            )
        }

        val radioOptions = listOf(
            "Allow",
            "Disallow",
            "None"
        )
        val (selectedOptionText, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            radioOptions.forEach { radioOptionText ->
                Row(
                    modifier = Modifier
                        .selectable(
                            selected = (radioOptionText == selectedOptionText),
                            onClick = {
                                onOptionSelected(radioOptionText)
                                val newAppSetsVpnData =
                                    appSetsVpnData.copy(packagesCommaSeparatedAllowed = radioOptionText)
                                viewModel.updateAppsetsVpnData(newAppSetsVpnData)
                            },
                            role = Role.RadioButton
                        )
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(selected = radioOptionText == selectedOptionText, onClick = null)
                    Text(text = radioOptionText)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppSetsVpnMainContentPreView() {
    AppSetsVpnMainContent({})
}