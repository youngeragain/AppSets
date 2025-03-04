package xcj.app.share.http.controller

import android.content.Context
import xcj.app.share.base.ShareDevice
import xcj.app.share.http.model.ContentInfoListWrapper
import xcj.app.share.http.service.AppSetsShareService
import xcj.app.share.http.service.AppSetsShareServiceImpl
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.web.webserver.base.ContentDownloadN
import xcj.app.web.webserver.base.FileUploadN
import xcj.app.web.webserver.interfaces.AndroidContext
import xcj.app.web.webserver.interfaces.Controller
import xcj.app.web.webserver.interfaces.HttpBody
import xcj.app.web.webserver.interfaces.HttpHeader
import xcj.app.web.webserver.interfaces.HttpMethod
import xcj.app.web.webserver.interfaces.RequestBody
import xcj.app.web.webserver.interfaces.RequestInfo
import xcj.app.web.webserver.interfaces.RequestMapping

@Controller
class AppSetsShareController {
    companion object {
        private const val TAG = "AppSetsShareController"
    }

    private val appSetsShareService: AppSetsShareService = AppSetsShareServiceImpl()

    @RequestMapping(path = "/appsets/share", [HttpMethod.GET])
    fun greeting(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
    ): DesignResponse<String> {
        PurpleLogger.current.d(
            TAG,
            "greeting, context:$context, clientHost:$clientHost"
        )
        return appSetsShareService.greeting(context, clientHost)
    }

    @RequestMapping(path = "/appsets/share/ping", [HttpMethod.GET])
    fun ping(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
    ): DesignResponse<String> {
        PurpleLogger.current.d(TAG, "ping, context:$context, clientHost:$clientHost")
        return appSetsShareService.ping(context, clientHost)
    }

    @RequestMapping(path = "/appsets/share/pin/isneed", [HttpMethod.GET])
    fun isNeedPin(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
    ): DesignResponse<Boolean> {
        PurpleLogger.current.d(
            TAG,
            "isNeedPin, context:$context, clientHost:$clientHost"
        )
        return appSetsShareService.isNeedPin(context, clientHost)
    }

    @RequestMapping(path = "/appsets/share/pair", [HttpMethod.POST])
    fun pair(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @HttpHeader("pin") pin: Int
    ): DesignResponse<Boolean> {
        PurpleLogger.current.d(
            TAG,
            "greeting, context:$context, clientHost:$clientHost"
        )
        return appSetsShareService.pair(context, clientHost, pin)
    }

    @RequestMapping(path = "/appsets/share/pair_response", [HttpMethod.POST])
    fun pairResponse(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @HttpHeader("share_token") shareToken: String
    ): DesignResponse<Boolean> {
        PurpleLogger.current.d(
            TAG,
            "pairResponse, context:$context, clientHost:$clientHost, shareToken:$shareToken"
        )
        return appSetsShareService.pairResponse(context, clientHost, shareToken)
    }

    @RequestMapping(path = "/appsets/share/prepare", [HttpMethod.POST])
    fun prepareSend(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @HttpHeader("share_token") shareToken: String,
        @HttpHeader("uri") uri: String
    ): DesignResponse<Boolean> {
        PurpleLogger.current.d(
            TAG,
            "prepareSend, context:$context, shareToken:$shareToken, clientHost:$clientHost"
        )
        return appSetsShareService.prepareSend(context, clientHost, shareToken, uri)
    }

    @RequestMapping(path = "/appsets/share/prepare_response", [HttpMethod.POST])
    fun prepareSendResponse(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @HttpHeader("share_token") shareToken: String,
        @HttpHeader("is_accept") isAccept: Boolean,
        @HttpHeader("prefer_download_self") preferDownloadSelf: Boolean,
    ): DesignResponse<Boolean> {
        PurpleLogger.current.d(
            TAG,
            "prepareSendResponse, context:$context, shareToken:$shareToken, clientHost:$clientHost, isAccept:$isAccept, preferDownloadSelf:$preferDownloadSelf"
        )
        return appSetsShareService.prepareSendResponse(
            context,
            clientHost,
            shareToken,
            isAccept,
            preferDownloadSelf
        )
    }

    @RequestMapping(path = "/appsets/share/text", [HttpMethod.POST])
    fun postText(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @HttpHeader("share_token") shareToken: String,
        @HttpBody(HttpBody.TYPE_RAW_TEXT) text: String
    ): DesignResponse<Boolean> {
        PurpleLogger.current.d(
            TAG,
            "postText, context:$context, text:$text, clientHost:$clientHost"
        )
        return appSetsShareService.postText(context, clientHost, shareToken, text)
    }

    @RequestMapping(path = "/appsets/share/file", [HttpMethod.POST])
    fun postFile(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @HttpHeader("share_token") shareToken: String,
        @HttpBody(HttpBody.TYPE_FILE) fileUploadN: FileUploadN
    ): DesignResponse<Boolean> {
        PurpleLogger.current.d(
            TAG,
            "postFile, context:$context, fileUploadN:$fileUploadN, clientHost:$clientHost"
        )
        return appSetsShareService.postFile(context, clientHost, shareToken, fileUploadN)
    }

    @RequestMapping(path = "/appsets/share/file/chunked", [HttpMethod.POST])
    fun postFileChunked(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @HttpHeader("share_token") shareToken: String,
        @HttpHeader("file_id") fileId: String,
        @HttpHeader("chunk_count") chunkCount: Int,
        @HttpHeader("chunk") chunk: Int,
        @HttpBody(HttpBody.TYPE_FILE) fileUploadN: FileUploadN
    ): DesignResponse<Boolean> {
        PurpleLogger.current.d(
            TAG,
            "postFile, context:$context, fileUploadN:$fileUploadN, clientHost:$clientHost"
        )
        return appSetsShareService.postFileChunked(
            context,
            clientHost,
            shareToken,
            fileId,
            chunkCount,
            chunk,
            fileUploadN
        )
    }

    @RequestMapping(path = "/appsets/share/content/get", [HttpMethod.GET])
    fun getContent(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @HttpHeader("share_token") shareToken: String,
        @HttpHeader("content_id") contentId: String
    ): DesignResponse<ContentDownloadN> {
        PurpleLogger.current.d(
            TAG,
            "getContent, context:$context, clientHost:$clientHost"
        )
        return appSetsShareService.getContent(context, clientHost, shareToken, contentId)
    }

    @RequestMapping(path = "/appsets/share/contents/get", [HttpMethod.POST])
    fun getContentList(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @HttpHeader("share_token") shareToken: String,
        @HttpHeader("uri") uri: String
    ): DesignResponse<ContentInfoListWrapper> {
        PurpleLogger.current.d(
            TAG,
            "getContentList, context:$context, clientHost:$clientHost"
        )
        return appSetsShareService.getContentList(context, clientHost, shareToken, uri)
    }


    @RequestMapping(path = "/appsets/share/device/info/exchange", [HttpMethod.POST])
    fun exchangeDeviceInfo(
        @AndroidContext(AndroidContext.TYPE_ACTIVITY) context: Context,
        @RequestInfo(what = RequestInfo.WHAT_REQUEST_REMOTE_HOST) clientHost: String,
        @RequestBody shareDevice: ShareDevice.HttpShareDevice
    ): DesignResponse<ShareDevice.HttpShareDevice> {
        PurpleLogger.current.d(
            TAG,
            "exchangeDeviceInfo, context:$context, clientHost:$clientHost"
        )
        return appSetsShareService.exchangeDeviceInfo(context, clientHost, shareDevice)
    }


}
