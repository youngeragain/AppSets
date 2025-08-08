package xcj.app.appsets.ui.compose.login

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.LocalUseCaseOfQRCode
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.DesignBottomBackButton
import xcj.app.appsets.ui.compose.custom_component.HideNavBarWhenOnLaunch
import xcj.app.appsets.ui.model.LoginSignUpState
import xcj.app.appsets.usecase.QRCodeInfoScannedState
import xcj.app.appsets.usecase.QRCodeUseCase
import xcj.app.compose_share.components.DesignTextField

private const val TAG = "LoginPage"

@Composable
fun LoginPage(
    onBackClick: () -> Unit,
    loginSignUpState: LoginSignUpState,
    qrCodeInfo: QRCodeInfoScannedState.AppSetsQRCodeInfo?,
    onLoggingFinish: () -> Unit,
    onSignUpButtonClick: () -> Unit,
    onQRCodeLoginButtonClick: () -> Unit,
    onScanQRCodeButtonClick: () -> Unit,
    onLoginConfirmButtonClick: (String, String) -> Unit,
) {
    HideNavBarWhenOnLaunch()
    LaunchedEffect(loginSignUpState) {
        if (loginSignUpState is LoginSignUpState.LoggingFinish) {
            onLoggingFinish()
        }
    }
    val qrCodeUseCase = LocalUseCaseOfQRCode.current
    DisposableEffect(Unit) {
        onDispose {
            qrCodeUseCase.onComposeDispose("page dispose")
        }
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val configuration = LocalConfiguration.current
        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            LoginComponent1(
                modifier = Modifier,
                qrCodeInfo = qrCodeInfo,
                onSignUpButtonClick = onSignUpButtonClick,
                onQRCodeLoginButtonClick = onQRCodeLoginButtonClick,
                onScanQRCodeButtonClick = onScanQRCodeButtonClick
            )
            LoginComponent2(
                modifier = Modifier.align(Alignment.BottomCenter),
                onBackClick = onBackClick,
                onLoginConfirmButtonClick = onLoginConfirmButtonClick
            )
        } else {
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    LoginComponent1(
                        modifier = Modifier,
                        qrCodeInfo = qrCodeInfo,
                        onSignUpButtonClick = onSignUpButtonClick,
                        onQRCodeLoginButtonClick = onQRCodeLoginButtonClick,
                        onScanQRCodeButtonClick = onScanQRCodeButtonClick
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                ) {
                    LoginComponent2(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onBackClick = onBackClick,
                        onLoginConfirmButtonClick = onLoginConfirmButtonClick
                    )
                }
            }
        }

        LoginIndicator(loginSignUpState = loginSignUpState)
    }
}

@Composable
fun LoginComponent2(
    modifier: Modifier,
    onBackClick: () -> Unit,
    onLoginConfirmButtonClick: (String, String) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    var accountText by remember {
        mutableStateOf("")
    }
    var passwordText by remember {
        mutableStateOf("")
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .imePadding()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DesignTextField(
                value = accountText,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                onValueChange = {
                    accountText = it
                },
                placeholder = {
                    Text(stringResource(id = xcj.app.appsets.R.string.account))
                })

            DesignTextField(
                value = passwordText,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                onValueChange = {
                    passwordText = it
                },
                placeholder = {
                    Text(stringResource(id = xcj.app.appsets.R.string.password))
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TextFieldDefaults.MinHeight),
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLoginConfirmButtonClick(accountText, passwordText)
                }
            ) {
                Text(text = stringResource(id = xcj.app.starter.R.string.ok))
            }
            Spacer(Modifier.height(12.dp))
            DesignBottomBackButton(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                onClick = onBackClick
            )
        }
    }
}

@Composable
fun LoginComponent1(
    modifier: Modifier,
    qrCodeInfo: QRCodeInfoScannedState.AppSetsQRCodeInfo?,
    onSignUpButtonClick: () -> Unit,
    onQRCodeLoginButtonClick: () -> Unit,
    onScanQRCodeButtonClick: () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = 12.dp)
            .animateContentSize(tween())
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row {
            TextButton(
                onClick = onSignUpButtonClick,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text(text = stringResource(id = xcj.app.appsets.R.string.sign_up))
            }
            TextButton(
                onClick = onQRCodeLoginButtonClick,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.login_with_qr_code))
            }
            TextButton(
                onClick = onScanQRCodeButtonClick,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.scan_qr_code))
            }
        }
        Text(
            text = stringResource(id = xcj.app.appsets.R.string.login),
            fontSize = 138.sp
        )
        val qrcodeState = qrCodeInfo?.state
        if (!qrcodeState.isNullOrEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(tween())
                    .padding(vertical = 12.dp)
            ) {
                when (qrcodeState) {
                    QRCodeUseCase.QR_STATE_NEW -> {
                        val qrCodeBitmap = qrCodeInfo.bitmap?.asImageBitmap()
                        if (qrCodeBitmap != null) {
                            AnyImage(
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        MaterialTheme.shapes.extraLarge
                                    )
                                    .clip(MaterialTheme.shapes.extraLarge),
                                any = qrCodeBitmap
                            )
                        }
                    }

                    QRCodeUseCase.QR_STATE_NO_EXIST_OR_EXPIRED -> {
                        TextButton(onClick = onQRCodeLoginButtonClick) {
                            Text(text = stringResource(xcj.app.appsets.R.string.invalid_regenerate))
                        }
                    }

                    QRCodeUseCase.QR_STATE_SCANNED -> {
                        stringResource(xcj.app.appsets.R.string.scanned)
                    }

                    QRCodeUseCase.QR_STATE_CONFIRMED -> {
                        stringResource(xcj.app.appsets.R.string.confirmed)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginIndicator(loginSignUpState: LoginSignUpState) {
    AnimatedVisibility(
        visible = loginSignUpState is LoginSignUpState.Logging,
        enter = fadeIn(tween()) + scaleIn(
            tween(),
            2f
        ),
        exit = fadeOut(tween()) + scaleOut(
            tween(),
            0.2f
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .padding(vertical = 12.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        modifier = Modifier.size(68.dp),
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                        contentDescription = null
                    )
                    Text(stringResource(xcj.app.appsets.R.string.logging_in), fontSize = 12.sp)
                }

            }
        }
    }
}