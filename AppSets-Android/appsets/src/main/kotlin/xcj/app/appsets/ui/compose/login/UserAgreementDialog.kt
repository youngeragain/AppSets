package xcj.app.appsets.ui.compose.login

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.compose_share.components.ComposeViewProvider

class UserAgreementComposeViewProvider(
    private val onNextClick: (Boolean) -> Unit
) : ComposeViewProvider {
    override fun provideComposeView(context: Context): ComposeView {
        return ComposeView(context).apply {
            setContent {
                UserAgreementPopupPage(onNextClick)
            }
        }
    }
}

@Composable
fun UserAgreementPopupPage(onClick: (Boolean) -> Unit) {
    val hapticFeedback = LocalHapticFeedback.current
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row {
            Spacer(Modifier.weight(1f))
            FilledTonalButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick(true)

                }
            ) {
                Text(text = stringResource(id = xcj.app.appsets.R.string.continue_1))
            }
        }
        Text(text = "AppSets", fontSize = 32.sp, softWrap = true)
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.user_agreement_and_data_policy),
                fontSize = 24.sp,
                softWrap = true
            )

            Text(text = stringResource(id = xcj.app.appsets.R.string.user_agreement))
        }

    }
}