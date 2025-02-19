package xcj.app.share.http.service

import android.content.Context
import xcj.app.share.base.ClientInfo
import xcj.app.share.base.ClientSendDataInfo
import xcj.app.share.base.DataContent
import xcj.app.share.base.DataProgressInfoPool
import xcj.app.share.base.ShareSystem
import xcj.app.share.http.HttpShareMethod
import xcj.app.share.ui.compose.AppSetsShareActivity
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.base.FileUploadN
import java.util.UUID

class AppSetsShareServiceImpl : AppSetsShareService {

    companion object {
        private const val TAG = "AppSetsShareServiceImpl"
    }

    override fun greeting(context: Context, clientHost: String): DesignResponse<String> {
        return DesignResponse(data = "Hello, AppSets Share!")
    }

    override fun ping(context: Context, clientHost: String): DesignResponse<String> {
        return DesignResponse(data = "pong")
    }

    override fun isNeedPin(context: Context, clientHost: String): DesignResponse<Boolean> {
        if (context !is AppSetsShareActivity) {
            return DesignResponse(data = false)
        }
        val shareMethod = context.getShareMethod()
        if (shareMethod !is HttpShareMethod) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        val isNeedPin = shareMethod.isNeedPin(clientInfo)
        PurpleLogger.current.d(
            TAG,
            "isNeedPin, clientHost:$clientHost, isNeedPin:$isNeedPin"
        )
        return DesignResponse(data = isNeedPin)
    }

    override fun pair(
        context: Context,
        clientHost: String,
        pin: Int
    ): DesignResponse<Boolean> {
        if (context !is AppSetsShareActivity) {
            return DesignResponse(data = false)
        }
        val shareMethod = context.getShareMethod()
        if (shareMethod !is HttpShareMethod) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        PurpleLogger.current.d(TAG, "pair, clientHost:$clientHost, pin:$pin")
        shareMethod.onClientRequestPair(pin, clientInfo)
        return DesignResponse(data = true)
    }

    override fun pairResponse(
        context: Context,
        clientHost: String,
        token: String
    ): DesignResponse<Boolean> {
        if (context !is AppSetsShareActivity) {
            return DesignResponse(data = false)
        }
        val shareMethod = context.getShareMethod()
        if (shareMethod !is HttpShareMethod) {
            return DesignResponse(data = false)
        }
        PurpleLogger.current.d(
            TAG,
            "pairResponse, clientHost:$clientHost, token:$token"
        )
        val clientInfo = ClientInfo(clientHost)
        shareMethod.onSeverPairResponse(token, clientInfo)
        return DesignResponse(data = true)
    }

    override fun prepareSend(
        context: Context,
        clientHost: String,
        token: String
    ): DesignResponse<Boolean> {
        if (context !is AppSetsShareActivity) {
            return DesignResponse(data = false)
        }
        val shareMethod = context.getShareMethod()
        if (shareMethod !is HttpShareMethod) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        val clientSendDataInfo = ClientSendDataInfo(emptyList())
        PurpleLogger.current.d(
            TAG,
            "prepareSend, clientHost:$clientHost, token:$token"
        )
        shareMethod.onClientPrepareSend(clientInfo, clientSendDataInfo)
        return DesignResponse(data = true)
    }

    override fun prepareSendResponse(
        context: Context,
        clientHost: String,
        token: String,
        isAccept: Boolean
    ): DesignResponse<Boolean> {
        if (context !is AppSetsShareActivity) {
            return DesignResponse(data = false)
        }
        val shareMethod = context.getShareMethod()
        if (shareMethod !is HttpShareMethod) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        PurpleLogger.current.d(
            TAG,
            "prepareSendResponse, clientHost:$clientHost, token:$token"
        )
        shareMethod.onServerPrepareSendResponse(clientInfo, isAccept)
        return DesignResponse(data = true)
    }

    override fun postText(
        context: Context,
        clientHost: String,
        token: String,
        text: String
    ): DesignResponse<Boolean> {
        if (context !is AppSetsShareActivity) {
            return DesignResponse(data = false)
        }
        val shareMethod = context.getShareMethod()
        if (shareMethod !is HttpShareMethod) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        val uuid = UUID.randomUUID().toString()
        val dataProgressInfo = DataProgressInfoPool.obtainById(uuid)
        dataProgressInfo.name = null
        val textLength = text.length.toLong()
        dataProgressInfo.total = textLength
        dataProgressInfo.current = textLength
        dataProgressInfo.percentage = 100.0
        PurpleLogger.current.d(
            TAG,
            "postText, clientHost:$clientHost, token:$token, text:$text"
        )
        shareMethod.dataReceivedProgressListener.onProgress(dataProgressInfo)
        shareMethod.onContentReceived(
            ContentType.APPLICATION_TEXT,
            DataContent.StringContent(text, clientInfo)
        )
        return DesignResponse(data = true)
    }

    override fun postFile(
        context: Context,
        clientHost: String,
        token: String,
        fileUploadN: FileUploadN
    ): DesignResponse<Boolean> {
        if (context !is AppSetsShareActivity) {
            return DesignResponse(data = false)
        }
        val shareMethod = context.getShareMethod()
        if (shareMethod !is HttpShareMethod) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        PurpleLogger.current.d(
            TAG,
            "postFile, clientHost:$clientHost, token:$token, fileUploadN:${fileUploadN}"
        )
        saveFileIfNeeded(shareMethod, clientInfo, fileUploadN)

        return DesignResponse(data = true)
    }

    fun saveFileIfNeeded(
        shareMethod: HttpShareMethod,
        clientInfo: ClientInfo,
        fileUploadN: FileUploadN?
    ) {
        PurpleLogger.current.d(TAG, "saveFileIfNeeded, fileCount:${fileUploadN?.amount}")
        var currentFileUploadN: FileUploadN? = fileUploadN
        do {
            if (currentFileUploadN == null) {
                return
            }
            val fileUpload = currentFileUploadN.fileUpload

            if (fileUpload.isCompleted) {
                val fileToSave =
                    ShareSystem.makeFileIfNeeded(fileUpload.filename, createFile = false)
                if (fileToSave == null) {
                    continue
                }
                fileUpload.renameTo(fileToSave)

                val fileId = UUID.randomUUID().toString()
                val dataProgressInfoForEnd = DataProgressInfoPool.makeEnd(fileId)
                shareMethod.dataReceivedProgressListener.onProgress(dataProgressInfoForEnd)

                shareMethod.onContentReceived(
                    ContentType.APPLICATION_FILE,
                    DataContent.FileContent(fileUpload.file, null, clientInfo)
                )
            }
            currentFileUploadN = currentFileUploadN.next
        } while (currentFileUploadN != null)
    }

}