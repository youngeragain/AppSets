package xcj.app.appsets.ui.compose.wlanp2p

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.ViewDataBinding
import xcj.app.appsets.R
import xcj.app.appsets.ktx.toast
import xcj.app.appsets.ui.compose.start.ComponentImageButton
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.compose.wlanp2p.WLANP2PActivity.Companion.getDeviceStatus
import xcj.app.appsets.ui.nonecompose.base.BaseActivity
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory
import xcj.app.appsets.ui.nonecompose.base.CommonViewModel
import xcj.app.compose_share.compose.BackActionTopBar
import xcj.app.compose_share.compose.usecase.PlatformUseCase
import xcj.app.compose_share.compose.util.FileUtils
import xcj.app.core.android.ApplicationHelper
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread


class WLANP2PActivity :
    BaseActivity<ViewDataBinding, CommonViewModel, BaseViewModelFactory<CommonViewModel>>() {
    private val TAG = "WLANP2PActivity"
    private lateinit var p2pOneThread: P2pOneThread

    /*   private lateinit var serverThread: ServerThread
       private lateinit var clientThread: ClientThread*/
    private var channel: WifiP2pManager.Channel? = null
    private var manager: WifiP2pManager? = null
    private var receiver: WLANP2PBroadCastReceiver? = null
    private var intentFilter: IntentFilter? = null
    private var p2pInfo: WifiP2pInfo? = null
    private var isGroupOwner = false
    private val receivedStringContent: MutableState<String?> = mutableStateOf(null)
    private val canLogicConnectState: MutableState<Boolean> = mutableStateOf(false)
    private val logicConnectEstablishedState: MutableState<Boolean> = mutableStateOf(false)
    val wifip2pIsEnableState: MutableState<Boolean> = mutableStateOf(false)
    val isDiscoveringState: MutableState<Boolean> = mutableStateOf(false)
    val thisWifip2pDeviceState: MutableState<WifiP2pDevice?> = mutableStateOf(null)
    val thisWifip2pDeviceGroupInfoState: MutableState<WifiP2pGroup?> = mutableStateOf(null)
    val wifip2pDeviceListState: MutableList<WifiP2pDevice> = mutableStateListOf()
    val receiveProgressState: MutableState<Pair<Map<String, Any>?, Int>?> = mutableStateOf(null)
    val sendProgressState: MutableState<Pair<Map<String, Any>?, Int>?> = mutableStateOf(null)


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(
                        wifip2pIsEnableState,
                        canLogicConnectState,
                        logicConnectEstablishedState,
                        thisWifip2pDeviceState,
                        thisWifip2pDeviceGroupInfoState,
                        isDiscoveringState,
                        wifip2pDeviceListState,
                        receivedStringContent,
                        receiveProgressState,
                        sendProgressState,
                        ::onBackClick,
                        ::onStartDiscoveryClick,
                        ::onCloseEstablishCLick,
                        ::doLogicConnect,
                        ::oP2PDeviceButtonClick,
                        ::onSendFileClick,
                        ::onSendInputContentClick
                    )
                }
            }
        }
        initP2pManager()
    }

    private fun onCloseEstablishCLick() {
        val closeResult = closeThread()
        if (closeResult)
            logicConnectEstablishedState.value = false
    }

    private fun onSendFileClick() {
        PlatformUseCase.openSystemFileProvider(this, 2222)
    }

    private fun onSendInputContentClick(content: Any) {
        p2pOneThread.writeAny(content)
    }

    fun onBackClick() {
        closeThread()
        onBackPressed()
    }

    private fun initP2pManager() {

        // Device capability definition check
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.i(
                TAG,
                "Wi-Fi Direct is not supported by this device."
            )
            return
        }

        // Hardware capability check
        val wifiManager = getSystemService(WIFI_SERVICE) as? WifiManager
        if (wifiManager == null) {
            Log.i(
                TAG,
                "Cannot get Wi-Fi system service."
            )
            return
        }

        if (!wifiManager.isP2pSupported) {
            Log.i(
                TAG,
                "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off."
            )
            return
        }
        manager = getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
        if (manager == null) {
            Log.i(
                TAG,
                "Cannot get Wi-Fi P2P system service."
            )
            return
        }
        channel = manager?.initialize(
            this,
            mainLooper
        ) {
            Log.i(TAG, "onChannelDisconnected")
        }

        receiver = WLANP2PBroadCastReceiver(this, manager, channel)
        intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun oP2PDeviceButtonClick(device: WifiP2pDevice) {
        Log.i(TAG, "onP2PDeviceClick, device:$device")
        if (device.status == WifiP2pDevice.CONNECTED) {
            disconnect(device)
        } else if (device.status == WifiP2pDevice.AVAILABLE) {
            val config = WifiP2pConfig().apply {
                deviceAddress = device.deviceAddress
                wps.setup = WpsInfo.PBC
            }

            manager?.connect(
                channel, config,
                object : ActionListener {

                    override fun onSuccess() {
                        Log.i(TAG, "connect onSuccess")
                        receiver?.toRequestGroupInfo()
                        // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                    }

                    override fun onFailure(reason: Int) {
                        Log.i(TAG, "connect onFailure")
                        Toast.makeText(
                            this@WLANP2PActivity,
                            "Connect failed. Retry.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    @SuppressLint("MissingPermission")
    private fun disconnect(device: WifiP2pDevice) {
        if (manager == null || channel == null) {
            return
        }
        if (!closeThread()) {
            return
        }
        manager!!.requestGroupInfo(channel) { group ->
            if (group == null || manager == null || channel == null) {
                return@requestGroupInfo
            }
            manager!!.removeGroup(channel, object : ActionListener {
                override fun onSuccess() {
                    Log.i(TAG, "removeGroup onSuccess -")
                    receiver?.toRequestGroupAndDeviceInfo()
                    onStartDiscoveryClick()
                }

                override fun onFailure(reasonCode: Int) {
                    Log.i(TAG, "removeGroup onFailure -$reasonCode")
                }
            })
        }
    }

    private fun closeThread(): Boolean {
        try {
            p2pOneThread.closeReadWrite()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun onStartDiscoveryClick() {
        manager?.discoverPeers(
            channel,
            object : ActionListener {

                override fun onSuccess() {
                    Log.e(TAG, "discoverPeers onSuccess")
                    isDiscoveringState.value = true
                    // Code for when the discovery initiation is successful goes here.
                    // No services have actually been discovered yet, so this method
                    // can often be left blank. Code for peer discovery goes in the
                    // onReceive method, detailed below.
                }

                override fun onFailure(reasonCode: Int) {
                    Log.e(TAG, "discoverPeers onFailure:${reasonCode}")
                    isDiscoveringState.value = false
                    // Code for when the discovery initiation fails goes here.
                    // Alert the user that something went wrong.
                }
            })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    public override fun onResume() {
        super.onResume()
        if (receiver != null) {
            registerReceiver(receiver, intentFilter)
        }
    }

    public override fun onPause() {
        super.onPause()
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun updateGroupOwner(p2pInfo: WifiP2pInfo) {
        if (!p2pInfo.groupFormed)
            return
        isGroupOwner = p2pInfo.isGroupOwner
        this.p2pInfo = p2pInfo
        canLogicConnectState.value = true
        //不管是不是owner, 都启动ServerSocket
    }


    private fun doLogicConnect() {
        if (p2pInfo == null)
            return
        val contentReceivedListener = ContentReceivedListener { contentType, contentAny ->
            when (contentType) {
                ContentType.ShareSystem -> {
                    val bytes = contentAny as ByteArray
                    val shareSystemMessage = bytes.decodeToString()
                    if (ShareSystemMessage.ShareSystemClose == shareSystemMessage) {
                        onCloseEstablishCLick()
                    }
                }

                ContentType.Text -> {
                    val contentBytes = contentAny as ByteArray
                    val stringContent = contentBytes.decodeToString()
                    Log.i(TAG, "length:${contentBytes.size} content:\n${stringContent}")
                    receivedStringContent.value = stringContent
                }

                ContentType.File -> {
                    val file = contentAny as File
                    thread {
                        val available = file.inputStream().available()
                        Log.i(TAG, "received a file, available:${available}")
                    }
                    receivedStringContent.value =
                        "收到文件，文件路径：${ApplicationHelper.getContextFileDir().publicDownloadDir}${File.separator}${file.name}"
                }
            }
        }
        val establishListener = EstablishListener { established ->
            logicConnectEstablishedState.value = established
            if (!established && !isGroupOwner) {
                runOnUiThread {
                    "连接失败".toast()
                }
            }
        }
        val socketExceptionListener = ISocketExceptionListener { type, exception ->
            logicConnectEstablishedState.value = false
        }
        val readProgressListener =
            ProgressListener { contentHeader, total, current, percentage, permillage ->
                receiveProgressState.value = contentHeader to percentage
            }

        val writeProgressListener =
            ProgressListener { contentHeader, total, current, percentage, permillage ->
                sendProgressState.value = contentHeader to percentage
            }
        var host: String? = null
        var port: Int? = null
        if (!isGroupOwner) {
            host = p2pInfo!!.groupOwnerAddress.hostAddress
            port = 8988
        }
        P2pOneThread(
            host,
            port,
            readProgressListener,
            writeProgressListener,
            contentReceivedListener,
            establishListener,
            socketExceptionListener
        ).also {
            p2pOneThread = it
        }.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onExternalAARFileSelectActivityResult(this, requestCode, resultCode, data)
    }

    private fun onExternalAARFileSelectActivityResult(
        context: Context,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode != 2222 || resultCode != Activity.RESULT_OK || data == null || data.data == null)
            return
        val fileName = FileUtils.getFileName(context, data.data!!)
        if (fileName.isNullOrEmpty()) {
            Log.i(TAG, "onExternalAARFileSelectActivityResult file name isEmptyOrNull")
            return
        }
        val inputStream = context.contentResolver.openInputStream(data.data!!)
        if (inputStream == null) {
            Log.i(TAG, "onExternalAARFileSelectActivityResult fileInputStream is null")
            return
        }
        val uriFileNameAndInputStream = UriFileNameAndInputStream(fileName, inputStream)
        p2pOneThread.writeAny(uriFileNameAndInputStream)
    }

    companion object {
        fun copyFile(inputStream: InputStream, out: OutputStream): Boolean {
            val buf = ByteArray(1024)
            var len: Int
            try {
                while (inputStream.read(buf).also { len = it } != -1) {
                    out.write(buf, 0, len)
                }
                out.close()
                inputStream.close()
            } catch (e: IOException) {
                Log.d("CopyFile", e.toString())
                return false
            }
            return true
        }

        fun getDeviceStatus(deviceStatus: Int): Triple<String, String, String?> {
            Log.i("WLANP2PActivity", "Peer status :$deviceStatus")
            return when (deviceStatus) {
                WifiP2pDevice.AVAILABLE -> Triple("Available", "可用", "邀请")

                WifiP2pDevice.INVITED -> Triple("Invited", "已邀请", "等待对方接受邀请中")
                WifiP2pDevice.CONNECTED -> Triple("Connected", "已连接", "断开")
                WifiP2pDevice.FAILED -> Triple("Failed", "失败", null)
                WifiP2pDevice.UNAVAILABLE -> Triple("Unavailable", "不可用", null)
                else -> Triple("Unknown", "未连接或未知", null)
            }
        }
    }
}


@Composable
fun MainContent(
    wifiP2PIsEnable: State<Boolean>,
    canLogicConnect: State<Boolean>,
    logicConnectEstablished: State<Boolean>,
    thisWifiP2pDevice: State<WifiP2pDevice?>,
    thisWifiP2pGroup: State<WifiP2pGroup?>,
    isDiscovering: State<Boolean>,
    wifiP2pDeviceList: List<WifiP2pDevice>,
    receivedStringContent: State<String?>,
    receiveProgress: State<Pair<Map<String, Any>?, Int>?>,
    sendProgress: State<Pair<Map<String, Any>?, Int>?>,
    onBackAction: () -> Unit,
    onStartDiscoveryClick: () -> Unit,
    onCloseEstablishCLick: () -> Unit,
    onLogicConnectClick: () -> Unit,
    oP2PDeviceButtonClick: (WifiP2pDevice) -> Unit,
    onSendFileClick: () -> Unit,
    onSendInputContentClick: (Any) -> Unit,
) {
    Column {
        val customEndContent: (@Composable () -> Unit)? = if (wifiP2PIsEnable.value) {
            {
                Row(modifier = Modifier.animateContentSize()) {
                    if (!canLogicConnect.value) {
                        if (!logicConnectEstablished.value) {
                            Button(onClick = onStartDiscoveryClick) {
                                Text(text = "搜索", fontSize = 12.sp)
                            }
                        }
                    } else {
                        if (logicConnectEstablished.value) {
                            Button(onClick = onCloseEstablishCLick) {
                                Text(text = "关闭", fontSize = 12.sp)
                            }
                        }
                    }

                    if (canLogicConnect.value && !logicConnectEstablished.value) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = {
                            onLogicConnectClick()
                        }) {
                            Text(text = "开启共享", fontSize = 12.sp)
                        }
                    }
                }

            }
        } else {
            null
        }
        BackActionTopBar(
            backButtonRightText = "Share",
            onBackAction = onBackAction,
            customEndContent = customEndContent
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (logicConnectEstablished.value) {
                Column(
                    Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                ) {
                    if (sendProgress.value != null) {
                        val progressText by remember {
                            derivedStateOf {
                                "发送进度(${sendProgress.value!!.second}%)"
                            }
                        }
                        Text(text = progressText, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    if (receiveProgress.value != null) {
                        val progressText by remember {
                            derivedStateOf {
                                "接收进度(${receiveProgress.value!!.second}%) ${
                                    receiveProgress.value!!.first?.get(
                                        "file-name"
                                    )
                                }"
                            }
                        }
                        Text(text = progressText, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Column(
                        Modifier
                            .heightIn(min = 52.dp)
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .animateContentSize()
                    ) {
                        Text(
                            text = receivedStringContent.value ?: "此处显示接收的内容",
                            fontSize = 12.sp
                        )
                        val clipboardManager = LocalClipboardManager.current
                        if (!receivedStringContent.value.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = {
                                clipboardManager.setText(AnnotatedString(receivedStringContent.value!!))
                            }, modifier = Modifier.align(Alignment.End)) {
                                Text(text = "复制", fontSize = 12.sp)
                            }
                        }

                    }
                    var inputText by remember {
                        mutableStateOf("")
                    }
                    TextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                        },
                        placeholder = {
                            Text(
                                text = "输入内容然后点击发送，AppSets Share会试图以最佳方式发送",
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Spacer(modifier = Modifier.weight(1f))
                        Button(onClick = onSendFileClick) {
                            Text(text = "选择文件发送", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(onClick = {
                            onSendInputContentClick(inputText)
                            inputText = ""
                        }) {
                            Text(text = "发送输入内容", fontSize = 12.sp)
                        }
                    }
                }
            }
            if (wifiP2pDeviceList.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .animateContentSize()
                ) {
                    wifiP2pDeviceList.forEach { wifiP2PDevice ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                            ) {
                                val status = getDeviceStatus(wifiP2PDevice.status)
                                val tag = status.second
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = tag, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = wifiP2PDevice.deviceName)
                                }
                                val actionText = status.third
                                if (!actionText.isNullOrEmpty()) {
                                    Button(onClick = {
                                        oP2PDeviceButtonClick(wifiP2PDevice)
                                    }) {

                                        Text(
                                            text = actionText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            } else if (isDiscovering.value) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp), contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "正在搜索", fontSize = 12.sp)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (wifiP2pDeviceList.isEmpty() && !logicConnectEstablished.value) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(120.dp), contentAlignment = Alignment.Center
                    ) {
                        val text = if (wifiP2PIsEnable.value) {
                            if (wifiP2pDeviceList.isEmpty()) {
                                "搜索对等设备"
                            } else {
                                "开启接收和发送"
                            }
                        } else {
                            "开启WIFI"
                        }
                        Text(text = text, fontSize = 12.sp)
                    }
                }
                var debug by remember {
                    mutableStateOf(false)
                }
                var help by remember {
                    mutableStateOf(false)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ComponentImageButton(
                        modifier = Modifier,
                        useImage = false,
                        resId = R.drawable.outline_help_outline_24
                    ) {
                        help = !help
                    }
                    ComponentImageButton(
                        modifier = Modifier,
                        useImage = false,
                        resId = R.drawable.outline_bug_report_24
                    ) {
                        debug = !debug
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    if (help) {
                        Text(
                            text = "Share 使用WIFI Direct，需要以下权限:\n附近的设备\n位置大致信息\n位置精确信息\n获取WIFI状态\n更改WIFI状态\n附近WIFI设备\n* 使用时打开WIFI开关和位置开关。",
                            fontSize = 10.sp
                        )
                    }
                    if (debug) {
                        Spacer(modifier = Modifier.height(12.dp))
                        if (thisWifiP2pDevice.value != null) {
                            Text(
                                text = "Device:${thisWifiP2pDevice.value!!.deviceName ?: "未生成"} | mac:${thisWifiP2pDevice.value!!.deviceAddress ?: ""}",
                                fontSize = 10.sp, fontWeight = FontWeight.Bold
                            )
                        }
                        if (thisWifiP2pGroup.value != null) {
                            Text(
                                text = "Group:${thisWifiP2pGroup.value}",
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}


/*
@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    AppSetsTheme {
        val callback: () -> Unit = {}
        MainContent(
            false,
            false,
            false,
            null,
            null,
            false,
            emptyList(),
            null,
            callback,
            callback,
            callback,
            callback,
            {},
            callback,
            {})
    }
}*/
