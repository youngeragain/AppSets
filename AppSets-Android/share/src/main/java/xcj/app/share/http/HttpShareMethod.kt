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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.share.base.ClientInfo
import xcj.app.share.base.DataContent
import xcj.app.share.base.DataSendContent
import xcj.app.share.base.DeviceAddress
import xcj.app.share.base.DeviceIP
import xcj.app.share.base.DeviceName
import xcj.app.share.base.ShareDevice
import xcj.app.share.base.ShareMethod
import xcj.app.share.http.common.ServerBootStateInfo
import xcj.app.share.http.repository.AppSetsShareRepository
import xcj.app.share.ui.compose.AppSetsShareActivity
import xcj.app.share.ui.compose.AppSetsShareViewModel
import xcj.app.share.util.NetworkUtil
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.requestNotNull
import xcj.app.starter.server.requestRaw
import xcj.app.starter.test.LocalPurpleCoroutineScope
import xcj.app.web.webserver.base.DataProgressInfo
import xcj.app.web.webserver.base.ProgressListener
import xcj.app.web.webserver.interfaces.ContentReceivedListener
import xcj.app.web.webserver.interfaces.ListenersProvider
import xcj.app.web.webserver.netty.ServerBootStrap
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

typealias SuspendRunnable = suspend () -> Unit

class HttpShareMethod : ShareMethod(), ContentReceivedListener, ServiceListener, ListenersProvider {
    companion object {
        private const val TAG = "HttpShareMethod"
        const val NAME = "HTTP"
        private const val DNS_SERVER_PORT = 11100
        private const val DNS_SERVER_TYPE = "_http._tcp.local."
        const val SHARE_SERVER_API_PORT = 11101
        const val SHARE_SERVER_FILE_API_PORT = 11102
    }

    data class SendDataRunnableInfo(
        val sendDataRunnable: SuspendRunnable,
        var isAccept: Boolean = false,
        var isCalled: Boolean = false,
        var isFinished: Boolean = false
    )

    private val appSetsShareRepository = AppSetsShareRepository()

    private var jmDNSMap: MutableMap<String, JmDNS> = mutableMapOf()

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

