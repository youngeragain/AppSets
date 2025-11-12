package xcj.app.appsets.ui.compose.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ui.compose.custom_component.DragValue
import xcj.app.appsets.ui.compose.custom_component.ImageButtonComponent
import xcj.app.appsets.ui.compose.custom_component.SwipeContainer
import xcj.app.appsets.ui.compose.search.LocalAccountUserAvatar
import xcj.app.appsets.ui.model.state.AccountStatus
import xcj.app.appsets.ui.model.state.QRCodeInfoScannedState
import xcj.app.appsets.usecase.QRCodeUseCase
import xcj.app.appsets.usecase.SystemUseCase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiteSettingsSheetContent(
    qrCodeInfo: QRCodeInfoScannedState?,
    onBioClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsLoginClick: () -> Unit,
    onGenQRCodeClick: () -> Unit,
    onToScanQRCodeClick: () -> Unit,
    onQRCodeConfirmClick: () -> Unit,
) {
    val context = LocalContext.current
    val loginStatusState by LocalAccountManager.accountStatus
    var isShowSwapToLogout by remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(
                start = 12.dp, end = 12.dp, bottom = 48.dp
            )
            .animateContentSize(tween())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1f)
                .verticalScroll(rememberScrollState())
                .animateContentSize(tween()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val packageVersionName =
                SystemUseCase.getAppSetsPackageVersionName(context)
            if (!packageVersionName.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(
                            id = xcj.app.appsets.R.string.version_x,
                            packageVersionName
                        ),
                        fontSize = 12.sp
                    )
                }
            }

            if (loginStatusState is AccountStatus.Logged) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(tween()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val qrCode = (qrCodeInfo as? QRCodeInfoScannedState.AppSetsQRCodeInfo)
                    when (qrCode?.state) {
                        QRCodeUseCase.QR_STATE_NEW -> {
                            val qrCodeBitmap = qrCode.bitmap?.asImageBitmap()
                            if (qrCodeBitmap != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = qrCodeBitmap,
                                    modifier = Modifier
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            MaterialTheme.shapes.extraLarge
                                        )
                                        .clip(MaterialTheme.shapes.extraLarge),
                                    contentDescription = null
                                )
                            }
                        }

                        QRCodeUseCase.QR_STATE_SCANNED -> {
                            Box(Modifier.padding(16.dp)) {
                                FilledTonalButton(
                                    onClick = {
                                        onQRCodeConfirmClick()
                                    }
                                ) {
                                    Text(text = stringResource(xcj.app.appsets.R.string.qrcode_scanned_login_tips))
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(
                        onClick = onBioClick
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LocalAccountUserAvatar(
                    onClick = {}
                )
                val name = if (loginStatusState is AccountStatus.Logged) {
                    loginStatusState.userInfo.bioName
                        ?: stringResource(xcj.app.appsets.R.string.jkx)
                } else {
                    stringResource(xcj.app.appsets.R.string.login_to_appsets)
                }
                Text(text = name)
            }
            if (loginStatusState is AccountStatus.Logged) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable(onClick = {
                            onGenQRCodeClick()
                        })
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ImageButtonComponent(
                        useImage = false,
                        resource = xcj.app.compose_share.R.drawable.ic_outline_qr_code_24,
                    )
                    Text(text = stringResource(xcj.app.appsets.R.string.mine_qr_code))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable(
                            onClick = {
                                onToScanQRCodeClick()
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ImageButtonComponent(
                        useImage = false,
                        resource = xcj.app.compose_share.R.drawable.ic_outline_qr_code_scanner_24,
                    )
                    Text(text = stringResource(xcj.app.appsets.R.string.scan_qr_code))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = onSettingsClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImageButtonComponent(
                    useImage = false,
                    resource = xcj.app.compose_share.R.drawable.ic_settings_24,
                )
                Text(text = stringResource(xcj.app.appsets.R.string.app_settings))
            }

            AnimatedContent(
                targetState = isShowSwapToLogout
            ) { targetIsShowSwapToLogout ->
                if (targetIsShowSwapToLogout) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SwipeContainer(onDragValueChanged = { dragValue ->
                            if (dragValue == DragValue.End) {
                                onSettingsLoginClick()
                                isShowSwapToLogout = false
                            }
                        }) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_arrow_ios_24),
                                    modifier = Modifier.rotate(180f),
                                    contentDescription = null
                                )
                            }
                        }
                        Text(
                            stringResource(xcj.app.appsets.R.string.slide_to_logout),
                            fontSize = 10.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable(onClick = {
                                if (LocalAccountManager.isLogged()) {
                                    isShowSwapToLogout = true
                                } else {
                                    onSettingsLoginClick()
                                }
                            })
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ImageButtonComponent(
                            useImage = false,
                            resource = null,
                        )
                        val loginOrLogOutTextRes =
                            if (loginStatusState is AccountStatus.Logged) {
                                xcj.app.appsets.R.string.logout
                            } else {
                                xcj.app.appsets.R.string.login_or_signup
                            }
                        Text(text = stringResource(loginOrLogOutTextRes))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}