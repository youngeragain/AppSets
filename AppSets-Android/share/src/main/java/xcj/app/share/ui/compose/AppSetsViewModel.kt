package xcj.app.share.ui.compose

import BoxFocusInfo
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.share.base.DataContent
import xcj.app.share.base.DataProgressInfo
import xcj.app.share.base.DeviceName
import xcj.app.share.base.ShareDevice
import xcj.app.share.base.ShareMethod
import xcj.app.share.http.HttpShareMethod
import xcj.app.share.wlanp2p.WlanP2pShareMethod
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel
import xcj.app.starter.android.util.FileUtils
import xcj.app.starter.android.util.PurpleLogger
import java.io.File
import java.text.DecimalFormat

class AppSetsShareViewModel : AnyStateViewModel() {

    companion object {
        private const val TAG = "AppSetsShareViewModel"
    }

    private val sendProgressDecimalFormat = DecimalFormat("#.00")
    private val receivedProgressDecimalFormat = DecimalFormat("#.00")

    val shareMethodTypeState: MutableState<Class<*>> =
        mutableStateOf(WlanP2pShareMethod::class.java)

    val pendingSendContentList: MutableList<DataContent> = mutableStateListOf()

    val receivedContentList: MutableList<DataContent> = mutableStateListOf()

    val isDiscoveringState: MutableState<Boolean> = mutableStateOf(false)

    val mShareDeviceState: MutableState<ShareDevice> = mutableStateOf(ShareDevice.Base())

    val shareDeviceListState: MutableState<List<ShareDevice>> = mutableStateOf(emptyList())

    val receiveDataProgressState: MutableState<DataProgressInfo?> =
        mutableStateOf(null)

    val sendDataProgressState: MutableState<DataProgressInfo?> =
        mutableStateOf(null)

    val boxFocusInfo: MutableState<BoxFocusInfo> = mutableStateOf(BoxFocusInfo())

    fun updateBoxFocusInfo(boxFocusInfo: BoxFocusInfo) {
        this.boxFocusInfo.value = boxFocusInfo
    }

    fun onShareMethodDestroy() {
        updateShareDeviceListState(emptyList())
    }

    fun onNewReceivedContent(content: DataContent) {
        PurpleLogger.current.d(
            TAG,
            "onNewReceivedContent, content:\n${content}"
        )
        viewModelScope.launch {
            receivedContentList.add(0, content)
        }
    }

    fun updateDeviceName(shareMethod: ShareMethod, deviceName: DeviceName) {
        PurpleLogger.current.d(
            TAG,
            "updateDeviceName, deviceName:$deviceName, thread:${Thread.currentThread()}"
        )
        when (shareMethod) {
            is WlanP2pShareMethod -> {
                val shareDeviceList =
                    shareDeviceListState.value.filterIsInstance<ShareDevice.P2pShareDevice>()
                if (!shareDeviceList.isEmpty()) {
                    PurpleLogger.current.d(
                        TAG,
                        "updateDeviceName, update ShareDeviceList, oldShareDeviceList:${shareDeviceList}"
                    )
                    val newShareDeviceList = mutableListOf<ShareDevice>()
                    var needToUpdate = false
                    shareDeviceList.forEach {
                        val newShareDevice = if (
                            it.deviceName.rawName == deviceName.rawName
                        ) {
                            needToUpdate = true
                            it.copy(deviceName = deviceName)
                        } else {
                            it.copy()
                        }
                        newShareDeviceList.add(newShareDevice)
                    }
                    PurpleLogger.current.d(
                        TAG,
                        "updateDeviceName, update ShareDeviceList, needToUpdate:${needToUpdate}"
                    )
                    if (needToUpdate) {
                        PurpleLogger.current.d(
                            TAG,
                            "updateDeviceName, update ShareDeviceList, newShareDeviceList:${newShareDeviceList}"
                        )

                        updateShareDeviceListWithDiff(shareMethod, newShareDeviceList)
                        PurpleLogger.current.d(
                            TAG,
                            "updateDeviceName, update ShareDeviceList, done!!!"
                        )
                    }
                }
            }
        }

        val mySelfShareDevice = mShareDeviceState.value
        PurpleLogger.current.d(
            TAG,
            "updateDeviceName, update mySelfShareDeviceName:${mySelfShareDevice.deviceName} ifNeeded, "
        )

        if (mySelfShareDevice.deviceName.rawName == deviceName.rawName) {
            when (mySelfShareDevice) {
                is ShareDevice.P2pShareDevice -> {

                    val newMySelfShareDeviceP2pShareDevice =
                        mySelfShareDevice.copy(deviceName = deviceName)
                    updateShareDeviceState(newMySelfShareDeviceP2pShareDevice)
                    PurpleLogger.current.d(
                        TAG,
                        "updateDeviceName, update mySelfShareDeviceName done!!!"
                    )
                }

                is ShareDevice.HttpShareDevice -> {

                }
            }
        }
    }