    val sendContentRunnableInfoMap: ConcurrentHashMap<ShareDevice.HttpShareDevice, SendDataRunnableInfo?> =
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
        val shareDevice =
            ShareDevice.HttpShareDevice(deviceName = mDeviceName)
        viewModel.updateShareDeviceState(shareDevice)
        open()
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
                    startServiceDiscovery(allAvailableLocalInetAddresses)
                }
            }

            override fun onFailure(reason: String?) {
                PurpleLogger.current.d(TAG, "open, onFailure")
                serverBootStateInfoState.value =
                    ServerBootStateInfo.BootFailed(reason, SHARE_SERVER_API_PORT)
            }

        }
        serverBootStrap = ServerBootStrap(SHARE_SERVER_API_PORT, SHARE_SERVER_FILE_API_PORT, this)
        activity.lifecycleScope.launch {
            serverBootStrap?.main(activity.application, actionLister)
        }
    }

    private fun makeBootedInfo(
        availableLocalInetAddresses: List<Pair<InetAddress, String>>
    ): ServerBootStateInfo.Booted {

        val allAvailableAddressInfo = availableLocalInetAddresses.map {
            val hostAddress = NetworkUtil.getHostAddress(it.first)
            "${it.second} | $hostAddress"
        }
        val availableIPSuffixesForDevice = availableLocalInetAddresses.mapNotNull {
            val inetAddress = it.first
            val hostAddress = NetworkUtil.getHostAddress(inetAddress)
            if (inetAddress is Inet4Address && !hostAddress.isEmpty()) {
                val lastIndexOf = hostAddress.lastIndexOf('.')
                val substring = hostAddress.substring(lastIndexOf + 1)
                "#$substring"
            } else if (inetAddress is Inet6Address && !hostAddress.isEmpty()) {
                if (!hostAddress.startsWith("fe80::")) {
                    null
                } else {
                    "#${hostAddress.substringAfter("fe80::")}"
                }
            } else {
                null
            }
        }.joinToString(separator = " ", prefix = " ")
        return ServerBootStateInfo.Booted(
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
            cancelServiceDiscovery()
            serverBootStrap?.close(actionLister)
        }
    }

    private suspend fun cancelServiceDiscovery() {
        PurpleLogger.current.d(TAG, "cancelServiceDiscovery")
        withContext(Dispatchers.IO) {
            runCatching {
                jmDNSMap.values.forEach {
                    it.removeServiceListener(DNS_SERVER_TYPE, this@HttpShareMethod)
                    it.unregisterAllServices()
                    it.close()
                }

            }.onSuccess {
                PurpleLogger.current.d(TAG, "cancelServiceDiscovery, success")
            }.onFailure {
                PurpleLogger.current.d(TAG, "cancelServiceDiscovery, failed!")
            }
        }
    }

    override fun discovery() {
        activity.lifecycleScope.launch {
            viewModel.updateIsDiscoveringState(true)
            withContext(Dispatchers.IO) {
                jmDNSMap.values.forEach {
                    it.requestServiceInfo(DNS_SERVER_TYPE, mDeviceName.nikeName)
                }
            }
            delay(2000)
            viewModel.updateIsDiscoveringState(false)
        }
    }

    private suspend fun startServiceDiscovery(allAvailableLocalInetAddresses: List<Pair<InetAddress, String>>) {
        PurpleLogger.current.d(TAG, "startServiceDiscovery")
        withContext(Dispatchers.IO) {
            viewModel.updateIsDiscoveringState(true)
            // 创建 JmDNS 实例
            allAvailableLocalInetAddresses.mapNotNull {
                if (it.first is Inet4Address) {
                    it.first
                } else {
                    null
                }
            }.forEach { inet4Address ->
                val jmdns = JmDNS.create(inet4Address)
                // 创建服务信息
                val serviceInfo = ServiceInfo.create(
                    DNS_SERVER_TYPE,
                    mDeviceName.nikeName,
                    DNS_SERVER_PORT,
                    0,
                    0,
                    false,
                    mapOf<String, String?>(
                        ShareDevice.RAW_NAME to mDeviceName.rawName,
                        ShareDevice.NICK_NAME to mDeviceName.nikeName
                    )
                )
                // 注册服务
                runCatching {
                    jmdns.registerService(serviceInfo)
                    PurpleLogger.current.d(
                        TAG,
                        "startServiceDiscovery, Listening for HTTP services... on hostAddress:${inet4Address.hostAddress}"
                    )
                    jmdns.addServiceListener(DNS_SERVER_TYPE, this@HttpShareMethod) // 添加服务监听器
                    jmDNSMap.put(inet4Address.hostAddress ?: "", jmdns)
                }.onSuccess {
                    PurpleLogger.current.d(
                        TAG,
                        "startServiceDiscovery, Service registered:${serviceInfo.qualifiedName},  on hostAddress:${inet4Address.hostAddress}"
                    )
                }
            }
            delay(2000)
            viewModel.updateIsDiscoveringState(false)
        }
    }

    override fun serviceAdded(event: ServiceEvent) {
        PurpleLogger.current.d(TAG, "serviceAdded, $event")
    }

    override fun serviceRemoved(event: ServiceEvent) {
        PurpleLogger.current.d(TAG, "serviceRemoved, $event")
        val serviceInfos = event.dns.list(DNS_SERVER_TYPE, 2000)
        PurpleLogger.current.d(TAG, "serviceRemoved, serviceInfos:${serviceInfos.joinToString()}")

        val shareDeviceListInJmdns = mutableListOf<ShareDevice.HttpShareDevice>()
        serviceInfos.forEach { serviceInfo ->
            val rawName = serviceInfo.getPropertyString(ShareDevice.RAW_NAME)
            val nickName = serviceInfo.getPropertyString(ShareDevice.NICK_NAME)
            if (!rawName.isNullOrEmpty() && !nickName.isNullOrEmpty()) {
                val deviceName = DeviceName(rawName, nickName)
                val ips = serviceInfo.inetAddresses.mapNotNull {
                    val deviceIP = if (it is Inet4Address) {
                        DeviceIP(it.hostAddress ?: "", DeviceIP.IP_4)
                    } else if (it is Inet6Address) {
                        DeviceIP(it.hostAddress ?: "", DeviceIP.IP_6)
                    } else {
                        null
                    }
                    deviceIP
                }
                val deviceAddress = DeviceAddress(ips = ips)
                shareDeviceListInJmdns.add(ShareDevice.HttpShareDevice(deviceName, deviceAddress))
            }
        }

        notifyShareDeviceRemovedOnJmdns(event.dns, shareDeviceListInJmdns)
    }

    private fun notifyShareDeviceRemovedOnJmdns(
        jmDNS: JmDNS,
        shareDeviceListInJmdns: MutableList<ShareDevice.HttpShareDevice>
    ) {
        PurpleLogger.current.d(
            TAG,
            "notifyShareDeviceRemoved, jmDNS:$jmDNS, shareDeviceListInJmdns:$shareDeviceListInJmdns"
        )

        val newShareDeviceList = mutableListOf<ShareDevice.HttpShareDevice>()
        val shareDeviceListInJmdnsRawNameMap =
            shareDeviceListInJmdns.associateBy { it.deviceName.rawName }

        val oldShareDeviceList =
            viewModel.shareDeviceListState.value.filterIsInstance<ShareDevice.HttpShareDevice>()
        val oldShareDeviceListJmdnsMap = oldShareDeviceList.groupBy { it.jmDNS }
        val notJmdnsShareDeviceList =
            oldShareDeviceListJmdnsMap.filter { it.key != jmDNS }.flatMap { it.value }
        newShareDeviceList.addAll(notJmdnsShareDeviceList)

        oldShareDeviceListJmdnsMap.get(jmDNS)?.forEach {
            if (shareDeviceListInJmdnsRawNameMap.containsKey(it.deviceName.rawName)) {
                newShareDeviceList.add(it)
            }
        }
        viewModel.updateShareDeviceListState(newShareDeviceList)
    }

    private fun notifyShareDeviceFoundOnJmdns(
        jmDNS: JmDNS,
        shareDeviceList: List<ShareDevice.HttpShareDevice>
    ) {
        PurpleLogger.current.d(
            TAG,
            "notifyShareDeviceFoundOnJmdns, jmDNS:$jmDNS, shareDeviceList:$shareDeviceList"
        )
        viewModel.updateShareDeviceListWithDiff(
            this@HttpShareMethod,
            shareDeviceList
        )
    }

    override fun serviceResolved(event: ServiceEvent) {
        PurpleLogger.current.d(TAG, "serviceResolved, $event")
        val rawName = event.info.getPropertyString(ShareDevice.RAW_NAME)
        val nickName = event.info.getPropertyString(ShareDevice.NICK_NAME)
        if (rawName.isNullOrEmpty() || nickName.isNullOrEmpty()) {
            return
        }
        val deviceName = DeviceName(rawName, nickName)
        val serverBootStateInfo = serverBootStateInfoState.value
        var isSelf = false
        val ips = event.info.inetAddresses.mapNotNull {
            val deviceIP = if (it is Inet4Address) {
                DeviceIP(it.hostAddress ?: "", DeviceIP.IP_4)
            } else if (it is Inet6Address) {
                DeviceIP(it.hostAddress ?: "", DeviceIP.IP_6)
            } else {
                null
            }
            if (deviceIP != null && serverBootStateInfo is ServerBootStateInfo.Booted) {
                val contains =
                    serverBootStateInfo.availableAddressInfo.firstOrNull { it.contains(deviceIP.ip) } != null
                if (contains) {
                    isSelf = true
                }
            }
            deviceIP
        }
        if (isSelf) {
            return
        }
        val deviceAddress = DeviceAddress(ips = ips)
        val shareDeviceList = mutableListOf<ShareDevice.HttpShareDevice>()
        shareDeviceList.add(
            ShareDevice.HttpShareDevice(
                deviceName,
                deviceAddress,
                jmDNS = event.dns
            )
        )
        notifyShareDeviceFoundOnJmdns(event.dns, shareDeviceList)
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
        if (shareDevice !is ShareDevice.HttpShareDevice) {
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
        shareDevice: ShareDevice.HttpShareDevice
    ) {
        activity.lifecycleScope.launch {
            requestNotNull(
                action = {
                    appSetsShareRepository.getContentList(shareDevice, contentListId = "/")
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
                contentListId = "/",
                sendDirect = false
            )
        }
    }

    fun prepareSendContentRunnableInfoMap(
        shareDevices: List<ShareDevice>,
        sendContentList: MutableList<DataContent>
    ) {
        shareDevices.filterIsInstance<ShareDevice.HttpShareDevice>().forEach { shareDevice ->
            val dataSendContentList = sendContentList.map { dataContent ->
                DataSendContent.HttpContent(shareDevice, dataContent)
            }
            val sendContentRunnable: SuspendRunnable = {
                requestRaw(
                    action = {
                        appSetsShareRepository.sendContentList(activity, this, dataSendContentList)
                    }
                )
            }
            val sendDataRunnableInfo = SendDataRunnableInfo(sendContentRunnable)
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
                    contentListId = "/",
                    by = "onSeverPairResponse"
                )
            }
        }
    }

    fun onClientPrepareSend(clientInfo: ClientInfo, contentListId: String) {
        PurpleLogger.current.d(
            TAG,
            "onClientPrepareSend, clientInfo:$clientInfo, contentListId:$contentListId"
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
                contentListId
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
                            contentListId
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
            "onServerPrepareSendResponse, clientInfo:$clientInfo, isAccept:$isAccept, preferDownloadSelf:$preferDownloadSelf"
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
                    contentListId = "/",
                    by = "onServerPrepareSendResponse"
                )
            }
        }
    }

    private fun handleClientPrepareSendRequest(
        shareDevice: ShareDevice.HttpShareDevice,
        isAccept: Boolean,
        isPreferDownloadSelf: Boolean,
        contentListId: String,
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
                    contentListId
                )
            }
        }
    }

    private fun handleClientPinRequest(shareDevice: ShareDevice.HttpShareDevice, pin: Int) {
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


    override fun findShareDeviceForClientInfo(clientInfo: ClientInfo): ShareDevice.HttpShareDevice? {
        return super.findShareDeviceForClientInfo(clientInfo) as? ShareDevice.HttpShareDevice
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

    fun getPendingSendFileList(contentListUri: String): List<DataContent> {
        return viewModel.pendingSendContentList
    }

    fun findContentForContentUri(contentId: String): DataContent? {
        val dataContent = viewModel.pendingSendContentList.firstOrNull {
            it.id == contentId
        }
        return dataContent
    }
}