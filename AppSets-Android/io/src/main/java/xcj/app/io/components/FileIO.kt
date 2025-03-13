package xcj.app.io.components

import android.content.Context
import android.net.Uri
import xcj.app.io.compress.ICompressor
import java.io.File

interface ObjectUploadOptions {
    fun getInfixPath(): String
    fun compressOptions(): ICompressor.CompressOptions
}

interface FileIO {

    interface ProgressObserver {
        fun id(): String
        fun removeOnDone(): Boolean = true
        fun onProgress(id: String, total: Long, current: Long)
    }

    interface UploadResultObserver {
        fun id(): String
        fun removeOnDone(): Boolean = true
        fun onResult(
            id: String,
            isDone: Boolean,
            clientException: Exception?,
            serverException: Exception?
        )
    }

    suspend fun uploadWithFile(
        context: Context,
        file: File,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions? = null
    )

    suspend fun uploadWithUri(
        context: Context,
        uri: Uri,
        urlMarker: String,
        uploadOptions: ObjectUploadOptions? = null
    )

    suspend fun uploadWithMultiFile(
        context: Context,
        files: List<File>,
        urlMarkers: List<String>,
        uploadOptions: ObjectUploadOptions? = null
    )

    suspend fun uploadWithMultiUri(
        context: Context,
        uris: List<Uri>,
        urlMarkers: List<String>,
        uploadOptions: ObjectUploadOptions? = null
    )

    suspend fun getFile(path: String): File?
}