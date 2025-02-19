package xcj.app.share.base

data class DeviceAddress(
    val ips: List<DeviceIP> = emptyList()
) {
    val ip4: String?
        get() {
            return ips.firstOrNull {
                it.type == DeviceIP.IP_4
            }?.ip
        }

    val ip6: String?
        get() {
            return ips.firstOrNull {
                it.type == DeviceIP.IP_6
            }?.ip
        }

    fun containsIp(inIp: String): Boolean {
        for (ip in ips) {
            if (inIp == ip.ip) {
                return true
            }
        }
        return false
    }

    companion object {
        val NONE: DeviceAddress
            get() = DeviceAddress()
    }
}
