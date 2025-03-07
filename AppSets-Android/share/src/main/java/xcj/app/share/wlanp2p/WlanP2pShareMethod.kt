package xcj.app.share.wlanp2p

import BoxFocusInfo
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.WIFI_P2P_SERVICE
import android.content.Context.WIFI_SERVICE
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.MacAddress
import android.net.wifi.WifiManager
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.WifiP2pManager.ActionListener
import android.net.wifi.p2p.WifiP2pManager.ChannelListener
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.share.base.ClientInfo
import xcj.app.share.base.DataContent
import xcj.app.share.base.DeviceName
import xcj.app.share.base.DeviceNameExchangeListener
import xcj.app.share.base.ShareDevice
import xcj.app.share.base.ShareMethod
import xcj.app.share.ui.compose.AppSetsShareActivity
import xcj.app.share.ui.compose.AppSetsShareViewModel
import xcj.app.share.wlanp2p.base.ISocketExceptionListener
import xcj.app.share.wlanp2p.base.LogicEstablishListener
import xcj.app.share.wlanp2p.base.P2pShareDevice
import xcj.app.share.wlanp2p.base.WlanP2pContent
import xcj.app.share.wlanp2p.common.P2pOneThread
import xcj.app.share.wlanp2p.common.WlanP2pBroadCastReceiver
import xcj.app.share.wlanp2p.common.WlanP2pEnableInfo
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.web.webserver.interfaces.ContentReceivedListener
import xcj.app.web.webserver.interfaces.ProgressListener

class WlanP2pShareMethod : ShareMethod(), ContentReceivedListener {
    companion object {
        private const val TAG = "WlanP2pShareMethod"
        const val NAME = "WLAN P2P"
    }

    val wifiP2PEnableInfoState: MutableState<WlanP2pEnableInfo> =
        mutableStateOf(WlanP2pEnableInfo())

    val canLogicConnectState: MutableState<Boolean> = mutableStateOf(false)

    val logicConnectEstablishedState: MutableState<Boolean> = mutableStateOf(false)

    private var wifiP2pChannel: WifiP2pManager.Channel? = null
    private var wifiP2pManager: WifiP2pManager? = null
    private var wifiP2pInfo: WifiP2pInfo? = null
    private var isGroupOwner = false

    private var receiver: WlanP2pBroadCastReceiver? = null
    private var receiverRegistered: Boolean = false

    private var p2pOneThread: P2pOneThread? = null

    val dataReceivedProgressListener =
        ProgressListener { dataProgressInfo ->
            viewModel.updateReceiveDataProgressState(dataProgressInfo)
        }

    val dataSendProgressListener =
        ProgressListener { dataProgressInfo ->
            viewModel.updateSendDataProgressState(dataProgressInfo)
        }

    fun updateCanLogicConnectState(canLogicConnect: Boolean) {
        canLogicConnectState.value = canLogicConnect
    }

    fun updateLogicConnectEstablishedState(isEstablished: Boolean) {
        logicConnectEstablishedState.value = isEstablished
    }

    override fun init(
        activity: AppSetsShareActivity,
        appSetsShareViewModel: AppSetsShareViewModel
    ) {
        super.init(activity, appSetsShareViewModel)
        PurpleLogger.current.d(TAG, "init")
        open()
    }

    override fun updateShareDevice() {
        val shareDevice =
            P2pShareDevice(deviceName = mDeviceName)
        viewModel.updateShareDeviceState(shareDevice)
    }

    override fun open() {
        initP2pManager()
    }

