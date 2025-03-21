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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.io.components.FileIO.ProgressObserver
import xcj.app.io.components.FileIO.UploadResultObserver
import xcj.app.io.compress.CompressorHelper
import xcj.app.io.tencent.STSCredentialProvider
import xcj.app.io.tencent.TencentCosInfoProvider
import xcj.app.io.tencent.TencentCosRegionBucket
import xcj.app.starter.android.ktx.startWithHttpSchema
import xcj.app.starter.android.util.FileUtil
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

    var progressObserver: ProgressObserver? = null

    var uploadResultObserver: UploadResultObserver? = null

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

    private fun getUpdateResultListener(urlMarker: String): CosXmlResultListener {
        return object : CosXmlResultListener {
            override fun onSuccess(request: CosXmlRequest, result: CosXmlResult) {
                //val uploadResult = result as COSXMLUploadTaskResult
                PurpleLogger.current.d(TAG, "UpdateResultListener, onSuccess")
                val resultObserver = uploadResultObserver
                if (resultObserver != null && resultObserver.id() == urlMarker) {
                    resultObserver.onResult(urlMarker, true, null, null)
                    if (resultObserver.removeOnDone()) {
                        uploadResultObserver = null
                    }
                }
            }

            override fun onFail(
                request: CosXmlRequest,
                clientException: CosXmlClientException?,
                serviceException: CosXmlServiceException?
            ) {
                val resultObserver = uploadResultObserver
                if (resultObserver != null && resultObserver.id() == urlMarker) {
                    resultObserver.onResult(urlMarker, false, clientException, serviceException)
                    if (resultObserver.removeOnDone()) {
                        uploadResultObserver = null
                    }
                }
                PurpleLogger.current.d(
                    TAG,
                    "UpdateResultListener, onFail, clientException:${clientException?.message} " +
                            "serviceException:${serviceException?.message}"
                )
            }
        }
    }

    private fun getUploadProgressListener(urlMarker: String): CosXmlProgressListener {
        return object : CosXmlProgressListener {
            override fun onProgress(complete: Long, target: Long) {
                val progressObserver = this@SimpleFileIO.progressObserver
                if (progressObserver != null && progressObserver.id() == urlMarker) {
                    progressObserver.onProgress(urlMarker, target, complete)
                    if (target == complete && progressObserver.removeOnDone()) {
                        progressObserver.removeOnDone()
                    }
                }
            }
        }
    }

    override suspend fun uploadWithFile(
        context: Context,
        file: File,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions?
    ) {
        finalUploadPreCheck(context, file, urlMarker, uploadOptions)
    }


    private suspend fun finalUploadPreCheck(
        context: Context,
        file: File,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions?
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
        if (!file.exists() || !file.isFile || !file.canRead()) {
            PurpleLogger.current.d(TAG, "uploadForUrlMarker, file not valid! return")
            return
        }
        withContext(Dispatchers.IO) {
            val fileToUpload = compressedFileIfNeeded(context, file, uploadOptions)
            finalUpload(
                tencentCosRegionBucket,
                transferManager,
                fileToUpload,
                urlMarker,
                uploadOptions
            )
        }
    }

    private suspend fun compressedFileIfNeeded(
        context: Context,
        file: File,
        uploadOptions: ObjectUploadOptions?
    ): File {
        return CompressorHelper().compress(context, file, uploadOptions?.compressOptions())
    }

    private fun finalUpload(
        tencentCosRegionBucket: TencentCosRegionBucket,
        transferManager: TransferManager,
        file: File,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions?
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
        cosxmlUploadTask.setCosXmlProgressListener(getUploadProgressListener(urlMarker))
        cosxmlUploadTask.setCosXmlResultListener(getUpdateResultListener(urlMarker))
    }

    override suspend fun uploadWithUri(
        context: Context,
        uri: Uri,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions?,
    ) {
        val file = FileUtil.parseUriToAndroidUriFile(context, uri)?.file
        if (file == null) {
            PurpleLogger.current.d(TAG, "uploadWithUri, file is null, return")
            return
        }
        PurpleLogger.current.d(
            TAG,
            "uploadWithUri uri content file: $file"
        )
        finalUploadPreCheck(context, file, urlMarker, uploadOptions)
    }

    override suspend fun uploadWithMultiFile(
        context: Context,
        files: List<File>,
        urlMarkers: List<String>,
        uploadOptions: ObjectUploadOptions?
    ) {

    }

    override suspend fun uploadWithMultiUri(
        context: Context,
        uris: List<Uri>,
        urlMarkers: List<String>,
        uploadOptions: ObjectUploadOptions?
    ) {
        PurpleLogger.current.d(TAG, "uploadWithMultiUri")
        uris.forEachIndexed { index, uri ->
            uploadWithUri(context, uri, urlMarkers[index], uploadOptions)
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
        val tempFilesCacheDir = LocalAndroidContextFileDir.current.tempFilesCacheDir
        val objectRequest = GetObjectRequest(
            getTencentCosRegionBucket.bucketName,
            path,
            tempFilesCacheDir.toString()
        )
        objectRequest.progressListener = object : CosXmlProgressListener {
            override fun onProgress(complete: Long, target: Long) {

            }
        }

        runCatching {
            val getObjectResult = cosXmlService.getObject(objectRequest)
            return File(tempFilesCacheDir + path.substringAfterLast("/")).run {
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
