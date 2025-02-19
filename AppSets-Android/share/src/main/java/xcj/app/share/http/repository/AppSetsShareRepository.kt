package xcj.app.share.http.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import xcj.app.share.base.DataContent
import xcj.app.share.base.DataProgressInfoPool
import xcj.app.share.base.DataSendContent
import xcj.app.share.base.ShareDevice
import xcj.app.share.http.HttpShareMethod
import xcj.app.share.http.api.AppSetsShareApi
import xcj.app.share.http.common.asProgressRequestBody
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.starter.server.RetrofitProvider
import xcj.app.starter.util.ContentType
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.random.Random

class AppSetsShareRepository() {

    companion object {
        private const val TAG = "AppSetsShareRepository"
    }

    private fun buildApi(shareDevice: ShareDevice): AppSetsShareApi? {
        PurpleLogger.current.d(TAG, "buildApi, shareDevice:$shareDevice")
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
        } else {
            val ip6 = shareDevice.deviceAddress.ip6
            if (!ip6.isNullOrEmpty()) {
                PurpleLogger.current.d(TAG, "buildApi, use device ip6:$ip6")
                baseUrlStringBuilder.append("[")
                baseUrlStringBuilder.append(ip6)
                baseUrlStringBuilder.append("]")
            }
        }
        baseUrlStringBuilder.append(":")
        baseUrlStringBuilder.append(HttpShareMethod.SHARE_SERVER_PORT)
        var baseUrl = baseUrlStringBuilder.toString()

        //test
        //baseUrl = "http://192.168.198.62:8090"

