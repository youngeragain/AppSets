package xcj.app.share.http.common

import android.content.Context
import xcj.app.share.base.ClientInfo
import xcj.app.share.base.DataContent
import xcj.app.share.http.HttpShareMethod
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.web.webserver.base.ContentUploadN

class PostFileHelper {

    companion object {
        private const val TAG = "PostFileHelper"
    }

    fun handleChunkedFile(
        context: Context,
        fileId: String,
        chunkCount: Int,
        chunk: Int,
        contentUploadN: ContentUploadN
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
        contentUploadN: ContentUploadN
    ): Boolean {
        val files = contentUploadN.getContents()
        PurpleLogger.current.d(
            TAG,
            "handleFile, fileCount:${files.size}"
        )
        if (files.isEmpty()) {
            return true
        }
        files.forEach { file ->
            shareMethod.onContentReceived(
                DataContent.FileContent(file, null, clientInfo, contentUploadN.id)
            )
        }
        return true
    }
}