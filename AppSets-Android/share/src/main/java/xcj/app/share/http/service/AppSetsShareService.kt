package xcj.app.share.http.service

import android.content.Context
import xcj.app.starter.foundation.http.DesignResponse
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
        token: String
    ): DesignResponse<Boolean>

    fun prepareSend(
        context: Context,
        clientHost: String,
        token: String
    ): DesignResponse<Boolean>

    fun prepareSendResponse(
        context: Context,
        clientHost: String,
        token: String,
        isAccept: Boolean
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
}