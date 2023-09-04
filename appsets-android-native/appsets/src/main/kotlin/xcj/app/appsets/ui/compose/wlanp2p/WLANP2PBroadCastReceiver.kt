package xcj.app.appsets.ui.compose.wlanp2p

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.util.Log

class WLANP2PBroadCastReceiver(
    private val wlanP2PActivity: WLANP2PActivity,
    private val manager: WifiP2pManager?,
    private val channel: WifiP2pManager.Channel?
) : BroadcastReceiver() {
    private val TAG = "WLANP2PBR"
    private val peerListListener = WifiP2pManager.PeerListListener { peerList ->
        val refreshedPeers = peerList.deviceList
        if (refreshedPeers != wlanP2PActivity.wifip2pDeviceListState) {
            wlanP2PActivity.wifip2pDeviceListState.clear()
            wlanP2PActivity.wifip2pDeviceListState.addAll(refreshedPeers)

            // If an AdapterView is backed by this data, notify it
            // of the change. For instance, if you have a ListView of
            // available peers, trigger an update.

            // Perform any other updates needed based on the new list of
            // peers connected to the Wi-Fi P2P network.
        }

        if (wlanP2PActivity.wifip2pDeviceListState.isEmpty()) {
            Log.d(TAG, "No devices found")
            return@PeerListListener
        }
    }
    private val connectionListener = WifiP2pManager.ConnectionInfoListener { info ->
        Log.e(TAG, "onConnectionInfoAvailable info:$info")
        // InetAddress from WifiP2pInfo struct.
        wlanP2PActivity.updateGroupOwner(info)
        // After the group negotiation, we can determine the group owner
    }

    private val deviceInfoListener = WifiP2pManager.DeviceInfoListener {
        wlanP2PActivity.thisWifip2pDeviceState.value = it
    }
    private val groupInfoListener = WifiP2pManager.GroupInfoListener {
        wlanP2PActivity.thisWifip2pDeviceGroupInfoState.value = it
    }

    @SuppressLint("MissingPermission")
    fun toRequestGroupAndDeviceInfo() {
        if (manager != null && channel != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                manager.requestDeviceInfo(channel, deviceInfoListener)
            }
            manager.requestGroupInfo(channel, groupInfoListener)
        }

    }

    @SuppressLint("MissingPermission")
    fun toRequestGroupInfo() {
        if (manager != null && channel != null) {
            manager.requestGroupInfo(channel, groupInfoListener)
        }

    }

    @SuppressLint("MissingPermission")
    fun toRequestPeers() {
        manager?.requestPeers(channel, peerListListener)
    }

    @SuppressLint("MissingPermission")
    fun toRequestConnectionInfo() {
        manager?.requestConnectionInfo(channel, connectionListener)
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context?, intent: Intent) {
        when (intent.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                Log.e(TAG, "onReceive WIFI_P2P_STATE_CHANGED_ACTION")
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                wlanP2PActivity.wifip2pIsEnableState.value =
                    state == WifiP2pManager.WIFI_P2P_STATE_ENABLED
                toRequestGroupAndDeviceInfo()
                toRequestConnectionInfo()
            }

            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                Log.e(TAG, "onReceive WIFI_P2P_PEERS_CHANGED_ACTION")
                // The peer list has changed! We should probably do something about
                // that.
                toRequestPeers()
            }

            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                Log.e(TAG, "onReceive WIFI_P2P_CONNECTION_CHANGED_ACTION")
                // Connection state changed! We should probably do something about
                // that.
                val networkInfo: NetworkInfo? = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO) as? NetworkInfo

                if (networkInfo?.isConnected == true) {

                    // We are connected with the other device, request connection
                    // info to find group owner IP
                    toRequestConnectionInfo()
                }

            }

            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                Log.e(TAG, "onReceive WIFI_P2P_THIS_DEVICE_CHANGED_ACTION")
                (intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE
                ) as? WifiP2pDevice)?.let {
                    wlanP2PActivity.thisWifip2pDeviceState.value = it
                }
            }
        }
    }
}