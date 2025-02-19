package xcj.app.share.util

import xcj.app.starter.android.util.PurpleLogger
import java.net.Inet6Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Enumeration

object NetworkUtil {

    private const val TAG = "NetworkUtils"

    /**
     * key is InetAddress, value is InetAddress's NetworkInterface Human readable name
     */
    fun getAllAvailableLocalInetAddresses(): List<Pair<InetAddress, String>> {
        val ipAddresses = mutableListOf<Pair<InetAddress, String>>()
        try {
            val networkInterfaces: Enumeration<NetworkInterface>? =
                NetworkInterface.getNetworkInterfaces()
            if (networkInterfaces == null) {
                return ipAddresses
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
                    networkInterface.name.startsWith("lo") ||
                    networkInterface.name.startsWith("dummy") ||
                    networkInterface.name.startsWith("rmnet") ||
                    networkInterface.name.startsWith("ppp")
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
                            ipAddresses.add(
                                inetAddress to getNetworkInterfaceHumanReadableName(
                                    networkInterface
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }

        return ipAddresses
    }

    private fun getNetworkInterfaceHumanReadableName(networkInterface: NetworkInterface): String {
        val prefix = if (networkInterface.displayName.startsWith("ap")) {
            "Hotspot-"
        } else if (networkInterface.displayName.startsWith("rmnet") || networkInterface.displayName.startsWith(
                "ppp"
            )
        ) {
            "Mobile Data-"
        } else if (networkInterface.displayName.startsWith("eth") || networkInterface.displayName.startsWith(
                "enpXsY"
            )
        ) {
            "Ethernet-"
        } else if (networkInterface.displayName.startsWith("wlan") || networkInterface.displayName.startsWith(
                "wlpXsY"
            )
        ) {
            "WIFI-"
        } else if (networkInterface.displayName.startsWith("bt") || networkInterface.displayName.startsWith(
                "hci"
            )
        ) {
            "Bluetooth-"
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