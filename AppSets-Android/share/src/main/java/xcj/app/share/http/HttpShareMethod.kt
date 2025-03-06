package xcj.app.share.http

import AppSetsShareClientPreSendSheet
import AppSetsSharePinSheet
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.share.base.ClientInfo
import xcj.app.share.base.DataContent
import xcj.app.share.base.DataSendContent
import xcj.app.share.base.DeviceAddress
import xcj.app.share.base.DeviceIP
import xcj.app.share.base.ShareDevice
import xcj.app.share.base.ShareMethod
import xcj.app.share.http.base.HttpContent
import xcj.app.share.http.base.HttpShareDevice
import xcj.app.share.http.common.ServerBootStateInfo
import xcj.app.share.http.discovery.BonjourDiscovery
import xcj.app.share.http.discovery.Discovery
import xcj.app.share.http.discovery.DiscoveryEndpoint
import xcj.app.share.http.repository.AppSetsShareRepository
import xcj.app.share.ui.compose.AppSetsShareActivity
import xcj.app.share.ui.compose.AppSetsShareViewModel
import xcj.app.share.util.NetworkUtil
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.requestNotNull
import xcj.app.starter.server.requestRaw
import xcj.app.starter.test.LocalPurpleCoroutineScope
import xcj.app.web.webserver.base.DataProgressInfo
import xcj.app.web.webserver.interfaces.ContentReceivedListener
import xcj.app.web.webserver.interfaces.ListenersProvider
import xcj.app.web.webserver.interfaces.ProgressListener
import xcj.app.web.webserver.netty.ServerBootStrap
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

typealias SuspendRunnable = suspend () -> Unit

class HttpShareMethod : ShareMethod(), ContentReceivedListener, ListenersProvider {
    companion object {
        private const val TAG = "HttpShareMethod"
        const val NAME = "HTTP"
        const val SHARE_SERVER_API_PORT = 11101
        const val SHARE_SERVER_FILE_API_PORT = 11102
    }

    data class SendDataRunnableInfo(
        var isAccept: Boolean = false,
        var isCalled: Boolean = false,
        var isFinished: Boolean = false
    ) {
        var sendDataRunnable: SuspendRunnable? = null
        var next: SendDataRunnableInfo? = null

        suspend fun run() {
            sendDataRunnable?.invoke()
            isCalled = true
        }
    }

    private val appSetsShareRepository = AppSetsShareRepository()

    private var discovery: Discovery? = null

    private var serverBootStrap: ServerBootStrap? = null

    val dataReceivedProgressListener: ProgressListener = object : ProgressListener {
        override fun onProgress(dataProgressInfo: DataProgressInfo) {
            viewModel.updateReceiveDataProgressState(dataProgressInfo)
        }
    }

    val dataSendProgressListener: ProgressListener = object : ProgressListener {
        override fun onProgress(dataProgressInfo: DataProgressInfo) {
            viewModel.updateSendDataProgressState(dataProgressInfo)
        }
    }

    val isNeedPinState: MutableState<Boolean> = mutableStateOf(false)

    val isAutoAcceptState: MutableState<Boolean> = mutableStateOf(false)

    val isPreferDownloadSelfState: MutableState<Boolean> = mutableStateOf(true)

    val serverBootStateInfoState: MutableState<ServerBootStateInfo> =
        mutableStateOf(ServerBootStateInfo.NotBooted)

    val sendContentRunnableInfoMap: ConcurrentHashMap<HttpShareDevice, SendDataRunnableInfo?> =
        ConcurrentHashMap()

    override fun getContentReceivedListener(): ContentReceivedListener? {
        return this
    }

    override fun getReceiveProgressListener(): ProgressListener? {
        return dataReceivedProgressListener
    }

    override fun getSendProgressListener(): ProgressListener? {
        return dataSendProgressListener
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
    }

