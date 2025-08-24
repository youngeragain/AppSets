@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

import android.graphics.Bitmap
import android.net.wifi.p2p.WifiP2pDevice
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.compose_share.components.BottomSheetContainer
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.LocalAnyStateProvider
import xcj.app.compose_share.modifier.combinedClickableSingle
import xcj.app.share.base.DataContent
import xcj.app.share.base.ShareDevice
import xcj.app.share.http.HttpShareMethod
import xcj.app.share.http.common.ServerBootStateInfo
import xcj.app.share.rpc.RpcShareMethod
import xcj.app.share.ui.compose.AppSetsShareActivity
import xcj.app.share.ui.compose.AppSetsShareViewModel
import xcj.app.share.wlanp2p.WlanP2pShareMethod
import xcj.app.share.wlanp2p.base.P2pShareDevice
import xcj.app.starter.util.QrCodeUtil

data class BoxFocusInfo(
    val receiveBoxFocus: Boolean = false,
    val devicesBoxFocus: Boolean = false,
    val sendBoxFocus: Boolean = false
) {
    fun isAllNotFocus(): Boolean {
        return !receiveBoxFocus && !devicesBoxFocus && !sendBoxFocus
    }
}

@Composable
fun AppSetsShareMainContent(
    onBackClick: () -> Unit,
    onDiscoveryClick: () -> Unit,
    onCloseEstablishCLick: () -> Unit,
    onShareDeviceClick: (ShareDevice, Int) -> Unit,
    onAddFileContentClick: () -> Unit,
    onAddTextContentClick: (String) -> Unit,
    onContentViewClick: (DataContent) -> Unit,
    onScanClick: () -> Unit
) {
    val viewModel = viewModel<AppSetsShareViewModel>()
    CompositionLocalProvider(
        LocalAnyStateProvider provides viewModel
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppSetsShareContainer(
                receivedSpaceContent = {
                    ReceivedSpace(
                        onContentViewClick = onContentViewClick
                    )
                },

                devicesSpaceContent = {
                    DevicesSpace(
                        onDiscoveryClick = onDiscoveryClick,
                        onCloseEstablishCLick = onCloseEstablishCLick,
                        onShareDeviceClick = onShareDeviceClick,
                        onScanClick = onScanClick
                    )
                },

                sendSpaceContent = {
                    SendSpace(
                        onBackClick = onBackClick,
                        onAddFileContentClick = onAddFileContentClick,
                        onAddTextContentClick = onAddTextContentClick
                    )
                }
            )
            BottomSheetContainer()
        }
    }

}

