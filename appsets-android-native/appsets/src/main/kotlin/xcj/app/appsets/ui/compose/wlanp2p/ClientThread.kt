package xcj.app.appsets.ui.compose.wlanp2p

import android.content.Context
import android.util.Log
import java.net.InetSocketAddress
import java.net.Socket

class ClientThread(
    private val context: Context,
    private val host: String?,
    private val port: Int,
    private val contentReceivedListener: ContentReceivedListener,
    private val establishListener: EstablishListener,
    private val iSocketExceptionListener: ISocketExceptionListener
) : Thread(), IThreadWriter {
    private val TAG = "ClientThread"
    private lateinit var socket: Socket
    private lateinit var readThread: ClientReadThread
    private lateinit var writeThread: ClientWriteThread
    fun closeReadWrite() {
        try {
            if (::readThread.isInitialized) {
                readThread.setClosed(true)
            }
            if (::writeThread.isInitialized) {
                writeThread.setClosed(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun run() {
        if (host.isNullOrEmpty())
            return
        try {
            socket = Socket()
            socket.bind(null)
            Log.d(TAG, "socket opened")
            socket.connect(InetSocketAddress(host, port), 5000)
            establishListener.onEstablishResult(true)
            Log.d(TAG, "connection done")
            val inputStream = socket.getInputStream()
            val outputStream = socket.getOutputStream()
            ClientReadThread(
                socket,
                inputStream,
                contentReceivedListener,
                iSocketExceptionListener
            ).also {
                readThread = it
            }.start()
            ClientWriteThread(socket, outputStream, iSocketExceptionListener).also {
                writeThread = it
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "ConnectException:" + e.message)
            iSocketExceptionListener.onException(TAG, e)
            establishListener.onEstablishResult(false)
        }
    }

    override fun writeAny(any: Any) {
        if (!::writeThread.isInitialized)
            return
        writeThread.writeAny(any)
    }
}