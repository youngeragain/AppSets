package xcj.app.share.http.common

import xcj.app.share.base.DeviceIP

sealed interface ServerBootStateInfo {

    object NotBooted : ServerBootStateInfo

    data class Booted(
        val availableDeviceIp: List<DeviceIP>,
        val availableAddressInfo: List<String>,
        val port: Int,
        val availableIPSuffixesForDevice: String
    ) : ServerBootStateInfo

    data class BootFailed(val reason: String?, val port: Int) : ServerBootStateInfo
}