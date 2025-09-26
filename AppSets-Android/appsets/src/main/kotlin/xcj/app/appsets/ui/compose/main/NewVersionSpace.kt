package xcj.app.appsets.ui.compose.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.usecase.AppUpdateState
import xcj.app.starter.android.ktx.startWithHttpSchema

@Composable
fun NewVersionSpace(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val systemUseCase = LocalUseCaseOfSystem.current
    val appUpdateState by systemUseCase.appUpdateState

    Box(
        Modifier
            .fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = appUpdateState is AppUpdateState.Checked,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 20 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 20 })
        ) {
            val updateCheckResult = (appUpdateState as? AppUpdateState.Checked)?.updateCheckResult
            Column(
                modifier = modifier.padding(
                    start = 12.dp,
                    top = WindowInsets
                        .systemBars
                        .asPaddingValues()
                        .calculateTopPadding(),
                    end = 12.dp,
                    bottom = 12.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.extraLarge
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        )
                        .padding(12.dp)
                ) {

                    if (updateCheckResult?.forceUpdate != true) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .clickable(
                                    onClick = {
                                        systemUseCase.dismissNewVersionTips()
                                    }
                                )
                                .padding(12.dp),
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_close_24),
                            contentDescription = "close",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "${stringResource(id = xcj.app.appsets.R.string.a_newer_version_available)}\n" +
                                    "${updateCheckResult?.versionFromTo}\n" +
                                    "${updateCheckResult?.publishDateTime}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Text(
                            text = stringResource(xcj.app.appsets.R.string.version_changes),
                            fontSize = 15.sp
                        )
                        Text(
                            text = updateCheckResult?.updateChangesHtml
                                ?: stringResource(xcj.app.appsets.R.string.not_provided),
                            fontSize = 15.sp
                        )
                        Box(Modifier.fillMaxWidth()) {
                            FilledTonalButton(
                                onClick = {
                                    if (!updateCheckResult?.downloadUrl.startWithHttpSchema()) {
                                        return@FilledTonalButton
                                    }
                                    val uri =
                                        updateCheckResult?.downloadUrl?.toUri()
                                    if (uri == null) {
                                        return@FilledTonalButton
                                    }
                                    navigateToExternalWeb(context, uri)
                                },
                                modifier = Modifier.align(Alignment.CenterEnd)
                            ) {
                                Text(text = stringResource(xcj.app.appsets.R.string.download_updates))
                            }
                        }
                    }
                }
            }
        }
    }

}
