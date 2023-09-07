package xcj.app.appsets.ui.compose.wlanp2p

import java.io.OutputStream
import java.net.ServerSocket

class ServerWriteThread(
    private val serverSocket: ServerSocket,
    outputStream: OutputStream,
    writeProgressListener: ProgressListener?,
    socketExceptionListener: ISocketExceptionListener
) : CommonWriteThread(
    outputStream,
    writeProgressListener,
    socketExceptionListener
) {
    override val TAG = "ServerWriteThread"

    override fun isSocketClosed(): Boolean {
        return serverSocket.isClosed
    }
}