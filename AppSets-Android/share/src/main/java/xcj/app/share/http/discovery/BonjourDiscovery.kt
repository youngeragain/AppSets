package xcj.app.share.http.discovery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.share.base.DeviceAddress
import xcj.app.share.base.DeviceIP
import xcj.app.share.base.DeviceName
import xcj.app.share.base.ShareDevice
import xcj.app.share.http.HttpShareMethod
import xcj.app.share.http.base.HttpShareDevice
import xcj.app.share.http.common.ServerBootStateInfo
import xcj.app.starter.android.util.PurpleLogger
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

class BonjourDiscovery(
    private val httpShareMethod: HttpShareMethod
) : Discovery, ServiceListener {
    companion object {
        private const val TAG = "BonjourDiscovery"
        private const val BONJOUR_PORT = 11100
        private const val BONJOUR_SERVER_TYPE = "_http._tcp.local."
    }

    private var jmDNSMap: MutableMap<String, JmDNS> = mutableMapOf()

    override suspend fun startService(inetAddressList: List<InetAddress>) = withContext(
        Dispatchers.IO
    ) {
        val currentShareDevice = httpShareMethod.getCurrentShareDevice() ?: return@withContext
        inetAddressList.forEach { inetAddress ->
            val jmdns = JmDNS.create(inetAddress)
            val serviceInfo = ServiceInfo.create(
                BONJOUR_SERVER_TYPE,
                currentShareDevice.deviceName.nickName,
                BONJOUR_PORT,
                0,
                0,
                false,
                mapOf<String, String?>(
                    ShareDevice.RAW_NAME to currentShareDevice.deviceName.rawName,
                    ShareDevice.NICK_NAME to currentShareDevice.deviceName.nickName,
                    ShareDevice.DEVICE_TYPE to currentShareDevice.deviceType.toString()
                )
            )
            runCatching {
                jmdns.registerService(serviceInfo)
                PurpleLogger.current.d(
                    TAG,
                    "startService, Listening for HTTP services... on hostAddress:${inetAddress.hostAddress}"
                )
                jmdns.addServiceListener(BONJOUR_SERVER_TYPE, this@BonjourDiscovery) // 添加服务监听器
                jmDNSMap.put(inetAddress.hostAddress ?: "", jmdns)
            }.onSuccess {
                PurpleLogger.current.d(
                    TAG,
                    "startService, Service registered:${serviceInfo.qualifiedName},  on hostAddress:${inetAddress.hostAddress}"
                )
            }
        }
    }

    override suspend fun stopService() {
        withContext(Dispatchers.IO) {
            runCatching {
                jmDNSMap.values.forEach { jmDNS ->
                    jmDNS.removeServiceListener(BONJOUR_SERVER_TYPE, this@BonjourDiscovery)
                    jmDNS.unregisterAllServices()
                    jmDNS.close()
                }

            }.onSuccess {
                PurpleLogger.current.d(TAG, "stopService, success")
            }.onFailure {
                PurpleLogger.current.d(TAG, "stopService, failed!")
            }
        }
    }

    override suspend fun startDiscovery() {
        withContext(Dispatchers.IO) {
            jmDNSMap.values.forEach { jmDNS ->
                parseDnsServiceInfos(jmDNS)
            }
        }
    }

    override suspend fun cancelDiscovery() {

    }

    override fun serviceAdded(event: ServiceEvent) {
        PurpleLogger.current.d(TAG, "serviceAdded, $event")
    }

    override fun serviceRemoved(event: ServiceEvent) {
        PurpleLogger.current.d(TAG, "serviceRemoved, $event")
        parseDnsServiceInfos(event.dns)
    }

    private fun parseDnsServiceInfos(jmDNS: JmDNS) {
        val serviceInfos = jmDNS.list(BONJOUR_SERVER_TYPE, 2000)
        PurpleLogger.current.d(
            TAG,
            "parseDnsServiceInfos, serviceInfos:${serviceInfos.joinToString()}"
        )

        val shareDeviceListInDiscoveryEndPoint = mutableListOf<HttpShareDevice>()
        val bonjourDiscoveryEndpoint = BonjourDiscoveryEndpoint(jmDNS)
        serviceInfos.forEach { serviceInfo ->
            val rawName = serviceInfo.getPropertyString(ShareDevice.RAW_NAME)
            val nickName = serviceInfo.getPropertyString(ShareDevice.NICK_NAME)
            val deviceType = serviceInfo.getPropertyString(ShareDevice.DEVICE_TYPE)?.toIntOrNull()
                ?: ShareDevice.DEVICE_TYPE_PHONE
            if (!rawName.isNullOrEmpty() && !nickName.isNullOrEmpty()) {
                val deviceName = DeviceName(rawName, nickName)
                val ips = serviceInfo.inetAddresses.mapNotNull {
                    val deviceIP = if (it is Inet4Address) {
                        DeviceIP(it.hostAddress ?: "", DeviceIP.IP_4)
                    } else if (it is Inet6Address) {
                        DeviceIP(it.hostAddress ?: "", DeviceIP.IP_6)
                    } else {
                        null
                    }
                    deviceIP
                }
                val deviceAddress = DeviceAddress(ips = ips)
                val httpShareDevice =
                    HttpShareDevice(deviceName, deviceAddress, deviceType, bonjourDiscoveryEndpoint)
                shareDeviceListInDiscoveryEndPoint.add(httpShareDevice)
            }
        }

        httpShareMethod.notifyShareDeviceRemovedOnDiscovery(
            bonjourDiscoveryEndpoint,
            shareDeviceListInDiscoveryEndPoint
        )
    }

    private fun parseServiceInfo(jmDNS: JmDNS, serviceInfo: ServiceInfo) {
        val rawName = serviceInfo.getPropertyString(ShareDevice.RAW_NAME)
        val nickName = serviceInfo.getPropertyString(ShareDevice.NICK_NAME)
        val deviceType = serviceInfo.getPropertyString(ShareDevice.DEVICE_TYPE)?.toIntOrNull()
            ?: ShareDevice.DEVICE_TYPE_PHONE
        if (rawName.isNullOrEmpty() || nickName.isNullOrEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "parseServiceInfo, rawName isNullOrEmpty or nickName isNullOrEmpty, return"
            )
            return
        }
        val deviceName = DeviceName(rawName, nickName)
        val serverBootStateInfo = httpShareMethod.serverBootStateInfoState.value
        var isSelf = false
        val ips = serviceInfo.inetAddresses.mapNotNull {
            val deviceIP = if (it is Inet4Address) {
                DeviceIP(it.hostAddress ?: "", DeviceIP.IP_4)
            } else if (it is Inet6Address) {
                DeviceIP(it.hostAddress ?: "", DeviceIP.IP_6)
            } else {
                null
            }
            if (deviceIP != null && serverBootStateInfo is ServerBootStateInfo.Booted) {
                val contains =
                    serverBootStateInfo.availableAddressInfo.firstOrNull { it.contains(deviceIP.ip) } != null
                if (contains) {
                    isSelf = true
                }
            }
            deviceIP
        }
        if (isSelf) {
            PurpleLogger.current.d(TAG, "parseServiceInfo, isSelf, return")
            return
        }
        val deviceAddress = DeviceAddress(ips = ips)
        val shareDeviceListInEndPoint = mutableListOf<HttpShareDevice>()
        val bonjourDiscoveryEndpoint = BonjourDiscoveryEndpoint(jmDNS)
        val httpShareDevice = HttpShareDevice(
            deviceName,
            deviceAddress,
            deviceType,
            bonjourDiscoveryEndpoint
        )
        shareDeviceListInEndPoint.add(
            httpShareDevice
        )
        httpShareMethod.notifyShareDeviceFoundOnDiscovery(
            bonjourDiscoveryEndpoint,
            shareDeviceListInEndPoint
        )
    }

    override fun serviceResolved(event: ServiceEvent) {
        PurpleLogger.current.d(TAG, "serviceResolved, $event")
        parseServiceInfo(event.dns, event.info)
    }
}