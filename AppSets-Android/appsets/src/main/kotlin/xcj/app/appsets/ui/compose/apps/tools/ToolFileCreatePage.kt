@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.apps.tools

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.quickstep.TextQuickStepContent
import xcj.app.compose_share.components.DesignTextField

@Composable
fun ToolFileCreatePage(
    quickStepContents: List<QuickStepContent>?,
    uri: Uri?,
    onBackClick: () -> Unit
) {
    HideNavBarWhenOnLaunch()
    var content by remember {
        mutableStateOf("")
    }
    var isShowFileNameInputSheet by remember {
        mutableStateOf(false)
    }
    var newFileName by remember {
        mutableStateOf("")
    }
    LaunchedEffect(true) {
        val firstOrNull = quickStepContents?.firstOrNull()
        if (firstOrNull != null && firstOrNull is TextQuickStepContent) {
            content = firstOrNull.text
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxSize(),
            value = content,
            onValueChange = {
                content = it
            }
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FilledTonalButton(
                enabled = content.isNotEmpty(),
                onClick = {
                    isShowFileNameInputSheet = true
                }
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.save))
            }
        }

        DesignBackButton(
            modifier = Modifier.align(Alignment.BottomStart),
            onClick = onBackClick
        )
    }

    if (isShowFileNameInputSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                isShowFileNameInputSheet = false
            }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.new_file))
                DesignTextField(
                    value = newFileName,
                    onValueChange = {
                        newFileName = it
                    },
                    placeholder = {
                        Text(text = stringResource(xcj.app.appsets.R.string.name))
                    }
                )
                FilledTonalButton(
                    modifier = Modifier.widthIn(min = TextFieldDefaults.MinWidth),
                    onClick = {
                        isShowFileNameInputSheet = false
                    }
                ) {
                    Text(text = stringResource(xcj.app.appsets.R.string.ok))
                }
            }
        }
    }

}