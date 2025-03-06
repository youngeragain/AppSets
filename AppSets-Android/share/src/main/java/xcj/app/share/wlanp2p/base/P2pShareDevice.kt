package xcj.app.share.wlanp2p.base

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import xcj.app.share.base.DeviceAddress
import xcj.app.share.base.DeviceName
import xcj.app.share.base.ShareDevice

data class P2pShareDevice(
    val wifiP2pDevice: WifiP2pDevice? = null,
    val wifiP2pGroup: WifiP2pGroup? = null,
    override var deviceName: DeviceName,
    override var deviceAddress: DeviceAddress = DeviceAddress.NONE,
    override var deviceType: Int = ShareDevice.DEVICE_TYPE_PHONE
) : ShareDevice {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("P2pShareDevice Info:").append("\n")
        sb.append("wifiP2pDevice:${wifiP2pDevice?.deviceName}").append("\n")
        sb.append("wifiP2pGroup:${wifiP2pGroup?.networkName}").append("\n")
        sb.append("deviceAddress:$deviceAddress").append("\n")
        sb.append("deviceType:$deviceType")
        return sb.toString()
    }
}