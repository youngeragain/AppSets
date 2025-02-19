package xcj.app.share.wlanp2p.stream

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.delay
import xcj.app.share.base.DataProgressInfoPool
import xcj.app.share.base.ProgressListener
import xcj.app.share.wlanp2p.base.DataHandleExceptionListener
import xcj.app.share.wlanp2p.base.WriteFunction
import xcj.app.share.wlanp2p.base.WriteMethod
import xcj.app.share.wlanp2p.common.ReadThread
import xcj.app.share.wlanp2p.common.WriteThread
import xcj.app.starter.android.util.FileUtils
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ByteUtil
import xcj.app.starter.util.ContentType
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.net.Socket
import java.util.UUID

class StreamWriteMethod(
    socket: Socket,
    private val writeThread: WriteThread,
    private val progressListener: ProgressListener? = null,
    private val dataHandleExceptionListener: DataHandleExceptionListener? = null
) : WriteMethod {

    companion object {
        private const val TAG = "StreamWriteMethod"
    }


    private val outputStream: BufferedOutputStream =
        socket.outputStream.buffered(ReadThread.Companion.WRITE_BUFFER_SIZE)

    override fun writeChunkStartBytes(
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
        //PurpleLogger.current.d(TAG, "uuidBytes size:${uuidBytes.size}:\n${uuidBytes.joinToString { it.toString() }}")
        headerBytesCount += uuidBytes.size
        headerBytes.addAll(uuidBytes)//uuid

        val contentTypeBytes = contentType.toByteArray().toMutableList()//byte[36]
        if (contentTypeBytes.size < 36) {
            for (i in contentTypeBytes.size until 36) {
                contentTypeBytes.add(0)
            }
        }
        //PurpleLogger.current.d(TAG, "contentTypeBytes size:${contentTypeBytes.size}:\n${contentTypeBytes.joinToString { it.toString() }}")
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
        //PurpleLogger.current.d(TAG, "fileNameBytes size:${fileNameBytes.size}:\n${fileNameBytes.joinToString { it.toString() }}")
        headerBytesCount += fileNameBytes.size
        headerBytes.addAll(fileNameBytes)//fileName

        val contentLengthBytes = mutableListOf<Byte>().apply {//byte[8]
            addAll(ByteUtil.longToByteArray(contentLength).toList())
        }
        if (contentLengthBytes.size < 8) {
            for (i in contentLengthBytes.size until 8) {
                contentLengthBytes.add(0)
            }
        }
        //PurpleLogger.current.d(TAG, "contentLength:${contentLength} contentLengthBytes size:${contentLengthBytes.size}:\n${contentLengthBytes.joinToString { it.toString() }}")
        headerBytesCount += contentLengthBytes.size
        headerBytes.addAll(contentLengthBytes)//contentLength


        val isContentChunkedBytes = mutableListOf<Byte>().apply {//byte[1]
            if (isContentChunked) {
                add(1)
            } else {
                add(0)
            }
        }
        if (isContentChunkedBytes.isEmpty()) {
            isContentChunkedBytes.add(0)
        }
        //PurpleLogger.current.d(TAG, "isContentChunkedBytes size:${isContentChunkedBytes.size}:\n${isContentChunkedBytes.joinToString { it.toString() }}")
        headerBytesCount += isContentChunkedBytes.size
        headerBytes.addAll(isContentChunkedBytes)//isContentChunked


        val chunkCountBytes = mutableListOf<Byte>().apply {//byte[4]
            if (chunkCount != null) {
                addAll(ByteUtil.intToByteArray(chunkCount).toList())
            }
        }
        if (chunkCountBytes.size < 4) {
            for (i in chunkCountBytes.size until 4) {
                chunkCountBytes.add(0)
            }
        }
        //PurpleLogger.current.d(TAG, "chunkCountBytes size:${chunkCountBytes.size}:\n${chunkCountBytes.joinToString { it.toString() }}")
        headerBytesCount += chunkCountBytes.size
        headerBytes.addAll(chunkCountBytes)//chunkCount


        headerBytes.addAll(0, contentLengthBytes)//byte[8]

        val headerCountBytes = mutableListOf<Byte>().apply {//byte[4]
            addAll(ByteUtil.intToByteArray(headerBytesCount).toList())
        }

        //PurpleLogger.current.d(TAG, "headerBytesCount:\n${headerBytesCount}")
        //PurpleLogger.current.d(TAG, "headerCountBytes size:${headerCountBytes.size}:\n${headerCountBytes.joinToString { it.toString() }}")
        headerBytes.addAll(0, headerCountBytes)

        PurpleLogger.current.d(
            TAG,
            "writeChunkStartBytes[$uuid], bytes size:${headerBytes.size}"
        )
        outputStream.write(headerBytes.toByteArray())
    }

    override fun writeChunkContentBytes(
        uuid: String,
        contentType: String,
        name: String?,
        totalLength: Long,
        writtenLength: Long,
        contentChunkBytes: ByteArray,
        startIndex: Int,
        length: Int
    ) {
        PurpleLogger.current.d(
            TAG,
            "writeChunkContentBytes[$uuid], length:${length}"
        )
        outputStream.write(contentChunkBytes, startIndex, length)
        if (!ReadThread.Companion.SYSTEM_CONTENT_TYPES.contains(contentType)) {
            val progressListener = progressListener
            if (progressListener != null) {
                val progress = ((writtenLength + length) / totalLength.toDouble()) * 100
                val dataProgressInfo = DataProgressInfoPool.obtainById(uuid)
                dataProgressInfo.name = name
                dataProgressInfo.total = totalLength
                dataProgressInfo.current = writtenLength
                dataProgressInfo.percentage = progress
                progressListener.onProgress(dataProgressInfo)
            }
        }
    }

    override suspend fun writeUriContent(context: Context, uri: Uri) {
        val androidUriFile = FileUtils.parseFromAndroidUri(context, uri)
        if (androidUriFile == null) {
            PurpleLogger.current.d(
                TAG,
                "writeUriContent Uri's parseFromAndroidUri return null, return"
            )
            return
        }
        PurpleLogger.current.d(
            TAG,
            "writeUriContent Uri's androidUriFile:$androidUriFile"
        )
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                PurpleLogger.current.d(
                    TAG,
                    "writeUriContent fileInputStream is null, return"
                )
                return
            }
            val bis = inputStream.buffered(ReadThread.READ_BUFFER_SIZE)
            delay(50)
            val uuid = UUID.randomUUID().toString()
            writeFromInputStream(
                bis,
                uuid,
                androidUriFile.displayName ?: uuid,
                androidUriFile.size
            )
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.d(
                TAG,
                "writeUriContent openInputStream exception, ${e.message}"
            )
        }
    }

    override suspend fun writeFileContent(file: File) {
        if (!file.canRead()) {
            PurpleLogger.current.d(
                TAG,
                "writeFileContent, write file failed! because the file ${file.name} can't be read"
            )
            return
        }
        PurpleLogger.current.d(TAG, "writeFileContent")
        val inputStream = file.inputStream()
        val bis = inputStream.buffered(ReadThread.READ_BUFFER_SIZE)
        delay(50)
        val uuid = UUID.randomUUID().toString()
        writeFromInputStream(bis, uuid, file.name, file.length())
    }

    override fun writeBytesContent(
        byteArray: ByteArray,
        contentType: String,
        fileName: String?,
        contentLength: Long,
        isContentChunked: Boolean,
        chunkCount: Int?
    ) {
        PurpleLogger.current.d(TAG, "writeBytesContent")
        val writeFunction =
            WriteFunction { tag ->
                val uuid = UUID.randomUUID().toString()
                //start
                writeChunkStartBytes(
                    uuid,
                    contentType,
                    fileName,
                    contentLength,
                    isContentChunked,
                    chunkCount
                )
                //content
                writeChunkContentBytes(
                    uuid,
                    contentType,
                    fileName,
                    byteArray.size.toLong(),
                    0L,
                    byteArray,
                    0,
                    byteArray.size
                )
                outputStream.flush()
            }
        writeThread.writeWithFunction(writeFunction)
    }

    private fun writeFromInputStream(
        bis: BufferedInputStream,
        uuid: String,
        name: String,
        size: Long = 0L
    ) {
        val writeFunction =
            WriteFunction { tag ->
                //start

                try {
                    val bisAvailable = bis.available().toLong()
                    PurpleLogger.current.d(
                        TAG,
                        "writeFromInputStream, bisAvailable:${bisAvailable}, size:$size"
                    )
                    if (size == 0L) {
                        return@WriteFunction
                    }

                    val bufferSize = ReadThread.READ_BUFFER_SIZE
                    val fileBytesBuffer = ByteArray(bufferSize)

                    var chunkCount: Int = (size / bufferSize).toInt()
                    if (size % bufferSize != 0L) {
                        chunkCount += 1
                    }
                    val startTimeMills = System.currentTimeMillis()
                    val contentType = ContentType.APPLICATION_FILE
                    writeChunkStartBytes(
                        uuid, contentType,
                        name, size,
                        true, chunkCount
                    )
                    var writtenBytesSize = 0L
                    while (true) {
                        val readCount = bis.read(fileBytesBuffer)
                        if (readCount == -1) {
                            PurpleLogger.current.d(
                                TAG,
                                "writeFromInputStream, file read count is -1 break"
                            )
                            break
                        }
                        //content
                        writeChunkContentBytes(
                            uuid,
                            contentType,
                            name,
                            size,
                            writtenBytesSize,
                            fileBytesBuffer,
                            0,
                            readCount
                        )
                        writtenBytesSize += readCount
                    }
                    outputStream.flush()
                    PurpleLogger.current.d(
                        TAG,
                        "writeFromInputStream, write to bos finish, time spend:" +
                                "${(System.currentTimeMillis() - startTimeMills) / 1000f}s," +
                                " bytesWriteSize:${writtenBytesSize}"
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    PurpleLogger.current.e(
                        TAG,
                        "writeFromInputStream, write to bos exception ${e.message}"
                    )
                    dataHandleExceptionListener?.onException(TAG, e)
                } finally {
                    bis.close()
                }
            }
        writeThread.writeWithFunction(writeFunction)
    }

    override fun close() {
        PurpleLogger.current.d(TAG, "close")
    }
}