@Composable
fun AppSetsShareContainer(
    modifier: Modifier = Modifier,
    receivedSpaceContent: @Composable () -> Unit,
    devicesSpaceContent: @Composable () -> Unit,
    sendSpaceContent: @Composable () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val viewModel = viewModel<AppSetsShareViewModel>()
    val mShareDevice by viewModel.mShareDeviceState

    val receivedContentList = viewModel.receivedContentList
    val receiveDataProgress by viewModel.receiveDataProgressState
    val shareDeviceList by viewModel.shareDeviceListState

    var boxSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var boxFocusInfo by viewModel.boxFocusInfo

    var isFirstIn by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {
        isFirstIn = false
    }

    val receiveBoxHeight by remember {
        derivedStateOf {
            with(density) {
                if (boxFocusInfo.receiveBoxFocus) {
                    (boxSize.height).toDp().times(0.7f)
                } else if (!boxFocusInfo.devicesBoxFocus && !boxFocusInfo.sendBoxFocus) {
                    (boxSize.height).toDp().times(0.333f)
                } else {
                    (boxSize.height).toDp().times(0.15f)
                }
            }
        }
    }
    val devicesBoxHeight by remember {
        derivedStateOf {
            with(density) {
                if (boxFocusInfo.devicesBoxFocus) {
                    (boxSize.height).toDp().times(0.7f)
                } else if (!boxFocusInfo.receiveBoxFocus && !boxFocusInfo.sendBoxFocus) {
                    (boxSize.height).toDp().times(0.333f)
                } else {
                    (boxSize.height).toDp().times(0.15f)
                }
            }
        }
    }
    val sendBoxHeight by remember {
        derivedStateOf {
            with(density) {
                if (boxFocusInfo.sendBoxFocus) {
                    (boxSize.height).toDp().times(0.7f)
                } else if (!boxFocusInfo.receiveBoxFocus && !boxFocusInfo.devicesBoxFocus) {
                    (boxSize.height).toDp().times(0.333f)
                } else {
                    (boxSize.height).toDp().times(0.15f)
                }
            }
        }
    }

    val receiveBoxHeightState = if (isFirstIn) {
        receiveBoxHeight
    } else {
        animateDpAsState(
            targetValue = receiveBoxHeight,
            animationSpec = spring(
                0.75f,
                155f
            )
        ).value
    }

    val devicesBoxHeightState = if (isFirstIn) {
        devicesBoxHeight
    } else {
        animateDpAsState(
            targetValue = devicesBoxHeight,
            animationSpec = spring(
                0.75f,
                155f
            )
        ).value
    }
    val sendBoxHeightState = if (isFirstIn) {
        sendBoxHeight
    } else {
        animateDpAsState(
            targetValue = sendBoxHeight,
            animationSpec = spring(
                0.75f,
                155f
            )
        ).value
    }

    LaunchedEffect(boxFocusInfo) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    LaunchedEffect(receiveDataProgress) {
        if (
            (receiveDataProgress != null)
        ) {
            boxFocusInfo = BoxFocusInfo(receiveBoxFocus = true)
        }
    }

    LaunchedEffect(receivedContentList) {
        if (
            (receivedContentList.isNotEmpty())
        ) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            boxFocusInfo = BoxFocusInfo(receiveBoxFocus = true)
        }
    }
    LaunchedEffect(shareDeviceList) {
        if (
            (shareDeviceList.isNotEmpty() && !boxFocusInfo.receiveBoxFocus && !boxFocusInfo.sendBoxFocus)
        ) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            boxFocusInfo = BoxFocusInfo(devicesBoxFocus = true)
        }
    }
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxSize()
            .statusBarsPadding()
            .displayCutoutPadding()
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onSizeChanged {
                    boxSize = it
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(receiveBoxHeightState)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            boxFocusInfo =
                                BoxFocusInfo(receiveBoxFocus = !boxFocusInfo.receiveBoxFocus)
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.shapes.extraLarge
                            )
                            .clip(MaterialTheme.shapes.extraLarge)
                    ) {
                        receivedSpaceContent()
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(devicesBoxHeightState)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            boxFocusInfo =
                                BoxFocusInfo(devicesBoxFocus = !boxFocusInfo.devicesBoxFocus)
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.shapes.extraLarge
                            )
                            .clip(MaterialTheme.shapes.extraLarge)
                    ) {
                        devicesSpaceContent()
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sendBoxHeightState)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {
                            boxFocusInfo = BoxFocusInfo(
                                sendBoxFocus = !boxFocusInfo.sendBoxFocus
                            )
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.shapes.extraLarge
                            )
                            .clip(MaterialTheme.shapes.extraLarge)
                    ) {
                        sendSpaceContent()
                    }
                }
            }
        }
    }
}

