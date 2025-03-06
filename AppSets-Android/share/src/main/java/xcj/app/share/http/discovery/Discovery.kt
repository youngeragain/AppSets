package xcj.app.share.http.discovery

import java.net.InetAddress

interface Discovery {

    suspend fun startService(inetAddressList: List<InetAddress>)

    suspend fun stopService()

    suspend fun startDiscovery()

    suspend fun cancelDiscovery()
}