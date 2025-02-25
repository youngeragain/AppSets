package xcj.app.starter.android.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Files.FileColumns
import android.provider.OpenableColumns
import android.text.TextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.ktx.SCHEMA_FIlE
import xcj.app.starter.util.ByteUtil
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.lang.NumberFormatException
import java.util.Objects
import kotlin.math.min

object FileUtil {

    private const val TAG = "FileUtils"

    private val contentUri: Uri? = null

    @JvmStatic
    @SuppressLint("NewApi")
    fun getPath(context: Context, uri: Uri): String? {
        // check here to KITKAT or new version
        val isNewerThanKitKat = true
        var selection: String? = null
        var selectionArgs: Array<String?>? = null

        // DocumentProvider
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type: String? = split[0]

            val fullPath = getPathFromExtSD(split)
            if (!fullPath.isEmpty()) {
                return fullPath
            } else {
                return null
            }
        }

        // DownloadsProvider
        if (isDownloadsDocument(uri)) {
            context.contentResolver
                .query(uri, arrayOf<String>(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
                .use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        val fileName = cursor.getString(0)
                        val path = Environment.getExternalStorageDirectory()
                            .toString() + "/Download/" + fileName
                        if (!TextUtils.isEmpty(path)) {
                            return path
                        }
                    }
                }
            val id: String = DocumentsContract.getDocumentId(uri)
            if (!TextUtils.isEmpty(id)) {
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:".toRegex(), "")
                }
                val contentUriPrefixesToTry: Array<String> = arrayOf<String>(
                    "content://downloads/public_downloads",
                    "content://downloads/my_downloads"
                )
                for (contentUriPrefix in contentUriPrefixesToTry) {
                    try {
                        val contentUri =
                            ContentUris.withAppendedId(Uri.parse(contentUriPrefix), id.toLong())


                        return getDataColumn(context, contentUri, null, null)
                    } catch (e: NumberFormatException) {
                        //In Android 8 and Android P the id is not a number
                        return Objects.requireNonNull<String?>(uri.path)
                            .replaceFirst("^/document/raw:".toRegex(), "")
                            .replaceFirst("^raw:".toRegex(), "")
                    }
                }
            }
        }


        // MediaProvider
        if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val type: String? = split[0]

            var contentUri: Uri? = null

            if ("image" == type) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if ("video" == type) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if ("audio" == type) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            selection = "_id=?"
            selectionArgs = arrayOf<String?>(split[1])


            return getDataColumn(
                context, contentUri!!, selection,
                selectionArgs
            )
        }

        if (isGoogleDriveUri(uri)) {
            return getDriveFilePath(context, uri)
        }

        if (isWhatsAppFile(uri)) {
            return getFilePathForWhatsApp(context, uri)
        }


        if ("content".equals(uri.getScheme(), ignoreCase = true)) {
            if (isGooglePhotosUri(uri)) {
                return uri.lastPathSegment
            }
            if (isGoogleDriveUri(uri)) {
                return getDriveFilePath(context, uri)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // return getFilePathFromURI(context,uri);

                return copyFileToInternalStorage(context, uri, null, "userfiles")
                // return getRealPathFromURI(context,uri);
            } else {
                return getDataColumn(context, uri, null, null)
            }
        }
        if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }

        return null
    }

    @JvmStatic
    private fun fileExists(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists()
    }

    @JvmStatic
    private fun getPathFromExtSD(pathData: Array<String>): String {
        val type: String? = pathData[0]
        val relativePath = "/" + pathData[1]


        // on my Sony devices (4.4.4 & 5.1.1), `type` is a dynamic string
        // something like "71F8-2C0A", some kind of unique id per storage
        // don't know any API that can get the root path of that storage based on its id.
        //
        // so no "primary" type, but let the check here for other devices
        if ("primary".equals(type, ignoreCase = true)) {
            val fullPath = Environment.getExternalStorageDirectory().toString() + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
        }

        // Environment.isExternalStorageRemovable() is `true` for external and internal storage
        // so we cannot relay on it.
        //
        // instead, for each possible path, check if file exists
        // we'll start with secondary storage as this could be our (physically) removable sd card
        System.getenv("SECONDARY_STORAGE")?.let {
            val fullPath = it + relativePath
            if (fileExists(fullPath)) {
                return fullPath
            }
        }


        System.getenv("EXTERNAL_STORAGE")?.let {
            val fullPath = it + relativePath
            if (fileExists(fullPath)) {
                fileExists(fullPath)
            }

        }

        return relativePath
    }

    @JvmStatic
    private fun getDriveFilePath(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
        if (cursor == null) {
            return null
        }
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        val name = cursor.getString(nameIndex)
        val size = cursor.getLong(sizeIndex).toString()
        val file = File(context.cacheDir, name)
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                return file.path
            }
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1024 * 1024
            val bytesAvailable = inputStream.available()

            //int bufferSize = 1024;
            val bufferSize: Int = min(bytesAvailable, maxBufferSize)

            val buffers = ByteArray(bufferSize)
            while ((inputStream.read(buffers).also { read = it }) != -1) {
                outputStream.write(buffers, 0, read)
            }
            PurpleLogger.current.d(TAG, "File Size:" + file.length(), null)
            inputStream.close()
            outputStream.close()
            PurpleLogger.current.d(TAG, "File Path:" + file.getPath(), null)
        } catch (e: Exception) {
            PurpleLogger.current.d(TAG, "Exception" + e.message, null)
        } finally {
            cursor.close()
        }
        return file.path
    }

    @JvmStatic
    private fun getFilePathForWhatsApp(context: Context, uri: Uri): String? {
        return copyFileToInternalStorage(context, uri, null, "whatsapp")
    }

    @JvmStatic
    private fun getDataColumn(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String?>?
    ): String? {
        val projections = arrayOf<String?>(FileColumns.DATA)
        context.contentResolver.query(
            uri, projections,
            selection, selectionArgs, null
        )?.use {
            if (it.moveToFirst()) {
                val projection = projections.first()
                val index = it.getColumnIndex(projection)
                if (index != -1) {
                    return it.getString(index)
                }
                return null
            }
        }
        return null
    }

    @JvmStatic
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.getAuthority()
    }

    @JvmStatic
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.getAuthority()
    }

    @JvmStatic
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.getAuthority()
    }

    @JvmStatic
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.getAuthority()
    }

    @JvmStatic
    fun isWhatsAppFile(uri: Uri): Boolean {
        return "com.whatsapp.provider.media" == uri.authority
    }

    @JvmStatic
    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority ||
                "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    /***
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    @JvmStatic
    fun copyFileToInternalStorage(
        context: Context,
        uri: Uri,
        pathName: String?,
        newDirName: String?
    ): String? {
        var pathName = pathName
        val cursor = context.contentResolver.query(
            uri, arrayOf<String>(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        )
        if (cursor == null) {
            PurpleLogger.current.d(
                TAG,
                "copyFileToInternalStorage early return, cursor is null",
                null
            )
            return null
        }
        /*
     * Get the column indexes of the data in the Cursor,
     *     * move to the first row in the Cursor, get the data,
     *     * and display it.
     * */
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        //int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst()
        val name = (cursor.getString(nameIndex))

        //String size = (Long.toString(returnCursor.getLong(sizeIndex)));
        var output: File?
        if (pathName == null) {
            pathName = context.getFilesDir().getPath()
        }
        if (!TextUtils.isEmpty(newDirName)) {
            val dir = File(pathName + File.separator + newDirName)
            if (!dir.exists()) {
                dir.mkdir()
            }
            output = File(pathName + File.separator + newDirName + File.separator + name)
        } else {
            output = File(pathName + File.separator + name)
        }
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                PurpleLogger.current.d(
                    TAG,
                    "copyFileToInternalStorage early return, input stream is null ",
                    null
                )
                return null
            }
            val outputStream = FileOutputStream(output)
            var read = 0
            val bufferSize = 1024
            val buffers = ByteArray(bufferSize)
            while ((inputStream.read(buffers).also { read = it }) != -1) {
                outputStream.write(buffers, 0, read)
            }

            inputStream.close()
            outputStream.close()
            return output.path
        } catch (e: Exception) {
            PurpleLogger.current.d(
                TAG,
                "copyFileToInternalStorage exception " + e.message,
                null
            )
        } finally {
            cursor.close()
        }
        return null
    }

    @JvmStatic
    fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(
            uri, arrayOf<String>(
                OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
            ), null, null, null
        )
        if (cursor == null) {
            PurpleLogger.current.d(
                TAG,
                "copyFileToInternalStorage early return, cursor is null",
                null
            )
        }
        /*
     * Get the column indexes of the data in the Cursor,
     *     * move to the first row in the Cursor, get the data,
     *     * and display it.
     * */
        val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        //int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst()
        val string = cursor.getString(nameIndex)
        cursor.close()
        return string
    }

    @JvmStatic
    suspend fun parseUriToAndroidUriFile(context: Context, uri: Uri): AndroidUriFile? =
        withContext(Dispatchers.IO) {
            if (uri.scheme == SCHEMA_FIlE) {
                val path = uri.path
                if (path.isNullOrEmpty()) {
                    PurpleLogger.current.d(
                        TAG,
                        "fromAndroidUri, uri content file path is null or empty! return"
                    )
                    return@withContext null
                }
                val file = File(path)
                file.setReadable(true)
                PurpleLogger.current.d(
                    TAG,
                    "fromAndroidUri, uri content file path:${file}"
                )
                val length = file.length()
                return@withContext AndroidUriFile(file.nameWithoutExtension, file, length)
            } else {
                val projections =
                    arrayOf(FileColumns.DATA, FileColumns.DISPLAY_NAME, FileColumns.SIZE)
                val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.contentResolver.query(uri, projections, null, null)
                } else {
                    context.contentResolver.query(uri, projections, null, null, null, null)
                }

                if (cursor == null) {
                    PurpleLogger.current.d(
                        TAG,
                        "fromAndroidUri, cursor is null, uri:${uri}, return"
                    )
                    return@withContext null
                }
                cursor.use {
                    if (!cursor.moveToFirst()) {
                        return@withContext null
                    }
                    val androidUriFile = AndroidUriFile()
                    projections.forEach { projection ->
                        val index = it.getColumnIndex(projection)
                        if (index != -1) {
                            when (projection) {
                                FileColumns.DATA -> {
                                    val path = it.getString(index)
                                    if (!path.isNullOrEmpty()) {
                                        androidUriFile.file = File(path)
                                    }
                                }

                                FileColumns.DISPLAY_NAME -> {
                                    androidUriFile.displayName = it.getString(index)
                                }

                                FileColumns.SIZE -> {
                                    val size = it.getLong(index)
                                    androidUriFile.size = size
                                    androidUriFile.sizeReadable =
                                        ByteUtil.getNetFileSizeDescription(size)
                                }
                            }
                        }

                    }
                    return@withContext androidUriFile
                }

            }
            return@withContext null
        }

    fun isDirectoryUriByType(context: Context, uri: Uri?): Boolean {
        if (uri == null) {
            return false // Uri 为空，肯定不是文件夹
        }

        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri)

        if (mimeType == null) {
            return false
        }
        // 常见的文件夹 MIME 类型，可能需要根据实际情况扩展
        if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR ||  // Document Provider 文件夹的 MIME 类型
            mimeType.startsWith("vnd.android.document/directory") || // 某些 Document Provider 可能使用这种前缀
            mimeType == "application/vnd.google-apps.folder"
        ) { // Google Drive Folder
            return true
        }
        return false // 无法确定或 MIME 类型不是文件夹类型，认为不是文件夹
    }
}

data class AndroidUriFile(
    var displayName: String? = null,
    var file: File? = null,
    var size: Long = 0L,
    var sizeReadable: String? = null,
)