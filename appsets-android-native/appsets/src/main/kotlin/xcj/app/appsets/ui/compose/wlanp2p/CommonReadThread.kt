package xcj.app.appsets.ui.compose.wlanp2p

import android.util.Log
import com.google.gson.Gson
import xcj.app.core.android.ApplicationHelper
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream
import java.util.concurrent.locks.ReentrantLock

class ContentCombinerValueObject(val contentHeader: Map<String, Any>, var value: Any?)

abstract class CommonReadThread(
    inputStream: InputStream,
    private val contentReceivedListener: ContentReceivedListener,
    private val iSocketExceptionListener: ISocketExceptionListener
) : Thread(), ISocketState {
    abstract val TAG: String

    private var bis = inputStream.buffered()
    private var closed = false
    private val gson = Gson()
    private var contentCombiner: ContentCombinerValueObject? = null
    private var readLength = 0L
    private var requiredLength = 0L

    private var startTimeMills = 0L
    override fun run() {
        try {
            while (!closed) {
                if (isSocketClosed())
                    break
                val bufferSize = 2048
                val buffer = ByteArray(bufferSize)
                var bufferPosition = 0
                val startBytes: ArrayList<Byte> = ArrayList(2 * 1024)
                while (!closed) {
                    val byte = bis.read()
                    if (contentCombiner == null) {
                        Log.i(TAG, "contentCombiner is null, byte:${byte}")
                        if (byte == 0) {
                            if (startBytes.isNotEmpty()) {
                                readChunk(startBytes.toByteArray())
                                Log.i(TAG, "read head info, head bytes size:${startBytes.size}")
                                startBytes.clear()
                            }
                        } else {
                            startBytes.add(byte.toByte())
                        }
                    } else {
                        buffer[bufferPosition] = byte.toByte()
                        bufferPosition++
                        if (bufferPosition == bufferSize) {
                            readChunk(buffer)
                            bufferPosition = 0
                        } else {
                            if (readLength + bufferPosition == requiredLength) {
                                val dstBytes = ByteArray(bufferPosition)
                                System.arraycopy(buffer, 0, dstBytes, 0, bufferPosition)
                                readChunk(dstBytes)
                                break
                            }
                        }
                    }

                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "run exception:" + e.message)
            iSocketExceptionListener.onException(TAG, e)
        }

    }

    private val reentrantLock: ReentrantLock = ReentrantLock()


    /**
     * @param byteArray chunk bytes
     * @return 0 is chunkStart, 1 is chunk content, 2 is chunk end
     */
    private fun readChunk(byteArray: ByteArray): Int {
        if (byteArray.isEmpty()) {
            return -1
        }
        val tempCombiner = contentCombiner
        if (tempCombiner == null) {
            val content = byteArray.decodeToString()
            if (content.isEmpty()) {
                return -1
            }
            val deliveryContent = try {
                gson.fromJson(content, DeliveryContent::class.java)
            } catch (e: Exception) {
                Log.i(TAG, "readChunk gson error:" + e.message)
                return -1
            }
            if (deliveryContent == null)
                return -1
            val header = deliveryContent.header
            if (header.isEmpty()) {
                return -1
            }
            if (header["start"] == true) {
                val flag = header["flag"]?.toString()
                if (flag.isNullOrEmpty()) {
                    return 0
                }
                val contentType = header["content-type"]
                val contentLength = header["content-length"]?.toString()?.toFloat()?.toLong() ?: 0
                val chunked = header["content-chunked"]
                val chunkedLength = header["chunk-length"]?.toString()?.toFloat()?.toInt() ?: 0
                requiredLength = contentLength
                Log.i(
                    TAG, """
                chunk:
                contentType:${contentType}
                contentLength:${contentLength}
                chunked:${chunked}
                chunkedLength:${chunkedLength}
            """.trimIndent()
                )
                if (contentType == null)
                    return 0
                val value = if (contentType == "application/file") {
                    val fileName = header["file-name"] ?: System.currentTimeMillis().toString()
                    val file =
                        File(ApplicationHelper.getContextFileDir().publicDownloadDir + File.separator + fileName)
                    if (file.exists())
                        file.delete()
                    file.createNewFile()
                    file to file.outputStream().buffered()
                } else {
                    null
                }
                startTimeMills = System.currentTimeMillis()
                contentCombiner = ContentCombinerValueObject(header, value)
                Log.i(TAG, "readChunk[$flag] start $contentType")
                return 0
            }
        } else {
            val flag = tempCombiner.contentHeader["flag"]?.toString()
            Log.i(TAG, "readChunk[$flag] content")
            val contentType = tempCombiner.contentHeader["content-type"]?.toString()
            if (contentType == "application/text") {
                try {
                    if (tempCombiner.value != null && tempCombiner.value is ByteArray) {
                        val lastBytes = tempCombiner.value as? ByteArray
                        if (lastBytes == null || lastBytes.isEmpty()) {
                            return 1
                        }
                        val newBytes = ByteArray(lastBytes.size + byteArray.size)
                        System.arraycopy(lastBytes, 0, newBytes, 0, lastBytes.size)
                        System.arraycopy(byteArray, 0, newBytes, lastBytes.size, byteArray.size)
                        tempCombiner.value = newBytes
                    } else {
                        val newBytes = ByteArray(byteArray.size)
                        System.arraycopy(byteArray, 0, newBytes, 0, byteArray.size)
                        tempCombiner.value = newBytes
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (contentType == "application/file") {
                try {
                    Log.i(TAG, "write file content")
                    val bos = (tempCombiner.value as? Pair<*, *>)?.second as? BufferedOutputStream
                    bos?.write(byteArray)
                    bos?.flush()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            readLength += byteArray.size.toLong()
            Log.i(TAG, "requiredLength:$requiredLength, readLength:$readLength")
            if (readLength == requiredLength) {//read finish
                Log.i(
                    TAG,
                    "read finish, time spend:${(System.currentTimeMillis() - startTimeMills) / 1000f}s"
                )
                if (contentType == "application/text") {
                    contentReceivedListener.onContentReceived(contentType, contentCombiner?.value)
                } else {
                    try {
                        val anyPair = contentCombiner?.value as? Pair<*, *>
                        val bufferedOutputStream = anyPair?.second as? BufferedOutputStream
                        bufferedOutputStream?.close()
                        contentReceivedListener.onContentReceived(contentType, anyPair?.first)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                contentCombiner = null
            }
            return 1
        }
        return -1
    }

    fun setClosed(closed: Boolean) {
        this.closed = closed
    }
}