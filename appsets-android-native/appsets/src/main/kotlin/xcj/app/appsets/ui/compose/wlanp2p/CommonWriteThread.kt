package xcj.app.appsets.ui.compose.wlanp2p

import android.net.Uri
import android.util.Log
import okhttp3.internal.notify
import okhttp3.internal.wait
import java.io.BufferedOutputStream
import java.io.File
import java.io.OutputStream
import java.util.UUID

abstract class CommonWriteThread(
    outputStream: OutputStream,
    private val progressListener: ProgressListener?,
    private val iSocketExceptionListener: ISocketExceptionListener
) : Thread(), ISocketState, IThreadWriter {
    abstract val TAG: String
    private var bos = outputStream.buffered()
    private var closed = false

    //TODO 变成链表
    @Volatile
    private var writeFunction: WriteFunction? = null
    private val readLock: Any = Any()
    override fun run() {
        try {
            synchronized(readLock) {
                while (!closed) {
                    if (isSocketClosed())
                        break
                    while (!closed) {
                        if (writeFunction != null) {
                            writeFunction?.writeContent(TAG, bos)
                            writeFunction = null
                        }
                        if (writeFunction == null) {
                            Log.i(TAG, "thread[${id}] waiting a new writeFunction")
                            readLock.wait()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "run exception:" + e.message)
            iSocketExceptionListener.onException(TAG, e)
        }
    }

    fun writeWithFunction(writeFunction: WriteFunction) {
        synchronized(readLock) {
            this.writeFunction = writeFunction
            Log.i(TAG, "thread[${id}] notify with a new writeFunction")
            this.readLock.notify()
        }
    }

    private fun writeBytesInternal(
        byteArray: ByteArray,
        contentType: String,
        fileName: String?,
        contentLength: Long,
        isContentChunked: Boolean,
        chunkCount: Int?
    ) {
        val writeFunction =
            WriteFunction { tag, bos ->
                val uuid = UUID.randomUUID().toString()
                //start
                writeChunkStartBytes(
                    bos,
                    uuid,
                    contentType,
                    fileName,
                    contentLength,
                    isContentChunked,
                    chunkCount
                )
                //content
                writeChunkContentBytes(
                    bos,
                    uuid,
                    byteArray.size.toLong(),
                    0L,
                    byteArray,
                    0,
                    byteArray.size
                )
                bos.flush()
            }
        writeWithFunction(writeFunction)
    }

    fun writeString(str: String) {
        val byteArray = str.toByteArray()
        writeBytesInternal(byteArray, ContentType.Text, null, byteArray.size.toLong(), false, 1)
    }

    fun writeBytes(byteArray: ByteArray) {
        writeBytesInternal(byteArray, ContentType.Bytes, null, byteArray.size.toLong(), false, 1)
    }

    fun writeFile(file: File) {
        if (!file.canRead()) {
            Log.i(TAG, "write file failed! because the file ${file.name} can't be read")
            return
        }
        writeUriFileInputStream(UriFileNameAndInputStream(file.name, file.inputStream()))
    }

    fun writeUriContent(uri: Uri) {
        //TODO
    }


    private fun writeChunkStartBytes(
        bos: BufferedOutputStream,
        uuid: String,
        contentType: String,
        fileName: String?,
        contentLength: Long,
        isContentChunked: Boolean,
        chunkCount: Int?
    ) {
        var headerBytesCount = 0


        val headerBytes = arrayListOf<Byte>()

        val uuidBytes = uuid.toByteArray().toMutableList()//byte[40]
        if (uuidBytes.size < 40) {
            for (i in uuidBytes.size until 40) {
                uuidBytes.add(0)
            }
        }
        //Log.i(TAG, "uuidBytes size:${uuidBytes.size}:\n${uuidBytes.joinToString { it.toString() }}")
        headerBytesCount += uuidBytes.size
        headerBytes.addAll(uuidBytes)//uuid

        val contentTypeBytes = contentType.toByteArray().toMutableList()//byte[36]
        if (contentTypeBytes.size < 36) {
            for (i in contentTypeBytes.size until 36) {
                contentTypeBytes.add(0)
            }
        }
        //Log.i(TAG, "contentTypeBytes size:${contentTypeBytes.size}:\n${contentTypeBytes.joinToString { it.toString() }}")
        headerBytesCount += contentTypeBytes.size
        headerBytes.addAll(contentTypeBytes)//contentType

        val fileNameBytes =
            fileName?.toByteArray()?.toMutableList() ?: mutableListOf<Byte>().apply {//byte[40]

            }//512个byte
        if (fileNameBytes.size < 512) {
            for (i in fileNameBytes.size until 512) {
                fileNameBytes.add(0)
            }
        }
        //Log.i(TAG, "fileNameBytes size:${fileNameBytes.size}:\n${fileNameBytes.joinToString { it.toString() }}")
        headerBytesCount += fileNameBytes.size
        headerBytes.addAll(fileNameBytes)//fileName

        val contentLengthBytes = mutableListOf<Byte>().apply {//byte[8]
            addAll(longToByteArray(contentLength).toList())
        }
        if (contentLengthBytes.size < 8) {
            for (i in contentLengthBytes.size until 8) {
                contentLengthBytes.add(0)
            }
        }
        //Log.i(TAG, "contentLength:${contentLength} contentLengthBytes size:${contentLengthBytes.size}:\n${contentLengthBytes.joinToString { it.toString() }}")
        headerBytesCount += contentLengthBytes.size
        headerBytes.addAll(contentLengthBytes)//contentLength


        val isContentChunkedBytes = mutableListOf<Byte>().apply {//byte[1]
            if (isContentChunked) {
                add(1)
            } else {
                add(0)
            }
        }
        if (isContentChunkedBytes.size < 1) {
            isContentChunkedBytes.add(0)
        }
        //Log.i(TAG, "isContentChunkedBytes size:${isContentChunkedBytes.size}:\n${isContentChunkedBytes.joinToString { it.toString() }}")
        headerBytesCount += isContentChunkedBytes.size
        headerBytes.addAll(isContentChunkedBytes)//isContentChunked


        val chunkCountBytes = mutableListOf<Byte>().apply {//byte[4]
            if (chunkCount != null) {
                addAll(intToByteArray(chunkCount).toList())
            }
        }
        if (chunkCountBytes.size < 4) {
            for (i in chunkCountBytes.size until 4) {
                chunkCountBytes.add(0)
            }
        }
        //Log.i(TAG, "chunkCountBytes size:${chunkCountBytes.size}:\n${chunkCountBytes.joinToString { it.toString() }}")
        headerBytesCount += chunkCountBytes.size
        headerBytes.addAll(chunkCountBytes)//chunkCount


        headerBytes.addAll(0, contentLengthBytes)//byte[8]

        val headerCountBytes = mutableListOf<Byte>().apply {//byte[4]
            addAll(intToByteArray(headerBytesCount).toList())
        }

        //Log.i(TAG, "headerBytesCount:\n${headerBytesCount}")
        //Log.i(TAG, "headerCountBytes size:${headerCountBytes.size}:\n${headerCountBytes.joinToString { it.toString() }}")
        headerBytes.addAll(0, headerCountBytes)

        Log.i(TAG, "[$uuid]writeChunkStartBytes, bytes size:${headerBytes.size}")
        bos.write(headerBytes.toByteArray())


    }

    private fun writeChunkContentBytes(
        bos: BufferedOutputStream,
        uuid: String,
        totalLength: Long,
        writtenLength: Long,
        contentChunkBytes: ByteArray,
        startIndex: Int,
        length: Int
    ) {
        //Log.i(TAG, "[$uuid]writeChunkContentBytes length:${length} byte first:${contentChunkBytes[0]} last:${contentChunkBytes[length-1]} last-1:${contentChunkBytes[length-2]}")
        Log.i(TAG, "[$uuid]writeChunkContentBytes length:${length}")
        bos.write(contentChunkBytes, startIndex, length)
        progressListener?.onProgress(
            null,
            totalLength,
            writtenLength,
            (((writtenLength + length) / totalLength.toDouble()) * 100).toInt(),
            0
        )
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
                val uuid = UUID.randomUUID().toString()
                var chunkCount: Int = (available / bufferSize).toInt()
                if (available % bufferSize != 0L) {
                    chunkCount += 1
                }
                //start
                try {
                    val startTimeMills = System.currentTimeMillis()
                    writeChunkStartBytes(
                        bos, uuid, ContentType.File,
                        uriFileNameAndInputStream.fileName, available,
                        true, chunkCount
                    )
                    var writtenBytesSize = 0L
                    while (true) {
                        val readCount = fbis.read(fileBuffer)
                        if (readCount == -1) {
                            Log.i(TAG, "file read count is -1 break")
                            break
                        }
                        //content
                        writeChunkContentBytes(
                            bos,
                            uuid,
                            available,
                            writtenBytesSize,
                            fileBuffer,
                            0,
                            readCount
                        )
                        writtenBytesSize += readCount
                    }
                    bos.flush()
                    Log.i(
                        TAG,
                        "write to bos finish, time spend:${(System.currentTimeMillis() - startTimeMills) / 1000f}s, bytesWriteSize:${writtenBytesSize}"
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