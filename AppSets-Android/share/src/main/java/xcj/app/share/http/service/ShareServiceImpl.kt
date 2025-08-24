@file:OptIn(ExperimentalEncodingApi::class)

package xcj.app.share.http.service

import android.content.Context
import xcj.app.share.base.ClientInfo
import xcj.app.share.base.DataContent
import xcj.app.share.http.HttpShareMethod
import xcj.app.share.http.base.HttpShareDevice
import xcj.app.share.http.common.DataContentReadableData
import xcj.app.share.http.common.PostFileHelper
import xcj.app.share.http.model.ContentInfo
import xcj.app.share.http.model.ContentInfoList
import xcj.app.share.ui.compose.AppSetsShareActivity
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.web.webserver.base.ContentDownloadN
import xcj.app.web.webserver.base.ContentUploadN
import xcj.app.web.webserver.base.DataProgressInfoPool
import xcj.app.web.webserver.base.ReadableData
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.UUID
import kotlin.io.encoding.ExperimentalEncodingApi

class ShareServiceImpl : ShareService {

    companion object Companion {
        private const val TAG = "AppSetsShareServiceImpl"
    }

    private val postFileHelper = PostFileHelper()

    override fun greeting(context: Context, clientHost: String): DesignResponse<String> {
        return DesignResponse(data = "Hello, AppSets Share!")
    }

    override fun ping(context: Context, clientHost: String): DesignResponse<String> {
        return DesignResponse(data = "pong")
    }

