package xcj.app.appsets.ui.compose.wlanp2p

import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream
import java.util.UUID

abstract class CommonWriteThread(
    outputStream: OutputStream,
    private val iSocketExceptionListener: ISocketExceptionListener
) : Thread(), ISocketState, IThreadWriter {
    abstract val TAG: String
    private var bos = outputStream.buffered()
    private var closed = false

    @Volatile
    private var writeFunction: WriteFunction? = null

    override fun run() {
        try {
            while (!closed) {
                if (isSocketClosed())
                    break
                var tempFunction: WriteFunction? = null
                while (!closed) {
                    tempFunction = writeFunction
                    if (tempFunction != null) {
                        tempFunction.writeContent(TAG, bos)
                        writeFunction = null
                    }
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "run exception:" + e.message)
            iSocketExceptionListener.onException(TAG, e)
        }
    }

    fun writeWithFunction(writeFunction: WriteFunction) {
        this.writeFunction = writeFunction
    }

    fun writeString(str: String) {
        val byteArray = str.toByteArray()
        val customHeader = mapOf(
            "content-type" to "application/text",
            "content-length" to byteArray.size,
            "content-chunked" to false
        )
        writeBytesInternal(byteArray, customHeader)
    }

    private fun writeBytesInternal(byteArray: ByteArray, customHeader: Map<String, Any>? = null) {
        val writeFunction =
            WriteFunction { tag, bos ->
                val flag = UUID.randomUUID().toString()
                val gson = Gson()
                //start
                writeChunkStartBytes(bos, flag, gson, customHeader)
                //content
                writeChunkContentBytes(bos, flag, byteArray)
            }
        writeWithFunction(writeFunction)
    }

    fun writeBytes(byteArray: ByteArray) {
        val customHeader = mapOf(
            "content-type" to "application/text",
            "content-length" to byteArray.size,
            "content-chunked" to false
        )
        writeBytesInternal(byteArray, customHeader)
    }

    fun writeFile(file: File) {
        if (!file.canRead()) {
            Log.i(TAG, "write file failed! because the file ${file.name} can't be read")
            return
        }
        writeUriFileInputStream(UriFileNameAndInputStream(file.name, file.inputStream()))
        /*        val writeFunction =
                    WriteFunction { tag, bos ->
                        val fileLength = file.length()
                        val fbis = file.inputStream().buffered()
                        val available = fbis.available()
                        Log.e(TAG, "file length:${fileLength}, inputStream available:${available}")
                        val bufferSize = 2048
                        val fileBuffer = ByteArray(bufferSize)
                        var readOffset = 0L
                        val flag = UUID.randomUUID().toString()
                        val gson = Gson()
                        var chunkLength = (available/bufferSize)
                        if(available%bufferSize!=0){
                            chunkLength += 1
                        }
                        //start
                        val customHeader = mapOf(
                            "content-type" to "application/file",
                            "file-name" to file.name,
                            "content-length" to fileLength,
                            "content-chunked" to true,
                            "chunk-length" to chunkLength
                        )
                        try {
                            val startTimeMills = System.currentTimeMillis()
                            writeChunkStartBytes(bos, flag, gson, customHeader)
                            while (true) {
                                val readCount = fbis.read(fileBuffer)
                                if(readCount==-1){
                                    Log.e(TAG, "file read count is -1 break")
                                    break
                                }
                                readOffset += readCount-1
                                //content
                                if(readCount==bufferSize){
                                    Log.e(TAG, "write full buffer")
                                    writeChunkContentBytes(bos, flag, fileBuffer)
                                }else{
                                    Log.e(TAG, "write not full buffer")
                                    val dstByteArray = ByteArray(readCount)
                                    System.arraycopy(fileBuffer, 0, dstByteArray, 0, readCount)
                                    writeChunkContentBytes(bos, flag, dstByteArray)
                                }
                                if ((readOffset+1) == fileLength)
                                    break
                            }
                            Log.e(TAG, "write to bos finish, time spend:${(System.currentTimeMillis()-startTimeMills)/1000f}s")
                        }catch (e:Exception){
                            e.printStackTrace()
                        }finally {
                            fbis.close()
                        }
                    }
                writeWithFunction(writeFunction)*/
    }

    fun writeUriContent(uri: Uri) {
        //TODO
    }

    private fun writeChunkStartBytes(
        bos: BufferedOutputStream,
        flag: String,
        gson: Gson,
        customHeader: Map<String, Any>? = null
    ) {

        val header = mutableMapOf<String, Any>(
            "start" to true,
            "flag" to flag
        )
        if (!customHeader.isNullOrEmpty()) {
            header.putAll(customHeader)
        }
        val contentStart = DeliveryContent(header, null)
        val bytes = gson.toJson(contentStart).toByteArray()
        Log.i(TAG, "[$flag]writeChunkStartBytes, bytes size:${bytes.size + 1}")
        bos.write(bytes)
        bos.write(0)
    }

    private fun writeChunkContentBytes(
        bos: BufferedOutputStream,
        flag: String,
        contentChunkBytes: ByteArray
    ) {
        Log.i(TAG, "[$flag]writeChunkContentBytes last bytes:${contentChunkBytes.last()}")
        bos.write(contentChunkBytes)
    }

    fun setClosed(closed: Boolean) {
        this.closed = closed
    }

    override fun writeAny(any: Any) {
        when (any) {
            is String -> {
                writeString(any)
            }

            is ByteArray -> {
                writeBytes(any)
            }

            is File -> {
                writeFile(any)
            }

            is Uri -> {
                writeUriContent(any)
            }

            is UriFileNameAndInputStream -> {
                writeUriFileInputStream(any)
            }
        }
    }

    private fun writeUriFileInputStream(uriFileNameAndInputStream: UriFileNameAndInputStream) {
        Log.i(TAG, "writeUriFileInputStream")
        val writeFunction =
            WriteFunction { tag, bos ->
                val fbis = uriFileNameAndInputStream.inputStream.buffered()
                val available = fbis.available().toLong()
                Log.i(TAG, "inputStream available:${available}")
                val bufferSize = 2048
                val fileBuffer = ByteArray(bufferSize)
                val flag = UUID.randomUUID().toString()
                val gson = Gson()
                var chunkLength = (available / bufferSize)
                if (available % bufferSize != 0L) {
                    chunkLength += 1
                }
                //start
                val customHeader = mapOf(
                    "content-type" to "application/file",
                    "file-name" to (uriFileNameAndInputStream.fileName ?: ""),
                    "content-length" to available,
                    "content-chunked" to true,
                    "chunk-length" to chunkLength
                )
                try {
                    val startTimeMills = System.currentTimeMillis()
                    writeChunkStartBytes(bos, flag, gson, customHeader)
                    while (true) {
                        val readCount = fbis.read(fileBuffer)
                        if (readCount == -1) {
                            Log.i(TAG, "file read count is -1 break")
                            break
                        }
                        //content
                        if (readCount == bufferSize) {
                            writeChunkContentBytes(bos, flag, fileBuffer)
                        } else {
                            val dstByteArray = ByteArray(readCount)
                            System.arraycopy(fileBuffer, 0, dstByteArray, 0, readCount)
                            writeChunkContentBytes(bos, flag, dstByteArray)
                        }
                    }
                    Log.i(
                        TAG,
                        "write to bos finish, time spend:${(System.currentTimeMillis() - startTimeMills) / 1000f}s"
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    fbis.close()
                }
            }
        writeWithFunction(writeFunction)
    }
}