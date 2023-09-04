package xcj.app.io.components

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Files.FileColumns
import android.util.Log
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.CosXmlSimpleService
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.model.PresignedUrlRequest
import com.tencent.cos.xml.transfer.TransferConfig
import com.tencent.cos.xml.transfer.TransferManager
import com.tencent.qcloud.core.auth.QCloudCredentialProvider
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.runBlocking
import xcj.app.core.android.ApplicationHelper
import java.io.File

class SimpleFileIO private constructor() : FileIO {
    private val TAG = "SimpleFileIO"
    // 初始化 COS Service，获取实例
    private var cosXmlService: CosXmlSimpleService? = null
    private var transferManager: TransferManager? = null

    private lateinit var tencentCosInfoProvider: TencentCosInfoProvider
    fun isThirdComponentsInit(): Boolean {
        return cosXmlService != null
    }

    fun initThirdComponents(context: Context, tencentCosInfoProvider: TencentCosInfoProvider) {
        if (cosXmlService != null)
            return
        this.tencentCosInfoProvider = tencentCosInfoProvider
        // 存储桶所在地域简称，例如广州地区是 ap-guangzhou
        val region = tencentCosInfoProvider.getTencentCosRegionBucket().region//BuildConfig.Region
        //val secretId = BuildConfig.SecrectId
        //val secretKey = BuildConfig.SecrectKey
        //ShortTimeCredentialProvider(secretId, secretKey, 300)
        val myCredentialProvider: QCloudCredentialProvider =
            STSCredentialProvider(tencentCosInfoProvider)

        // 创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
        val serviceConfig = CosXmlServiceConfig.Builder()
            .setRegion(region)
            .isHttps(true) // 使用 HTTPS 请求, 默认为 HTTP 请求
            .builder()
        cosXmlService = CosXmlSimpleService(context, serviceConfig, myCredentialProvider)
        val transferConfig = TransferConfig.Builder().build()
        transferManager = TransferManager(cosXmlService, transferConfig)
    }

    fun destroy() {
        transferManager = null
        cosXmlService = null
    }

    private fun getUpdateResultListener(): CosXmlResultListener {
        return object : CosXmlResultListener {
            override fun onSuccess(request: CosXmlRequest, result: CosXmlResult) {
                Log.i(TAG, "uploadReturnUrlMarker, onSuccess")
            }

            // 如果您使用 kotlin 语言来调用，请注意回调方法中的异常是可空的，否则不会回调 onFail 方法，即：
            // clientException 的类型为 CosXmlClientException?，serviceException 的类型为 CosXmlServiceException?
            override fun onFail(
                request: CosXmlRequest,
                clientException: CosXmlClientException?,
                serviceException: CosXmlServiceException?
            ) {
                if (clientException != null) {
                    clientException.printStackTrace()
                } else {
                    serviceException!!.printStackTrace()
                }
                Log.e(TAG, "uploadReturnUrlMarker, onFail")
            }
        }
    }

    override fun uploadWithFile(
        context: Context,
        file: File,
        urlMarker: String,
        resultListener: Any?
    ) {
        uploadReturnUrlMarker(context, file, urlMarker, resultListener ?: getUpdateResultListener())
    }

    /**
     * 图片上传时启用压缩
     */
    private fun File.isImage(): Boolean {
        return when (extension.lowercase()) {
            "png", "webp", "jpeg", "bmp", "svg", "jpg" -> true
            else -> false
        }
    }

    private fun uploadReturnUrlMarker(
        context: Context,
        file: File,
        urlMarker: String,
        resultListener: Any? = null
    ) {
        if (!file.isImage()) {
            finalUpload(file, urlMarker, resultListener)
            return
        }
        runBlocking {
            val tempFilesCacheDir = ApplicationHelper.getContextFileDir().tempFilesCacheDir
            val compressedFile = Compressor.compress(context, file) {
                default()
                destination(File(tempFilesCacheDir + File.separator + urlMarker + "." + file.extension.lowercase()))
            }
            Log.i(TAG, "compressed image file:${compressedFile}")
            finalUpload(compressedFile, urlMarker, resultListener)
        }

    }

