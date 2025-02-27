package xcj.app.share.base

import BoxFocusInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.share.http.HttpShareMethod
import xcj.app.share.rpc.RpcShareMethod
import xcj.app.share.ui.compose.AppSetsShareActivity
import xcj.app.share.ui.compose.AppSetsShareViewModel
import xcj.app.share.wlanp2p.WlanP2pShareMethod
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.ShareSystem
import xcj.app.web.webserver.interfaces.ProgressListener
import java.util.concurrent.CopyOnWriteArrayList

abstract class ShareMethod() : ConnectivityManager.NetworkCallback(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "ShareMethod"
        fun <SM : ShareMethod> createInstanceByType(shareMethodType: Class<SM>): ShareMethod {
            when (shareMethodType) {
                WlanP2pShareMethod::class.java -> {
                    return WlanP2pShareMethod()
                }

                HttpShareMethod::class.java -> {
                    return HttpShareMethod()
                }

                RpcShareMethod::class.java -> {
                    return RpcShareMethod()
                }
            }
            throw NotImplementedError()
        }
    }

    val shareContentLocationState: MutableState<String> = mutableStateOf("")

    open var mDeviceName: DeviceName = DeviceName.NONE

    internal lateinit var activity: AppSetsShareActivity

    internal lateinit var viewModel: AppSetsShareViewModel

    private lateinit var connectivityManager: ConnectivityManager

    private val sendProgressListeners: CopyOnWriteArrayList<ProgressListener> =
        CopyOnWriteArrayList()

    private val receiveProgressListeners: CopyOnWriteArrayList<ProgressListener> =
        CopyOnWriteArrayList()


    override fun onAvailable(network: Network) {
        PurpleLogger.current.d(TAG, "onAvailable, netWork:$network")
    }

    override fun onLost(network: Network) {
        PurpleLogger.current.d(TAG, "network, netWork:$network")
    }

    open fun init(activity: AppSetsShareActivity, viewModel: AppSetsShareViewModel) {
        activity.lifecycle.removeObserver(this)
        activity.lifecycle.addObserver(this)
        shareContentLocationState.value = ShareSystem.getShareDirPath()
        this.viewModel = viewModel
        this.activity = activity
        this.connectivityManager = activity.getSystemService(ConnectivityManager::class.java)
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        this.connectivityManager.registerNetworkCallback(networkRequest, this)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
    }

    open fun open() {

    }

    open fun close() {

    }

    open fun destroy() {
        if (!::viewModel.isInitialized) {
            return
        }
        viewModel.onShareMethodDestroy()
    }

    open fun send(shareDevices: List<ShareDevice>) {

    }

    open fun addSendDataProgressListener(progressListener: ProgressListener) {
        sendProgressListeners.add(progressListener)
    }

    open fun removeReceiveDataProgressListener(progressListener: ProgressListener) {
        receiveProgressListeners.remove(progressListener)
    }

    open fun discovery() {

    }

    open fun onShareDeviceClick(shareDevice: ShareDevice, clickType: Int) {
        if (clickType == AppSetsShareActivity.CLICK_TYPE_NORMAL) {
            viewModel.pendingSendContentList.ifEmpty {
                viewModel.updateBoxFocusInfo(BoxFocusInfo(sendBoxFocus = true))
            }
        }
    }

    open fun sendAll() {
        activity.lifecycleScope.launch {
            val shareDevices = viewModel.shareDeviceListState.value
            shareDevices.forEach { shareDevice ->
                onShareDeviceClick(shareDevice, AppSetsShareActivity.CLICK_TYPE_NORMAL)
                delay(1000)
            }
        }
    }

    open fun findShareDeviceForClientInfo(clientInfo: ClientInfo): ShareDevice? {
        if (clientInfo.host.isEmpty()) {
            return null
        }
        if (!clientInfo.host.contains('.') && !clientInfo.host.contains(":")) {
            return null
        }
        val hostIp = clientInfo.host.substringBefore(":")
        val shareDevices = viewModel.shareDeviceListState.value
        for (shareDevice in shareDevices) {
            if (shareDevice.deviceAddress.containsIp(hostIp)) {
                return shareDevice
            }
        }
        return null
    }

}