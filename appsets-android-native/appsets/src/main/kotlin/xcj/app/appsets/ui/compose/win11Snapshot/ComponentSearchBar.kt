package xcj.app.appsets.ui.compose.win11Snapshot

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ktx.isHttpUrl
import xcj.app.appsets.ktx.toast
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.ui.compose.PageRouteNameProvider
import xcj.app.appsets.ui.compose.settings.LiteSettingsPanelDialog

@UnstableApi
@Composable
fun ComponentSearchBar(
    modifier: Modifier = Modifier,
    currentDestinationRoute: String,
    onSearchBarClick: () -> Unit,
    onSettingsUserNameClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsLoginClick: () -> Unit,
    onAddButtonClick: () -> Unit,
) {
    Column {
        val context = LocalContext.current
        val viewModel = viewModel<MainViewModel>(context as AppCompatActivity)
        val updateCheckResult = viewModel.appSetsUseCase.newVersionState.value
        AnimatedVisibility(
            visible = updateCheckResult != null &&
                    currentDestinationRoute == PageRouteNameProvider.Win11SnapShotPage
        ) {
            NewVersionBoxComponent(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(),
                updateCheckResult = updateCheckResult!!,
                onDismissClick = {
                    viewModel.appSetsUseCase.dismissNewVersionTips()
                }
            )
        }
        Row(modifier, verticalAlignment = Alignment.CenterVertically) {
            val isShowLiteSettingsPanel = remember {
                mutableStateOf(false)
            }
            if (isShowLiteSettingsPanel.value) {
                LiteSettingsPanelDialog(
                    isShowLiteSettingsPanel = isShowLiteSettingsPanel,
                    onSettingsUserNameClick = onSettingsUserNameClick,
                    onSettingsClick = onSettingsClick,
                    onSettingsLoginClick = onSettingsLoginClick
                )
            }
            Card(
                modifier = Modifier
                    .height(46.dp)
                    .weight(1f)
                    .clip(RoundedCornerShape(23.dp))
                    .clickable {
                        if (updateCheckResult?.forceUpdate == true)
                            "需要更新应用".toast()
                        else
                            onSearchBarClick()
                    },
                shape = RoundedCornerShape(23.dp)
            ) {
                Row(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        null
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("在此键入以搜索")
                }
            }
            Row {
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(14.dp)
                        )
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            if (updateCheckResult?.forceUpdate == true)
                                "需要更新应用".toast()
                            else
                                isShowLiteSettingsPanel.value = !isShowLiteSettingsPanel.value
                        }, contentAlignment = Alignment.Center
                ) {
                    val loginState by LocalAccountManager.provideState<Boolean>()
                    if (loginState) {
                        val userInfoState by LocalAccountManager.provideState<UserInfo>()
                        LocalOrRemoteImage(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            any = userInfoState.avatarUrl,
                            defaultColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    } else {
                        Icon(
                            painterResource(id = R.drawable.outline_face_24),
                            null,
                            modifier = Modifier
                                .size(32.dp)
                        )
                    }
                }
            }
            AnimatedVisibility(visible = currentDestinationRoute == PageRouteNameProvider.AppSetsCenterPage) {
                ComponentAddButton(Modifier, onClick = onAddButtonClick)
            }
        }
    }
}

@Composable
fun NewVersionBoxComponent(
    modifier: Modifier,
    updateCheckResult: UpdateCheckResult,
    onDismissClick: (() -> Unit)?,
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .heightIn(min = 280.dp)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(32.dp)
                )
                .padding(16.dp)

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
                        .clickable {
                            onDismissClick?.invoke()
                        }
                        .padding(12.dp),
                    painter = painterResource(id = R.drawable.ic_round_close_24),
                    contentDescription = "close",
                    tint = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Column {
                Text(
                    text = "有新版本可用\n" +
                            "${updateCheckResult.versionFromTo}\n" +
                            "${updateCheckResult.publishDateTime}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp, color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "版本变化",
                    fontSize = 15.sp, color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = updateCheckResult.updateChangesHtml ?: "没有提供",
                    fontSize = 15.sp, color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.weight(1f))
                Box(Modifier.fillMaxWidth(1f)) {
                    val context = LocalContext.current
                    Button(onClick = {
                        runCatching {
                            if (!updateCheckResult.downloadUrl.isHttpUrl())
                                return@runCatching
                            val uri =
                                Uri.parse(updateCheckResult.downloadUrl)
                            val downloadIntent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(downloadIntent)
                        }
                    }, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Text(text = "下载更新")
                    }
                }
            }
        }
    }
}
