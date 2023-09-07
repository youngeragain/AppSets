package xcj.app.appsets.ui.compose.wlanp2p

import android.util.Log
import xcj.app.core.android.ApplicationHelper
import java.io.BufferedOutputStream
import java.io.File
import java.io.InputStream

class ContentCombinerValueObject(val contentHeader: Map<String, Any>, var value: Any?)

abstract class CommonReadThread(
    inputStream: InputStream,
    private val progressListener: ProgressListener?,
    private val contentReceivedListener: ContentReceivedListener,
    private val iSocketExceptionListener: ISocketExceptionListener
) : Thread(), ISocketState {
    abstract val TAG: String
    private var bis = inputStream.buffered()
    private var closed = false
    private var contentCombiner: ContentCombinerValueObject? = null
    private var readLength = 0L
    private var requiredLength = 0L
    private var startTimeMills = 0L
    override fun run() {
        try {
            val bufferSize = 2048
            val buffer = ByteArray(bufferSize)
            var bufferPosition: Int
            val headerCountBytes = ByteArray(4)
            var headerCountPosition: Int
            val bodyCountBytes = ByteArray(8)
            var bodyCountPosition: Int
            var headerContentBytes: ByteArray?
            var headerContentPosition: Int
            while (!closed) {
                if (isSocketClosed())
                    break
                contentCombiner = null
                requiredLength = 0
                readLength = 0
                bufferPosition = 0
                headerCountPosition = 0
                bodyCountPosition = 0
                headerContentBytes = null
                headerContentPosition = 0
                while (!closed) {
                    val byte = bis.read()
                    if (headerCountPosition < 4) {
                        headerCountBytes[headerCountPosition++] = byte.toByte()
                    } else if (bodyCountPosition < 8) {
                        bodyCountBytes[bodyCountPosition++] = byte.toByte()
                    } else {
                        if (headerContentBytes == null) {
                            val headerCount = byteArrayToInt(headerCountBytes)
                            Log.i(TAG, "headerCount:${headerCount}")
                            val bodyCount = byteArrayToLong(bodyCountBytes)
                            Log.i(TAG, "bodyCount:${bodyCount}")
                            headerContentBytes = ByteArray(headerCount)
                        }
                        if (headerContentPosition + 1 < headerContentBytes.size) {
                            headerContentBytes[headerContentPosition++] = byte.toByte()
                        } else {
                            if (contentCombiner == null) {
                                readHeaderContent(headerContentBytes)
                            } else {
                                buffer[bufferPosition] = byte.toByte()
                                if (bufferPosition + 1 < bufferSize) {
                                    bufferPosition++
                                    if (readLength + bufferPosition == requiredLength) {
                                        readContentChunk(buffer, 0, bufferPosition)
                                        break
                                    }
                                } else {//buffer full
                                    readContentChunk(buffer, 0, bufferSize)
                                    bufferPosition = 0
                                }
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

    private fun readHeaderContent(byteArray: ByteArray) {
        if (byteArray.isEmpty())
            return
        val header = mutableMapOf<String, Any>()
        var lastNoneZeroPositionInBytes: Int? = null
        var temBytes = readBytes(byteArray, 0, 40)
        lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(temBytes)
        val uuid = temBytes.decodeToString(0, lastNoneZeroPositionInBytes ?: temBytes.size)
        header["uuid"] = uuid

        temBytes = readBytes(byteArray, 40, 36)
        lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(temBytes)
        val contentType = temBytes.decodeToString(0, lastNoneZeroPositionInBytes ?: temBytes.size)
        header["content-type"] = contentType

        temBytes = readBytes(byteArray, 76, 512)
        lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(temBytes)
        val fileName = temBytes.decodeToString(0, lastNoneZeroPositionInBytes ?: temBytes.size)
        header["file-name"] = fileName

        temBytes = readBytes(byteArray, 588, 8)
        //lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(temBytes)
        val contentLength = byteArrayToLong(temBytes)
        header["content-length"] = contentLength

        temBytes = readBytes(byteArray, 596, 1)
        //lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(temBytes)
        val isContentChunked = temBytes[0].toInt() == 1
        header["content-chunked"] = isContentChunked

        temBytes = readBytes(byteArray, 597, 4)
        //lastNoneZeroPositionInBytes = lastNoneZeroPositionInBytes(temBytes)
        val chunkCount = byteArrayToInt(temBytes)
        header["chunk-count"] = chunkCount
        requiredLength = contentLength
        readLength = 0L
        if (contentType.isEmpty())
            return
        val value = if (contentType == "application/file") {
            val file =
                File(ApplicationHelper.getContextFileDir().publicDownloadDir + File.separator + fileName)
            if (file.exists())
                file.delete()
            file.createNewFile()
            file to file.outputStream().buffered(2 * 1024)
        } else {
            null
        }
        startTimeMills = System.currentTimeMillis()
        contentCombiner = ContentCombinerValueObject(header, value)
        progressListener?.onProgress(header, requiredLength, 0L, 0, 0)
        Log.i(TAG, "readChunk[$uuid] start:\n${header}")
    }

    private fun readBytes(byteArray: ByteArray, startIndex: Int, length: Int): ByteArray {
        val destBytes = ByteArray(length)
        System.arraycopy(byteArray, startIndex, destBytes, 0, length)
        return destBytes
    }

    private fun lastNoneZeroPositionInBytes(byteArray: ByteArray): Int? {
        for (i in byteArray.indices.reversed()) {
            if (byteArray[i].toInt() != 0)
                return i + 1
        }
        return null
    }

    /**
     * @param byteArray chunk bytes
     * @return 0 is chunkStart, 1 is chunk content, 2 is chunk end
     */
    private fun readContentChunk(byteArray: ByteArray, startIndex: Int, length: Int) {
        if (byteArray.isEmpty()) {
            return
        }
        val tempCombiner = contentCombiner ?: return

        val contentType = tempCombiner.contentHeader["content-type"]?.toString()

        if (contentType == ContentType.Text) {
            try {
                if (tempCombiner.value != null && tempCombiner.value is ByteArray) {
                    val lastBytes = tempCombiner.value as? ByteArray
                    if (lastBytes == null || lastBytes.isEmpty()) {
                        return
                    }
                    val newBytes = ByteArray(lastBytes.size + length)
                    System.arraycopy(lastBytes, 0, newBytes, 0, lastBytes.size)
                    System.arraycopy(byteArray, 0, newBytes, lastBytes.size, length)
                    tempCombiner.value = newBytes
                } else {
                    val newBytes = ByteArray(length)
                    System.arraycopy(byteArray, 0, newBytes, 0, length)
                    tempCombiner.value = newBytes
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (contentType == ContentType.File) {
            try {

                //Log.i(TAG, "write file content")
                val bos = (tempCombiner.value as? Pair<*, *>)?.second as? BufferedOutputStream
                //Log.i(TAG, "write byte length:${length} first:${byteArray[0]} last:${byteArray[length-1]} last-1:${byteArray[length-2]}")
                bos?.write(byteArray, startIndex, length)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        readLength += length
        progressListener?.onProgress(
            tempCombiner.contentHeader,
            requiredLength,
            readLength,
            ((readLength / requiredLength.toDouble()) * 100).toInt(),
            0
        )
        //Log.i(TAG, "requiredLength:$requiredLength, readLength:$readLength")
        if (readLength == requiredLength) {//read finish
            Log.i(
                TAG,
                "read finish, time spend:${(System.currentTimeMillis() - startTimeMills) / 1000f}s"
            )
            if (contentType == ContentType.Text) {
                contentReceivedListener.onContentReceived(contentType, contentCombiner?.value)
            } else {
                try {
                    val anyPair = contentCombiner?.value as? Pair<*, *>
                    val bufferedOutputStream = anyPair?.second as? BufferedOutputStream
                    bufferedOutputStream?.close()
                    Log.i(
                        TAG,
                        "read finish, close output stream"
                    )
                    contentReceivedListener.onContentReceived(contentType, anyPair?.first)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun setClosed(closed: Boolean) {
        this.closed = closed
    }
}