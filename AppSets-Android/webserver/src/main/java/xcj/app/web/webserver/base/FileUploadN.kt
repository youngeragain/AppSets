package xcj.app.web.webserver.base

import io.netty.handler.codec.http.HttpContent
import io.netty.handler.codec.http.multipart.FileUpload
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder
import io.netty.handler.codec.http.multipart.InterfaceHttpData
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.ShareSystem
import xcj.app.web.webserver.interfaces.ListenersProvider
import java.io.Closeable
import java.io.File
import java.util.UUID

class FileUploadN(
    val id: String = UUID.randomUUID().toString()
) : Closeable {
    companion object {
        private const val TAG = "FileUploadN"
    }
    var current: Long = 0L
    var total: Long = 0L

    private val files: MutableSet<File> = mutableSetOf()

    fun addHttpContent(
        httpContent: HttpContent,
        httpPostRequestDecoder: HttpPostRequestDecoder,
        listenersProvider: ListenersProvider?
    ) {
        val currentPartialHttpData = httpPostRequestDecoder.currentPartialHttpData()

        if (currentPartialHttpData is FileUpload) {
            val currentFileUpload = currentPartialHttpData
            current = currentFileUpload.length()
            val receiveProgressListener = listenersProvider?.getReceiveProgressListener()
            if (receiveProgressListener != null) {
                val dataProgressInfo = DataProgressInfoPool.obtainById(id)
                dataProgressInfo.name = currentFileUpload.filename
                dataProgressInfo.total = total
                dataProgressInfo.current = current
                receiveProgressListener.onProgress(dataProgressInfo)
            }
        }
        try {
            while (httpPostRequestDecoder.hasNext()) {
                val interfaceHttpData = httpPostRequestDecoder.next()
                if (interfaceHttpData.httpDataType != InterfaceHttpData.HttpDataType.FileUpload) {
                    continue
                }
                if (interfaceHttpData !is FileUpload) {
                    continue
                }
                val fileUpload = interfaceHttpData
                if (interfaceHttpData.isCompleted) {
                    val fileToSave =
                        ShareSystem.makeFileIfNeeded(fileUpload.filename, createFile = false)
                    if (fileToSave != null) {
                        fileUpload.renameTo(fileToSave)
                        files.add(fileToSave)
                    }
                    PurpleLogger.current.d(TAG, "addHttpContent, release fileUpload")
                    //fileUpload.release()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            PurpleLogger.current.d(TAG, "addHttpContent, failed, ${e.message}")
        } finally {
            //httpContent.release()
        }
    }

    override fun close() {
        files.clear()
    }

    fun getContents(): Set<File> {
        return files
    }
}
