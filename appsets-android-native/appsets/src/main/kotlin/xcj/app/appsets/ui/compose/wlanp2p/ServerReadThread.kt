package xcj.app.appsets.ui.compose.wlanp2p

import java.io.InputStream
import java.net.ServerSocket

class ServerReadThread(
    private val serverSocket: ServerSocket,
    inputStream: InputStream,
    readProgressListener: ProgressListener?,
    contentReceivedListener: ContentReceivedListener,
    iSocketExceptionListener: ISocketExceptionListener
) : CommonReadThread(
    inputStream,
    readProgressListener,
    contentReceivedListener,
    iSocketExceptionListener
) {
    override val TAG = "ServerReadThread"
    override fun isSocketClosed(): Boolean {
        return serverSocket.isClosed
    }
}