    override fun isNeedPin(context: Context, clientHost: String): DesignResponse<Boolean> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
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
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
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
        shareToken: String
    ): DesignResponse<Boolean> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
            return DesignResponse(data = false)
        }
        PurpleLogger.current.d(
            TAG,
            "pairResponse, clientHost:$clientHost, shareToken:$shareToken"
        )
        val clientInfo = ClientInfo(clientHost)
        shareMethod.onSeverPairResponse(shareToken, clientInfo)
        return DesignResponse(data = true)
    }

    override fun prepareSend(
        context: Context,
        clientHost: String,
        shareToken: String,
        uri: String,
    ): DesignResponse<Boolean> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        PurpleLogger.current.d(
            TAG,
            "prepareSend, clientHost:$clientHost, shareToken:$shareToken, uri:$uri"
        )
        shareMethod.onClientPrepareSend(clientInfo, uri)
        return DesignResponse(data = true)
    }

    override fun prepareSendResponse(
        context: Context,
        clientHost: String,
        shareToken: String,
        isAccept: Boolean,
        preferDownloadSelf: Boolean,
    ): DesignResponse<Boolean> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        PurpleLogger.current.d(
            TAG,
            "prepareSendResponse, clientHost:$clientHost, shareToken:$shareToken"
        )
        shareMethod.onServerPrepareSendResponse(clientInfo, isAccept, preferDownloadSelf)
        return DesignResponse(data = true)
    }

    override fun postText(
        context: Context,
        clientHost: String,
        shareToken: String,
        text: String
    ): DesignResponse<Boolean> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        val uuid = UUID.randomUUID().toString()
        val dataProgressInfo = DataProgressInfoPool.obtainById(uuid)
        dataProgressInfo.name = text
        val textLength = text.length.toLong()
        dataProgressInfo.total = textLength
        dataProgressInfo.current = textLength
        PurpleLogger.current.d(
            TAG,
            "postText, clientHost:$clientHost, shareToken:$shareToken, text:$text"
        )
        shareMethod.dataReceivedProgressListener.onProgress(dataProgressInfo)
        shareMethod.onContentReceived(DataContent.StringContent(text, clientInfo))
        return DesignResponse(data = true)
    }

    override fun postFile(
        context: Context,
        clientHost: String,
        shareToken: String,
        contentUploadN: ContentUploadN
    ): DesignResponse<Boolean> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
            return DesignResponse(data = false)
        }
        val clientInfo = ClientInfo(clientHost)
        PurpleLogger.current.d(
            TAG,
            "postFile, clientHost:$clientHost, shareToken:$shareToken, fileUploadN:${contentUploadN}"
        )
        val handleResult =
            postFileHelper.handleFile(context, shareMethod, clientInfo, contentUploadN)

        return DesignResponse(data = handleResult)
    }


    override fun postFileChunked(
        context: Context,
        clientHost: String,
        shareToken: String,
        fileId: String,
        chunkCount: Int,
        chunk: Int,
        contentUploadN: ContentUploadN
    ): DesignResponse<Boolean> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
            return DesignResponse(data = false)
        }
        PurpleLogger.current.d(
            TAG,
            "postFileChunked, clientHost:$clientHost, shareToken:$shareToken, fileUploadN:${contentUploadN}"
        )
        val handleResult =
            postFileHelper.handleChunkedFile(context, fileId, chunkCount, chunk, contentUploadN)
        return DesignResponse(data = handleResult)
    }

    override fun getContent(
        context: Context,
        clientHost: String,
        shareToken: String,
        contentId: String
    ): DesignResponse<ContentDownloadN> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
            return DesignResponse(data = null)
        }
        val dataContent = findContentForContentUri(shareMethod, contentId)
        if (dataContent == null) {
            return DesignResponse(data = null)
        }
        when (dataContent) {
            is DataContent.FileContent -> {
                val length = dataContent.file.length()
                val inputStream: InputStream = dataContent.file.inputStream()
                val readableData: ReadableData = DataContentReadableData(length, inputStream)
                val contentDownloadN =
                    ContentDownloadN(
                        dataContent.id,
                        dataContent.name,
                        readableData
                    )
                return DesignResponse(data = contentDownloadN)
            }

            is DataContent.UriContent -> {
                val inputStream: InputStream? =
                    context.applicationContext.contentResolver.openInputStream(dataContent.uri)
                if (inputStream == null) {
                    return DesignResponse(data = null)
                }

                val length = dataContent.androidUriFile?.size ?: 0
                val readableData: ReadableData = DataContentReadableData(length, inputStream)
                val contentDownloadN =
                    ContentDownloadN(
                        dataContent.id,
                        dataContent.name,
                        readableData
                    )
                return DesignResponse(data = contentDownloadN)

            }

            is DataContent.ByteArrayContent -> {
                val length = dataContent.bytes.size.toLong()
                val dataInputStream: InputStream = ByteArrayInputStream(dataContent.bytes)
                val readableData: ReadableData = DataContentReadableData(length, dataInputStream)
                val contentDownloadN =
                    ContentDownloadN(
                        dataContent.id,
                        dataContent.name,
                        readableData
                    )
                return DesignResponse(data = contentDownloadN)
            }

            else -> {
                return DesignResponse(data = null)
            }
        }
        return DesignResponse(data = null)
    }

    override fun getContentList(
        context: Context,
        clientHost: String,
        shareToken: String,
        uri: String
    ): DesignResponse<ContentInfoList> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
            return DesignResponse(data = null)
        }
        val dataContentList = findContentListForContentUri(shareMethod, uri)
        if (dataContentList == null) {
            return DesignResponse(data = null)
        }
        val contentInfoList = dataContentList.mapNotNull {
            when (it) {
                is DataContent.UriContent -> {
                    ContentInfo(
                        it.id,
                        it.androidUriFile?.displayName ?: "",
                        it.androidUriFile?.size ?: 0,
                        ContentInfo.TYPE_URI
                    )
                }

                is DataContent.FileContent -> {
                    ContentInfo(it.id, it.file.name, it.file.length(), ContentInfo.TYPE_FILE)

                }

                is DataContent.StringContent -> {
                    ContentInfo(it.id, it.name, it.content.length.toLong(), ContentInfo.TYPE_STRING)
                }

                is DataContent.ByteArrayContent -> {
                    ContentInfo(it.id, it.name, it.bytes.size.toLong(), ContentInfo.TYPE_BYTES)
                }

                else -> {
                    null
                }
            }
        }
        val contentInfoListWrapper =
            ContentInfoList(uri, dataContentList.size, contentInfoList).encode()
        return DesignResponse(data = contentInfoListWrapper)
    }

    private fun getShareMethod(context: Context): HttpShareMethod? {
        if (context !is AppSetsShareActivity) {
            return null
        }
        val shareMethod = context.getShareMethod()
        if (shareMethod !is HttpShareMethod) {
            return null
        }
        return shareMethod
    }

    private fun findContentForContentUri(
        shareMethod: HttpShareMethod,
        contentUri: String
    ): DataContent? {
        val dataContent = shareMethod.findContentForContentUri(contentUri)
        return dataContent
    }

    private fun findContentListForContentUri(
        shareMethod: HttpShareMethod,
        uri: String
    ): List<DataContent>? {
        val pendingSendFileList = shareMethod.getPendingSendContentList(uri)

        return pendingSendFileList
    }

    override fun exchangeDeviceInfo(
        context: Context,
        clientHost: String,
        device: HttpShareDevice
    ): DesignResponse<HttpShareDevice> {
        val shareMethod = getShareMethod(context)
        if (shareMethod == null) {
            return DesignResponse(data = null)
        }
        val currentDeviceInfo = shareMethod.exchangeDeviceInfo(device)
        return DesignResponse(data = currentDeviceInfo)
    }

}