        PurpleLogger.current.d(TAG, "buildApi, baseUrl:$baseUrl")
        runCatching {
            val appSetsShareApi: AppSetsShareApi =
                RetrofitProvider.getService(baseUrl, AppSetsShareApi::class.java)
            return appSetsShareApi
        }.onFailure {
            PurpleLogger.current.d(TAG, "buildApi, baseUrl:$baseUrl, failed:${it.message}")
        }
        return null
    }

    suspend fun sendContentList(
        context: Context,
        shareMethod: HttpShareMethod,
        dataSendContentList: List<DataSendContent.HttpContent>
    ) {
        dataSendContentList.forEach { dataSendContent ->
            sendContent(context, shareMethod, dataSendContent)
        }
    }

    suspend fun sendContent(
        context: Context,
        shareMethod: HttpShareMethod,
        dataSendContent: DataSendContent.HttpContent
    ) {
        val dataContent = dataSendContent.content

        when (dataContent) {
            is DataContent.StringContent -> {
                PurpleLogger.current.d(TAG, "sendContent, StringContent")
                postText(context, shareMethod, dataSendContent.dstDevice, dataContent)
            }

            is DataContent.ByteArrayContent -> {
                PurpleLogger.current.d(TAG, "sendContent, ByteArrayContent")
                postByteArray(context, shareMethod, dataSendContent.dstDevice, dataContent)
            }

            is DataContent.FileContent -> {
                PurpleLogger.current.d(TAG, "sendContent, FileContent")
                postFile(context, shareMethod, dataSendContent.dstDevice, dataContent)
            }

            is DataContent.UriContent -> {
                PurpleLogger.current.d(TAG, "sendContent, UriContent")
                postUri(context, shareMethod, dataSendContent.dstDevice, dataContent)
            }

            else -> {
                PurpleLogger.current.d(TAG, "sendContent, not implementation!")
            }
        }
    }

    suspend fun isNeedPin(shareDevice: ShareDevice.HttpShareDevice) = withContext(Dispatchers.IO) {
        val api = buildApi(shareDevice)
        if (api == null) {
            return@withContext DesignResponse.NOT_FOUND
        }
        PurpleLogger.current.d(TAG, "isNeedPin, shareDevice:$shareDevice")
        return@withContext api.isNeedPin()
    }

    suspend fun pair(shareDevice: ShareDevice.HttpShareDevice) = withContext(Dispatchers.IO) {
        val api = buildApi(shareDevice)
        if (api == null) {
            return@withContext DesignResponse.NOT_FOUND
        }
        PurpleLogger.current.d(TAG, "pair, shareDevice:$shareDevice")
        val pinNumber = Random.nextInt(100, 10000)
        shareDevice.pin = pinNumber
        api.pair(pin = pinNumber)
    }

    suspend fun pairResponse(shareDevice: ShareDevice.HttpShareDevice, token: String) =
        withContext(Dispatchers.IO) {

            val api = buildApi(shareDevice)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(
                TAG,
                "postText, shareDevice:$shareDevice sendPairResponse:$token"
            )
            return@withContext api.pairResponse(token = token)
        }


    suspend fun postText(
        context: Context,
        shareMethod: HttpShareMethod,
        shareDevice: ShareDevice.HttpShareDevice,
        dataContent: DataContent.StringContent
    ) =
        withContext(Dispatchers.IO) {

            val api = buildApi(shareDevice)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(
                TAG,
                "postText, shareDevice:$shareDevice dataContent:$dataContent"
            )
            val dataProgressForStart = DataProgressInfoPool.makeStart(dataContent.id)
            shareMethod.dataSendProgressListener.onProgress(dataProgressForStart)
            api.postText(token = shareDevice.token ?: "", text = dataContent.content)
            val dataProgressForEnd = DataProgressInfoPool.makeStart(dataContent.id)
            shareMethod.dataSendProgressListener.onProgress(dataProgressForEnd)
        }

    suspend fun postByteArray(
        context: Context,
        shareMethod: HttpShareMethod,
        shareDevice: ShareDevice.HttpShareDevice,
        dataContent: DataContent.ByteArrayContent
    ) =
        withContext(Dispatchers.IO) {

            val api = buildApi(shareDevice)
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
            // 创建 MultipartBody.Part
            val multiPartBodyPart =
                MultipartBody.Part.createFormData(
                    "bytes",
                    UUID.randomUUID().toString(),
                    requestBody
                )
            val multiPartBodyPartDescription =
                "postByteArray description".toRequestBody(ContentType.TEXT_PLAIN.toMediaTypeOrNull())

            api.postFile(
                token = shareDevice.token ?: "",
                multiPartBodyPart = multiPartBodyPart,
                multiPartBodyPartDescription
            )
        }


    suspend fun postFile(
        context: Context,
        shareMethod: HttpShareMethod,
        shareDevice: ShareDevice.HttpShareDevice,
        dataContent: DataContent.FileContent
    ) =
        withContext(Dispatchers.IO) {

            val api = buildApi(shareDevice)
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
            // 创建 MultipartBody.Part
            val multiPartBodyPart =
                MultipartBody.Part.createFormData("file", dataContent.file.name, requestBody)
            val multiPartBodyPartDescription =
                "file description".toRequestBody(ContentType.TEXT_PLAIN.toMediaTypeOrNull())

            api.postFile(
                token = shareDevice.token ?: "",
                multiPartBodyPart = multiPartBodyPart,
                multiPartBodyPartDescription
            )
        }

    suspend fun postUri(
        context: Context,
        shareMethod: HttpShareMethod,
        shareDevice: ShareDevice.HttpShareDevice,
        dataContent: DataContent.UriContent
    ) =
        withContext(Dispatchers.IO) {

            val api = buildApi(shareDevice)
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
                MultipartBody.Part.createFormData("file", uriFileName, requestBody)
            val multiPartBodyPartDescription =
                "file description".toRequestBody(ContentType.TEXT_PLAIN.toMediaTypeOrNull())

            val designResponse =
                api.postFile(
                    token = shareDevice.token ?: "",
                    multiPartBodyPart = multiPartBodyPart,
                    description = multiPartBodyPartDescription
                )
            //requestBody.close()
            designResponse
        }

    suspend fun prepareSend(shareDevice: ShareDevice.HttpShareDevice) =
        withContext(Dispatchers.IO) {

            val api = buildApi(shareDevice)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(TAG, "sendPrepareSend, shareDevice:$shareDevice")
            api.prepareSend(token = shareDevice.token ?: "")
        }

    suspend fun prepareSendResponse(shareDevice: ShareDevice.HttpShareDevice, isAccept: Boolean) =
        withContext(Dispatchers.IO) {

            val api = buildApi(shareDevice)
            if (api == null) {
                return@withContext DesignResponse.NOT_FOUND
            }
            PurpleLogger.current.d(TAG, "prepareSendResponse, shareDevice:$shareDevice")
            api.prepareSendResponse(token = shareDevice.token ?: "", isAccept = isAccept)
        }

    suspend fun resumeHandleSend(
        shareMethod: HttpShareMethod,
        shareDevice: ShareDevice.HttpShareDevice,
        by: String
    ) {
        PurpleLogger.current.d(TAG, "resumeHandleSend, shareDevice:$shareDevice, by:$by")
        //todo why key is same, but no value get
        val sendContentRunnableInfoMap = shareMethod.sendContentRunnableInfoMap
        for ((mappedShareDevice, sendDataRunnableInfo) in sendContentRunnableInfoMap) {
            if (shareDevice == mappedShareDevice) {
                handleSendForDevice(shareMethod, mappedShareDevice, sendDataRunnableInfo)
                break
            }
        }
    }

    /**
     * 发送前检查
     * 1.对方是否需要配对，如需配对，则先发起配对请求，等待配对响应
     * 2.请求对方接受状态，等待接受响应
     * 3.发起业务传输
     */
    suspend fun handleSend(shareMethod: HttpShareMethod, sendDirect: Boolean = false) =
        coroutineScope {
            PurpleLogger.current.d(TAG, "handleSend")
            val sendContentRunnableInfoMap = shareMethod.sendContentRunnableInfoMap
            for ((mappedShareDevice, sendDataRunnableInfo) in sendContentRunnableInfoMap) {
                launch {
                    if (sendDirect) {
                        handleSendForDevice(
                            shareMethod,
                            mappedShareDevice,
                            sendDataRunnableInfo,
                            sendDirect
                        )
                    } else {
                        checkDevicePin(shareMethod, mappedShareDevice)
                    }
                }
            }
        }

    private suspend fun handleSendForDevice(
        shareMethod: HttpShareMethod,
        shareDevice: ShareDevice.HttpShareDevice,
        sendDataRunnableInfo: HttpShareMethod.SendDataRunnableInfo?,
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
            sendDataRunnableInfo.sendDataRunnable()
            sendDataRunnableInfo.isCalled = true
            shareMethod.removeSendDataRunnableForDevice(shareDevice)
            return
        }
        if (shareDevice.isNeedPin && !shareDevice.isPaired) {
            PurpleLogger.current.d(
                TAG,
                "handleSendForDevice, shareDevice not paired, do pair action"
            )
            pair(shareDevice)
            return
        }
        if (!sendDataRunnableInfo.isAccept) {
            PurpleLogger.current.d(
                TAG,
                "handleSendForDevice, shareDevice not accept, do prepareSend action"
            )
            prepareSend(shareDevice)
            return
        }

        PurpleLogger.current.d(TAG, "handleSendForDevice, run sendDataRunnable")

        sendDataRunnableInfo.sendDataRunnable()
        sendDataRunnableInfo.isCalled = true
        shareMethod.removeSendDataRunnableForDevice(shareDevice)
    }

    suspend fun checkDevicePin(
        shareMethod: HttpShareMethod,
        shareDevice: ShareDevice.HttpShareDevice
    ) {
        PurpleLogger.current.d(TAG, "checkDevicePin, shareDevice:$shareDevice")
        val needPinResponse = isNeedPin(shareDevice)
        if (needPinResponse == DesignResponse.NOT_FOUND) {
            return
        }
        val needPin = needPinResponse.data == true
        shareDevice.isNeedPin = needPin
        PurpleLogger.current.d(TAG, "checkDevicePin, shareDevice:$shareDevice")

        resumeHandleSend(shareMethod, shareDevice, "checkDevicePin")
    }
}