    private fun initP2pManager() {

        if (!checkConditions(true)) {
            return
        }

        val wifiP2pManager = activity.getSystemService(WIFI_P2P_SERVICE) as? WifiP2pManager
        if (wifiP2pManager == null) {
            PurpleLogger.current.d(
                TAG,
                "Cannot get Wi-Fi P2P system service."
            )
            return
        }
        PurpleLogger.current.d(TAG, "initP2pManager")
        val channelListener = ChannelListener {
            PurpleLogger.current.d(TAG, "onChannelDisconnected")
            updateLogicConnectEstablishedState(false)
        }
        val wifiP2pChannel = wifiP2pManager.initialize(
            activity,
            activity.mainLooper,
            channelListener
        )

        this.wifiP2pManager = wifiP2pManager
        this.wifiP2pChannel = wifiP2pChannel

        val peerListListener = WifiP2pManager.PeerListListener { peerList ->
            val wifiP2pDeviceList = peerList.deviceList
            PurpleLogger.current.d(TAG, "onPeersAvailable")
            if (wifiP2pDeviceList.isEmpty()) {
                PurpleLogger.current.d(TAG, "onPeersAvailable, No devices found")
                return@PeerListListener
            }
            val p2pShareDevice =
                viewModel.mShareDeviceState.value as? P2pShareDevice
            val wifiP2pGroup = p2pShareDevice?.wifiP2pGroup
            val shareDeviceList = wifiP2pDeviceList.map { wifiP2PDevice ->
                P2pShareDevice(
                    wifiP2PDevice,
                    wifiP2pGroup,
                    DeviceName(wifiP2PDevice.deviceName)
                )
            }
            viewModel.updateShareDeviceListWithDiff(this, shareDeviceList)
            val boxFocusInfo = viewModel.boxFocusInfo.value
            if (!boxFocusInfo.sendBoxFocus) {
                viewModel.updateBoxFocusInfo(BoxFocusInfo(devicesBoxFocus = true))
            }
            activity.lifecycleScope.launch {
                delay(2000)
                viewModel.updateIsDiscoveringState(false)
            }
        }
        val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->
            PurpleLogger.current.d(TAG, "onConnectionInfoAvailable info:$info")
            // InetAddress from WifiP2pInfo struct.
            handleConnectionInfo(info)
            // After the group negotiation, we can determine the group owner
        }

