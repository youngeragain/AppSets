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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.server.repository.QRCodeRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.ui.compose.LocalUseCaseOfQRCode
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.model.page_state.LoginPageUIState
import xcj.app.appsets.ui.model.state.QRCodeInfoScannedState
import xcj.app.appsets.usecase.NavigationUseCase
import xcj.app.appsets.usecase.QRCodeUseCase
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer

private const val TAG = "LoginPage"

@Preview(showBackground = true, widthDp = 600, heightDp = 1280)
@Composable
fun LoginPagePreview() {
    val configuration = LocalConfiguration.current
    configuration.orientation = Configuration.ORIENTATION_PORTRAIT
    val loginPageUIState by remember {
        mutableStateOf<LoginPageUIState>(LoginPageUIState.LoginStart())
    }
    val qrCodeUseCase = remember {
        QRCodeUseCase(QRCodeRepository.getInstance(), UserRepository.getInstance())
    }

    val navigationUseCase = remember {
        NavigationUseCase()
    }

    CompositionLocalProvider(
        LocalUseCaseOfQRCode provides qrCodeUseCase,
        LocalUseCaseOfNavigation provides navigationUseCase,
        LocalConfiguration provides configuration
    ) {
        LoginPage(
            loginPageUIState = loginPageUIState,
            generatedQRCodeInfo = null,
            onBackClick = {},
            onLoggingFinish = {},
            onSignUpClick = {},
            onQRCodeLoginButtonClick = {},
            onScanQRCodeButtonClick = {},
            onLoginConfirmClick = { account, password ->

            }
        )
    }
}

@Composable
fun LoginPage(
    loginPageUIState: LoginPageUIState,
    generatedQRCodeInfo: QRCodeInfoScannedState.AppSetsQRCodeInfo?,
    onBackClick: () -> Unit,
    onLoggingFinish: () -> Unit,
    onSignUpClick: () -> Unit,
    onQRCodeLoginButtonClick: () -> Unit,
    onScanQRCodeButtonClick: () -> Unit,
    onLoginConfirmClick: (String, String) -> Unit,
) {
    HideNavBar()
    val configuration = LocalConfiguration.current
    val qrCodeUseCase = LocalUseCaseOfQRCode.current
    LaunchedEffect(loginPageUIState) {
        if (loginPageUIState is LoginPageUIState.LoggingSuccess) {
            onLoggingFinish()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            qrCodeUseCase.onComposeDispose("page dispose")
        }
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
        VerticalOverscrollBox(modifier = Modifier.widthIn(max = TextFieldDefaults.MinWidth * 2)) {
            LoginComponent2(
                modifier = Modifier.fillMaxSize(),
                loginPageUIState = loginPageUIState,
                onSignUpClick = onSignUpClick,
                onLoginConfirmClick = onLoginConfirmClick
            )

            LoginComponent1(
                modifier = Modifier.fillMaxSize(),
                generatedQRCodeInfo = generatedQRCodeInfo,
                onQRCodeLoginClick = onQRCodeLoginButtonClick
            )

            LoginIndicator(loginPageUIState = loginPageUIState)

            BackActionTopBar(
                onBackClick = onBackClick
            )
        }
    }
}

@Composable
private fun LoginComponent2(
    modifier: Modifier,
    loginPageUIState: LoginPageUIState,
    onSignUpClick: () -> Unit,
    onLoginConfirmClick: (String, String) -> Unit,
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
            .padding(horizontal = 12.dp)
            .imePadding()
    ) {
        Column {
            StatusBarWithTopActionBarSpacer()
            Row {
                TextButton(
                    onClick = onSignUpClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.sign_up))
                }
            }
            Text(
                text = stringResource(id = xcj.app.appsets.R.string.login),
                fontSize = 138.sp
            )
        }
        Column(
            modifier = Modifier.align(Alignment.BottomStart),
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
                enabled = loginPageUIState is LoginPageUIState.LoginStart,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLoginConfirmClick(accountText, passwordText)
                }
            ) {
                Text(text = stringResource(id = xcj.app.starter.R.string.ok))
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LoginComponent1(
    modifier: Modifier,
    generatedQRCodeInfo: QRCodeInfoScannedState.AppSetsQRCodeInfo?,
    onQRCodeLoginClick: () -> Unit,
) {
    val qrcodeState = generatedQRCodeInfo?.state
    Box(
        modifier = modifier
    ) {
        AnimatedVisibility(visible = !qrcodeState.isNullOrEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(tween())
                    .padding(vertical = 12.dp)
            ) {
                when (qrcodeState) {
                    QRCodeUseCase.QR_STATE_NEW -> {
                        val qrCodeBitmap = generatedQRCodeInfo.bitmap?.asImageBitmap()
                        if (qrCodeBitmap != null) {
                            AnyImage(
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        MaterialTheme.shapes.extraLarge
                                    )
                                    .clip(MaterialTheme.shapes.extraLarge),
                                model = qrCodeBitmap
                            )
                        }
                    }

                    QRCodeUseCase.QR_STATE_NO_EXIST_OR_EXPIRED -> {
                        TextButton(onClick = onQRCodeLoginClick) {
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
private fun LoginIndicator(loginPageUIState: LoginPageUIState) {
    AnimatedVisibility(
        visible = loginPageUIState is LoginPageUIState.Logging,
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