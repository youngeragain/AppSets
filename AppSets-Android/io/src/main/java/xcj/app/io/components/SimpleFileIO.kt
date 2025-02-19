package xcj.app.io.components

import android.content.Context
import android.net.Uri
import com.tencent.cos.xml.CosXmlServiceConfig
import com.tencent.cos.xml.CosXmlSimpleService
import com.tencent.cos.xml.exception.CosXmlClientException
import com.tencent.cos.xml.exception.CosXmlServiceException
import com.tencent.cos.xml.listener.CosXmlProgressListener
import com.tencent.cos.xml.listener.CosXmlResultListener
import com.tencent.cos.xml.model.CosXmlRequest
import com.tencent.cos.xml.model.CosXmlResult
import com.tencent.cos.xml.model.PresignedUrlRequest
import com.tencent.cos.xml.model.`object`.GetObjectRequest
import com.tencent.cos.xml.transfer.TransferConfig
import com.tencent.cos.xml.transfer.TransferManager
import com.tencent.qcloud.core.auth.QCloudCredentialProvider
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.io.tencent.STSCredentialProvider
import xcj.app.io.tencent.TencentCosInfoProvider
import xcj.app.io.tencent.TencentCosRegionBucket
import xcj.app.starter.android.ktx.startWithHttpSchema
import xcj.app.starter.android.util.FileUtils
import xcj.app.starter.android.util.LocalMessager
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.staticProvider
import xcj.app.starter.test.LocalAndroidContextFileDir
import xcj.app.starter.test.LocalApplication
import java.io.File

class SimpleFileIO : FileIO {
    companion object {
        private const val GET = "GET"
        private const val HOST = "Host"

        private const val TAG = "SimpleFileIO"

        const val MESSAGE_KEY_ON_COMPONENTS_INITIALED = "on_components_initialed"
    }

    private var cosXmlService: CosXmlSimpleService? = null
    private var transferManager: TransferManager? = null

    private var tencentCosInfoProvider: TencentCosInfoProvider? = null

    private var lastExceptionTimeMills = 0L

    fun isThirdComponentsInit(): Boolean {
        return tencentCosInfoProvider != null
    }

    fun initThirdComponents(tencentCosInfoProvider: TencentCosInfoProvider) {
        PurpleLogger.current.d(TAG, "initThirdComponents")
        this.tencentCosInfoProvider = tencentCosInfoProvider
        LocalMessager.post(MESSAGE_KEY_ON_COMPONENTS_INITIALED, true)
    }

    private fun ensureCosXmlServiceIfNeeded() {
        val tencentCosInfoProvider = tencentCosInfoProvider
        if (tencentCosInfoProvider == null) {
            return
        }
        val cosRegionBucket = tencentCosInfoProvider.getTencentCosRegionBucket()
        if (cosRegionBucket == null) {
            return
        }
        val tencentCosRegionBucket = cosRegionBucket
        val region = tencentCosRegionBucket.region
        val myCredentialProvider: QCloudCredentialProvider =
            STSCredentialProvider(tencentCosInfoProvider)
        val serviceConfig = CosXmlServiceConfig.Builder()
            .setRegion(region)
            .isHttps(true)
            .builder()
        cosXmlService =
            CosXmlSimpleService(LocalApplication.current, serviceConfig, myCredentialProvider)
        val transferConfig = TransferConfig.Builder().build()
        transferManager = TransferManager(cosXmlService, transferConfig)
    }

    fun destroy() {
        tencentCosInfoProvider = null
        transferManager = null
        cosXmlService = null
    }

    private fun getUpdateResultListener(): CosXmlResultListener {
        return object : CosXmlResultListener {
            override fun onSuccess(request: CosXmlRequest, result: CosXmlResult) {
                //val uploadResult = result as COSXMLUploadTaskResult
                PurpleLogger.current.d(TAG, "UpdateResultListener, onSuccess")
            }

            override fun onFail(
                request: CosXmlRequest,
                clientException: CosXmlClientException?,
                serviceException: CosXmlServiceException?
            ) {
                PurpleLogger.current.d(
                    TAG,
                    "UpdateResultListener, onFail, clientException:${clientException?.message} " +
                            "serviceException:${serviceException?.message}"
                )
            }
        }
    }

