package xcj.app.share.wlanp2p.channel

import com.google.gson.GsonBuilder
import xcj.app.share.base.ClientInfo
import xcj.app.share.base.ContentCombiner
import xcj.app.share.base.ContentReceivedListener
import xcj.app.share.base.DataContent
import xcj.app.web.webserver.base.DataProgressInfoPool
import xcj.app.share.base.DeviceNameAddress
import xcj.app.web.webserver.base.ProgressListener
import xcj.app.share.base.ShareSystem
import xcj.app.share.wlanp2p.base.DataHandleExceptionListener
import xcj.app.share.wlanp2p.base.ReadMethod
import xcj.app.share.wlanp2p.common.P2pOneThread
import xcj.app.share.wlanp2p.common.ReadThread
import xcj.app.share.wlanp2p.common.ReadThread.Companion.HEADER_CHUNK_COUNT
import xcj.app.share.wlanp2p.common.ReadThread.Companion.HEADER_CONTENT_CHUNKED
import xcj.app.share.wlanp2p.common.ReadThread.Companion.HEADER_CONTENT_LENGTH
import xcj.app.share.wlanp2p.common.ReadThread.Companion.HEADER_CONTENT_TYPE
import xcj.app.share.wlanp2p.common.ReadThread.Companion.HEADER_FILE_NAME
import xcj.app.share.wlanp2p.common.ReadThread.Companion.HEADER_UUID
import xcj.app.share.wlanp2p.common.ReadThread.Companion.READ_BUFFER_SIZE
import xcj.app.share.wlanp2p.common.ReadThread.Companion.SYSTEM_CONTENT_TYPES
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ByteUtil
import xcj.app.starter.util.ContentType
import java.io.BufferedOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.channels.SocketChannel
import java.text.DecimalFormat
import kotlin.io.outputStream

