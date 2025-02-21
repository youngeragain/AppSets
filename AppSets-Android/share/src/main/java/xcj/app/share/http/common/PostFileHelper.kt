package xcj.app.share.http.common

import android.content.Context
import xcj.app.share.base.ClientInfo
import xcj.app.share.base.DataContent
import xcj.app.share.base.ShareSystem
import xcj.app.share.http.HttpShareMethod
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.web.webserver.base.DataProgressInfoPool
import xcj.app.web.webserver.base.FileUploadN
import java.util.UUID

class PostFileHelper {

    companion object {
        private const val TAG = "PostFileHelper"
    }

    fun handleChunkedFile(
        context: Context,
        fileId: String,
        chunkCount: Int,
        chunk: Int,
        fileUploadN: FileUploadN
    ): Boolean {
        if (chunk < chunkCount) {

        }
        if (chunk == chunkCount) {

        }
        return true
    }

    fun handleFile(
        context: Context,
        shareMethod: HttpShareMethod,
        clientInfo: ClientInfo,
        fileUploadN: FileUploadN
    ): Boolean {
        PurpleLogger.current.d(
            TAG,
            "handleFile, fileCount:${fileUploadN.fileUploadList.size}"
        )
        if (fileUploadN.fileUploadList.isEmpty()) {
            return true
        }
        val fileUploadMap =
            fileUploadN.fileUploadList.groupBy { UUID.randomUUID().toString() to it.filename }
        val fileUploadProgressMap: MutableMap<String, Long> =
            fileUploadN.fileUploadList.groupBy { it.file.name }.mapValues { 0L }.toMutableMap()
        var hasNoCompleted = true
        while (hasNoCompleted) {
            fileUploadMap.forEach { (uuid, fileName), fileUploadList ->
                for (fileUpload in fileUploadList) {
                    hasNoCompleted = !fileUpload.isCompleted
                    if (fileUpload.isCompleted) {
                        val fileToSave =
                            ShareSystem.makeFileIfNeeded(fileUpload.filename, createFile = false)
                        if (fileToSave == null) {
                            continue
                        }
                        val total = fileUpload.definedLength()
                        val current = fileUploadProgressMap[fileName]?.plus(fileUpload.length())
                            ?: fileUpload.length()
                        fileUploadProgressMap[fileName] = current
                        fileUpload.renameTo(fileToSave)
                        val dataProgressInfo = DataProgressInfoPool.obtainById(uuid)
                        dataProgressInfo.name = fileName
                        dataProgressInfo.total = total
                        dataProgressInfo.current = current
                        shareMethod.dataReceivedProgressListener.onProgress(dataProgressInfo)
                        if (dataProgressInfo.percentage >= 100) {
                            shareMethod.onContentReceived(
                                DataContent.FileContent(fileUpload.file, null, clientInfo)
                            )
                        }
                    }
                }
            }
        }
        return true
    }
}