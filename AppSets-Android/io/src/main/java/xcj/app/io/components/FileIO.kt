package xcj.app.io.components

import android.content.Context
import android.net.Uri
import java.io.File

interface ObjectUploadOptions {
    fun getInfixPath(): String?
    fun imageCompressQuality(): Int?
}

interface FileIO {
    suspend fun uploadWithFile(
        context: Context,
        file: File,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions? = null,
        resultListener: Any? = null
    )

    suspend fun uploadWithUri(
        context: Context,
        uri: Uri,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions? = null,
        resultListener: Any? = null
    )

    suspend fun uploadWithMultiFile(
        context: Context,
        files: List<File>,
        urlMarkers: List<String>,
        uploadOptions: ObjectUploadOptions? = null,
        resultListener: Any? = null
    )

    suspend fun uploadWithMultiUri(
        context: Context,
        uris: List<Uri>,
        urlMarkers: List<String>,
        uploadOptions: ObjectUploadOptions? = null,
        resultListener: Any? = null
    )

    suspend fun getFile(path: String): File?
}