@Composable
fun DevicesSpace(
    onDiscoveryClick: () -> Unit,
    onCloseEstablishCLick: () -> Unit,
    onShareDeviceClick: (ShareDevice, Int) -> Unit,
    onScanClick: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val appSetsShareActivity = context as AppSetsShareActivity
    val viewModel = viewModel<AppSetsShareViewModel>()
    val isDiscovering by viewModel.isDiscoveringState
    val shareDeviceList by viewModel.shareDeviceListState
    val sendProgress by viewModel.sendDataProgressState
    val boxFocusInfo by viewModel.boxFocusInfo
    val shareMethodType by viewModel.shareMethodTypeState
    val shareDevice by viewModel.mShareDeviceState

    val coroutineScope = rememberCoroutineScope()

    var isShowSettings by remember {
        mutableStateOf(false)
    }
    var isShowDeviceContentList by remember {
        mutableStateOf(false)
    }
    var whichIsShowDeviceContentList: ShareDevice? by remember {
        mutableStateOf(null)
    }

    var isShowQrCode by remember {
        mutableStateOf(false)
    }

    var qrCodeBitmap: Bitmap? by remember {
        mutableStateOf(null)
    }

    var shouldShowTips by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(isDiscovering, shareDeviceList) {
        coroutineScope.launch {
            delay(5000)
            shouldShowTips = shareDeviceList.isEmpty()
        }
    }

    LaunchedEffect(isShowQrCode) {
        if (!isShowQrCode) {
            return@LaunchedEffect
        }
        val shareMethod = appSetsShareActivity.getShareMethod()
        if (shareMethod !is HttpShareMethod) {
            return@LaunchedEffect
        }
        val serverBootStateInfo = shareMethod.serverBootStateInfoState.value
        if (serverBootStateInfo !is ServerBootStateInfo.Booted) {
            return@LaunchedEffect
        }
        coroutineScope.launch {
            val shareQrCodeContent = serverBootStateInfo.availableDeviceIp.joinToString(
                separator = ":",
                prefix = "asqr:appsets_share:"
            ) { it.ip }
            qrCodeBitmap = QrCodeUtil.encodeAsBitmap(shareQrCodeContent)
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = boxFocusInfo.devicesBoxFocus,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(68.dp),
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 12.dp
                )
            ) {
                items(items = shareDeviceList) { shareDevice ->
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.large)
                            .combinedClickableSingle(
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onShareDeviceClick(
                                        shareDevice,
                                        ShareDevice.CLICK_TYPE_NORMAL
                                    )
                                },
                                onLongClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onShareDeviceClick(
                                        shareDevice,
                                        ShareDevice.CLICK_TYPE_LONG
                                    )
                                    if (shareMethodType == HttpShareMethod::class.java) {
                                        isShowDeviceContentList = true
                                        whichIsShowDeviceContentList = shareDevice
                                    }
                                },
                                onDoubleClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onShareDeviceClick(
                                        shareDevice,
                                        ShareDevice.CLICK_TYPE_DOUBLE
                                    )
                                }
                            )
                            .animateItem(
                                fadeInSpec = tween()
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        ShareDeviceNormalComponent(
                            modifier = Modifier,
                            shareDevice = shareDevice
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = boxFocusInfo.devicesBoxFocus && shareDeviceList.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(xcj.app.share.R.string.no_devices))
            }
        }
        AnimatedVisibility(
            visible = !boxFocusInfo.devicesBoxFocus,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val rotate = if (isDiscovering) {
                    720f
                } else {
                    0f
                }
                val rotateSate by animateFloatAsState(
                    targetValue = rotate,
                    animationSpec = tween(3000)
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onDiscoveryClick)
                            .padding(12.dp)
                            .animateContentSize(alignment = Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                modifier = Modifier.rotate(rotateSate),
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_refresh_24),
                                contentDescription = null
                            )
                            val tipsText = if (shareDeviceList.isEmpty()) {
                                stringResource(xcj.app.share.R.string.devices)
                            } else {
                                "${stringResource(xcj.app.share.R.string.devices)}(${shareDeviceList.size})"
                            }
                            Text(text = tipsText)
                        }
                    }
                    Text(
                        text = shareDevice.deviceName.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (shareMethodType == HttpShareMethod::class.java) {
                        val httpShareMethod =
                            appSetsShareActivity.getShareMethod() as? HttpShareMethod
                        if (httpShareMethod != null) {
                            val serverBootStateInfo =
                                httpShareMethod.serverBootStateInfoState.value
                            if (serverBootStateInfo is ServerBootStateInfo.Booted) {
                                Text(
                                    text = serverBootStateInfo.availableIPSuffixesForDevice,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = boxFocusInfo.isAllNotFocus() && shareDeviceList.isEmpty() && shouldShowTips,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    text = stringResource(xcj.app.share.R.string.share_device_connect_tips),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 12.dp)
                .animateContentSize(animationSpec = tween()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (shareMethodType == HttpShareMethod::class.java) {
                Icon(
                    modifier = Modifier
                        .clickable(
                            onClick = onScanClick
                        )
                        .padding(12.dp),
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_outline_qr_code_scanner_24),
                    contentDescription = null
                )
                Icon(
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                isShowQrCode = !isShowQrCode
                            }
                        )
                        .padding(12.dp),
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_outline_qr_code_24),
                    contentDescription = null
                )
            }
            Icon(
                modifier = Modifier
                    .clickable(
                        onClick = {
                            isShowSettings = true
                        }
                    )
                    .padding(12.dp),
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_settings_24),
                contentDescription = null
            )
        }
        AnimatedVisibility(
            visible = isShowQrCode,
            enter = fadeIn() + scaleIn(initialScale = 1.5f),
            exit = fadeOut() + scaleOut(targetScale = 1.5f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = qrCodeBitmap,
                    modifier = Modifier
                        .size(98.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                    contentDescription = null
                )
            }
        }

        SettingsSheet(
            isShow = isShowSettings,
            onDismissRequest = {
                isShowSettings = false
            }
        )
        DeviceContentListSheet(
            isShow = isShowDeviceContentList,
            shareDevice = whichIsShowDeviceContentList,
            onDismissRequest = {
                isShowDeviceContentList = false
                whichIsShowDeviceContentList = null
            }
        )
    }
}

@Composable
fun DeviceContentListSheet(
    isShow: Boolean,
    shareDevice: ShareDevice?,
    onDismissRequest: () -> Unit,
) {

    if (isShow && shareDevice != null) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = onDismissRequest
        ) {
            DeviceContentListComponent(
                shareDevice = shareDevice,
                isShowBackButton = false
            )
        }
    }
}

