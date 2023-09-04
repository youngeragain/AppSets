package xcj.app.appsets.ui.compose.wlanp2p

import java.io.OutputStream
import java.net.Socket

class ClientWriteThread(
    private val socket: Socket,
    outputStream: OutputStream,
    iSocketExceptionListener: ISocketExceptionListener
) : CommonWriteThread(outputStream, iSocketExceptionListener) {
    override
    val TAG = "ClientWriteThread"
    override fun isSocketClosed(): Boolean {
        return socket.isClosed
    }
}