    override suspend fun uploadWithFile(
        context: Context,
        file: File,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions?,
        resultListener: Any?
    ) {
        finalUploadPreCheck(context, file, urlMarker, uploadOptions, resultListener)
    }


    private fun File.isImage(): Boolean {
        return when (extension.lowercase()) {
            "png", "webp", "jpeg", "bmp", "svg", "jpg" -> true
            else -> false
        }
    }

    private suspend fun finalUploadPreCheck(
        context: Context,
        file: File,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions?,
        resultListener: Any?
    ) {
        PurpleLogger.current.d(TAG, "uploadForUrlMarker")
        ensureCosXmlServiceIfNeeded()
        val cosInfoProvider = tencentCosInfoProvider
        if (cosInfoProvider == null) {
            PurpleLogger.current.d(
                TAG,
                "uploadForUrlMarker, tencentCosInfoProvider is not init! return"
            )
            return
        }
        val transferManager = transferManager
        if (transferManager == null) {
            PurpleLogger.current.d(TAG, "uploadForUrlMarker, transferManager is null! return")
            return
        }

        val tencentCosRegionBucket = tencentCosInfoProvider?.getTencentCosRegionBucket()
        if (tencentCosRegionBucket == null) {
            PurpleLogger.current.d(TAG, "finalUpload, tencentCosRegionBucket is null, return")
            return
        }
        ///storage/emulated/0/Android/data/xcj.app.container/cache/temp/audios/audio_record/1737029140177/audio_1737029140191_1.mp3
        if (!file.exists() || !file.isFile || !file.canRead()) {
            PurpleLogger.current.d(TAG, "uploadForUrlMarker, file not valid! return")
            return
        }
        withContext(Dispatchers.IO) {
            var tempFile = file
            if (file.isImage()) {
                val compressedFile = Compressor.compress(context, file) {
                    default(quality = uploadOptions?.imageCompressQuality() ?: 80)
                    val tempFilesCacheDir = LocalAndroidContextFileDir.current.tempFilesCacheDir
                    val cacheFile =
                        File(tempFilesCacheDir + File.separator + urlMarker + "." + file.extension.lowercase())
                    destination(cacheFile)
                }
                if (compressedFile.exists()) {
                    PurpleLogger.current.d(
                        TAG,
                        "uploadForUrlMarker, compressed image file:${compressedFile}"
                    )
                    tempFile = compressedFile
                }
            }
            finalUpload(
                tencentCosRegionBucket,
                transferManager,
                tempFile,
                urlMarker,
                uploadOptions,
                resultListener
            )
        }
    }

    private fun finalUpload(
        tencentCosRegionBucket: TencentCosRegionBucket,
        transferManager: TransferManager,
        file: File,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions?,
        resultListener: Any?
    ) {
        PurpleLogger.current.d(TAG, "finalUpload")
        val bucket = tencentCosRegionBucket.bucketName
        val infixPath = uploadOptions?.getInfixPath()
        val cosPath = if (infixPath != null) {
            "${tencentCosRegionBucket.filePathPrefix}${infixPath}${urlMarker}"
        } else {
            "${tencentCosRegionBucket.filePathPrefix}${urlMarker}"
        }
        val srcPath = file.toString()
        val uploadId: String? = null
        val cosxmlUploadTask = transferManager.upload(bucket, cosPath, srcPath, uploadId)
        if (resultListener != null && resultListener is CosXmlResultListener) {
            cosxmlUploadTask.setCosXmlResultListener(resultListener)
        } else {
            cosxmlUploadTask.setCosXmlResultListener(getUpdateResultListener())
        }
    }

    override suspend fun uploadWithUri(
        context: Context,
        uri: Uri,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions?,
        resultListener: Any?
    ) {
        val file = FileUtils.parseFromAndroidUri(context, uri)?.file
        if (file == null) {
            PurpleLogger.current.d(TAG, "uploadWithUri, file is null, return")
            return
        }
        PurpleLogger.current.d(
            TAG,
            "uploadWithUri uri content file: $file"
        )
        finalUploadPreCheck(context, file, urlMarker, uploadOptions, resultListener)
    }