@Composable
fun DeviceContentListComponent(
    shareDevice: ShareDevice,
    isShowBackButton: Boolean,
    onBackClick: (() -> Unit)? = null
) {
    val viewModel = viewModel<AppSetsShareViewModel>()
    val deviceContentListMap = viewModel.deviceContentListMap
    val contentInfoList = deviceContentListMap[shareDevice]
    Column(
        modifier = Modifier
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
            .fillMaxWidth()
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isShowBackButton) {
                Icon(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .clickable(
                            onClick = {
                                onBackClick?.invoke()
                            }
                        )
                        .align(Alignment.TopStart),
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_arrow_back_24),
                    contentDescription = null
                )
            }
            Column(
                modifier = Modifier.align(Alignment.TopCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val titleText = if (contentInfoList?.infoList.isNullOrEmpty()) {
                    stringResource(xcj.app.share.R.string.share_content_list)
                } else {
                    "${stringResource(xcj.app.share.R.string.share_content_list)}(${(contentInfoList.infoList.size)})"
                }
                Text(
                    text = titleText,
                    fontWeight = FontWeight.Bold
                )
            }

        }
        Row {
            ShareDeviceSmallComponent(modifier = Modifier, shareDevice)
            Spacer(modifier = Modifier.weight(1f))
        }
        DesignHDivider()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            if (contentInfoList == null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(xcj.app.share.R.string.getting_in),
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                if (contentInfoList.infoList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(xcj.app.share.R.string.no_content),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                items(contentInfoList.infoList) { contentInfo ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            text = contentInfo.name,
                            fontSize = 12.sp,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        DesignHDivider()
                    }

                }
            }

        }
    }
}


@Composable
fun SettingsSheet(
    isShow: Boolean,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val appSetsShareActivity = context as AppSetsShareActivity
    val viewModel = viewModel<AppSetsShareViewModel>()
    val pendingSendContentList = viewModel.pendingSendContentList
    val mShareDevice by viewModel.mShareDeviceState
    val shareMethodType by viewModel.shareMethodTypeState
    val shareMethod by rememberUpdatedState(
        appSetsShareActivity.getShareMethod()
    )
    if (isShow) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = onDismissRequest
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(xcj.app.share.R.string.share_settings),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                DesignHDivider()
                Text(
                    text = stringResource(xcj.app.share.R.string.nike_name),
                    fontSize = 14.sp,
                )
                Text(
                    modifier = Modifier,
                    text = mShareDevice.deviceName.name,
                    fontSize = 14.sp,
                )
                DesignHDivider()
                Text(
                    text = stringResource(xcj.app.share.R.string.content_save_path),
                    fontSize = 14.sp,
                )
                Text(
                    modifier = Modifier,
                    text = shareMethod.shareContentLocationState.value,
                    fontSize = 14.sp,
                )
                DesignHDivider()
                Text(
                    text = stringResource(xcj.app.share.R.string.share_method),
                    fontSize = 14.sp,
                )
                val shareMethods = remember {
                    listOf(
                        WlanP2pShareMethod::class.java to WlanP2pShareMethod.NAME,
                        HttpShareMethod::class.java to HttpShareMethod.NAME,
                        RpcShareMethod::class.java to RpcShareMethod.NAME
                    )
                }
                val firstPageIndex = shareMethods.indexOfFirst { it.first == shareMethodType }
                val pagerState = rememberPagerState(firstPageIndex) { 3 }
                val coroutineScope = rememberCoroutineScope()
                SingleChoiceSegmentedButtonRow(modifier = Modifier.width(120.dp * 3)) {
                    shareMethods.forEachIndexed { index, shareMethod ->
                        SegmentedButton(
                            selected = shareMethodType == shareMethod.first,
                            onClick = {
                                appSetsShareActivity.updateShareMethod(shareMethod.first)
                                val pageIndex =
                                    shareMethods.indexOfFirst { it.first == shareMethod.first }
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pageIndex)
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = shareMethods.size
                            ),
                        ) {
                            Text(
                                modifier = Modifier.padding(vertical = 4.dp),
                                text = shareMethod.second,
                                maxLines = 1
                            )
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween()),
                    userScrollEnabled = false
                ) { index ->
                    when (shareMethods[index].first) {
                        WlanP2pShareMethod::class.java -> {
                            Box {
                                Column {
                                    Text(
                                        text = stringResource(xcj.app.share.R.string.appsets_share_wlanp2p_usage_tips),
                                        fontSize = 12.sp
                                    )
                                    val wlanP2pShareMethod =
                                        appSetsShareActivity.getShareMethod() as? WlanP2pShareMethod
                                    if (wlanP2pShareMethod == null) {
                                        return@Box
                                    }
                                    val wifiP2PEnableInfoState by wlanP2pShareMethod.wifiP2PEnableInfoState
                                    val p2pShareDevice =
                                        (mShareDevice as? P2pShareDevice)
                                    if (p2pShareDevice == null) {
                                        return@Box
                                    }
                                    if (!wifiP2PEnableInfoState.enable) {
                                        Text(
                                            text = wifiP2PEnableInfoState.reason ?: "",
                                            fontSize = 10.sp,
                                            color = Color.Red
                                        )
                                    }
                                }
                            }
                        }

                        HttpShareMethod::class.java -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val httpShareMethod =
                                    appSetsShareActivity.getShareMethod() as? HttpShareMethod
                                if (httpShareMethod == null) {
                                    return@Column
                                }
                                val serverBootStateInfo =
                                    httpShareMethod.serverBootStateInfoState.value


                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = stringResource(xcj.app.share.R.string.need_a_pin))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = httpShareMethod.isNeedPinState.value,
                                        onCheckedChange = {
                                            httpShareMethod.updateIsNeedPinState(it)
                                        }
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = stringResource(xcj.app.share.R.string.auto_accept))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = httpShareMethod.isAutoAcceptState.value,
                                        onCheckedChange = {
                                            httpShareMethod.updateIsAutoAcceptState(it)
                                        }
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = stringResource(xcj.app.share.R.string.prefer_download_self))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = httpShareMethod.isPreferDownloadSelfState.value,
                                        onCheckedChange = {
                                            httpShareMethod.updateIsPreferDownloadSelfState(it)
                                        }
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = stringResource(xcj.app.share.R.string.server_status))
                                    Spacer(modifier = Modifier.weight(1f))
                                    Switch(
                                        checked = serverBootStateInfo is ServerBootStateInfo.Booted,
                                        onCheckedChange = {
                                            httpShareMethod.toggleServer()
                                        }
                                    )
                                }

                                when (serverBootStateInfo) {
                                    is ServerBootStateInfo.NotBooted -> {
                                        Text(
                                            stringResource(xcj.app.share.R.string.server_is_not_boot),
                                            fontSize = 12.sp
                                        )
                                    }

                                    is ServerBootStateInfo.Booted -> {
                                        Column {
                                            Text(
                                                stringResource(xcj.app.share.R.string.server_booted),
                                                fontSize = 12.sp
                                            )
                                            val serverInfo =
                                                serverBootStateInfo.availableAddressInfo.joinToString(
                                                    "\n"
                                                ) { "${it}:${serverBootStateInfo.port}" }
                                            Text(serverInfo, fontSize = 12.sp)
                                        }
                                    }

                                    is ServerBootStateInfo.BootFailed -> {
                                        Text(
                                            stringResource(
                                                xcj.app.share.R.string.sever_boot_failed,
                                                serverBootStateInfo.reason ?: ""
                                            ),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        RpcShareMethod::class.java -> {

                        }

                    }
                }
                Spacer(modifier = Modifier.height(68.dp))
            }
        }
    }
}