        val deviceInfoListener = WifiP2pManager.DeviceInfoListener { wifiP2pDevice ->
            PurpleLogger.current.d(TAG, "onDeviceInfoAvailable deviceInfo:$wifiP2pDevice")
            if (wifiP2pDevice == null) {
                return@DeviceInfoListener
            }
            val p2pShareDevice =
                viewModel.mShareDeviceState.value as? P2pShareDevice
            val newShareDevice = p2pShareDevice?.copy(wifiP2pDevice = wifiP2pDevice)
                ?: P2pShareDevice(
                    wifiP2pDevice = wifiP2pDevice,
                    deviceName = mDeviceName
                )
            viewModel.updateShareDeviceState(newShareDevice)

        }
        val groupInfoListener = WifiP2pManager.GroupInfoListener { wifiP2pGroup ->
            PurpleLogger.current.d(TAG, "onGroupInfoAvailable wifiP2pGroup:$wifiP2pGroup")
            if (wifiP2pGroup == null) {
                return@GroupInfoListener
            }
            val p2pShareDevice =
                viewModel.mShareDeviceState.value as? P2pShareDevice
            if (p2pShareDevice == null) {
                return@GroupInfoListener
            }
            val newShareDevice = p2pShareDevice.copy(wifiP2pGroup = wifiP2pGroup)
            viewModel.updateShareDeviceState(newShareDevice)

        }
        receiver = WlanP2pBroadCastReceiver(
            this,
            wifiP2pManager,
            wifiP2pChannel,
            peerListListener,
            connectionListener,
            deviceInfoListener,
            groupInfoListener
        )
        if (activity.lifecycle.currentState == Lifecycle.State.RESUMED) {
            registerBroadcastReceiver()
        }
    }

    override fun close() {
        updateLogicConnectEstablishedState(false)
        closeThread()
        removeGroupIfNeeded()
    }

    @SuppressLint("MissingPermission")
    private fun removeGroupIfNeeded() {
        if (!isGroupOwner) {
            return
        }

        val wifiP2pManager = wifiP2pManager ?: return
        val wifiP2pChannel = wifiP2pChannel ?: return

        val groupInfoListener = WifiP2pManager.GroupInfoListener { group ->
            if (group == null) {
                return@GroupInfoListener
            }
            val actionListener = object : ActionListener {
                override fun onSuccess() {
                    PurpleLogger.current.d(TAG, "removeGroup onSuccess")
                }

                override fun onFailure(reasonCode: Int) {
                    PurpleLogger.current.d(
                        TAG,
                        "removeGroup onFailure:$reasonCode"
                    )
                }
            }
            wifiP2pManager.removeGroup(wifiP2pChannel, actionListener)
        }
        PurpleLogger.current.d(TAG, "removeGroupIfNeeded")
        wifiP2pManager.requestGroupInfo(wifiP2pChannel, groupInfoListener)
    }

    @SuppressLint("MissingPermission")
    private fun removeClientIfNeeded(shareDevice: P2pShareDevice) {
        if (!isGroupOwner) {
            return
        }

        val wifiP2pManager = wifiP2pManager ?: return
        val wifiP2pChannel = wifiP2pChannel ?: return
        val deviceAddress = shareDevice.wifiP2pDevice?.deviceAddress ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val actionListener = object : ActionListener {
                override fun onSuccess() {
                    PurpleLogger.current.d(TAG, "removeClient onSuccess")
                }

                override fun onFailure(reasonCode: Int) {
                    PurpleLogger.current.d(
                        TAG,
                        "removeClient onFailure:$reasonCode"
                    )
                }
            }
            PurpleLogger.current.d(TAG, "removeClientIfNeeded")
            wifiP2pManager.removeClient(
                wifiP2pChannel,
                MacAddress.fromString(deviceAddress),
                actionListener
            )
        }
    }

    private fun closeThread() {
        p2pOneThread?.close()
        p2pOneThread = null
    }

    override fun send(shareDevices: List<ShareDevice>) {
        PurpleLogger.current.d(TAG, "send")
        val sendContentList = viewModel.pendingSendContentList
        if (sendContentList.isEmpty()) {
            PurpleLogger.current.d(TAG, "send, sendContentList is empty, return")
            return
        }
        shareDevices.filterIsInstance<P2pShareDevice>().forEach { shareDevice ->
            sendContentList.forEach { dataContent ->
                val dataSendContent =
                    WlanP2pContent(shareDevice, dataContent)
                activity.lifecycleScope.launch {
                    p2pOneThread?.writeContent(activity, dataSendContent)
                }
            }
        }
    }

    fun handleConnectionInfo(p2pInfo: WifiP2pInfo) {
        PurpleLogger.current.d(TAG, "handleConnectionInfo")
        if (!p2pInfo.groupFormed) {
            return
        }
        updateCanLogicConnectState(true)
        isGroupOwner = p2pInfo.isGroupOwner
        wifiP2pInfo = p2pInfo
        doLogicConnect()
    }


    private fun doLogicConnect() {
        val mySelfShareDevice = viewModel.mShareDeviceState.value
        if (mySelfShareDevice !is P2pShareDevice) {
            PurpleLogger.current.d(
                TAG,
                "doLogicConnect, shareDevice is not P2pShareDevice, return"
            )
            return
        }
        val wifiP2pDevice = mySelfShareDevice.wifiP2pDevice
        if (wifiP2pDevice == null) {
            PurpleLogger.current.d(
                TAG,
                "doLogicConnect, shareDevice.wifiP2pDevice is null, return"
            )
            return
        }
        val p2pInfo = wifiP2pInfo
        if (p2pInfo == null) {
            PurpleLogger.current.d(
                TAG,
                "doLogicConnect, p2pInfo is null, return"
            )
            return
        }
        val oldP2pOneThread = p2pOneThread
        if (oldP2pOneThread != null) {
            if (!oldP2pOneThread.hasClosed()) {
                PurpleLogger.current.d(
                    TAG,
                    "doLogicConnect, exist a connect thread, return"
                )
                return
            }
        }
        PurpleLogger.current.d(
            TAG,
            "doLogicConnect, start"
        )

        val logicEstablishListener = LogicEstablishListener { established ->
            updateLogicConnectEstablishedState(established)
            if (!established) {
                activity.runOnUiThread {
                    Toast.makeText(
                        activity, activity.getString(xcj.app.share.R.string.connect_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        val socketExceptionListener = ISocketExceptionListener { type, exception ->
            updateLogicConnectEstablishedState(false)
        }

        val deviceNameExchangeListener = DeviceNameExchangeListener { deviceName ->
            viewModel.updateDeviceName(this, deviceName)
        }
        var host: String? = null
        if (!isGroupOwner) {
            host = p2pInfo.groupOwnerAddress.hostAddress
        }

        val aNewP2pOneThread = P2pOneThread(
            mySelfShareDevice,
            host,
            P2pOneThread.Companion.SERVER_PORT,
            dataReceivedProgressListener,
            dataSendProgressListener,
            this,
            logicEstablishListener,
            socketExceptionListener,
            deviceNameExchangeListener
        )
        this.p2pOneThread = aNewP2pOneThread
        aNewP2pOneThread.start()
    }

    override fun onContentReceived(content: Any) {
        PurpleLogger.current.d(
            TAG,
            "onContentReceived content:$content"
        )
        when (content) {
            is DataContent.StringContent -> {
                viewModel.onContentReceived(content)
            }

            is DataContent.FileContent -> {
                viewModel.onContentReceived(content)
            }
        }
    }

    override fun onShareDeviceClick(shareDevice: ShareDevice, clickType: Int) {
        super.onShareDeviceClick(shareDevice, clickType)
        PurpleLogger.current.d(
            TAG,
            "onShareDeviceClick, shareDevice:$shareDevice, clickType:$clickType"
        )
        if (shareDevice !is P2pShareDevice) {
            return
        }
        val wifiP2pDevice = shareDevice.wifiP2pDevice
        if (wifiP2pDevice == null) {
            return
        }
        when (clickType) {
            AppSetsShareActivity.CLICK_TYPE_NORMAL -> {
                if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED ||
                    !shareDevice.deviceName.nickName.isNullOrEmpty()
                ) {
                    val pendingSendContentList = viewModel.pendingSendContentList
                    if (pendingSendContentList.isEmpty()) {
                        return
                    }
                    send(listOf(shareDevice))
                } else if (wifiP2pDevice.status == WifiP2pDevice.AVAILABLE) {
                    toConnectDevice(shareDevice)
                }
            }

            AppSetsShareActivity.CLICK_TYPE_LONG -> {
                if (wifiP2pDevice.status == WifiP2pDevice.CONNECTED) {
                    disconnect(shareDevice)
                }
            }

            AppSetsShareActivity.CLICK_TYPE_DOUBLE -> {

            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun toConnectDevice(shareDevice: P2pShareDevice) {
        if (!checkConditions()) {
            return
        }
        val wifiP2pDevice = shareDevice.wifiP2pDevice
        if (wifiP2pDevice == null) {
            return
        }
        val wifiP2pManager = wifiP2pManager
        if (wifiP2pManager == null) {
            return
        }
        PurpleLogger.current.d(
            TAG,
            "toConnectDevice, wifiP2pDevice.deviceAddress:${wifiP2pDevice.deviceAddress}"
        )
        val config = WifiP2pConfig().apply {
            deviceAddress = wifiP2pDevice.deviceAddress
            wps.setup = WpsInfo.PBC
        }
        val actionListener = object : ActionListener {

            override fun onSuccess() {
                PurpleLogger.current.d(TAG, "toConnectDevice onSuccess")
                receiver?.toRequestGroupInfo()
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
            }

            override fun onFailure(reason: Int) {
                val reason = when (reason) {
                    WifiP2pManager.ERROR -> {
                        "ERROR"
                    }

                    WifiP2pManager.P2P_UNSUPPORTED -> {
                        "P2P_UNSUPPORTED"
                    }

                    WifiP2pManager.BUSY -> {
                        "BUSY"
                    }

                    WifiP2pManager.NO_SERVICE_REQUESTS -> {
                        "NO_SERVICE_REQUESTS"
                    }

                    else -> "UNKNOWN"
                }
                PurpleLogger.current.d(TAG, "toConnectDevice onFailure, reason:${reason}")

                activity.runOnUiThread {
                    val text = String.format(
                        "%s, %s",
                        activity.getString(xcj.app.share.R.string.please_retry),
                        reason
                    )
                    Toast.makeText(
                        activity, text,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        wifiP2pManager.connect(
            wifiP2pChannel,
            config,
            actionListener
        )
    }

    @SuppressLint("MissingPermission")
    private fun disconnect(shareDevice: P2pShareDevice) {
        if (!checkConditions()) {
            return
        }
        val wifiP2pManager = wifiP2pManager
        val wifiP2pChannel = wifiP2pChannel
        if (wifiP2pManager == null || wifiP2pChannel == null) {
            return
        }
        if (isGroupOwner) {
            closeClientThread(shareDevice)
            removeClientIfNeeded(shareDevice)
        } else {
            close()
        }
    }

    private fun closeClientThread(shareDevice: P2pShareDevice) {
        p2pOneThread?.close(shareDevice)
    }

    @SuppressLint("MissingPermission")
    override fun discovery() {
        if (!checkConditions()) {
            return
        }
        val wifiP2pManager = wifiP2pManager
        val wifiP2pChannel = wifiP2pChannel
        if (wifiP2pManager == null) {
            return
        }
        if (wifiP2pChannel == null) {
            return
        }

        val actionListener = object : ActionListener {
            override fun onSuccess() {
                PurpleLogger.current.d(TAG, "discoverPeers onSuccess")
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank. Code for peer discovery goes in the
                // onReceive method, detailed below.
                activity.lifecycleScope.launch {
                    if (viewModel.isDiscoveringState.value) {
                        viewModel.updateIsDiscoveringState(false)
                        delay(3000)
                    }
                    viewModel.updateIsDiscoveringState(true)
                }
            }

            override fun onFailure(reason: Int) {
                val reason = when (reason) {
                    WifiP2pManager.ERROR -> {
                        "ERROR"
                    }

                    WifiP2pManager.P2P_UNSUPPORTED -> {
                        "P2P_UNSUPPORTED"
                    }

                    WifiP2pManager.BUSY -> {
                        "BUSY"
                    }

                    WifiP2pManager.NO_SERVICE_REQUESTS -> {
                        "NO_SERVICE_REQUESTS"
                    }

                    else -> "UNKNOWN"
                }
                PurpleLogger.current.d(
                    TAG,
                    "discoverPeers onFailure:${reason}"
                )
                activity.runOnUiThread {
                    val text = String.format(
                        "%s, %s",
                        activity.getString(xcj.app.share.R.string.please_retry),
                        reason
                    )
                    Toast.makeText(
                        activity, text,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                activity.lifecycleScope.launch {
                    delay(2000)
                    viewModel.updateIsDiscoveringState(false)
                }
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
            }
        }
        wifiP2pManager.discoverPeers(wifiP2pChannel, actionListener)
    }

    override fun onResume(owner: LifecycleOwner) {
        registerBroadcastReceiver()
    }

    override fun onPause(owner: LifecycleOwner) {
        unRegisterBroadcastReceiver()
    }

    private fun registerBroadcastReceiver() {
        if (receiver != null && !receiverRegistered) {
            receiverRegistered = true
            PurpleLogger.current.d(TAG, "registerBroadcastReceiver, registerReceiver")
            val intentFilter: IntentFilter = IntentFilter().apply {
                addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
                addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
            }
            activity.registerReceiver(receiver, intentFilter)
        }
    }

    private fun unRegisterBroadcastReceiver() {
        if (receiver != null && receiverRegistered) {
            receiverRegistered = false
            PurpleLogger.current.d(TAG, "unRegisterBroadcastReceiver, unregisterReceiver")
            activity.unregisterReceiver(receiver)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        destroy()
    }

    override fun destroy() {
        super.destroy()
        PurpleLogger.current.d(TAG, "destroy")
        close()
        unRegisterBroadcastReceiver()
        receiver = null
        wifiP2pManager = null
        wifiP2pChannel = null
        wifiP2pInfo = null
    }

    private fun checkConditions(onlyHardwareAndPermissions: Boolean = false): Boolean {
        PurpleLogger.current.d(
            TAG,
            "checkConditions, onlyHardwareAndPermissions:$onlyHardwareAndPermissions"
        )
        // Device capability definition check
        if (!activity.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            PurpleLogger.current.d(
                TAG,
                "checkConditions, Wi-Fi Direct is not supported by this device."
            )
            wifiP2PEnableInfoState.value = WlanP2pEnableInfo(false, "No Hardware Features")
            return false
        }

        // Hardware capability check
        val wifiManager = activity.getSystemService(WIFI_SERVICE) as? WifiManager
        if (wifiManager == null) {
            PurpleLogger.current.d(
                TAG,
                "checkConditions, Cannot get Wi-Fi system service."
            )
            wifiP2PEnableInfoState.value = WlanP2pEnableInfo(false, "No Hardware Features")
            return false
        }

        if (!wifiManager.isP2pSupported) {
            PurpleLogger.current.d(
                TAG,
                "checkConditions, Wi-Fi Direct is not supported by the hardware or Wi-Fi is off."
            )
            wifiP2PEnableInfoState.value = WlanP2pEnableInfo(false, "No Hardware Features")
            return false
        }
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            PurpleLogger.current.d(
                TAG,
                "checkConditions, Android Permissions has not granted! Manifest.permission.ACCESS_FINE_LOCATION"
            )
            wifiP2PEnableInfoState.value = WlanP2pEnableInfo(
                false,
                "No Android Permissions:Manifest.permission.ACCESS_FINE_LOCATION"
            )
            return false
        }
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            PurpleLogger.current.d(
                TAG,
                "checkConditions, Android Permissions has not granted! Manifest.permission.NEARBY_WIFI_DEVICES"
            )
            wifiP2PEnableInfoState.value = WlanP2pEnableInfo(
                false,
                "No Android Permissions:Manifest.permission.NEARBY_WIFI_DEVICES"
            )
            return false
        }
        if (onlyHardwareAndPermissions) {
            return true
        }
        val locationManager = activity.getSystemService(LocationManager::class.java)
        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!gpsEnabled && !networkEnabled) {
            PurpleLogger.current.d(
                TAG,
                "checkConditions, Location not enable!"
            )
            wifiP2PEnableInfoState.value =
                WlanP2pEnableInfo(false, "Location not enable")
            return false
        }
        //check is wifi ap is enable when wifi is open
        return wifiP2PEnableInfoState.value.enable
    }

    fun updateWlanP2pEnable(isEnable: Boolean) {
        wifiP2PEnableInfoState.value = WlanP2pEnableInfo(isEnable)
        if (isEnable) {
            activity.lifecycleScope.launch {
                delay(1000)
                discovery()
            }
        }
    }

    fun updateShareDeviceState(wifiP2pDevice: WifiP2pDevice) {
        val appSetsViewModel = viewModel
        val shareDevice = appSetsViewModel.mShareDeviceState.value
        if (shareDevice is P2pShareDevice) {
            appSetsViewModel.updateShareDeviceState(shareDevice.copy(wifiP2pDevice = wifiP2pDevice))
        }
    }

    override fun findShareDeviceForClientInfo(clientInfo: ClientInfo): P2pShareDevice? {
        return super.findShareDeviceForClientInfo(clientInfo) as? P2pShareDevice
    }
}