    override fun onLost(network: Network) {
        super.onLost(network)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun init(activity: AppSetsShareActivity, viewModel: AppSetsShareViewModel) {
        super.init(activity, viewModel)
        PurpleLogger.current.d(TAG, "init")
        open()
    }

    override fun updateShareDevice() {
        val shareDevice =
            HttpShareDevice(deviceName = mDeviceName)
        viewModel.updateShareDeviceState(shareDevice)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun open() {
        PurpleLogger.current.d(TAG, "open")
        val actionLister = object : ServerBootStrap.ActionLister {
            override fun onSuccess() {
                PurpleLogger.current.d(TAG, "open, onSuccess")
                val allAvailableLocalInetAddresses =
                    NetworkUtil.getAllAvailableLocalInetAddresses()
                        .filter { it.first is Inet4Address }
                serverBootStateInfoState.value = makeBootedInfo(allAvailableLocalInetAddresses)

                activity.lifecycleScope.launch {
                    startDiscoveryService(allAvailableLocalInetAddresses)
                }
            }

            override fun onFailure(reason: String?) {
                PurpleLogger.current.d(TAG, "open, onFailure")
                serverBootStateInfoState.value =
                    ServerBootStateInfo.BootFailed(reason, SHARE_SERVER_API_PORT)
            }

        }
        serverBootStrap = ServerBootStrap()
        activity.lifecycleScope.launch {
            serverBootStrap?.main(
                activity.application,
                SHARE_SERVER_API_PORT,
                SHARE_SERVER_FILE_API_PORT,
                this@HttpShareMethod,
                actionLister
            )
        }
    }

    private fun makeBootedInfo(
        availableLocalInetAddresses: List<Pair<InetAddress, NetworkInterface>>
    ): ServerBootStateInfo.Booted {
        val allAvailableDeviceIp = mutableListOf<DeviceIP>()
        val allAvailableAddressInfo = mutableListOf<String>()

        val allAvailableIPSuffixes = mutableListOf<String>()
        availableLocalInetAddresses.forEach {
            val inetAddress = it.first
            val networkInterface = it.second
            val hostAddress = NetworkUtil.getHostAddress(inetAddress)


            if (inetAddress is Inet4Address && !hostAddress.isEmpty()) {
                val deviceIP = DeviceIP(ip = hostAddress)
                allAvailableDeviceIp.add(deviceIP)

                val humanReadableName =
                    NetworkUtil.getNetworkInterfaceHumanReadableName(networkInterface)
                val availableAddress = "$humanReadableName | $hostAddress"
                allAvailableAddressInfo.add(availableAddress)

                val lastIndexOf = hostAddress.lastIndexOf('.')
                val substring = hostAddress.substring(lastIndexOf + 1)
                val availableIPSuffix = "#$substring"
                allAvailableIPSuffixes.add(availableIPSuffix)
            }
            /* else if (inetAddress is Inet6Address && !hostAddress.isEmpty()) {
                if (hostAddress.startsWith("fe80::")) {
                    val availableIPSuffix = "#${hostAddress.substringAfter("fe80::")}"
                    allAvailableIPSuffixes.add(availableIPSuffix)
                }
            }*/
        }

        val currentShareDevice = getCurrentShareDevice()
        val deviceAddress = DeviceAddress(allAvailableDeviceIp)
        currentShareDevice?.deviceAddress = deviceAddress

        val availableIPSuffixesForDevice =
            allAvailableIPSuffixes.joinToString(separator = " ", prefix = " ")

        return ServerBootStateInfo.Booted(
            allAvailableDeviceIp,
            allAvailableAddressInfo,
            SHARE_SERVER_API_PORT,
            availableIPSuffixesForDevice
        )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        destroy()
    }

    override fun destroy() {
        super.destroy()
        close()
    }

    override fun close() {
        PurpleLogger.current.d(TAG, "close")
        val actionLister = object : ServerBootStrap.ActionLister {
            override fun onSuccess() {
                PurpleLogger.current.d(TAG, "close, onSuccess")
                serverBootStateInfoState.value = ServerBootStateInfo.NotBooted
            }


            override fun onFailure(reason: String?) {
                PurpleLogger.current.d(TAG, "close, onFailure")
            }

        }
        LocalPurpleCoroutineScope.current.launch {
            stopDiscoveryService()
            serverBootStrap?.close(actionLister)
        }
    }

    private suspend fun stopDiscoveryService() {
        PurpleLogger.current.d(TAG, "stopDiscoveryService")
        discovery?.stopService()
    }

    override fun discovery() {
        activity.lifecycleScope.launch {
            viewModel.updateIsDiscoveringState(true)
            discovery?.startDiscovery()
            delay(2000)
            viewModel.updateIsDiscoveringState(false)
        }
    }

    private suspend fun startDiscoveryService(inetAddresses: List<Pair<InetAddress, NetworkInterface>>) {
        PurpleLogger.current.d(TAG, "startDiscoveryService")
        viewModel.updateIsDiscoveringState(true)
        // 创建 JmDNS 实例
        val inetAddresses = inetAddresses.mapNotNull {
            if (it.first is Inet4Address) {
                it.first
            } else {
                null
            }
        }
        val bonjourDiscovery = BonjourDiscovery(this)
        discovery = bonjourDiscovery
        bonjourDiscovery.startService(inetAddresses)
        delay(2000)
        viewModel.updateIsDiscoveringState(false)
    }

    fun notifyShareDeviceRemovedOnDiscovery(
        discoveryEndpoint: DiscoveryEndpoint,
        shareDeviceList: MutableList<HttpShareDevice>
    ) {
        PurpleLogger.current.d(
            TAG,
            "notifyShareDeviceRemovedOnDiscovery, discoveryEndpoint:$discoveryEndpoint, " +
                    "shareDeviceList:$shareDeviceList"
        )

        val newShareDeviceList = mutableListOf<HttpShareDevice>()
        val shareDeviceListInDiscoveryEndpointRawNameMap =
            shareDeviceList.associateBy { it.deviceName.rawName }

        val oldShareDeviceList =
            viewModel.shareDeviceListState.value.filterIsInstance<HttpShareDevice>()
        val oldShareDeviceListJmdnsMap = oldShareDeviceList.groupBy { it.discoveryEndPoint }
        val notDiscoveryEndpointShareDeviceList =
            oldShareDeviceListJmdnsMap.filter { it.key?.endpointHash() != discoveryEndpoint.endpointHash() }
                .flatMap { it.value }
        newShareDeviceList.addAll(notDiscoveryEndpointShareDeviceList)

        oldShareDeviceListJmdnsMap[discoveryEndpoint]?.forEach {
            if (shareDeviceListInDiscoveryEndpointRawNameMap.containsKey(it.deviceName.rawName)) {
                newShareDeviceList.add(it)
            }
        }
        viewModel.updateShareDeviceListState(newShareDeviceList)
    }

    fun notifyShareDeviceFoundOnDiscovery(
        discoveryEndpoint: DiscoveryEndpoint,
        shareDeviceList: List<HttpShareDevice>
    ) {
        PurpleLogger.current.d(
            TAG,
            "notifyShareDeviceFoundOnDiscovery, discoveryEndpoint:$discoveryEndpoint, shareDeviceList:$shareDeviceList"
        )
        viewModel.updateShareDeviceListWithDiff(this@HttpShareMethod, shareDeviceList)
    }

    override fun onContentReceived(content: Any) {
        when (content) {
            is DataContent.StringContent -> {
                viewModel.onNewReceivedContent(content)
            }

            is DataContent.FileContent -> {
                viewModel.onNewReceivedContent(content)
            }
        }
    }

    override fun onShareDeviceClick(shareDevice: ShareDevice, clickType: Int) {
        super.onShareDeviceClick(shareDevice, clickType)
        PurpleLogger.current.d(
            TAG,
            "onShareDeviceClick, shareDevice:$shareDevice, clickType:$clickType"
        )
        if (shareDevice !is HttpShareDevice) {
            return
        }

        when (clickType) {
            AppSetsShareActivity.CLICK_TYPE_NORMAL -> {
                val pendingSendContentList = viewModel.pendingSendContentList
                if (pendingSendContentList.isEmpty()) {
                    return
                }
                send(listOf(shareDevice))
            }

            AppSetsShareActivity.CLICK_TYPE_LONG -> {
                getDeviceContentList(this, shareDevice)
            }

            AppSetsShareActivity.CLICK_TYPE_DOUBLE -> {

            }
        }
    }

    private fun getDeviceContentList(
        method: HttpShareMethod,
        shareDevice: HttpShareDevice
    ) {
        activity.lifecycleScope.launch {
            requestNotNull(
                action = {
                    appSetsShareRepository.getContentList(shareDevice, uri = "/")
                },
                onSuccess = {
                    viewModel.updateDeviceContentListMap(shareDevice, it.decode())
                },
                onFailed = {

                }
            )
        }
    }

    override fun send(shareDevices: List<ShareDevice>) {
        PurpleLogger.current.d(TAG, "send")
        val sendContentList = viewModel.pendingSendContentList
        if (sendContentList.isEmpty()) {
            PurpleLogger.current.d(TAG, "send, sendContentList is empty, return")
            return
        }
        prepareSendContentRunnableInfoMap(shareDevices, sendContentList)
        activity.lifecycleScope.launch {
            appSetsShareRepository.handleSend(
                this@HttpShareMethod,
                uri = "/",
                sendDirect = false
            )
        }
    }

    fun prepareSendContentRunnableInfoMap(
        shareDevices: List<ShareDevice>,
        sendContentList: MutableList<DataContent>
    ) {
        shareDevices.filterIsInstance<HttpShareDevice>().forEach { shareDevice ->
            val dataSendContentList = sendContentList.map { dataContent ->
                HttpContent(shareDevice, dataContent)
            }
            val sendDataRunnableInfo = SendDataRunnableInfo()
            val sendContentRunnable: SuspendRunnable = suspend {
                appSetsShareRepository.sendContentList(activity, this, dataSendContentList)
            }
            sendDataRunnableInfo.sendDataRunnable = sendContentRunnable
            sendContentRunnableInfoMap.put(shareDevice, sendDataRunnableInfo)
        }
    }

    fun isNeedPin(clientInfo: ClientInfo): Boolean {
        val shareDevice = findShareDeviceForClientInfo(clientInfo)
        if (shareDevice == null) {
            PurpleLogger.current.d(
                TAG,
                "isNeedPin, shareDevice is null, return"
            )
            return true
        }
        PurpleLogger.current.d(
            TAG,
            "isNeedPin, shareDevice:$shareDevice"
        )
        val isNeedPin = isNeedPinState.value
        PurpleLogger.current.d(TAG, "isNeedPin, isNeedPin:$isNeedPin")
        return isNeedPin
    }

    fun showPinToServerSheet(shareDevice: HttpShareDevice) {
        PurpleLogger.current.d(
            TAG,
            "onClientRequestPair, shareDevice:$shareDevice"
        )
        //todo
        /*val bottomSheetState = viewModel.bottomSheetState()
        bottomSheetState.show {
            AppSetsSharePinSheet(
                shareDevice = shareDevice,
                pin = 0,
                onConfirmClick = {

                }
            )
        }*/
    }

    fun onClientRequestPair(pin: Int, clientInfo: ClientInfo) {
        PurpleLogger.current.d(
            TAG,
            "onClientRequestPair, pin:$pin, clientInfo:$clientInfo"
        )
        val shareDevice = findShareDeviceForClientInfo(clientInfo)
        if (shareDevice == null) {
            PurpleLogger.current.d(
                TAG,
                "onClientRequestPair, shareDevice is null, return"
            )
            return
        }
        PurpleLogger.current.d(
            TAG,
            "onClientRequestPair, shareDevice:$shareDevice"
        )

        showPinToClientSheet(shareDevice, pin)
    }

    private fun showPinToClientSheet(shareDevice: HttpShareDevice, pin: Int) {
        val bottomSheetState = viewModel.bottomSheetState()
        bottomSheetState.show {
            AppSetsSharePinSheet(
                shareDevice = shareDevice,
                pin = pin,
                onConfirmClick = {
                    bottomSheetState.hide()
                    handleClientPinRequest(shareDevice, pin)
                }
            )
        }
    }

    fun onSeverPairResponse(shareToken: String, clientInfo: ClientInfo) {
        PurpleLogger.current.d(
            TAG,
            "onSeverPairResponse, shareToken:$shareToken, clientInfo:$clientInfo"
        )
        val shareDevice = findShareDeviceForClientInfo(clientInfo)
        if (shareDevice == null) {
            PurpleLogger.current.d(
                TAG,
                "onSeverPairResponse, shareDevice is null, return"
            )
            return
        }
        PurpleLogger.current.d(
            TAG,
            "onSeverPairResponse, shareDevice:$shareDevice"
        )

        shareDevice.token = shareToken
        if (shareDevice.isPaired) {
            activity.lifecycleScope.launch {
                appSetsShareRepository.resumeHandleSend(
                    shareMethod = this@HttpShareMethod,
                    shareDevice = shareDevice,
                    uri = "/",
                    by = "onSeverPairResponse"
                )
            }
        }
    }

    fun onClientPrepareSend(clientInfo: ClientInfo, uri: String) {
        PurpleLogger.current.d(
            TAG,
            "onClientPrepareSend, clientInfo:$clientInfo, uri:$uri"
        )
        val shareDevice = findShareDeviceForClientInfo(clientInfo)
        if (shareDevice == null) {
            PurpleLogger.current.d(
                TAG,
                "onClientPrepareSend, shareDevice is null, return"
            )
            return
        }
        PurpleLogger.current.d(
            TAG,
            "onClientPrepareSend, shareDevice:$shareDevice"
        )

        if (isAutoAcceptState.value) {
            handleClientPrepareSendRequest(
                shareDevice,
                true,
                isPreferDownloadSelfState.value,
                uri
            )
        } else {
            val bottomSheetState = viewModel.bottomSheetState()
            bottomSheetState.show {
                AppSetsShareClientPreSendSheet(
                    shareDevice = shareDevice,
                    isAutoAccept = isAutoAcceptState.value,
                    onAcceptClick = { isAccept ->
                        bottomSheetState.hide()
                        handleClientPrepareSendRequest(
                            shareDevice,
                            isAccept,
                            isPreferDownloadSelfState.value,
                            uri
                        )
                    },
                    onAutoAcceptChanged = { isAutoAccept ->
                        updateIsAutoAcceptState(isAutoAccept)
                    },
                    onContentListShowClick = {
                        getDeviceContentList(this, shareDevice)
                    }
                )
            }
        }
    }

    fun onServerPrepareSendResponse(
        clientInfo: ClientInfo,
        isAccept: Boolean,
        preferDownloadSelf: Boolean
    ) {
        PurpleLogger.current.d(
            TAG,
            "onServerPrepareSendResponse, clientInfo:$clientInfo, isAccept:$isAccept, " +
                    "preferDownloadSelf:$preferDownloadSelf"
        )
        val shareDevice = findShareDeviceForClientInfo(clientInfo)
        if (shareDevice == null) {
            PurpleLogger.current.d(
                TAG,
                "onServerPrepareSendResponse, shareDevice is null, return"
            )
            return
        }
        PurpleLogger.current.d(
            TAG,
            "onServerPrepareSendResponse, shareDevice:$shareDevice"
        )

        if (preferDownloadSelf) {
            removeSendDataRunnableForDevice(shareDevice, "preferDownloadSelf")
            return
        }
        for ((mappedShareDevice, sendDataRunnableInfo) in sendContentRunnableInfoMap) {
            if (mappedShareDevice == shareDevice) {
                sendDataRunnableInfo?.isAccept = isAccept
                break
            }
        }
        if (isAccept) {
            activity.lifecycleScope.launch {
                appSetsShareRepository.resumeHandleSend(
                    shareMethod = this@HttpShareMethod,
                    shareDevice = shareDevice,
                    uri = "/",
                    by = "onServerPrepareSendResponse"
                )
            }
        }
    }

    private fun handleClientPrepareSendRequest(
        shareDevice: HttpShareDevice,
        isAccept: Boolean,
        isPreferDownloadSelf: Boolean,
        uri: String,
    ) {
        activity.lifecycleScope.launch {
            requestRaw(
                action = {
                    appSetsShareRepository.prepareSendResponse(
                        shareDevice,
                        isAccept,
                        isPreferDownloadSelf
                    )
                }
            )
        }
        if (!isAccept) {
            return
        }
        activity.lifecycleScope.launch {
            if (isPreferDownloadSelf) {
                appSetsShareRepository.handleDownload(
                    this@HttpShareMethod,
                    shareDevice,
                    uri
                )
            }
        }
    }

    private fun handleClientPinRequest(shareDevice: HttpShareDevice, pin: Int) {
        activity.lifecycleScope.launch {
            val shareToken = UUID.randomUUID().toString()
            requestRaw(
                action = {
                    appSetsShareRepository.pairResponse(shareDevice, shareToken)
                },
                onSuccess = {
                    shareDevice.pin = pin
                    shareDevice.token = shareToken
                }
            )
        }
    }


    override fun findShareDeviceForClientInfo(clientInfo: ClientInfo): HttpShareDevice? {
        return super.findShareDeviceForClientInfo(clientInfo) as? HttpShareDevice
    }

    override fun onScanShareDeviceAddress(addresses: Array<String>?) {
        if (addresses.isNullOrEmpty()) {
            return
        }
        addresses.forEach { address ->
            activity.lifecycleScope.launch {
                requestRaw(
                    action = {
                        appSetsShareRepository.exchangeDeviceInfo(this@HttpShareMethod, address)
                    }
                )
            }
        }
    }


    fun updateIsNeedPinState(isNeedPin: Boolean) {
        isNeedPinState.value = isNeedPin
    }

    fun updateIsAutoAcceptState(isAutoAccept: Boolean) {
        isAutoAcceptState.value = isAutoAccept
    }

    fun updateIsPreferDownloadSelfState(isPreferDownloadSelf: Boolean) {
        isPreferDownloadSelfState.value = isPreferDownloadSelf
    }

    fun toggleServer() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val serverBootStateInfo = serverBootStateInfoState.value
        if (serverBootStateInfo is ServerBootStateInfo.Booted) {
            close()
        } else {
            open()
        }
    }

    fun removeSendDataRunnableForDevice(shareDevice: ShareDevice, by: String) {
        PurpleLogger.current.d(TAG, "removeSendDataRunnableForDevice, by:$by")
        val v = sendContentRunnableInfoMap.remove(shareDevice)
        if (v != null) {
            PurpleLogger.current.d(TAG, "removeSendDataRunnableForDevice, remove done!")
        } else {
            PurpleLogger.current.d(TAG, "removeSendDataRunnableForDevice, remove nothing!")
        }
    }

    fun getPendingSendFileList(uri: String): List<DataContent> {
        return viewModel.pendingSendContentList
    }

    fun findContentForContentUri(contentId: String): DataContent? {
        val dataContent = viewModel.pendingSendContentList.firstOrNull {
            it.id == contentId
        }
        return dataContent
    }

    fun exchangeDeviceInfo(shareDevice: HttpShareDevice): HttpShareDevice? {
        viewModel.updateShareDeviceListWithDiff(this, listOf(shareDevice))
        return getCurrentShareDevice()
    }

    fun getCurrentShareDevice(): HttpShareDevice? {
        val mShareDevice = viewModel.mShareDeviceState.value
        return mShareDevice as? HttpShareDevice
    }
}