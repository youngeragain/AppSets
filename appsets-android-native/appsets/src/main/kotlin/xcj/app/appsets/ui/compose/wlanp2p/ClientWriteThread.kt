package xcj.app.appsets.ui.compose.wlanp2p

import java.io.OutputStream
import java.net.Socket

class ClientWriteThread(
    private val socket: Socket,
    outputStream: OutputStream,
    progressListener: ProgressListener?,
    iSocketExceptionListener: ISocketExceptionListener
) : CommonWriteThread(outputStream, progressListener, iSocketExceptionListener) {
    override
    val TAG = "ClientWriteThread"
    override fun isSocketClosed(): Boolean {
        return socket.isClosed
    }
}