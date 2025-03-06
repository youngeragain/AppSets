package xcj.app.share.wlanp2p.common

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import xcj.app.share.base.DataContent
import xcj.app.share.base.DataSendContent
import xcj.app.share.base.DeviceAddress
import xcj.app.share.base.DeviceNameAddress
import xcj.app.web.webserver.interfaces.ProgressListener
import xcj.app.share.base.ShareDevice
import xcj.app.share.wlanp2p.base.DataHandleExceptionListener
import xcj.app.share.wlanp2p.base.ISocketState
import xcj.app.share.wlanp2p.base.IThreadWriter
import xcj.app.share.wlanp2p.base.P2pShareDevice
import xcj.app.share.wlanp2p.base.WlanP2pContent
import xcj.app.share.wlanp2p.base.WriteFunction
import xcj.app.share.wlanp2p.base.WriteMethod
import xcj.app.share.wlanp2p.channel.ChannelWriteMethod
import xcj.app.share.wlanp2p.stream.StreamWriteMethod
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType
import java.io.File
import java.net.Socket

class WriteThread(
    private val shareDevice: P2pShareDevice,
    private val socket: Socket,
    progressListener: ProgressListener?,
    private val dataHandleExceptionListener: DataHandleExceptionListener?
) : Thread(), ISocketState, IThreadWriter {

    companion object {
        private const val TAG: String = "WriteThread"
        const val USE_NIO = false
    }

    private val writeMethod: WriteMethod = if (USE_NIO) {
        ChannelWriteMethod(socket, this, progressListener, dataHandleExceptionListener)
    } else {
        StreamWriteMethod(socket, this, progressListener, dataHandleExceptionListener)
    }

    private val gson = Gson()

    private var exit = false

    override fun isSocketClosed(): Boolean {
        return socket.isClosed
    }

    //TODO 变成链表
    @Volatile
    private var writeFunction: WriteFunction? = null

    private val writeLock: Any = Any()

    override fun run() {
        try {
            while (!exit) {
                if (isSocketClosed()) {
                    break
                }
                synchronized(writeLock) {
                    while (!exit) {
                        if (writeFunction != null) {
                            try {
                                writeFunction?.writeContent(TAG)
                                writeFunction = null
                            } catch (e: Exception) {
                                e.printStackTrace()
                                PurpleLogger.current.d(
                                    TAG,
                                    "run writeFunction exception, ${e.message}"
                                )
                            }
                        }
                        if (writeFunction == null) {
                            PurpleLogger.current.d(
                                TAG,
                                "run waiting a new writeFunction"
                            )
                            (writeLock as Object).wait()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.d(TAG, "run exception:" + e.message)
            dataHandleExceptionListener?.onException(TAG, e)
        } finally {
            try {
                writeMethod.close()
            } catch (_: Exception) {

            }
        }
    }

    fun writeWithFunction(writeFunction: WriteFunction) {
        synchronized(writeLock) {
            this.writeFunction = writeFunction
            PurpleLogger.current.d(
                TAG,
                "writeWithFunction, notify with a new writeFunction"
            )
            (this.writeLock as Object).notify()
        }
    }

    private fun writeBytesContent(
        byteArray: ByteArray,
        contentType: String,
        fileName: String?,
        contentLength: Long,
        isContentChunked: Boolean,
        chunkCount: Int?
    ) {
        writeMethod.writeBytesContent(
            byteArray, contentType, fileName, contentLength, isContentChunked, chunkCount
        )
    }

    fun writeString(str: String) {
        val byteArray = str.toByteArray()
        writeBytesContent(
            byteArray,
            ContentType.APPLICATION_TEXT,
            null,
            byteArray.size.toLong(),
            false,
            1
        )
    }

    fun writeBytes(byteArray: ByteArray) {
        writeBytesContent(
            byteArray,
            ContentType.APPLICATION_BYTES,
            null,
            byteArray.size.toLong(),
            false,
            1
        )
    }

    fun writeShareSystemMessage(contentType: String, message: String) {
        val bytes = message.toByteArray()
        writeBytesContent(
            bytes,
            contentType,
            null,
            bytes.size.toLong(),
            false,
            1
        )
    }

    /**
     * 如果是服务端，则服务端发送客户端的ip和服务端自己的设备名称
     * 如果是客户端，则客户端发送从服务端收到的自己的ip和客户端自己的名称
     */
    fun exchangePreInformation(isServer: Boolean, clientDeviceAddress: DeviceAddress) {
        val wifiP2pDevice = shareDevice.wifiP2pDevice
        if (wifiP2pDevice == null) {
            return
        }
        PurpleLogger.current.d(
            TAG,
            "exchangePreInformation, shareDevice.deviceName:${shareDevice.deviceName}," +
                    " clientDeviceAddress:$clientDeviceAddress"
        )
        val deviceNameAndAddress = DeviceNameAddress(
            deviceName = shareDevice.deviceName,
            deviceAddress = clientDeviceAddress
        )
        val deviceNameAndAddressJsonString = gson.toJson(deviceNameAndAddress)

        if (isServer) {
            val contentType =
                ContentType.APPSETS_SHARE_SYSTEM_SERVER_SEND_CLIENT_IP_AND_SELF_NAME
            val message = deviceNameAndAddressJsonString
            writeShareSystemMessage(contentType, message)
        } else {
            val contentType = ContentType.APPSETS_SHARE_SYSTEM_CLIENT_SEND_IP_AND_SELF_NAME
            val message = deviceNameAndAddressJsonString
            writeShareSystemMessage(contentType, message)
        }

    }

    suspend fun writeFileContent(file: File) {
        writeMethod.writeFileContent(file)
    }

    suspend fun writeUriContent(context: Context, uri: Uri) {
        writeMethod.writeUriContent(context, uri)
    }

    private fun writeChunkStartBytes(
        uuid: String,
        contentType: String,
        fileName: String?,
        contentLength: Long,
        isContentChunked: Boolean,
        chunkCount: Int?
    ) {
        writeMethod.writeChunkStartBytes(
            uuid,
            contentType,
            fileName,
            contentLength,
            isContentChunked,
            chunkCount
        )

    }

    private fun writeChunkContentBytes(
        uuid: String,
        contentType: String,
        name: String?,
        totalLength: Long,
        writtenLength: Long,
        contentChunkBytes: ByteArray,
        startIndex: Int,
        length: Int
    ) {
        writeMethod.writeChunkContentBytes(
            uuid,
            contentType,
            name,
            totalLength,
            writtenLength,
            contentChunkBytes,
            startIndex,
            length
        )
    }

    fun setClosed(closed: Boolean) {
        this.exit = closed
    }

    override suspend fun writeContent(
        context: Context,
        dataSendContent: DataSendContent
    ) {
        val dataContent = dataSendContent.content
        when (dataContent) {
            is DataContent.StringContent -> {
                writeContentInternal(context, dataContent.content)
            }

            is DataContent.FileContent -> {
                writeContentInternal(context, dataContent.file)
            }

            is DataContent.ByteArrayContent -> {
                writeContentInternal(context, dataContent.bytes)
            }

            is DataContent.UriContent -> {
                writeContentInternal(context, dataContent.uri)
            }
        }
    }

    suspend fun writeContentInternal(context: Context, content: Any) {
        when (content) {
            is String -> {
                writeString(content)
            }

            is ByteArray -> {
                writeBytes(content)
            }

            is File -> {
                writeFileContent(content)
            }

            is Uri -> {
                writeUriContent(context, content)
            }
        }
    }
}