@Composable
fun AppSetsShareActionsSpace(
    onBackClick: () -> Unit,
    onAddFileContentClick: () -> Unit,
    onAddTextContentClick: (String) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val appSetsShareActivity = context as AppSetsShareActivity
    val viewModel = viewModel<AppSetsShareViewModel>()
    val pendingSendContentList = viewModel.pendingSendContentList

    var inputContentText by remember {
        mutableStateOf("")
    }

    var isShowInputContentSheet by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .animateContentSize(animationSpec = tween()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clickable(onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAddFileContentClick()
                })
                .padding(12.dp),
            painter = painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
            contentDescription = null
        )
        Icon(
            modifier = Modifier
                .clickable(onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    inputContentText = ""
                    isShowInputContentSheet = true
                })
                .padding(12.dp),
            painter = painterResource(xcj.app.compose_share.R.drawable.ic_notes_24),
            contentDescription = null
        )
        if (pendingSendContentList.isNotEmpty()) {
            Icon(
                modifier = Modifier
                    .clickable(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        appSetsShareActivity.sendAll()
                    })
                    .padding(12.dp),
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_ios_share_24),
                contentDescription = null
            )
            Icon(
                modifier = Modifier
                    .clickable(onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.removeAllPendingSendContent()
                    })
                    .padding(12.dp),
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_delete_forever_24),
                contentDescription = null
            )
        }
    }

    if (isShowInputContentSheet) {
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            onDismissRequest = {
                isShowInputContentSheet = false
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    FilledTonalButton(onClick = {
                        isShowInputContentSheet = false
                        onAddTextContentClick(inputContentText)
                    }) {
                        Text(text = stringResource(xcj.app.starter.R.string.ok))
                    }
                }
                DesignTextField(
                    value = inputContentText,
                    onValueChange = {
                        inputContentText = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.share.R.string.appsets_share_send_tips),
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .height(350.dp)
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun SendSpace(
    onBackClick: () -> Unit,
    onAddFileContentClick: () -> Unit,
    onAddTextContentClick: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<AppSetsShareViewModel>()
    val sendDataProgress by viewModel.sendDataProgressState
    val boxFocusInfo by viewModel.boxFocusInfo
    val pendingSendContentList = viewModel.pendingSendContentList

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = sendDataProgress != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (sendDataProgress != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val contentName =
                            sendDataProgress?.name
                        if (!contentName.isNullOrEmpty()) {
                            Text(text = contentName, fontSize = 6.sp)
                        }
                        LinearProgressIndicator(
                            progress = {
                                (sendDataProgress?.percentage ?: 0f) / 100f
                            }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = boxFocusInfo.sendBoxFocus,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 48.dp
                    )
                ) {
                    itemsIndexed(items = pendingSendContentList) { index, dataContent ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            when (dataContent) {
                                is DataContent.StringContent -> {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "${index + 1}", fontSize = 10.sp)
                                        Icon(
                                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_notes_24),
                                            contentDescription = "text"
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = dataContent.content,
                                                fontSize = 12.sp,
                                                modifier = Modifier,
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            modifier = Modifier
                                                .clickable(onClick = {
                                                    viewModel.removePendingSendContent(dataContent)
                                                })
                                                .padding(12.dp),
                                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_delete_forever_24),
                                            contentDescription = null
                                        )
                                    }
                                }

                                is DataContent.FileContent -> {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "${index + 1}", fontSize = 10.sp)
                                        Icon(
                                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                                            contentDescription = "file"
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = dataContent.file.name,
                                                fontSize = 12.sp,
                                            )
                                        }
                                        Icon(
                                            modifier = Modifier
                                                .clickable(onClick = {
                                                    viewModel.removePendingSendContent(dataContent)
                                                })
                                                .padding(12.dp),
                                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_delete_forever_24),
                                            contentDescription = null
                                        )
                                    }
                                }

                                is DataContent.UriContent -> {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "${index + 1}", fontSize = 10.sp)
                                        Icon(
                                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                                            contentDescription = "file"
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = dataContent.androidUriFile?.displayName
                                                    ?: "",
                                                fontSize = 12.sp,
                                            )
                                        }
                                        Icon(
                                            modifier = Modifier
                                                .clickable(onClick = {
                                                    viewModel.removePendingSendContent(dataContent)
                                                })
                                                .padding(12.dp),
                                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_delete_forever_24),
                                            contentDescription = null
                                        )
                                    }
                                }

                                else -> {}
                            }

                            DesignHDivider()
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = pendingSendContentList.isEmpty() && boxFocusInfo.sendBoxFocus,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(xcj.app.share.R.string.content_here_can_send)
                )
            }
        }

        AnimatedVisibility(
            visible = !boxFocusInfo.sendBoxFocus,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(12.dp)
                        .animateContentSize(animationSpec = tween()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_call_made_24),
                            contentDescription = null
                        )
                        val tipsText = if (pendingSendContentList.isEmpty()) {
                            stringResource(xcj.app.share.R.string.send_space)
                        } else {
                            "${stringResource(xcj.app.share.R.string.send_space)}(${pendingSendContentList.size})"
                        }
                        Text(text = tipsText)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd),
        ) {
            AppSetsShareActionsSpace(
                onBackClick = onBackClick,
                onAddFileContentClick = onAddFileContentClick,
                onAddTextContentClick = onAddTextContentClick
            )
        }
    }
}

