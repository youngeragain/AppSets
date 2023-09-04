package xcj.app.appsets.ui.compose.wlanp2p

import java.io.InputStream
import java.net.Socket

class ClientReadThread(
    private val socket: Socket,
    inputStream: InputStream,
    contentReceivedListener: ContentReceivedListener,
    iSocketExceptionListener: ISocketExceptionListener
) : CommonReadThread(inputStream, contentReceivedListener, iSocketExceptionListener) {
    override val TAG = "ClientReadThread"
    override fun isSocketClosed(): Boolean {
        return socket.isClosed
    }
}