    private fun finalUpload(file: File, urlMarker: String, resultListener: Any?) {
        if (!::tencentCosInfoProvider.isInitialized) {
            throw Exception("tencentCosInfoProvider is not init when uploadReturnUrlMarker!!!")
        }
        if (transferManager == null)
            throw Exception("transferManager is null when uploadReturnUrlMarker!!!")
        val tencentCosRegionBucket = tencentCosInfoProvider.getTencentCosRegionBucket()
        val bucket = tencentCosRegionBucket.bucketName//BuildConfig.BucketName
        //val contentUrlMarker = UUID.randomUUID().toString() + file.extension
        val cosPath = tencentCosRegionBucket.filePathPrefix + urlMarker //对象在存储桶中的位置标识符，即称对象键

        val srcPath = file.toString()

        //若存在初始化分块上传的 UploadId，则赋值对应的 uploadId 值用于续传；否则，赋值 null
        val uploadId: String? = null
        val cosxmlUploadTask = transferManager!!.upload(
            bucket, cosPath,
            srcPath, uploadId
        )
        if (resultListener != null && resultListener is CosXmlResultListener)
            cosxmlUploadTask.setCosXmlResultListener(resultListener)
        else {
            cosxmlUploadTask.setCosXmlResultListener(getUpdateResultListener())
        }
    }


    override fun uploadWithUri(
        context: Context,
        uri: Uri,
        urlMarker: String,
        resultListener: Any?
    ) {
        if (uri.scheme == "file") {
            val file = File(uri.path!!)
            Log.i(TAG, "uploadWithUri uri content file path:${file}")
            uploadReturnUrlMarker(context, file, urlMarker, resultListener)
            return
        }
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.contentResolver.query(uri, arrayOf(FileColumns.DATA), null, null)
        } else {
            context.contentResolver.query(uri, arrayOf(FileColumns.DATA), null, null, null, null)
        }
        cursor?.use {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(FileColumns.DATA)
                cursor.getString(columnIndex)
            } else
                null
        }?.let { filePath ->
            Log.i(TAG, "uploadWithUri uri content file path:${filePath}")
            uploadReturnUrlMarker(context, File(filePath), urlMarker, resultListener)
        }
    }

    override fun uploadWithMultiFile(
        context: Context,
        files: List<File>,
        urlMarkers: List<String>,
        resultListener: Any?
    ) {

    }

    override fun uploadWithMultiUri(
        context: Context,
        uris: List<Uri>,
        urlMarkers: List<String>,
        resultListener: Any?
    ) {
        val tempResultListener = resultListener ?: getUpdateResultListener()
        uris.forEachIndexed { index, uri ->
            uploadWithUri(context, uri, urlMarkers[index], tempResultListener)
        }
    }

    fun generatePreSign(contentUrlMarker: String): String? {
        if (!::tencentCosInfoProvider.isInitialized) {
            Log.i(TAG, "tencentCosInfoProvider is not init when generatePreSign")
            return null
        }
        if (cosXmlService == null) {
            Log.i(TAG, "cosXmlService is null when generatePreSign")
            return null
        }
        try {
            val tencentCosRegionBucket = tencentCosInfoProvider.getTencentCosRegionBucket()
            //存储桶名称
            val bucket = tencentCosRegionBucket.bucketName
            // 对象在存储桶中的位置标识符，对象键(Key)是对象在存储桶中的唯一标识。详情请参见 [对象键]
            // https://cloud.tencent.com/document/product/436/13324#.E5.AF.B9.E8.B1.A1.E9.94.AE)
            // 注意：用户无需对 cosPath 进行编码操作

            val cosPath = tencentCosRegionBucket.filePathPrefix + contentUrlMarker
            //请求 HTTP 方法.
            val method = "GET"
            val presignedUrlRequest = PresignedUrlRequest(
                bucket, cosPath
            )
            presignedUrlRequest.setRequestMethod(method)

            // 设置签名有效期为 60s，注意这里是签名有效期，您需要自行保证密钥有效期
            presignedUrlRequest.setSignKeyTime(60 * 10)
            // 设置不签名 Host
            presignedUrlRequest.addNoSignHeader("Host")
            val presignedURL = cosXmlService!!.getPresignedURL(presignedUrlRequest)
            Log.i(TAG, "generatePreSign presignedURL:${presignedURL}")
            return presignedURL
        } catch (e: CosXmlClientException) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        private var INSTANCE: SimpleFileIO? = null
        fun getInstance(): SimpleFileIO {
            return INSTANCE ?: synchronized(SimpleFileIO::class) {
                if (INSTANCE == null) {
                    val io = SimpleFileIO()
                    INSTANCE = io
                    io
                } else
                    INSTANCE!!
            }
        }
    }
}