@Composable
fun ReceivedSpace(
    onContentViewClick: (DataContent) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val appSetsShareActivity = context as AppSetsShareActivity
    val viewModel = viewModel<AppSetsShareViewModel>()
    val receivedContentList = viewModel.receivedContentList
    val receiveDataProgress by viewModel.receiveDataProgressState
    val boxFocusInfo by viewModel.boxFocusInfo

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = receiveDataProgress != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                if (receiveDataProgress != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .align(Alignment.TopCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LinearProgressIndicator(
                            progress = {
                                (receiveDataProgress?.percentage ?: 0f) / 100f
                            }
                        )
                        val contentName =
                            receiveDataProgress?.name
                        if (!contentName.isNullOrEmpty()) {
                            Text(text = contentName, fontSize = 6.sp)
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = boxFocusInfo.receiveBoxFocus,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 48.dp
                    )
                ) {

                    itemsIndexed(items = receivedContentList) { index, dataContent ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            when (dataContent) {
                                is DataContent.StringContent -> {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "${index + 1}", fontSize = 10.sp)
                                        Icon(
                                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_notes_24),
                                            contentDescription = "text"
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            val shareDevice =
                                                appSetsShareActivity.getShareMethod()
                                                    .findShareDeviceForClientInfo(dataContent.clientInfo)
                                            if (shareDevice != null) {
                                                ShareDeviceSmallComponent(
                                                    modifier = Modifier,
                                                    shareDevice = shareDevice
                                                )
                                            }
                                            Text(
                                                text = dataContent.content,
                                                fontSize = 12.sp,
                                                modifier = Modifier,
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                clipboardManager.setText(
                                                    AnnotatedString(
                                                        dataContent.content
                                                    )
                                                )
                                            }
                                        ) {
                                            Text(
                                                text = stringResource(xcj.app.share.R.string.copy),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                is DataContent.FileContent -> {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(text = "${index + 1}", fontSize = 10.sp)
                                        Icon(
                                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                                            contentDescription = "file"
                                        )
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            val shareDevice =
                                                appSetsShareActivity.getShareMethod()
                                                    .findShareDeviceForClientInfo(dataContent.clientInfo)
                                            if (shareDevice != null) {
                                                ShareDeviceSmallComponent(
                                                    modifier = Modifier,
                                                    shareDevice = shareDevice
                                                )
                                            }
                                            Text(
                                                text = dataContent.file.name,
                                                fontSize = 12.sp,
                                            )
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                onContentViewClick(dataContent)
                                            }
                                        ) {
                                            Text(
                                                text = stringResource(xcj.app.share.R.string.view),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }

                                else -> {}
                            }

                            DesignHDivider()
                        }
                    }
                }
                if (receivedContentList.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clickable(
                                    onClick = {
                                        viewModel.removeAllReceivedContent()
                                    }
                                )
                                .padding(12.dp),
                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_delete_forever_24),
                            contentDescription = null
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = receivedContentList.isEmpty() && boxFocusInfo.receiveBoxFocus,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(xcj.app.share.R.string.show_content_here_when_received)
                )
            }
        }

        AnimatedVisibility(
            visible = !boxFocusInfo.receiveBoxFocus,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(xcj.app.compose_share.R.drawable.ic_call_received_24),
                            contentDescription = null
                        )
                        val tipsText = if (receivedContentList.isEmpty()) {
                            stringResource(xcj.app.share.R.string.receive_space)
                        } else {
                            "${stringResource(xcj.app.share.R.string.receive_space)}(${receivedContentList.size})"
                        }
                        Text(text = tipsText)
                    }
                }
            }
        }

    }
}

