@file:OptIn(ExperimentalEncodingApi::class)

package xcj.app.share.http.repository

import android.content.Context
import android.os.Build
import androidx.lifecycle.lifecycleScope
import io.netty.handler.codec.http.HttpHeaderNames
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import xcj.app.share.base.DataContent
import xcj.app.share.http.HttpShareMethod
import xcj.app.share.http.api.ShareApi
import xcj.app.share.http.base.HttpContent
import xcj.app.share.http.base.HttpShareDevice
import xcj.app.share.http.common.ResponseProgressInterceptor
import xcj.app.share.http.common.asProgressRequestBody
import xcj.app.share.http.model.ContentInfo
import xcj.app.share.http.model.ContentInfoList
import xcj.app.share.util.ShareSystem
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.starter.server.RetrofitProvider
import xcj.app.starter.util.ContentType
import xcj.app.web.webserver.base.DataProgressInfoPool
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class ShareRepository() {

    fun interface BaseUrlProvider {
        fun provideUrl(): String?
    }

    class StringBaseUrlProvider(
        private val address: String,
        private val port: Int
    ) : BaseUrlProvider {
        override fun provideUrl(): String? {
            return "http://$address:$port"
        }
    }

    class ShareDeviceBaseUrlProvider(
        private val shareDevice: HttpShareDevice,
        private val port: Int
    ) : BaseUrlProvider {
        override fun provideUrl(): String? {
            if (shareDevice.deviceAddress.ips.isEmpty()) {
                PurpleLogger.current.d(TAG, "buildApi, device ips is empty, return")
                return null
            }
            val baseUrlStringBuilder = StringBuilder()
            baseUrlStringBuilder.append("http://")
            val ip4 = shareDevice.deviceAddress.ip4
            if (!ip4.isNullOrEmpty()) {
                PurpleLogger.current.d(TAG, "buildApi, use device ip4:$ip4")
                baseUrlStringBuilder.append(ip4)
                baseUrlStringBuilder.append(":")
                baseUrlStringBuilder.append(port)
                return baseUrlStringBuilder.toString()
            }
            val ip6 = shareDevice.deviceAddress.ip6
            if (!ip6.isNullOrEmpty()) {
                PurpleLogger.current.d(TAG, "buildApi, use device ip6:$ip6")
                baseUrlStringBuilder.append("[")
                baseUrlStringBuilder.append(ip6)
                baseUrlStringBuilder.append("]")
                return baseUrlStringBuilder.toString()
            }
            return null
        }
    }

    companion object Companion {
        private const val TAG = "AppSetsShareRepository"
    }

    private val saveFileExecutors = Executors.newScheduledThreadPool(2)

    private fun buildApi(
        baseUrlProvider: BaseUrlProvider,
        okHttpClient: OkHttpClient? = null
    ): ShareApi? {
        PurpleLogger.current.d(TAG, "buildApi, baseUrlProvider:$baseUrlProvider")
        val baseUrl = baseUrlProvider.provideUrl()
        if (baseUrl.isNullOrEmpty()) {
            return null
        }

        PurpleLogger.current.d(TAG, "buildApi, baseUrl:$baseUrl")
        runCatching {
            val shareApi: ShareApi =
                RetrofitProvider.getService(
                    baseUrl,
                    ShareApi::class.java,
                    okHttpClient,
                    okHttpClient != null
                )
            return shareApi
        }.onFailure {
            PurpleLogger.current.d(TAG, "buildApi, baseUrl:$baseUrl, failed:${it.message}")
        }
        return null
    }

    suspend fun sendContentList(
        context: Context,
        shareMethod: HttpShareMethod,
        dataSendContentList: List<HttpContent>
    ) = coroutineScope {
        dataSendContentList.forEach { dataSendContent ->
            launch {
                sendContent(context, shareMethod, dataSendContent)
            }
        }
    }

    suspend fun sendContent(
        context: Context,
        shareMethod: HttpShareMethod,
        dataSendContent: HttpContent
    ) {
        val dataContent = dataSendContent.content

        when (dataContent) {
            is DataContent.FileContent -> {
                PurpleLogger.current.d(TAG, "sendContent, FileContent")
                postFile(context, shareMethod, dataSendContent.dstDevice, dataContent)
            }

            is DataContent.UriContent -> {
                PurpleLogger.current.d(TAG, "sendContent, UriContent")
                postUri(context, shareMethod, dataSendContent.dstDevice, dataContent)
            }

            is DataContent.ByteArrayContent -> {
                PurpleLogger.current.d(TAG, "sendContent, ByteArrayContent")
                postByteArray(context, shareMethod, dataSendContent.dstDevice, dataContent)
            }

            is DataContent.StringContent -> {
                PurpleLogger.current.d(TAG, "sendContent, StringContent")
                postText(context, shareMethod, dataSendContent.dstDevice, dataContent)
            }
        }
    }

    suspend fun isNeedPin(shareDevice: HttpShareDevice) = withContext(Dispatchers.IO) {
        val urlProvider =
            ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_API_PORT)
        val api = buildApi(urlProvider)
        if (api == null) {
            return@withContext DesignResponse.NOT_FOUND
        }
        PurpleLogger.current.d(TAG, "isNeedPin, shareDevice:$shareDevice")
        return@withContext api.isNeedPin()
    }

    suspend fun pair(shareDevice: HttpShareDevice, pin: Int) =
        withContext(Dispatchers.IO) {
            val urlProvider =
                ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_API_PORT)
            val api = buildApi(urlProvider)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(TAG, "pair, shareDevice:$shareDevice")

            api.pair(pin = pin)

            shareDevice.pin = pin
        }

    suspend fun pairResponse(shareDevice: HttpShareDevice, shareToken: String) =
        withContext(Dispatchers.IO) {
            val urlProvider =
                ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_API_PORT)
            val api = buildApi(urlProvider)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(
                TAG,
                "pairResponse, shareDevice:$shareDevice shareToken:$shareToken"
            )
            return@withContext api.pairResponse(shareToken = shareToken)
        }


    suspend fun postText(
        context: Context,
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        dataContent: DataContent.StringContent
    ) =
        withContext(Dispatchers.IO) {
            val urlProvider =
                ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_API_PORT)
            val api = buildApi(urlProvider)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(
                TAG,
                "postText, shareDevice:$shareDevice dataContent:$dataContent"
            )
            val dataProgressInfoForStart = DataProgressInfoPool.makeStart(dataContent.id)
            shareMethod.dataSendProgressListener.onProgress(dataProgressInfoForStart)
            api.postText(shareToken = shareDevice.token ?: "", text = dataContent.content)
            val dataProgressInfoForEnd = DataProgressInfoPool.makeEnd(dataContent.id)
            shareMethod.dataSendProgressListener.onProgress(dataProgressInfoForEnd)
        }

    suspend fun postByteArray(
        context: Context,
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        dataContent: DataContent.ByteArrayContent
    ) =
        withContext(Dispatchers.IO) {
            val urlProvider =
                ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_FILE_API_PORT)
            val api = buildApi(urlProvider)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(
                TAG,
                "postByteArray, shareDevice:$shareDevice dataContent:${dataContent}"
            )
            val requestBody =
                dataContent.asProgressRequestBody(
                    context,
                    ContentType.MULTIPART_FORM_DATA.toMediaTypeOrNull(),
                    shareMethod.dataSendProgressListener
                )
            if (requestBody == null) {
                PurpleLogger.current.d(TAG, "postByteArray, requestBody is null")
                return@withContext DesignResponse.BAD_REQUEST
            }

            val multiPartBodyPart =
                MultipartBody.Part.createFormData(
                    "bytes",
                    UUID.randomUUID().toString(),
                    requestBody
                )
            val multiPartBodyPartDescription =
                "bytes description".toRequestBody(ContentType.TEXT_PLAIN.toMediaTypeOrNull())

            api.postFile(
                shareToken = shareDevice.token ?: "",
                multiPartBodyPart = multiPartBodyPart,
                multiPartBodyPartDescription
            )
        }


    suspend fun postFile(
        context: Context,
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        dataContent: DataContent.FileContent
    ) =
        withContext(Dispatchers.IO) {
            val urlProvider =
                ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_FILE_API_PORT)
            val api = buildApi(urlProvider)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(
                TAG,
                "postFile, shareDevice:$shareDevice dataContent:${dataContent}"
            )
            val requestBody =
                dataContent.asProgressRequestBody(
                    context,
                    ContentType.MULTIPART_FORM_DATA.toMediaTypeOrNull(),
                    shareMethod.dataSendProgressListener
                )
            if (requestBody == null) {
                PurpleLogger.current.d(TAG, "postFile, requestBody is null")
                return@withContext DesignResponse.BAD_REQUEST
            }

            val multiPartBodyPart =
                MultipartBody.Part.createFormData(
                    dataContent.file.name,
                    dataContent.file.name,
                    requestBody
                )
            val multiPartBodyPartDescription =
                "file description".toRequestBody(ContentType.TEXT_PLAIN.toMediaTypeOrNull())

            api.postFile(
                shareToken = shareDevice.token ?: "",
                multiPartBodyPart = multiPartBodyPart,
                multiPartBodyPartDescription
            )
        }

    suspend fun postUri(
        context: Context,
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        dataContent: DataContent.UriContent
    ) =
        withContext(Dispatchers.IO) {
            val urlProvider =
                ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_FILE_API_PORT)
            val api = buildApi(urlProvider)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }

            val requestBody = dataContent.asProgressRequestBody(
                context,
                ContentType.MULTIPART_FORM_DATA.toMediaTypeOrNull(),
                shareMethod.dataSendProgressListener
            )
            if (requestBody == null) {
                PurpleLogger.current.d(TAG, "postUri, requestBody is null")
                return@withContext DesignResponse.BAD_REQUEST
            }

            val uriFileName =
                dataContent.androidUriFile?.displayName ?: UUID.randomUUID().toString()

            PurpleLogger.current.d(
                TAG,
                "postUri, shareDevice:$shareDevice uri:${dataContent.uri}, uriFileName:$uriFileName"
            )
            val multiPartBodyPart =
                MultipartBody.Part.createFormData(uriFileName, uriFileName, requestBody)
            val multiPartBodyPartDescription =
                "file description".toRequestBody(ContentType.TEXT_PLAIN.toMediaTypeOrNull())

            val designResponse =
                api.postFile(
                    shareToken = shareDevice.token ?: "",
                    multiPartBodyPart = multiPartBodyPart,
                    description = multiPartBodyPartDescription
                )
            designResponse
        }

    suspend fun prepareSend(shareDevice: HttpShareDevice, uri: String) =
        withContext(Dispatchers.IO) {
            val urlProvider =
                ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_API_PORT)
            val api = buildApi(urlProvider)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(TAG, "sendPrepareSend, shareDevice:$shareDevice")
            api.prepareSend(shareToken = shareDevice.token ?: "", uri = uri)
        }

    suspend fun prepareSendResponse(
        shareDevice: HttpShareDevice,
        isAccept: Boolean,
        isPreferDownloadSelf: Boolean
    ) =
        withContext(Dispatchers.IO) {
            val urlProvider =
                ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_API_PORT)
            val api = buildApi(urlProvider)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(
                TAG,
                "prepareSendResponse, shareDevice:$shareDevice, isAccept:$isAccept, isPreferDownloadSelf:$isPreferDownloadSelf"
            )
            api.prepareSendResponse(
                shareToken = shareDevice.token ?: "",
                isAccept = isAccept,
                isPreferDownloadSelf = isPreferDownloadSelf
            )
        }

    suspend fun exchangeDeviceInfo(
        shareMethod: HttpShareMethod,
        address: String,
    ) = withContext(
        Dispatchers.IO
    ) {
        val currentShareDevice = shareMethod.getCurrentShareDevice()
        if (currentShareDevice == null) {
            return@withContext
        }
        val urlProvider =
            StringBaseUrlProvider(address, HttpShareMethod.SHARE_SERVER_API_PORT)
        val api = buildApi(urlProvider)
        if (api == null) {
            return@withContext
        }

        PurpleLogger.current.d(
            TAG,
            "exchangeDeviceInfo, address:$address, currentShareDevice:$currentShareDevice"
        )
        val designResponse = api.exchangeDeviceInfo(currentShareDevice)
        val httpShareDevice = designResponse.data
        if (httpShareDevice != null) {
            shareMethod.exchangeDeviceInfo(httpShareDevice)
        }
    }

    /**
     * 发送前检查
     * 1.对方是否需要配对，如需配对，则先发起配对请求，等待配对响应
     * 2.请求对方接受状态，等待接受响应
     * 3.发起业务传输
     */
    suspend fun handleSend(
        shareMethod: HttpShareMethod,
        uri: String,
        sendDirect: Boolean = false
    ) = coroutineScope {
        PurpleLogger.current.d(TAG, "handleSend")
        val sendContentRunnableInfoMap = shareMethod.sendContentRunnableInfoMap
        for ((mappedShareDevice, sendDataRunnableInfo) in sendContentRunnableInfoMap) {
            launch {
                resumeHandleSend(
                    shareMethod,
                    mappedShareDevice,
                    uri,
                    "handleSend",
                    sendDirect,
                    sendDataRunnableInfo
                )
            }
        }
    }

    suspend fun resumeHandleSend(
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        uri: String,
        by: String,
        sendDirect: Boolean = false,
        sendDataRunnableInfo: HttpShareMethod.SendDataRunnableInfo? = null,
    ) {
        PurpleLogger.current.d(TAG, "resumeHandleSend, shareDevice:$shareDevice, by:$by")

        if (sendDataRunnableInfo != null) {
            handleSendForDevice(
                shareMethod,
                shareDevice,
                sendDataRunnableInfo,
                uri,
                sendDirect
            )
            return
        }

        //todo why key is same, but no value get
        val sendContentRunnableInfoMap = shareMethod.sendContentRunnableInfoMap
        for ((mappedShareDevice, sendDataRunnableInfo) in sendContentRunnableInfoMap) {
            if (shareDevice != mappedShareDevice) {
                continue
            }
            handleSendForDevice(
                shareMethod,
                mappedShareDevice,
                sendDataRunnableInfo,
                uri,
                sendDirect
            )
        }
    }

    private suspend fun handleSendForDevice(
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        sendDataRunnableInfo: HttpShareMethod.SendDataRunnableInfo?,
        uri: String,
        sendDirect: Boolean = false
    ) {
        PurpleLogger.current.d(TAG, "handleSendForDevice, shareDevice:$shareDevice")
        if (sendDataRunnableInfo == null) {
            PurpleLogger.current.d(TAG, "handleSendForDevice, sendDataRunnableInfo is null, return")
            return
        }
        if (sendDataRunnableInfo.isCalled) {
            PurpleLogger.current.d(
                TAG,
                "handleSendForDevice, sendDataRunnableInfo is called, return"
            )
            return
        }

        if (sendDirect) {
            PurpleLogger.current.d(
                TAG,
                "handleSendForDevice, sendDirect is true, run sendDataRunnable"
            )
            sendDataRunnableInfo.run()
            shareMethod.removeSendDataRunnableForDevice(shareDevice, "sendDataRunnable Called")
            return
        }
        PurpleLogger.current.d(TAG, "checkDevicePin, shareDevice:$shareDevice")
        if (!shareDevice.isPaired) {
            val needPinResponse = isNeedPin(shareDevice)
            if (needPinResponse == DesignResponse.NOT_FOUND) {
                return
            }
            val needPin = needPinResponse.data == true
            shareDevice.isNeedPin = needPin
        }

        if (shareDevice.isNeedPin && !shareDevice.isPaired) {
            PurpleLogger.current.d(
                TAG,
                "handleSendForDevice, shareDevice not paired, do pair action"
            )
            val pinNumber = Random.nextInt(100, 10000)
            pair(shareDevice, pinNumber)
            return
        }

        if (!sendDataRunnableInfo.isAccept) {
            PurpleLogger.current.d(
                TAG,
                "handleSendForDevice, shareDevice not accept, do prepareSend action"
            )
            prepareSend(shareDevice, uri = uri)
            return
        }

        PurpleLogger.current.d(TAG, "handleSendForDevice, run sendDataRunnable")

        sendDataRunnableInfo.run()
        shareMethod.removeSendDataRunnableForDevice(shareDevice, "sendDataRunnable Called")
    }

    suspend fun checkDevicePin(
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        uri: String
    ) {
        PurpleLogger.current.d(TAG, "checkDevicePin, shareDevice:$shareDevice")
        val needPinResponse = isNeedPin(shareDevice)
        if (needPinResponse == DesignResponse.NOT_FOUND) {
            return
        }
        val needPin = needPinResponse.data == true
        shareDevice.isNeedPin = needPin
        PurpleLogger.current.d(TAG, "checkDevicePin, shareDevice:$shareDevice")

        resumeHandleSend(shareMethod, shareDevice, uri, "checkDevicePin")
    }

    suspend fun handleDownload(
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        uri: String
    ) = coroutineScope {
        PurpleLogger.current.d(TAG, "handleDownload")
        val designResponse = getContentList(shareDevice, uri = uri)
        if (designResponse == DesignResponse.NOT_FOUND) {
            return@coroutineScope
        }
        val encodeContentInfoList = designResponse.data?.infoList//contentInfo's name is encoded
        if (encodeContentInfoList.isNullOrEmpty()) {
            return@coroutineScope
        }

        val clientInfo = shareDevice.toClientInfo(HttpShareMethod.SHARE_SERVER_API_PORT)


        encodeContentInfoList.forEach { encodeContentInfo ->
            val decodeContentInfo = encodeContentInfo.decode()
            when (encodeContentInfo.type) {
                ContentInfo.TYPE_FILE, ContentInfo.TYPE_URI -> {
                    val contentFile =
                        ShareSystem.makeFileIfNeeded(decodeContentInfo.name, createFile = false)
                    if (contentFile != null) {
                        launch {
                            val fileContent =
                                DataContent.FileContent(contentFile, clientInfo = clientInfo)
                            getContent(
                                shareMethod,
                                shareDevice,
                                decodeContentInfo,
                                fileContent

                            )
                        }
                    }
                }

                ContentInfo.TYPE_STRING -> {
                    launch {
                        val stringContent = DataContent.StringContent(
                            encodeContentInfo.name,
                            clientInfo = clientInfo
                        )
                        getContent(
                            shareMethod,
                            shareDevice,
                            encodeContentInfo,
                            stringContent
                        )
                    }
                }
            }
        }
    }

    suspend fun getContentList(
        shareDevice: HttpShareDevice,
        uri: String
    ): DesignResponse<ContentInfoList> =
        withContext(Dispatchers.IO) {
            val urlProvider =
                ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_API_PORT)
            val api = buildApi(urlProvider)
            if (api == null) {
                return@withContext DesignResponse(data = null)
            }
            PurpleLogger.current.d(
                TAG,
                "getContentList, shareDevice:$shareDevice uri:$uri"
            )
            val designResponse = api.getContentList(
                shareToken = shareDevice.token ?: "",
                uri = uri,
            )
            return@withContext designResponse
        }

    suspend fun getContent(
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        contentInfo: ContentInfo,
        tempDataContent: DataContent
    ) = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(
            TAG,
            "getContent, shareDevice:$shareDevice contentInfo:$contentInfo"
        )

        if (contentInfo.type == ContentInfo.TYPE_STRING) {
            val dataProgressInfoForStart = DataProgressInfoPool.makeStart(contentInfo.id)
            shareMethod.dataReceivedProgressListener.onProgress(dataProgressInfoForStart)
            val dataProgressInfoForEnd = DataProgressInfoPool.makeEnd(contentInfo.id)
            shareMethod.dataReceivedProgressListener.onProgress(dataProgressInfoForEnd)
            shareMethod.onContentReceived(tempDataContent)
            return@withContext
        }

        val responseProgressInterceptor = ResponseProgressInterceptor(
            tempDataContent,
            shareMethod.dataReceivedProgressListener
        )
        val defaultOkHttpClientBuilder = RetrofitProvider.defaultOkHttpClientBuilder()
        defaultOkHttpClientBuilder.apply {
            callTimeout(5, TimeUnit.SECONDS)
            webSocketCloseTimeout(1, TimeUnit.DAYS)
            connectTimeout(1, TimeUnit.DAYS)
            readTimeout(1, TimeUnit.DAYS)
            writeTimeout(1, TimeUnit.DAYS)
        }
        defaultOkHttpClientBuilder.addInterceptor(responseProgressInterceptor)
        val okHttpClient = defaultOkHttpClientBuilder.build()
        val urlProvider =
            ShareDeviceBaseUrlProvider(shareDevice, HttpShareMethod.SHARE_SERVER_FILE_API_PORT)
        val api = buildApi(urlProvider, okHttpClient)
        if (api == null) {
            return@withContext
        }

        val call = api.getContent(shareToken = shareDevice.token ?: "", contentId = contentInfo.id)
        call.enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody>
            ) {
                if (!response.isSuccessful) {
                    PurpleLogger.current.d(
                        TAG, "getContent, onResponse failed: " + response.code()
                    )
                    return
                }
                val request = call.request()
                saveContentResponseToFile(
                    shareMethod,
                    shareDevice,
                    tempDataContent,
                    request,
                    response
                )
            }

            override fun onFailure(call: Call<ResponseBody?>, tr: Throwable) {
                PurpleLogger.current.d(
                    TAG, "getContent, onFailure: " + tr.message
                )
            }
        })
    }

    private fun saveContentResponseToFile(
        shareMethod: HttpShareMethod,
        shareDevice: HttpShareDevice,
        tempDataContent: DataContent,
        request: Request,
        response: Response<ResponseBody>
    ) {
        PurpleLogger.current.d(TAG, "saveContentResponseToFile, tempDataContent:$tempDataContent")
        val contentId = request.headers["content_id"]
        if (contentId.isNullOrEmpty()) {
            return
        }
        if (tempDataContent !is DataContent.FileContent) {
            return
        }
        val responseBody = response.body()
        if (responseBody == null) {
            return
        }
        val contentDisposition =
            response.headers()[HttpHeaderNames.CONTENT_DISPOSITION.toString()]
        if (contentDisposition?.startsWith("attachment;") != true) {
            return
        }
        if (!contentDisposition.contains("filename=")) {
            return
        }
        val attachmentFileName =
            contentDisposition.substringAfter("filename=").trimStart('"').trimEnd('"')
        PurpleLogger.current.d(
            TAG,
            "saveContentResponseToFile, attachmentFileName:$attachmentFileName, " +
                    "dataContent.file.name:${tempDataContent.file.name}"
        )
        shareMethod.activity.lifecycleScope.launch {
            saveFileWithCoroutine(
                responseBody,
                tempDataContent.file
            ).onSuccess {
                PurpleLogger.current.d(
                    TAG, "saveContentResponseToFile, onSuccess"
                )
                shareMethod.onContentReceived(tempDataContent)
            }.onFailure { tr ->
                tr.printStackTrace()
                PurpleLogger.current.d(
                    TAG, "saveContentResponseToFile, onFailure: " + tr.message
                )
            }
        }
    }

    private fun saveFileWithExecutors(
        body: ResponseBody,
        file: File,
        onResult: (Result<Any>) -> Unit
    ) {
        saveFileExecutors.submit {
            val result = saveFile(body, file)
            onResult(result)
        }
    }

    private suspend fun saveFileWithCoroutine(
        body: ResponseBody,
        file: File
    ) = withContext(Dispatchers.IO) {
        return@withContext saveFile(body, file)
    }

    private fun saveFile0WithExecutors(
        body: ResponseBody,
        file: File,
        onResult: (Result<Any>) -> Unit
    ) {
        saveFileExecutors.submit {
            val result = saveFile0(body, file)
            onResult(result)
        }
    }

    private suspend fun saveFile0WithCoroutine(
        body: ResponseBody,
        file: File,
        onResult: (Result<Any>) -> Unit
    ) = withContext(Dispatchers.IO) {
        return@withContext saveFile0(body, file)
    }

    private fun saveFile(
        body: ResponseBody,
        file: File,
    ): Result<Any> {
        return runCatching {
            val inputStream = body.byteStream()
            inputStream.use {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Files.copy(it, file.toPath())
                } else {

                }
            }
        }
    }

    private fun saveFile0(body: ResponseBody, file: File): Result<Any> {
        return runCatching {
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(8 * 1024)
            var bytesRead = 0
            while (true) {
                bytesRead = inputStream.read(buffer)
                if (bytesRead == -1) {
                    break
                }
                outputStream.write(buffer, 0, bytesRead)
            }
            inputStream.close()
            outputStream.close()
        }
    }
}