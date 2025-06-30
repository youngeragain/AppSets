package xcj.app.appsets.ui.compose.main

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.server.model.UpdateCheckResult

@Composable
fun NewVersionSpace(
    modifier: Modifier = Modifier,
    updateCheckResult: UpdateCheckResult?,
    onDismissClick: () -> Unit,
    onDownloadClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        if (updateCheckResult != null) {
            Column(
                modifier = modifier.padding(
                    start = 12.dp,
                    top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding(),
                    end = 12.dp,
                    bottom = 12.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.shapes.extraLarge
                        )
                        .padding(12.dp)
                ) {
                    if (updateCheckResult.forceUpdate != true) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .clickable(onClick = onDismissClick)
                                .padding(12.dp),
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_close_24),
                            contentDescription = "close",
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "${stringResource(id = xcj.app.appsets.R.string.a_newer_version_available)}\n" +
                                    "${updateCheckResult.versionFromTo}\n" +
                                    "${updateCheckResult.publishDateTime}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp, color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Text(
                            text = stringResource(xcj.app.appsets.R.string.version_changes),
                            fontSize = 15.sp, color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = updateCheckResult.updateChangesHtml
                                ?: stringResource(xcj.app.appsets.R.string.not_provided),
                            fontSize = 15.sp, color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Box(Modifier.fillMaxWidth()) {
                            FilledTonalButton(
                                onClick = onDownloadClick,
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
