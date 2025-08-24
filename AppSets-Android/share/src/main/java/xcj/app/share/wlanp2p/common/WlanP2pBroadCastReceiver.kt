package xcj.app.share.wlanp2p.common

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import androidx.core.content.IntentCompat
import xcj.app.share.wlanp2p.WlanP2pShareMethod
import xcj.app.starter.android.util.PurpleLogger

class WlanP2pBroadCastReceiver(
    private val shareMethod: WlanP2pShareMethod,
    private val manager: WifiP2pManager,
    private val channel: WifiP2pManager.Channel,
    private val peerListListener: WifiP2pManager.PeerListListener,
    private val connectionListener: WifiP2pManager.ConnectionInfoListener,
    private val deviceInfoListener: WifiP2pManager.DeviceInfoListener,
    private val groupInfoListener: WifiP2pManager.GroupInfoListener
) : BroadcastReceiver() {
    companion object {
        private const val TAG = "WlanP2pBroadCastReceiver"
    }

    @SuppressLint("MissingPermission")
    fun toRequestDeviceInfo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            manager.requestDeviceInfo(channel, deviceInfoListener)
        }
    }

    @SuppressLint("MissingPermission")
    fun toRequestGroupInfo() {
        manager.requestGroupInfo(channel, groupInfoListener)
    }

    @SuppressLint("MissingPermission")
    fun toRequestPeers() {
        manager.requestPeers(channel, peerListListener)
    }

    @SuppressLint("MissingPermission")
    fun toRequestConnectionInfo() {
        manager.requestConnectionInfo(channel, connectionListener)
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                PurpleLogger.current.d(
                    TAG,
                    "onReceive WIFI_P2P_STATE_CHANGED_ACTION"
                )
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                val wifiP2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                shareMethod.updateWlanP2pEnable(wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                toRequestDeviceInfo()
                toRequestGroupInfo()
                toRequestConnectionInfo()
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                PurpleLogger.current.d(
                    TAG,
                    "onReceive WIFI_P2P_PEERS_CHANGED_ACTION"
                )
                // The peer list has changed! We should probably do something about
                // that.
                toRequestPeers()
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                PurpleLogger.current.d(
                    TAG,
                    "onReceive WIFI_P2P_CONNECTION_CHANGED_ACTION"
                )
                // Connection state changed! We should probably do something about
                // that.
                val networkInfo: NetworkInfo? = IntentCompat.getParcelableExtra(
                    intent, WifiP2pManager.EXTRA_NETWORK_INFO,
                    NetworkInfo::class.java
                )
                if (networkInfo == null) {
                    return
                }
                if (networkInfo.isConnected) {
                    // We are connected with the other device, request connection
                    // info to find group owner IP
                    toRequestConnectionInfo()
                }

            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                PurpleLogger.current.d(
                    TAG,
                    "onReceive WIFI_P2P_THIS_DEVICE_CHANGED_ACTION"
                )
                val wifiP2pDevice = IntentCompat.getParcelableExtra(
                    intent, WifiP2pManager.EXTRA_WIFI_P2P_DEVICE,
                    WifiP2pDevice::class.java
                )
                if (wifiP2pDevice == null) {
                    return
                }
                shareMethod.updateShareDeviceState(wifiP2pDevice)
            }
        }
    }
}