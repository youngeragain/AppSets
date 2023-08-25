package xcj.app.io.components

import android.content.Context
import android.net.Uri
import java.io.File

interface FileIO {
    fun uploadWithFile(context: Context, file: File, urlMarker: String, resultListener: Any? = null)

    fun uploadWithUri(context: Context, uri: Uri, urlMarker: String, resultListener: Any? = null)

    fun uploadWithMultiFile(
        context: Context,
        files: List<File>,
        urlMarkers: List<String>,
        resultListener: Any? = null
    )

    fun uploadWithMultiUri(
        context: Context,
        uris: List<Uri>,
        urlMarkers: List<String>,
        resultListener: Any? = null
    )
}