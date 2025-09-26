package xcj.app.share.wlanp2p.common

import android.content.Context
import xcj.app.share.base.DataSendContent
import xcj.app.share.base.DeviceAddress
import xcj.app.share.base.DeviceIP
import xcj.app.share.base.DeviceName
import xcj.app.share.base.DeviceNameAddress
import xcj.app.share.base.DeviceNameExchangeListener
import xcj.app.share.base.ShareDevice
import xcj.app.share.util.ShareSystem
import xcj.app.share.wlanp2p.base.DataHandleExceptionListener
import xcj.app.share.wlanp2p.base.ISocketExceptionListener
import xcj.app.share.wlanp2p.base.IThreadWriter
import xcj.app.share.wlanp2p.base.LogicEstablishListener
import xcj.app.share.wlanp2p.base.P2pShareDevice
import xcj.app.share.wlanp2p.base.WlanP2pContent
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.interfaces.ContentReceivedListener
import xcj.app.web.webserver.interfaces.ProgressListener
import java.io.Closeable
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class P2pOneThread(
    private val shareDevice: P2pShareDevice,
    private val host: String? = null,
    private val port: Int = SERVER_PORT,
    private val readProgressListener: ProgressListener?,
    private val writeProgressListener: ProgressListener?,
    private val contentReceivedListener: ContentReceivedListener?,
    private val logicEstablishListener: LogicEstablishListener?,
    private val socketExceptionListener: ISocketExceptionListener?,
    private val deviceNameExchangeListener: DeviceNameExchangeListener?
) : Thread(), IThreadWriter {
    companion object {
        private const val TAG = "P2pOneThread"
        const val SERVER_PORT = 10101
    }

    private val isServer: Boolean
        get() {
            return host.isNullOrEmpty()
        }

    //if is server this will be ServerSocket,
    //if is client this will be Socket
    private var socket: Closeable? = null

    private val clientRWThreadMap: MutableMap<P2pShareDevice, ClientRWThread> =
        mutableMapOf()

    private var serverClosed = true

    private var clientClosed = true

    override suspend fun writeContent(
        context: Context,
        dataSendContent: DataSendContent
    ) {
        if(dataSendContent !is WlanP2pContent){
            return
        }
        if (isServer) {
            if (serverClosed) {
                return
            }
            val wifiP2pDevice = dataSendContent.dstDevice
            var clientRWThread: ClientRWThread? = null
            for ((shareDevice, readWriteThread) in clientRWThreadMap) {
                if (shareDevice.deviceName.rawName == wifiP2pDevice.deviceName.rawName) {
                    clientRWThread = readWriteThread
                    break
                }
            }
            val writeThread = clientRWThread?.writeThread
            PurpleLogger.current.d(
                TAG,
                """
                    writeContent, mCurrent is Server
                    clientReadWriteThreadMap:${clientRWThreadMap}
                    forDevice:${dataSendContent.dstDevice}
                    writeThread:$writeThread
                """.trimIndent()
            )
            writeThread?.writeContent(context, dataSendContent)
        } else {
            if (clientClosed) {
                return
            }
            var clientRWThread: ClientRWThread? = null
            clientRWThread = clientRWThreadMap.values.firstOrNull()
            val writeThread = clientRWThread?.writeThread
            PurpleLogger.current.d(
                TAG,
                """
                    writeContent, mCurrent is Client
                    clientReadWriteThreadMap:${clientRWThreadMap}
                    forDevice:${dataSendContent.dstDevice}
                    writeThread:$writeThread
                """.trimIndent()
            )
            writeThread?.writeContent(context, dataSendContent)
        }
    }

    override fun run() {
        if (isServer) {
            runAsServer()
        } else {
            runAsClient()
        }
    }

    private fun runAsClient() {
        val host = host ?: return
        try {
            val clientSocket = Socket()
            clientSocket.bind(null)
            PurpleLogger.current.d(TAG, "runAsClient, socket opened")
            clientSocket.connect(InetSocketAddress(host, port), 5000)
            setupSocketStream(clientSocket, shareDevice)
            this.socket = clientSocket
            clientClosed = false
            PurpleLogger.current.d(TAG, "runAsClient, done")
        } catch (e: Exception) {
            PurpleLogger.current.d(TAG, "runAsClient, exception:$e.message")
            socketExceptionListener?.onException(TAG, e)
            logicEstablishListener?.onEstablishResult(false)
            clientClosed = true
        }
    }

    private fun runAsServer() {
        try {
            serverClosed = false
            val serverSocket = ServerSocket(port)
            this.socket = serverSocket
            PurpleLogger.current.d(TAG, "runAsServer, socket opened")
            while (!serverClosed) {
                PurpleLogger.current.d(TAG, "runAsServer accept waiting...")
                val clientSocket = serverSocket.accept()
                PurpleLogger.current.d(TAG, "runAsServer accept one, handle clientSocket start")
                setupSocketStream(clientSocket, shareDevice)
                PurpleLogger.current.d(TAG, "runAsServer accept one, handle clientSocket done")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            PurpleLogger.current.d(TAG, "runAsServer, exception:${e.message}")
            socketExceptionListener?.onException(TAG, e)
            logicEstablishListener?.onEstablishResult(false)
            PurpleLogger.current.d(TAG, e.message ?: "")
            serverClosed = true
        } finally {

        }
    }

    private fun setupSocketStream(
        clientSocket: Socket,
        currentDeviceShareDevice: P2pShareDevice
    ) {
        try {
            PurpleLogger.current.d(
                TAG,
                "setupSocketStream, start, clientSocket:${clientSocket}"
            )
            val clientHostAddressIp4 = clientSocket.inetAddress.hostAddress
            if (clientHostAddressIp4.isNullOrEmpty()) {
                PurpleLogger.current.d(
                    TAG,
                    "setupSocketStream, clientHostAddressIp4 isNullOrEmpty, return"
                )
                return
            }
            val dataHandleExceptionListenerForRead = object : DataHandleExceptionListener {
                override fun onException(type: String, exception: Exception) {

                }
            }
            val dataHandleExceptionListenerForWrite = object : DataHandleExceptionListener {
                override fun onException(type: String, exception: Exception) {

                }
            }
            val readThread = ReadThread(
                this,
                clientSocket,
                readProgressListener,
                contentReceivedListener,
                dataHandleExceptionListenerForRead
            )

            val writeThread = WriteThread(
                currentDeviceShareDevice,
                clientSocket,
                writeProgressListener,
                dataHandleExceptionListenerForWrite
            )

            val clientRWThread = ClientRWThread(readThread, writeThread)
            if (isServer) {
                val deviceIPS = listOf(DeviceIP(ip = clientSocket.inetAddress.hostAddress ?: ""))
                val deviceAddress = DeviceAddress(ips = deviceIPS)
                val mockShareDeviceForClient = P2pShareDevice(
                    deviceName = DeviceName.NONE,
                    deviceAddress = deviceAddress
                )
                clientRWThreadMap.put(
                    mockShareDeviceForClient,
                    clientRWThread
                )
            } else {
                clientRWThreadMap.put(
                    currentDeviceShareDevice,
                    clientRWThread
                )
            }

            readThread.start()
            writeThread.start()

            if (isServer) {
                writeThread.exchangePreInformation(
                    true,
                    DeviceAddress(ips = listOf(DeviceIP(ip = clientHostAddressIp4)))
                )
            } else {
                //client wait server to send client's ip and server's name, then client send self name to the sever!
            }

            logicEstablishListener?.onEstablishResult(true)
            PurpleLogger.current.d(TAG, "setupSocketStream, done")
        } catch (e: Exception) {
            PurpleLogger.current.d(TAG, "setupSocketStream, exception:${e.message}")
            socketExceptionListener?.onException(TAG, e)
            logicEstablishListener?.onEstablishResult(false)
        }
    }

    fun hasClosed(): Boolean {
        return if (isServer) {
            serverClosed
        } else {
            clientClosed
        }
    }

    fun close(shareDevice: P2pShareDevice? = null) {
        if (shareDevice != null) {
            closeServerClientThreadForDevice(shareDevice)
        } else {
            closeThreads()
        }
    }

    private fun closeThreads() {
        try {
            if (isServer) {
                clientRWThreadMap.values.forEach { clientRWThread ->
                    val contentType = ContentType.APPSETS_SHARE_SYSTEM
                    val contentMessage = ShareSystem.SHARE_SYSTEM_CLOSE
                    clientRWThread.writeThread.writeShareSystemMessage(contentType, contentMessage)
                }
            }
            clientRWThreadMap.values.forEach {
                it.readThread.setExit(true)
                it.writeThread.setClosed(true)
            }
            clientRWThreadMap.clear()

            serverClosed = true
            clientClosed = true
        } catch (e: Exception) {
            PurpleLogger.current.d(TAG, "closeThreads, exception:${e.message}")
        } finally {
            try {
                socket?.close()
            } catch (e: Exception) {
                PurpleLogger.current.d(TAG, "closeThreads, exception:${e.message}")
            }
        }
    }

    private fun closeServerClientThreadForDevice(shareDevice: P2pShareDevice) {
        PurpleLogger.current.d(
            TAG,
            "closeServerClientThreadForDevice, shareDevice:$shareDevice"
        )
        try {
            var clientRWThread: ClientRWThread? = null
            var shareDeviceToRemove: ShareDevice? = null
            for ((shareDeviceN, readWriteThread) in clientRWThreadMap) {
                if (shareDeviceN.deviceName.rawName == shareDevice.deviceName.rawName) {
                    clientRWThread = readWriteThread
                    shareDeviceToRemove = shareDeviceN
                    break
                }
            }
            PurpleLogger.current.d(
                TAG,
                """
                    closeServerClientThreadForDevice
                     shareDeviceToRemove:$shareDeviceToRemove
                     deviceAddress:${shareDeviceToRemove?.deviceAddress}
                     writeThread:${clientRWThread?.writeThread}
                     readThread:${clientRWThread?.readThread}
                """.trimIndent()
            )
            if (clientRWThread != null) {
                val contentType = ContentType.APPSETS_SHARE_SYSTEM
                val contentMessage = ShareSystem.SHARE_SYSTEM_CLOSE
                clientRWThread.writeThread.writeShareSystemMessage(contentType, contentMessage)
                clientRWThread.writeThread.setClosed(true)
                clientRWThread.readThread.setExit(true)
                if (shareDeviceToRemove != null) {
                    clientRWThreadMap.remove(shareDeviceToRemove)
                }
            }
        } catch (e: Exception) {
            PurpleLogger.current.d(TAG, "closeServerClientThreadForDevice, exception:${e.message}")
        }
    }

    fun updateClientNameForShareDevice(
        readThread: ReadThread,
        deviceNameAddress: DeviceNameAddress
    ) {
        PurpleLogger.current.d(
            TAG,
            """
                ---------------------------------------------
                updateClientNameForShareDevice step1
                upComingDeviceNameAddress:${deviceNameAddress},
                readThread:$readThread,
                clientReadWriteThreadMap:${clientRWThreadMap}
            """.trimIndent()
        )
        val deviceName = deviceNameAddress.deviceName
        val deviceAddress = deviceNameAddress.deviceAddress
        if (deviceName.rawName.isEmpty()) {
            return
        }
        for ((shareDeviceN, readWriteThread) in clientRWThreadMap) {
            if (readWriteThread.readThread == readThread
                && shareDeviceN.deviceAddress.ip4 == deviceAddress.ip4
            ) {
                PurpleLogger.current.d(
                    TAG,
                    """
                 ---------------------------------------------
                updateClientNameForShareDevice step1.5
                upComingDeviceNameAddress:${deviceNameAddress},
                replace shareDeviceN.deviceName and shareDeviceN.deviceAddress
            """.trimIndent()
                )
                shareDeviceN.deviceName = deviceName
                shareDeviceN.deviceAddress = deviceAddress
                break
            }
        }
        PurpleLogger.current.d(
            TAG,
            """
                 ---------------------------------------------
                updateClientNameForShareDevice step2
                 upComingDeviceNameAddress:${deviceNameAddress},
                readThread:$readThread,
                clientReadWriteThreadMap:${clientRWThreadMap}
            """.trimIndent()
        )
        notifyClientsDeviceNameExchanged(deviceName)

        PurpleLogger.current.d(
            TAG,
            """
                 ---------------------------------------------
                updateClientNameForShareDevice step3
                upComingDeviceNameAddress:${deviceNameAddress},
                readThread:$readThread,
                clientReadWriteThreadMap:${clientRWThreadMap}
            """.trimIndent()
        )
    }

    private fun notifyClientsDeviceNameExchanged(deviceName: DeviceName) {
        deviceNameExchangeListener?.onDeviceNameExchange(deviceName)
    }

    //Client will receive, and call this method
    fun onServerSendClientAddressAndServerDeviceName(
        readThread: ReadThread,
        deviceNameAddress: DeviceNameAddress
    ) {

        val writeThread = findWriteThread(readThread)
        PurpleLogger.current.d(
            TAG, """
            onServerSendClientAddressAndServerDeviceName
            serverDeviceName:${deviceNameAddress.deviceName},
            currentClientAddress:${deviceNameAddress.deviceAddress}
            readThread:$readThread
            writeThread:$writeThread
        """.trimIndent()
        )

        writeThread?.exchangePreInformation(false, deviceNameAddress.deviceAddress)

        updateClientNameForShareDevice(
            readThread,
            deviceNameAddress
        )
    }

    //Server will receive, and call this method
    fun onClientSendClientAddressAndClientDeviceName(
        readThread: ReadThread,
        deviceNameAddress: DeviceNameAddress
    ) {
        PurpleLogger.current.d(
            TAG, """
            onClientSendClientAddressAndClientDeviceName
            clientDeviceName:${deviceNameAddress.deviceName},
            currentClientAddress:${deviceNameAddress.deviceAddress}
            readThread:$readThread
        """.trimIndent()
        )

        updateClientNameForShareDevice(
            readThread,
            deviceNameAddress
        )
    }


    private fun findWriteThread(readThread: ReadThread): WriteThread? {
        for ((_, readWriteThread) in clientRWThreadMap) {
            if (readWriteThread.readThread == readThread) {
                return readWriteThread.writeThread
            }
        }
        return null
    }

    private fun findReadThread(writeThread: WriteThread): ReadThread? {
        for ((_, readWriteThread) in clientRWThreadMap) {
            if (readWriteThread.writeThread == writeThread) {
                return readWriteThread.readThread
            }
        }
        return null
    }
}