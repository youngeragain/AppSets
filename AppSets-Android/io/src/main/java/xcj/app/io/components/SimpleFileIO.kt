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
import xcj.app.starter.foundation.lazyStaticProvider
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

    var shouldActive: Boolean = true

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
        if (!shouldActive) {
            return
        }
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

    private fun getUpdateResultListener(urlEndpoint: String): CosXmlResultListener {
        return object : CosXmlResultListener {
            override fun onSuccess(request: CosXmlRequest, result: CosXmlResult) {
                //val uploadResult = result as COSXMLUploadTaskResult
                PurpleLogger.current.d(TAG, "UpdateResultListener, onSuccess")
                val resultObserver = uploadResultObserver
                if (resultObserver != null && resultObserver.id() == urlEndpoint) {
                    resultObserver.onResult(urlEndpoint, true, null, null)
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
                if (resultObserver != null && resultObserver.id() == urlEndpoint) {
                    resultObserver.onResult(urlEndpoint, false, clientException, serviceException)
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

    private fun getUploadProgressListener(urlEndpoint: String): CosXmlProgressListener {
        return CosXmlProgressListener { complete, target ->
            val progressObserver = this@SimpleFileIO.progressObserver
            if (progressObserver != null && progressObserver.id() == urlEndpoint) {
                progressObserver.onProgress(urlEndpoint, target, complete)
                if (target == complete && progressObserver.removeSelfWhenDone()) {
                    this@SimpleFileIO.progressObserver = null
                }
            }
        }
    }

    override suspend fun uploadWithFile(
        context: Context,
        file: File,
        urlEndpoint: String,
        uploadOptions: ObjectUploadOptions?
    ) {
        finalUploadPreCheck(context, file, urlEndpoint, uploadOptions)
    }


    private suspend fun finalUploadPreCheck(
        context: Context,
        file: File,
        urlEndpoint: String,
        uploadOptions: ObjectUploadOptions?
    ) {
        if (!shouldActive) {
            return
        }
        PurpleLogger.current.d(TAG, "finalUploadPreCheck")
        ensureCosXmlServiceIfNeeded()
        val cosInfoProvider = tencentCosInfoProvider
        if (cosInfoProvider == null) {
            PurpleLogger.current.d(
                TAG,
                "finalUploadPreCheck, tencentCosInfoProvider is not init! return"
            )
            return
        }
        val transferManager = transferManager
        if (transferManager == null) {
            PurpleLogger.current.d(TAG, "finalUploadPreCheck, transferManager is null! return")
            return
        }

        val tencentCosRegionBucket = tencentCosInfoProvider?.getTencentCosRegionBucket()
        if (tencentCosRegionBucket == null) {
            PurpleLogger.current.d(
                TAG,
                "finalUploadPreCheck, tencentCosRegionBucket is null, return"
            )
            return
        }
        if (!file.exists() || !file.isFile || !file.canRead()) {
            PurpleLogger.current.d(TAG, "finalUploadPreCheck, file not valid! return")
            return
        }
        withContext(Dispatchers.IO) {
            val fileToUpload = compressedFileIfNeeded(context, file, uploadOptions)
            finalUpload(
                tencentCosRegionBucket,
                transferManager,
                fileToUpload,
                urlEndpoint,
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
        urlEndpoint: String,
        uploadOptions: ObjectUploadOptions?
    ) {
        PurpleLogger.current.d(TAG, "finalUpload")
        val bucket = tencentCosRegionBucket.bucketName
        val infixPath = uploadOptions?.getInfixPath()
        val cosPath = if (infixPath != null) {
            "${tencentCosRegionBucket.filePathPrefix}${infixPath}${urlEndpoint}"
        } else {
            "${tencentCosRegionBucket.filePathPrefix}${urlEndpoint}"
        }
        val srcPath = file.toString()
        val uploadId: String? = null
        val cosxmlUploadTask = transferManager.upload(bucket, cosPath, srcPath, uploadId)
        cosxmlUploadTask.setCosXmlProgressListener(getUploadProgressListener(urlEndpoint))
        cosxmlUploadTask.setCosXmlResultListener(getUpdateResultListener(urlEndpoint))
    }

    override suspend fun uploadWithUri(
        context: Context,
        uri: Uri,
        urlEndpoint: String,
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
        finalUploadPreCheck(context, file, urlEndpoint, uploadOptions)
    }

    override suspend fun uploadWithMultiFile(
        context: Context,
        files: List<File>,
        urlEndpoints: List<String>,
        uploadOptions: ObjectUploadOptions?
    ) {

    }

    override suspend fun uploadWithMultiUri(
        context: Context,
        uris: List<Uri>,
        urlEndpoints: List<String>,
        uploadOptions: ObjectUploadOptions?
    ) {
        PurpleLogger.current.d(TAG, "uploadWithMultiUri")
        uris.forEachIndexed { index, uri ->
            uploadWithUri(context, uri, urlEndpoints[index], uploadOptions)
        }
    }

    suspend fun generatePreSign(
        urlEndpoint: String?,
        objectPathOptions: ObjectUploadOptions? = null
    ): String? = withContext(Dispatchers.IO) {
        if (!shouldActive) {
            return@withContext "http://localhost/"
        }
        if (System.currentTimeMillis() - lastExceptionTimeMills < 10 * 1000) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, lastExceptionTimeMills is closer fo now, return"
            )
            return@withContext urlEndpoint
        }
        if (urlEndpoint.isNullOrEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, urlEndpoint is null or empty, return"
            )
            return@withContext null
        }
        if (urlEndpoint.startWithHttpSchema()) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, urlEndpoint http or https schema, return"
            )
            return@withContext urlEndpoint
        }
        ensureCosXmlServiceIfNeeded()
        val xmlService = cosXmlService
        if (xmlService == null) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, cosXmlService is null, return"
            )
            return@withContext urlEndpoint
        }

        val cosInfoProvider = tencentCosInfoProvider
        if (cosInfoProvider == null) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, tencentCosInfoProvider is null, return"
            )
            return@withContext urlEndpoint
        }

        val tencentCosRegionBucket = cosInfoProvider.getTencentCosRegionBucket()
        if (tencentCosRegionBucket == null) {
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, tencentCosRegionBucket is null, return"
            )
            return@withContext urlEndpoint
        }
        val bucket = tencentCosRegionBucket.bucketName

        val infixPath = objectPathOptions?.getInfixPath()
        val cosPath = if (!infixPath.isNullOrEmpty()) {
            "${tencentCosRegionBucket.filePathPrefix}${infixPath}${urlEndpoint}"
        } else {
            "${tencentCosRegionBucket.filePathPrefix}${urlEndpoint}"
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
        } catch (e: Exception) {
            lastExceptionTimeMills = System.currentTimeMillis()
            PurpleLogger.current.d(
                TAG,
                "generatePreSign, exception, ${e.message}"
            )
        }
        return@withContext urlEndpoint
    }

    override suspend fun getFile(path: String): File? {
        if (!shouldActive) {
            return null
        }
        val cosXmlService = cosXmlService ?: return null
        val getTencentCosRegionBucket =
            tencentCosInfoProvider?.getTencentCosRegionBucket() ?: return null
        val tempFilesCacheDir = LocalAndroidContextFileDir.current.tempFilesCacheDir
        val objectRequest = GetObjectRequest(
            getTencentCosRegionBucket.bucketName,
            path,
            tempFilesCacheDir.toString()
        )
        objectRequest.progressListener = CosXmlProgressListener { complete, target -> }

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
val LocalFileIO = lazyStaticProvider<SimpleFileIO>().apply {
    provide {
        val simpleFileIO = SimpleFileIO()
        simpleFileIO
    }
}
