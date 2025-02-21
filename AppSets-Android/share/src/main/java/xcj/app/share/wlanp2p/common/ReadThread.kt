package xcj.app.share.wlanp2p.common

import xcj.app.share.base.ContentReceivedListener
import xcj.app.web.webserver.base.ProgressListener
import xcj.app.share.wlanp2p.base.DataHandleExceptionListener
import xcj.app.share.wlanp2p.base.ISocketState
import xcj.app.share.wlanp2p.base.ReadMethod
import xcj.app.share.wlanp2p.channel.ChannelReadMethod
import xcj.app.share.wlanp2p.stream.StreamReadMethod
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType
import java.net.Socket

class ReadThread(
    p2pOneThread: P2pOneThread,
    private val socket: Socket,
    progressListener: ProgressListener?,
    contentReceivedListener: ContentReceivedListener?,
    private val dataHandleExceptionListener: DataHandleExceptionListener?
) : Thread(), ISocketState {

    companion object {
        private const val TAG = "ReadThread"
        const val USE_NIO = false
        const val HEADER_UUID = "uuid"
        const val HEADER_CONTENT_TYPE = "content-type"
        const val HEADER_FILE_NAME = "file-name"
        const val HEADER_CONTENT_LENGTH = "content-length"
        const val HEADER_CONTENT_CHUNKED = "content-chunked"
        const val HEADER_CHUNK_COUNT = "chunk-count"
        const val READ_BUFFER_SIZE = 4 * 1024
        const val WRITE_BUFFER_SIZE = 4 * 1024
        internal val SYSTEM_CONTENT_TYPES = listOf<String>(
            ContentType.APPSETS_SHARE_SYSTEM,
            ContentType.APPSETS_SHARE_SYSTEM_SERVER_SEND_CLIENT_IP_AND_SELF_NAME,
            ContentType.APPSETS_SHARE_SYSTEM_CLIENT_SEND_IP_AND_SELF_NAME
        )
    }

    private val readMethod: ReadMethod = if (USE_NIO) {
        ChannelReadMethod(
            socket,
            p2pOneThread,
            this,
            progressListener,
            contentReceivedListener,
            dataHandleExceptionListener
        )
    } else {
        StreamReadMethod(
            socket,
            p2pOneThread,
            this,
            progressListener,
            contentReceivedListener,
            dataHandleExceptionListener
        )
    }

    private var exit = false

    fun isExit(): Boolean {
        return exit
    }

    override fun isSocketClosed(): Boolean {
        return socket.isClosed
    }

    override fun run() {
        try {
            while (!isExit()) {
                if (isSocketClosed()) {
                    break
                }
                readMethod.doRead()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.d(TAG, "run exception:" + e.message)
            dataHandleExceptionListener?.onException(TAG, e)
        } finally {
            try {
                readMethod.close()
            } catch (e: Exception) {
                e.printStackTrace()
                PurpleLogger.current.d(TAG, "run, finally close exception:" + e.message)
            }
        }
    }

    fun setExit(exit: Boolean) {
        this.exit = exit
    }
}