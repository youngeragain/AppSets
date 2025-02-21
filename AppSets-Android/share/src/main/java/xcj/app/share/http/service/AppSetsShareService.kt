package xcj.app.share.http.service

import android.content.Context
import xcj.app.share.http.model.ContentListInfo
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.web.webserver.base.ContentDownloadN
import xcj.app.web.webserver.base.FileUploadN

interface AppSetsShareService {
    fun greeting(context: Context, clientHost: String): DesignResponse<String>

    fun ping(context: Context, clientHost: String): DesignResponse<String>

    fun isNeedPin(context: Context, clientHost: String): DesignResponse<Boolean>

    fun pair(
        context: Context,
        clientHost: String,
        pin: Int
    ): DesignResponse<Boolean>

    fun pairResponse(
        context: Context,
        clientHost: String,
        shareToken: String
    ): DesignResponse<Boolean>

    fun prepareSend(
        context: Context,
        clientHost: String,
        token: String,
        contentListUri: String
    ): DesignResponse<Boolean>

    fun prepareSendResponse(
        context: Context,
        clientHost: String,
        token: String,
        isAccept: Boolean,
        preferDownloadSelf: Boolean,
    ): DesignResponse<Boolean>

    fun postText(
        context: Context,
        clientHost: String,
        token: String,
        text: String
    ): DesignResponse<Boolean>

    fun postFile(
        context: Context,
        clientHost: String,
        token: String,
        fileUploadN: FileUploadN
    ): DesignResponse<Boolean>

    fun postFileChunked(
        context: Context,
        clientHost: String,
        token: String,
        fileId: String,
        chunkCount: Int,
        chunk: Int,
        fileUploadN: FileUploadN
    ): DesignResponse<Boolean>

    fun getContent(
        context: Context,
        clientHost: String,
        token: String,
        contentUri: String
    ): DesignResponse<ContentDownloadN>

    fun getContentList(
        context: Context,
        clientHost: String,
        token: String,
        contentListUri: String
    ): DesignResponse<ContentListInfo>
}