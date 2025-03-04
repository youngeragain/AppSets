package xcj.app.share.base

import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pGroup
import javax.jmdns.JmDNS

interface ShareDevice {
    companion object {
        const val RAW_NAME = "rawName"
        const val NICK_NAME = "nickName"
        const val DEVICE_TYPE_PHONE = 0
        const val DEVICE_TYPE_TABLET = 1
        const val DEVICE_TYPE_COMPUTER = 2
        const val DEVICE_TYPE_TV = 3
        const val DEVICE_TYPE_WEB_DEVICE = 4
    }

    var deviceAddress: DeviceAddress
    var deviceName: DeviceName
    var deviceType: Int


    data class Base(
        override var deviceAddress: DeviceAddress = DeviceAddress.NONE,
        override var deviceName: DeviceName = DeviceName.NONE,
        override var deviceType: Int = DEVICE_TYPE_PHONE
    ) : ShareDevice

    data class P2pShareDevice(
        val wifiP2pDevice: WifiP2pDevice? = null,
        val wifiP2pGroup: WifiP2pGroup? = null,
        override var deviceName: DeviceName,
        override var deviceAddress: DeviceAddress = DeviceAddress.NONE,
        override var deviceType: Int = DEVICE_TYPE_PHONE
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

    data class HttpShareDevice(
        override var deviceName: DeviceName,
        override var deviceAddress: DeviceAddress = DeviceAddress.NONE,
        override var deviceType: Int = DEVICE_TYPE_PHONE,
        @Transient
        val jmDNS: JmDNS? = null,
        @Transient
        var isNeedPin: Boolean = false,
        @Transient
        var pin: Int = 0,
        //if paired, the server will send token to client, client should update this value
        @Transient
        var token: String? = null
    ) : ShareDevice {
        val isPaired: Boolean
            get() = !token.isNullOrEmpty()

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("HttpShareDevice Info").append("\n")
            sb.append("deviceName:$deviceName").append("\n")
            sb.append("deviceAddress:$deviceAddress").append("\n")
            sb.append("deviceType:$deviceType").append("\n")
            sb.append("isNeedPin:$isNeedPin").append("\n")
            sb.append("pin:$pin").append("\n")
            sb.append("token:$token")
            return sb.toString()
        }

        fun toClientInfo(port: Int): ClientInfo {
            val ip4 = deviceAddress.ip4
            if (ip4.isNullOrEmpty()) {
                return ClientInfo.NONE_RESOLVED
            }
            val host = "$ip4:$port"
            return ClientInfo(host)
        }
    }

    data class RpcShareDevice(
        override var deviceName: DeviceName,
        override var deviceAddress: DeviceAddress = DeviceAddress.NONE,
        override var deviceType: Int = DEVICE_TYPE_PHONE
    ) : ShareDevice
}