    override suspend fun uploadWithMultiFile(
        context: Context,
        files: List<File>,
        urlMarkers: List<String>,
        uploadOptions: ObjectUploadOptions?,
        resultListener: Any?
    ) {

    }

    override suspend fun uploadWithMultiUri(
        context: Context,
        uris: List<Uri>,
        urlMarkers: List<String>,
        uploadOptions: ObjectUploadOptions?,
        resultListener: Any?
    ) {
        PurpleLogger.current.d(TAG, "uploadWithMultiUri")
        val tempResultListener = resultListener ?: getUpdateResultListener()
        uris.forEachIndexed { index, uri ->
            uploadWithUri(context, uri, urlMarkers[index], uploadOptions, tempResultListener)
        }
    }

    suspend fun generatePreSign(
        contentUrlMarker: String?,
        objectPathOptions: ObjectUploadOptions? = null
    ): String? = withContext(Dispatchers.IO) {
        if (System.currentTimeMillis() - lastExceptionTimeMills < 10 * 1000) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, lastExceptionTimeMills is closer fo now, return"
            )
            return@withContext contentUrlMarker
        }
        if (contentUrlMarker.isNullOrEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, contentUrlMarker is null or empty, return"
            )
            return@withContext null
        }
        if (contentUrlMarker.startWithHttpSchema()) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, contentUrlMarker http or https schema, return"
            )
            return@withContext contentUrlMarker
        }
        ensureCosXmlServiceIfNeeded()
        val xmlService = cosXmlService
        if (xmlService == null) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, cosXmlService is null, return"
            )
            return@withContext contentUrlMarker
        }

        val cosInfoProvider = tencentCosInfoProvider
        if (cosInfoProvider == null) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, tencentCosInfoProvider is null, return"
            )
            return@withContext contentUrlMarker
        }

        val tencentCosRegionBucket = cosInfoProvider.getTencentCosRegionBucket()
        if (tencentCosRegionBucket == null) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, tencentCosRegionBucket is null, return"
            )
            return@withContext contentUrlMarker
        }
        val bucket = tencentCosRegionBucket.bucketName

        val infixPath = objectPathOptions?.getInfixPath()
        val cosPath = if (!infixPath.isNullOrEmpty()) {
            "${tencentCosRegionBucket.filePathPrefix}${infixPath}${contentUrlMarker}"
        } else {
            "${tencentCosRegionBucket.filePathPrefix}${contentUrlMarker}"
        }

        val method = GET
        val preSignedUrlRequest = PresignedUrlRequest(
            bucket, cosPath
        )
        preSignedUrlRequest.setRequestMethod(method)

        preSignedUrlRequest.setSignKeyTime(43200)

        preSignedUrlRequest.addNoSignHeader(HOST)
        try {
            val preSignedURL = xmlService.getPresignedURL(preSignedUrlRequest)
            lastExceptionTimeMills = 0
            return@withContext preSignedURL
        } catch (e: CosXmlClientException) {
            lastExceptionTimeMills = System.currentTimeMillis()
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, exception, ${e.message}"
            )
        }
        return@withContext contentUrlMarker
    }

    override suspend fun getFile(path: String): File? {
        val cosXmlService = cosXmlService ?: return null
        val getTencentCosRegionBucket =
            tencentCosInfoProvider?.getTencentCosRegionBucket() ?: return null
        val objectRequest = GetObjectRequest(
            getTencentCosRegionBucket.bucketName,
            path,
            LocalAndroidContextFileDir.current.tempFilesCacheDir.toString()
        )
        objectRequest.progressListener = object : CosXmlProgressListener {
            override fun onProgress(complete: Long, target: Long) {

            }
        }

        runCatching {
            val getObjectResult = cosXmlService.getObject(objectRequest)
            return File(
                LocalAndroidContextFileDir.current.tempFilesCacheDir + path.substringAfterLast(
                    "/"
                )
            ).run {
                if (exists()) {
                    this
                } else {
                    null
                }
            }
        }

        return null
    }
}

@JvmField
val LocalFileIO = staticProvider<SimpleFileIO>().apply {
    provide(SimpleFileIO())
}
