package xcj.app.share.util

import xcj.app.starter.android.util.PurpleLogger
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration

object NetworkUtil {

    private const val TAG = "NetworkUtils"
    private const val PREFIX_LOOPBACK = "lo"
    private const val PREFIX_DUMMY = "dummy"
    private const val PREFIX_MOBILE_DATA_RMNET = "rmnet"
    private const val PREFIX_MOBILE_DATA_PPP = "ppp"
    private const val PREFIX_TUN = "tun"
    private const val PREFIX_BLUETOOTH_BT = "bt"
    private const val PREFIX_BLUETOOTH_HCI = "hci"
    private const val PREFIX_WLAN_WLAN = "wlan"
    private const val PREFIX_WLAN_WLP = "wlpXsY"
    private const val PREFIX_ETHERNET_ETH = "eth"
    private const val PREFIX_ETHERNET_ENP = "wlpXsY"
    private const val PREFIX_WIFI_AP = "ap"

    /**
     * key is InetAddress, value is InetAddress's NetworkInterface Human readable name
     */
    fun getAllAvailableLocalInetAddresses(): List<Pair<InetAddress, NetworkInterface>> {
        val result = mutableListOf<Pair<InetAddress, NetworkInterface>>()
        try {
            val networkInterfaces: Enumeration<NetworkInterface>? =
                NetworkInterface.getNetworkInterfaces()
            if (networkInterfaces == null) {
                return result
            }
            while (networkInterfaces.hasMoreElements()) {
                val networkInterface = networkInterfaces.nextElement()
                val descriptionStringBuilder = StringBuilder()
                getNetworkInterfaceDescription(networkInterface, descriptionStringBuilder)
                PurpleLogger.current.d(
                    TAG,
                    "getAllAvailableLocalInetAddresses,$descriptionStringBuilder"
                )
                if (
                    !networkInterface.isUp ||
                    networkInterface.isLoopback ||
                    networkInterface.isVirtual ||
                    networkInterface.name.startsWith(PREFIX_LOOPBACK) ||
                    networkInterface.name.startsWith(PREFIX_DUMMY) ||
                    networkInterface.name.startsWith(PREFIX_MOBILE_DATA_RMNET) ||
                    networkInterface.name.startsWith(PREFIX_MOBILE_DATA_PPP) ||
                    networkInterface.name.startsWith(PREFIX_TUN)
                ) {
                    continue
                }
                val inetAddresses: Enumeration<InetAddress>? = networkInterface.inetAddresses
                if (inetAddresses == null) {
                    continue
                }
                while (inetAddresses.hasMoreElements()) {
                    val inetAddress = inetAddresses.nextElement()
                    // 排除 LoopbackAddress 和 公网地址
                    PurpleLogger.current.d(
                        TAG, "getAllAvailableLocalInetAddresses, inetAddress:$inetAddress\n" +
                                "isLoopbackAddress:${inetAddress.isLoopbackAddress}\n" +
                                "isSiteLocalAddress:${inetAddress.isSiteLocalAddress}\n" +
                                "isLinkLocalAddress:${inetAddress.isLinkLocalAddress}"
                    )
                    if (!inetAddress.isLoopbackAddress && (inetAddress.isSiteLocalAddress || inetAddress.isLinkLocalAddress)) {
                        val hostAddress = inetAddress.hostAddress
                        if (!hostAddress.isNullOrEmpty()) {
                            result.add(inetAddress to networkInterface)
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }

        return result
    }

    fun getNetworkInterfaceHumanReadableName(networkInterface: NetworkInterface): String {
        val prefix =
            if (networkInterface.displayName.startsWith(PREFIX_WIFI_AP)) {
                "Hotspot-"
            } else if (
                networkInterface.displayName.startsWith(PREFIX_MOBILE_DATA_RMNET) ||
                networkInterface.displayName.startsWith(PREFIX_MOBILE_DATA_PPP)
            ) {
                "Mobile Data-"
            } else if (
                networkInterface.displayName.startsWith(PREFIX_ETHERNET_ETH) ||
                networkInterface.displayName.startsWith(PREFIX_ETHERNET_ENP)
            ) {
                "Ethernet-"
            } else if (
                networkInterface.displayName.startsWith(PREFIX_WLAN_WLAN) ||
                networkInterface.displayName.startsWith(PREFIX_WLAN_WLP)
            ) {
                "WIFI-"
            } else if (
                networkInterface.displayName.startsWith(PREFIX_BLUETOOTH_BT) ||
                networkInterface.displayName.startsWith(PREFIX_BLUETOOTH_HCI)
            ) {
                "Bluetooth-"
            } else if (
                networkInterface.displayName.startsWith(PREFIX_TUN)
            ) {
                "TUNNEL-"
            } else {
                ""
            }
        return "$prefix${networkInterface.displayName}"
    }

    private fun getNetworkInterfaceDescription(
        networkInterface: NetworkInterface,
        sb: StringBuilder
    ) {
        val subStringBuilder = StringBuilder()
        while (networkInterface.subInterfaces.hasMoreElements()) {
            val subInterface = networkInterface.subInterfaces.nextElement()
            getNetworkInterfaceDescription(subInterface, subStringBuilder)
        }
        sb.append(
            "networkInterface:$networkInterface\n" +
                    "isLoopback:${networkInterface.isLoopback}\n" +
                    "isVirtual:${networkInterface.isVirtual}\n" +
                    "isUp:${networkInterface.isUp}\n" +
                    "isPointToPoint:${networkInterface.isPointToPoint}\n" +
                    "supportsMulticast:${networkInterface.supportsMulticast()}\n" +
                    "subInterface:${subStringBuilder}"
        )
    }

    fun getHostAddress(inetAddress: InetAddress): String {
        if (inetAddress is Inet6Address) {
            return getInet6HostAddress(inetAddress)
        }
        return inetAddress.hostAddress
    }

    private fun getInet6HostAddress(inet6Address: Inet6Address): String {
        try {
            val hostAddress = inet6Address.hostAddress
            if (hostAddress.isNullOrEmpty()) {
                return "::"
            }
            if (hostAddress.contains("%")) {
                return hostAddress.substringBefore("%")
            }
            return hostAddress
        } catch (e: Exception) {

        }
        return "::"
    }
}