@Composable
fun getP2pShareDeviceStatus(shareDevice: P2pShareDevice): Pair<String, String> {
    return when (shareDevice.wifiP2pDevice?.status) {
        WifiP2pDevice.AVAILABLE -> {
            Pair(
                "Available",
                stringResource(xcj.app.share.R.string.available)
            )
        }

        WifiP2pDevice.INVITED -> {
            Pair(
                "Invited",
                stringResource(xcj.app.share.R.string.invited)
            )
        }

        WifiP2pDevice.CONNECTED -> {
            Pair(
                "Connected",
                stringResource(xcj.app.share.R.string.connected)
            )
        }

        WifiP2pDevice.FAILED -> {
            Pair(
                "Failed",
                stringResource(xcj.app.share.R.string.failed)
            )
        }

        WifiP2pDevice.UNAVAILABLE -> {
            Pair(
                "Unavailable",
                stringResource(xcj.app.share.R.string.not_available)
            )
        }

        else -> {
            Pair("", stringResource(xcj.app.share.R.string.unconnected_or_unknown))
        }
    }
}

@Composable
fun ShareDeviceSmallComponent(modifier: Modifier, shareDevice: ShareDevice) {
    Row(
        modifier = modifier
            .animateContentSize(
                animationSpec = tween(),
                alignment = Alignment.TopCenter
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val deviceIconResource = getDeviceIconResource(shareDevice)
        Icon(
            modifier = Modifier.size(16.dp),
            painter = painterResource(deviceIconResource),
            contentDescription = null
        )
        val nikeName = shareDevice.deviceName.nickName
        if (!nikeName.isNullOrEmpty()) {
            Text(
                text = nikeName,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
        val rawNameText =
            when (shareDevice) {
                is P2pShareDevice -> {
                    val statusInfo =
                        getP2pShareDeviceStatus(shareDevice)
                    "${shareDevice.deviceName.rawName} ${statusInfo.second}"
                }

                else -> {
                    shareDevice.deviceName.rawName
                }
            }
        Text(
            text = rawNameText,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
fun ShareDeviceMiddleComponent(modifier: Modifier, shareDevice: ShareDevice) {
    Row(
        modifier = modifier
            .animateContentSize(
                animationSpec = tween(),
                alignment = Alignment.TopCenter
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val deviceIconResource = getDeviceIconResource(shareDevice)
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(deviceIconResource),
            contentDescription = null
        )
        val nikeName = shareDevice.deviceName.nickName
        if (!nikeName.isNullOrEmpty()) {
            Text(
                text = nikeName,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
        val rawNameText =
            when (shareDevice) {
                is P2pShareDevice -> {
                    val statusInfo =
                        getP2pShareDeviceStatus(shareDevice)
                    "${shareDevice.deviceName.rawName} ${statusInfo.second}"
                }

                else -> {
                    shareDevice.deviceName.rawName
                }
            }
        Text(
            text = rawNameText,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )
    }
}

@Composable
fun ShareDeviceNormalComponent(modifier: Modifier, shareDevice: ShareDevice) {
    Column(
        modifier = modifier
            .padding(vertical = 6.dp)
            .animateContentSize(
                animationSpec = tween(),
                alignment = Alignment.TopCenter
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val deviceIconResource = getDeviceIconResource(shareDevice)
        DeviceIcon(
            modifier = Modifier,
            resource = deviceIconResource
        )
        val nikeName = shareDevice.deviceName.nickName
        if (!nikeName.isNullOrEmpty()) {
            Text(
                text = nikeName,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
        }
        val rawNameText =
            when (shareDevice) {
                is P2pShareDevice -> {
                    val statusInfo =
                        getP2pShareDeviceStatus(shareDevice)
                    "${shareDevice.deviceName.rawName} ${statusInfo.second}"
                }

                else -> {
                    shareDevice.deviceName.rawName
                }
            }
        Text(
            text = rawNameText,
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            softWrap = false
        )

    }
}

@Composable
fun AppSetsSharePinSheetContent(
    shareDevice: ShareDevice,
    pin: Int,
    onConfirmClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "PIN: $pin", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        FilledTonalIconButton(onClick = onConfirmClick) {
            Icon(
                modifier = Modifier
                    .size(32.dp),
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_check_24),
                contentDescription = null
            )
        }
        Row {
            ShareDeviceSmallComponent(modifier = Modifier, shareDevice)
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


@Composable
fun AppSetsShareClientPreSendSheetContent(
    shareDevice: ShareDevice,
    isAutoAccept: Boolean,
    onAcceptClick: (Boolean) -> Unit,
    onAutoAcceptChanged: (Boolean) -> Unit,
    onContentListShowClick: () -> Unit,
) {
    var isShowDeviceContentList by remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        AnimatedContent(isShowDeviceContentList) { targetIsShowDeviceContentList ->
            if (!targetIsShowDeviceContentList) {
                ShareClientPreSendComponent(
                    shareDevice = shareDevice,
                    isAutoAccept = isAutoAccept,
                    onAcceptClick = onAcceptClick,
                    onAutoAcceptChanged = onAutoAcceptChanged,
                    onContentListShowClick = {
                        isShowDeviceContentList = true
                        onContentListShowClick()
                    }
                )
            } else {
                DeviceContentListComponent(
                    shareDevice = shareDevice,
                    isShowBackButton = true,
                    onBackClick = {
                        isShowDeviceContentList = false
                    }
                )
            }
        }
    }
}

@Composable
fun ShareClientPreSendComponent(
    shareDevice: ShareDevice,
    isAutoAccept: Boolean,
    onAcceptClick: (Boolean) -> Unit,
    onAutoAcceptChanged: (Boolean) -> Unit,
    onContentListShowClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(xcj.app.share.R.string.new_share_content_to_you),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(
                    onClick = onContentListShowClick
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp, horizontal = 12.dp),
            ) {
                Text(
                    text = stringResource(xcj.app.share.R.string.see_share_content_list),
                    fontSize = 12.sp
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalIconButton(onClick = {
                onAcceptClick(false)
            }) {
                Icon(
                    modifier = Modifier
                        .size(32.dp),
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_close_24),
                    contentDescription = null
                )
            }

            FilledTonalIconButton(onClick = {
                onAcceptClick(true)
            }) {
                Icon(
                    modifier = Modifier
                        .size(32.dp),
                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_check_24),
                    contentDescription = null
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            ShareDeviceSmallComponent(modifier = Modifier, shareDevice)
            Spacer(modifier = Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = stringResource(xcj.app.share.R.string.auto_accept), fontSize = 10.sp)
                Switch(checked = isAutoAccept, onCheckedChange = onAutoAcceptChanged)
            }
        }
    }
}

@Composable
fun DeviceIcon(modifier: Modifier, resource: Int) {
    Box(
        modifier = modifier
            .size(42.dp)
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                CircleShape
            )
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource),
            contentDescription = null
        )
    }
}

private fun getDeviceIconResource(shareDevice: ShareDevice): Int {
    val deviceIconResource = when (shareDevice.deviceType) {
        ShareDevice.DEVICE_TYPE_TABLET -> {
            xcj.app.compose_share.R.drawable.ic_tablet_24
        }

        ShareDevice.DEVICE_TYPE_COMPUTER -> {
            xcj.app.compose_share.R.drawable.ic_computer_24
        }

        ShareDevice.DEVICE_TYPE_TV -> {
            xcj.app.compose_share.R.drawable.ic_tv_gen_24
        }

        ShareDevice.DEVICE_TYPE_WEB_DEVICE -> {
            xcj.app.compose_share.R.drawable.ic_language_24
        }

        else -> xcj.app.compose_share.R.drawable.ic_smartphone_24
    }
    return deviceIconResource
}