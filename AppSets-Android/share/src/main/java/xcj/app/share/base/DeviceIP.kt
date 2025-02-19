package xcj.app.share.base

data class DeviceIP(
    val ip: String,
    val type: Int = IP_4
) {
    companion object {
        const val IP_4 = 4
        const val IP_6 = 6
        val LOCALHOST: DeviceIP
            get() = DeviceIP("127.0.0.1")
    }
}