package xcj.app.appsets.ui.compose.wlanp2p

import android.util.Log
import java.io.IOException
import java.net.ServerSocket

class ServerThread(
    private val readProgressListener: ProgressListener?,
    private val writeProgressListener: ProgressListener?,
    private val contentReceivedListener: ContentReceivedListener,
    private val establishListener: EstablishListener,
    private val socketExceptionListener: ISocketExceptionListener
) : Thread(), IThreadWriter {
    private val TAG = "ServerThread"
    private var clientIp: String? = null
    private lateinit var serverSocket: ServerSocket
    private lateinit var readThread: ServerReadThread
    private lateinit var writeThread: ServerWriteThread
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
                serverSocket.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    override fun run() {
        try {
            serverSocket = ServerSocket(8988)
            Log.d(TAG, "socket opened")
            val client = serverSocket.accept()
            establishListener.onEstablishResult(true)
            clientIp = client.inetAddress.hostAddress
            Log.d(TAG, "connection done")
            val inputStream = client.getInputStream().buffered()
            val outputStream = client.getOutputStream()
            ServerReadThread(
                serverSocket,
                inputStream,
                readProgressListener,
                contentReceivedListener,
                socketExceptionListener
            ).also {
                readThread = it
            }.start()
            ServerWriteThread(
                serverSocket,
                outputStream,
                writeProgressListener,
                socketExceptionListener
            ).also {
                writeThread = it
            }.start()
        } catch (e: IOException) {
            establishListener.onEstablishResult(false)
            Log.e(TAG, e.message ?: "")
        }
    }

    override fun writeAny(any: Any) {
        if (!::writeThread.isInitialized)
            return
        writeThread.writeAny(any)
    }
}