    fun updateShareDeviceListWithDiff(
        shareMethod: ShareMethod,
        upcomingDevices: List<ShareDevice>
    ) {
        val shareDeviceList = shareDeviceListState.value
        if (shareDeviceList.isEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "updateShareDeviceListWithDiff, old is empty! update all, thread:${Thread.currentThread()}"
            )
            updateShareDeviceListState(upcomingDevices)
            return
        }
        PurpleLogger.current.d(
            TAG,
            "updateShareDeviceListWithDiff, old is not empty, update with diff, thread:${Thread.currentThread()}"
        )
        when (shareMethod) {
            is WlanP2pShareMethod -> {
                val newShareDeviceList = mutableListOf<ShareDevice>()

                val wifiP2pDeviceMapShareDevice =
                    shareDeviceList.filterIsInstance<ShareDevice.P2pShareDevice>()
                        .associateBy { it.deviceName.rawName }

                upcomingDevices.filterIsInstance<ShareDevice.P2pShareDevice>()
                    .forEach { upcomingDevice ->
                        val existed =
                            wifiP2pDeviceMapShareDevice[upcomingDevice.deviceName.rawName]
                        if (existed != null) {
                            PurpleLogger.current.d(
                                TAG,
                                "updateShareDeviceListWithDiff, upcomingDevice:${upcomingDevice.deviceName} has and old exist!, only change old deviceName"
                            )
                            if (existed.deviceName.nikeName.isNullOrEmpty()) {
                                val newToAdd = existed.copy(deviceName = upcomingDevice.deviceName)
                                newShareDeviceList.add(newToAdd)
                            } else {
                                val newToAdd =
                                    existed.copy()
                                newShareDeviceList.add(newToAdd)
                            }
                        } else {
                            PurpleLogger.current.d(
                                TAG,
                                "updateShareDeviceListWithDiff, upcomingDevice:${upcomingDevice.deviceName} not have old!, use upcoming"
                            )
                            newShareDeviceList.add(upcomingDevice)
                        }
                    }
                updateShareDeviceListState(newShareDeviceList)
            }

            is HttpShareMethod -> {
                val newShareDeviceList = mutableListOf<ShareDevice>()

                val httpDeviceMapShareDevice =
                    shareDeviceList.filterIsInstance<ShareDevice.HttpShareDevice>()
                        .associateBy { it.deviceName.rawName }
                val upcomingDeviceMap = upcomingDevices.associateBy { it.deviceName.rawName }
                httpDeviceMapShareDevice.forEach {
                    if (!upcomingDeviceMap.containsKey(it.key)) {
                        newShareDeviceList.add(it.value)
                    }
                }

                upcomingDevices.forEach { upcomingDevice ->
                    val existed =
                        httpDeviceMapShareDevice[upcomingDevice.deviceName.rawName]
                    if (existed != null) {
                        PurpleLogger.current.d(
                            TAG,
                            "updateShareDeviceListWithDiff, upcomingDevice:${upcomingDevice.deviceName} has and old exist!, only change old deviceAddress"
                        )
                        val newToAdd = existed.copy(deviceAddress = upcomingDevice.deviceAddress)
                        newShareDeviceList.add(newToAdd)
                    } else {
                        PurpleLogger.current.d(
                            TAG,
                            "updateShareDeviceListWithDiff, upcomingDevice:${upcomingDevice.deviceName} not have old!, use upcoming"
                        )
                        newShareDeviceList.add(upcomingDevice)
                    }
                }

                updateShareDeviceListState(newShareDeviceList)
            }
        }
    }

    fun updateShareDeviceListState(shareDeviceList: List<ShareDevice>) {
        PurpleLogger.current.d(
            TAG,
            "updateShareDeviceListState, shareDeviceList:\n${shareDeviceList}"
        )
        viewModelScope.launch {
            shareDeviceListState.value = shareDeviceList

        }
    }

    fun updateShareDeviceState(shareDevice: ShareDevice) {
        PurpleLogger.current.d(
            TAG,
            "updateShareDeviceState, shareDevice:\n${shareDevice}"
        )
        viewModelScope.launch {
            mShareDeviceState.value = shareDevice
        }
    }

    fun updateIsDiscoveringState(isDiscovery: Boolean) {
        isDiscoveringState.value = isDiscovery
    }

    fun <SM : ShareMethod> updateShareMethodTypeState(shareMethodType: Class<SM>) {
        shareMethodTypeState.value = shareMethodType
    }

    fun addPendingContent(context: Context, content: Any) {
        if (content is DataContent) {
            pendingSendContentList.add(0, content)
            return
        }
        if (content is String) {
            pendingSendContentList.add(0, DataContent.StringContent(content))
            return
        }
        if (content is Uri) {
            viewModelScope.launch {
                val androidUriFile = FileUtils.parseFromAndroidUri(context, content)
                pendingSendContentList.add(0, DataContent.UriContent(content, androidUriFile))
            }
            return
        }
        if (content is File) {
            pendingSendContentList.add(0, DataContent.FileContent(content))
            return
        }
    }

    fun updateReceiveDataProgressState(dataProgressInfo: DataProgressInfo) {

        val oldProgress = receiveDataProgressState.value
        if (dataProgressInfo.percentage - (oldProgress?.percentage ?: 0.00) < 0.10) {
            return
        }

        val progressOverride =
            receivedProgressDecimalFormat.format(dataProgressInfo.percentage).toDouble()
        receiveDataProgressState.value = dataProgressInfo.copy(percentage = progressOverride)
        if (dataProgressInfo.percentage >= 99.9) {
            viewModelScope.launch {
                delay(100)
                receiveDataProgressState.value = null
            }
            return
        }
    }

    fun updateSendDataProgressState(dataProgressInfo: DataProgressInfo) {

        val oldProgress = sendDataProgressState.value
        if (dataProgressInfo.percentage - (oldProgress?.percentage ?: 0.00) < 0.10) {
            return
        }
        val progressOverride =
            sendProgressDecimalFormat.format(dataProgressInfo.percentage).toDouble()
        sendDataProgressState.value = dataProgressInfo.copy(percentage = progressOverride)
        if (dataProgressInfo.percentage >= 99.9) {
            viewModelScope.launch {
                delay(100)
                sendDataProgressState.value = null
            }
            return
        }
    }

    fun removeAllReceivedContent() {
        //todo make sync
        receivedContentList.clear()
    }

    fun removePendingSendContent(dataContent: DataContent) {
        //todo make sync
        pendingSendContentList.remove(dataContent)
    }

    fun removeAllPendingSendContent() {
        //todo make sync
        pendingSendContentList.clear()
    }

}