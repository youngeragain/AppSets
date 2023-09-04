package xcj.app.appsets.ui.compose.wlanp2p

import java.io.OutputStream
import java.net.ServerSocket

class ServerWriteThread(
    private val serverSocket: ServerSocket,
    outputStream: OutputStream,
    socketExceptionListener: ISocketExceptionListener
) : CommonWriteThread(
    outputStream,
    socketExceptionListener
) {
    override val TAG = "ServerWriteThread"

    override fun isSocketClosed(): Boolean {
        return serverSocket.isClosed
    }
}