class ChannelReadMethod(
    socket: Socket,
    private val p2pOneThread: P2pOneThread,
    private val readThread: ReadThread,
    private val progressListener: ProgressListener?,
    private val contentReceivedListener: ContentReceivedListener?,
    private val dataHandleExceptionListener: DataHandleExceptionListener?
) : ReadMethod {

    companion object {
        private const val TAG = "ChannelReadMethod"
    }

    private val remoteClientInfo: ClientInfo = ClientInfo(
        (socket.remoteSocketAddress as? InetSocketAddress)?.hostString ?: ""
    )

    private val gson = GsonBuilder().create()


    private val channel: SocketChannel = SocketChannel.open(socket.localSocketAddress)

    private var contentCombiner: ContentCombiner? = null
    private var readLength = 0L
    private var requiredLength = 0L
    private var startTimeMills = 0L


    private val bufferSize = READ_BUFFER_SIZE
    private val buffer = ByteArray(bufferSize)
    private var bufferPosition: Int = 0
    private var headerCount: Int = 0
    private var bodyCount: Long = 0L
    private val headerCountBytes = ByteArray(4)
    private var headerCountPosition: Int = 0
    private val bodyCountBytes = ByteArray(8)
    private var bodyCountPosition: Int = 0

    private var headerContentBytes: ByteArray? = null
    private var headerContentPosition: Int = 0

    private val byteBuffer = ByteBuffer.wrap(buffer)

    override fun reset() {
        contentCombiner = null
        requiredLength = 0
        readLength = 0
        bufferPosition = 0
        headerCountPosition = 0
        bodyCountPosition = 0
        headerCount = 0
        bodyCount = 0L
        headerContentBytes = null
        headerContentPosition = 0
    }

    //in while loop
    override fun doRead() {
        reset()
        while (!readThread.isExit() && channel.isConnected) {
            val readByteCount = channel.read(byteBuffer)
            if (readByteCount > 0) {
                toHandleReadedBytesInBuffer(byteBuffer, readByteCount)
            } else if (readByteCount == 0) {

            } else if (readByteCount == -1) {

            }
        }
    }

    private fun toHandleReadedBytesInBuffer(buffer: ByteBuffer, readCount: Int) {
        if (readCount <= 0) {
            return
        }
        for (byteIndex in 0..readCount) {
            val byte = buffer.get(byteIndex)
            if (headerCountPosition < 4) {
                headerCountBytes[headerCountPosition++] = byte
            } else if (bodyCountPosition < 8) {
                bodyCountBytes[bodyCountPosition++] = byte
            } else {
                if (headerContentBytes == null) {
                    headerCount = ByteUtil.byteArrayToInt(headerCountBytes)
                    bodyCount = ByteUtil.byteArrayToLong(bodyCountBytes)
                    PurpleLogger.current.d(
                        TAG,
                        "doRead, headerCount:${headerCount}, bodyCount:${bodyCount}"
                    )
                    this.headerContentBytes = ByteArray(headerCount)
                }
                if (headerContentPosition + 1 < headerCount) {
                    headerContentBytes?.set(headerContentPosition++, byte)
                } else {
                    if (contentCombiner == null) {
                        headerContentBytes?.let { readHeaderContent(it) }
                    } else {
                        if (bufferPosition + 1 < bufferSize) {
                            bufferPosition++
                            if (readLength + bufferPosition == requiredLength) {
                                readContentChunk(buffer, 0, bufferPosition)
                                break
                            }
                        } else {//buffer full
                            readContentChunk(buffer, 0, bufferSize)
                            bufferPosition = 0
                        }
                    }
                }
            }
        }
    }

    private fun readHeaderContent(byteArray: ByteArray) {
        if (byteArray.isEmpty()) {
            return
        }
        startTimeMills = System.currentTimeMillis()

        val header = mutableMapOf<String, Any>()
        var lastNoneZeroPositionInBytes: Int? = null
        var tempBytes = readBytes(byteArray, 0, 40)
        lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(tempBytes)
        val uuid = tempBytes.decodeToString(0, lastNoneZeroPositionInBytes ?: tempBytes.size)
        header[HEADER_UUID] = uuid

        tempBytes = readBytes(byteArray, 40, 36)
        lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(tempBytes)
        val contentType = tempBytes.decodeToString(0, lastNoneZeroPositionInBytes ?: tempBytes.size)
        header[HEADER_CONTENT_TYPE] = contentType

        tempBytes = readBytes(byteArray, 76, 512)
        lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(tempBytes)

        var fileName: String? = null
        if (lastNoneZeroPositionInBytes != null) {
            fileName = tempBytes.decodeToString(0, lastNoneZeroPositionInBytes)
            header[HEADER_FILE_NAME] = fileName
        }

        tempBytes = readBytes(byteArray, 588, 8)
        //lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(temBytes)
        val contentLength = ByteUtil.byteArrayToLong(tempBytes)
        header[HEADER_CONTENT_LENGTH] = contentLength

        tempBytes = readBytes(byteArray, 596, 1)
        //lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(temBytes)
        val isContentChunked = tempBytes[0].toInt() == 1
        header[HEADER_CONTENT_CHUNKED] = isContentChunked

        tempBytes = readBytes(byteArray, 597, 4)
        //lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(temBytes)
        val chunkCount = ByteUtil.byteArrayToInt(tempBytes)
        header[HEADER_CHUNK_COUNT] = chunkCount
        requiredLength = contentLength
        readLength = 0L
        var dataContent: DataContent? = null
        when (contentType) {
            ContentType.APPLICATION_FILE -> {
                try {
                    val fileName = header[HEADER_FILE_NAME]?.toString() ?: uuid
                    val file = ShareSystem.makeFileIfNeeded(fileName)
                    if (file != null) {
                        if (file.canWrite()) {
                            dataContent =
                                DataContent.FileContent(
                                    file,
                                    file.outputStream().channel
                                )
                            PurpleLogger.current.d(
                                TAG,
                                "readHeaderContent, file:${fileName}, create and open output steam"
                            )
                        } else {
                            PurpleLogger.current.d(
                                TAG,
                                "readHeaderContent, file:${fileName}, can't be write"
                            )
                        }

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    PurpleLogger.current.d(TAG, "readHeaderContent, exception:${e.message}")
                }
            }

            else -> {
                val newBytes = ByteArray(0)
                dataContent = DataContent.ByteArrayContent(newBytes)
            }
        }


        contentCombiner = ContentCombiner(header, dataContent)

        if (!SYSTEM_CONTENT_TYPES.contains(contentType)) {
            val progressListener = progressListener
            if (progressListener != null) {
                val dataProgressInfo = DataProgressInfoPool.obtainById(uuid)
                dataProgressInfo.name = fileName ?: uuid
                dataProgressInfo.total = requiredLength
                dataProgressInfo.current = 0L
                progressListener.onProgress(dataProgressInfo)
            }
        }

        PurpleLogger.current.d(TAG, "readHeaderContent:$uuid\nheader:\n$header")
    }

    /**
     * @param byteArrayBuffer chunk bytes buffer
     */
    private fun readContentChunk(byteBuffer: ByteBuffer, startIndex: Int, length: Int) {
        val tempCombiner = contentCombiner
        if (tempCombiner == null) {
            return
        }

        val contentHeader = tempCombiner.contentHeader
        if (contentHeader.isEmpty()) {
            return
        }

        val contentType = contentHeader[HEADER_CONTENT_TYPE]

        when (contentType) {
            ContentType.APPLICATION_FILE -> {
                try {
                    //PurpleLogger.current.d(TAG, "write file content")
                    val content = tempCombiner.content
                    if (content is DataContent.FileContent) {
                        val fileContent = content
                        val fileChannel = fileContent.out as? FileChannel
                        //PurpleLogger.current.d(TAG, "write byte length:${length} first:${byteArray[0]} last:${byteArray[length-1]} last-1:${byteArray[length-2]}")
                        fileChannel?.write(byteBuffer)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    PurpleLogger.current.d(TAG, "readContentChunk, exception:${e.message}")
                }
            }

            else -> {
                try {
                    val content = tempCombiner.content
                    if (content is DataContent.ByteArrayContent) {
                        val lastByteArrayContent = content
                        val lastBytes = lastByteArrayContent.bytes
                        val newBytes = ByteArray(lastBytes.size + length)
                        System.arraycopy(lastBytes, 0, newBytes, 0, lastBytes.size)
                        System.arraycopy(byteBuffer, 0, newBytes, lastBytes.size, length)
                        val newByteArrayContent = DataContent.ByteArrayContent(newBytes)
                        tempCombiner.content = newByteArrayContent
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    PurpleLogger.current.d(TAG, "readContentChunk, exception:${e.message}")
                }
            }
        }
        readLength += length

        if (!SYSTEM_CONTENT_TYPES.contains(contentType)) {
            val progressListener = progressListener
            if (progressListener != null) {
                val header = contentHeader
                val uuid = header[HEADER_UUID]?.toString() ?: ""
                val name = header[HEADER_FILE_NAME]?.toString() ?: ""
                val dataProgressInfo = DataProgressInfoPool.obtainById(uuid)
                dataProgressInfo.name = name
                dataProgressInfo.total = requiredLength
                dataProgressInfo.current = readLength
                progressListener.onProgress(dataProgressInfo)
            }
        }
        //PurpleLogger.current.d(TAG, "requiredLength:$requiredLength, readLength:$readLength")
        if (readLength == requiredLength) {
            //read finish
            PurpleLogger.current.d(
                TAG,
                "readContentChunk, read finish, time spend:${(System.currentTimeMillis() - startTimeMills) / 1000f}s"
            )
            when (contentType) {
                ContentType.APPLICATION_FILE -> {
                    val content = tempCombiner.content
                    if (content !is DataContent.FileContent) {
                        return
                    }
                    try {
                        val bos = content.out as? BufferedOutputStream
                        bos?.close()
                        content.out = null
                        content.clientInfo = remoteClientInfo
                        contentReceivedListener?.onContentReceived(content)
                    } catch (e: Exception) {
                        PurpleLogger.current.d(TAG, "readContentChunk, exception:${e.message}")
                    }
                }

                ContentType.APPSETS_SHARE_SYSTEM_SERVER_SEND_CLIENT_IP_AND_SELF_NAME -> {
                    val content = tempCombiner.content
                    if (content !is DataContent.ByteArrayContent) {
                        return
                    }
                    try {
                        val stringContent =
                            DataContent.StringContent(content.bytes.decodeToString())
                        val deviceNameAndAddress = gson.fromJson<DeviceNameAddress>(
                            stringContent.content,
                            DeviceNameAddress::class.java
                        )

                        p2pOneThread.onServerSendClientAddressAndServerDeviceName(
                            readThread,
                            deviceNameAndAddress
                        )
                    } catch (e: Exception) {
                        PurpleLogger.current.d(TAG, "readContentChunk, exception:${e.message}")
                    }
                }

                ContentType.APPSETS_SHARE_SYSTEM_CLIENT_SEND_IP_AND_SELF_NAME -> {
                    val content = tempCombiner.content
                    if (content !is DataContent.ByteArrayContent) {
                        return
                    }
                    try {
                        val stringContent =
                            DataContent.StringContent(content.bytes.decodeToString())
                        val deviceNameAddress = gson.fromJson<DeviceNameAddress>(
                            stringContent.content,
                            DeviceNameAddress::class.java
                        )
                        p2pOneThread.onClientSendClientAddressAndClientDeviceName(
                            readThread,
                            deviceNameAddress
                        )
                    } catch (e: Exception) {
                        PurpleLogger.current.d(TAG, "readContentChunk, exception:${e.message}")
                    }
                }

                ContentType.APPSETS_SHARE_SYSTEM -> {
                    val content = tempCombiner.content
                    if (content !is DataContent.ByteArrayContent) {
                        return
                    }
                    try {
                        val stringContent =
                            DataContent.StringContent(content.bytes.decodeToString())
                        val shareSystemMessage = stringContent.content
                        when (shareSystemMessage) {
                            ShareSystem.SHARE_SYSTEM_CLOSE -> {
                                //p2pOneThread.closeForClient()
                            }
                        }
                    } catch (e: Exception) {
                        PurpleLogger.current.d(TAG, "readContentChunk, exception:${e.message}")
                    }

                }

                ContentType.APPLICATION_TEXT -> {
                    val content = tempCombiner.content
                    if (content !is DataContent.ByteArrayContent) {
                        return
                    }
                    try {
                        val stringContent =
                            DataContent.StringContent(content.bytes.decodeToString())

                        stringContent.clientInfo = remoteClientInfo
                        contentReceivedListener?.onContentReceived(stringContent)
                    } catch (e: Exception) {
                        PurpleLogger.current.d(TAG, "readContentChunk, exception:${e.message}")
                    }

                }
            }
        }
    }

    private fun readBytes(byteArray: ByteArray, startIndex: Int, length: Int): ByteArray {
        //todo should need to copy?
        val destBytes = ByteArray(length)
        System.arraycopy(byteArray, startIndex, destBytes, 0, length)
        return destBytes
    }

    private fun lastNoneZeroPositionInBytes(byteArray: ByteArray): Int? {
        for (i in byteArray.indices.reversed()) {
            if (byteArray[i].toInt() != 0) {
                return i + 1
            }
        }
        return null
    }


    override fun close() {
        PurpleLogger.current.d(TAG, "close")
    }

}