package xcj.app.share.http.base

import xcj.app.share.base.ClientInfo
import xcj.app.share.base.DeviceAddress
import xcj.app.share.base.DeviceName
import xcj.app.share.base.ShareDevice
import xcj.app.share.http.discovery.DiscoveryEndpoint

data class HttpShareDevice(
    override var deviceName: DeviceName,
    override var deviceAddress: DeviceAddress = DeviceAddress.NONE,
    override var deviceType: Int = ShareDevice.DEVICE_TYPE_PHONE,
    @Transient
        val discoveryEndPoint: DiscoveryEndpoint? = null,
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