package xcj.app.starter.android.ktx

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.appcompat.view.ContextThemeWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.android.util.model.MediaStoreDataUri
import xcj.app.starter.util.ByteUtil

private const val TAG = "ContextKTX"

/**
 * 扩展context, 用于获取Manifests.xml文件中定义的元数据meta-data
 * BroadcastReceiver获取方式由另外的方法
 */
fun <T> T.getMetaData(key: String): Any? {
    return when (this) {
        is Application -> {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            ).metaData.get(key)
        }

        is Activity -> {
            packageManager.getActivityInfo(
                componentName,
                PackageManager.GET_META_DATA
            ).metaData.get(key)
        }

        is Service -> {
            packageManager.getServiceInfo(
                ComponentName.unflattenFromString(
                    this::class.simpleName ?: ""
                )!!, PackageManager.GET_META_DATA
            ).metaData.get(key)
        }

        is ContentProvider -> {
            null
        }

        is BroadcastReceiver -> {
            null
        }

        else -> null
    }
}

/**
 * @param mediaType MediaStore支持的类型
 * @see MediaStore.Images
 * @see MediaStore.Audio
 * @see MediaStore.Video
 * @see MediaStore.Files
 *
 * @param sortDirection 1 降序，0 升序
 * @see ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
 * @see ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
 */
suspend fun Context.getSystemFileUris(
    mediaType: Class<*>?,
    sortDirection: Int = ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
    offset: Int = 0,
    limit: Int = 20,
): List<MediaStoreDataUri>? = withContext(Dispatchers.IO) {
    if (mediaType == null)
        return@withContext null
    PurpleLogger.current.d(
        "getSystemFileUris",
        "mediaType:${mediaType.simpleName}, offset:${offset}, limit:${limit}"
    )
    val uri: Uri = when (mediaType) {
        MediaStore.Images::class.java -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        MediaStore.Video::class.java -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        MediaStore.Audio::class.java -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        MediaStore.Files::class.java -> MediaStore.Files.getContentUri("external")
        else -> throw UnsupportedOperationException()
    } ?: return@withContext null
    val projections: Array<String> = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.MIME_TYPE
    )
    val sortColumns: Array<String> = arrayOf(MediaStore.MediaColumns.DATE_ADDED)
    val selectionArgs = Bundle().apply {
        putString(
            ContentResolver.QUERY_ARG_SQL_SELECTION,
            "((is_pending=0) AND (is_trashed=0) AND (volume_name IN ( 'external_primary' , '0901-3108' )))"
        )
        putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, sortColumns)
        putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, sortDirection)
        putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
        putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
    }
    val cursor = contentResolver.query(
        uri,
        projections,
        selectionArgs,
        null
    )
    cursor?.use {
        if (it.count == 0) {
            return@withContext null
        }
        val mediaStoreDataUris = mutableListOf<MediaStoreDataUri>()
        val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val displayNameColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val sizeColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
        val dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
        val dataAddedColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
        val mimeTypeColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

        while (it.moveToNext()) {
            val id = cursor.getLong(idColumnIndex)
            val displayName = cursor.getString(displayNameColumnIndex)
            val size = cursor.getLong(sizeColumnIndex)
            val data = cursor.getString(dataColumnIndex)
            val dataAdded = cursor.getString(dataAddedColumnIndex)
            val mimeType = cursor.getString(mimeTypeColumnIndex)

            if (size != 0L) {
                val fullUri = createUri(id, mimeType)
                val mediaStoreDataUri = MediaStoreDataUri(
                    id = id,
                    uri = fullUri,
                    date = dataAdded,
                    displayName = displayName,
                    size = size,
                    sizeReadable = ByteUtil.getNetFileSizeDescription(size),
                    mimeType = mimeType
                )
                mediaStoreDataUris.add(mediaStoreDataUri)
            } else {
                PurpleLogger.current.d(
                    TAG,
                    "getSystemFileUris, mediaStoreDataUri size is 0, $displayName"
                )
            }
        }
        return@withContext mediaStoreDataUris
    }

    return@withContext null
}

suspend fun Context.getSystemFileUri(
    uri: Uri,
): MediaStoreDataUri? = withContext(Dispatchers.IO) {
    PurpleLogger.current.d(
        "getSystemFileUri",
        "uri:${uri}"
    )
    val projections: Array<String> = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.MIME_TYPE
    )
    val selectionArgs = Bundle().apply {
        putString(
            ContentResolver.QUERY_ARG_SQL_SELECTION,
            "((is_pending=0) AND (is_trashed=0) AND (volume_name IN ( 'external_primary' , '0901-3108' )))"
        )
    }
    val cursor = contentResolver.query(
        uri,
        projections,
        selectionArgs,
        null
    )
    cursor?.use {
        if (it.count == 0) {
            return@withContext null
        }

        val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
        val displayNameColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val sizeColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
        val dataColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
        val dataAddedColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
        val mimeTypeColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

        if (!it.moveToNext()) {
            return@withContext null
        }
        val id = cursor.getLong(idColumnIndex)
        val displayName = cursor.getString(displayNameColumnIndex)
        val size = cursor.getLong(sizeColumnIndex)
        val data = cursor.getString(dataColumnIndex)
        val dataAdded = cursor.getString(dataAddedColumnIndex)
        val mimeType = cursor.getString(mimeTypeColumnIndex)

        if (size == 0L) {
            PurpleLogger.current.d(
                TAG,
                "getSystemFileUris, mediaStoreDataUri size is 0, $displayName"
            )
            return@withContext null
        }
        val mediaStoreDataUri = MediaStoreDataUri(
            id = id,
            uri = uri,
            date = dataAdded,
            displayName = displayName,
            size = size,
            sizeReadable = ByteUtil.getNetFileSizeDescription(size),
            mimeType = mimeType
        )
        return@withContext mediaStoreDataUri
    }

    return@withContext null
}

private fun createUri(id: Long, mimeType: String? = null): Uri {
    val contentUri: Uri

    if (mimeType?.startsWith("image") == true) {
        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    } else if (mimeType?.startsWith("video") == true) {
        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    } else {
        // ?
        contentUri = MediaStore.Files.getContentUri("external")
    }

    val uri = ContentUris.withAppendedId(contentUri, id)
    return uri
}

fun Context.getScreenWidthHeight(): Pair<Int, Int> {
    val displayMetrics = this.resources?.displayMetrics
    val width: Int = displayMetrics?.widthPixels ?: 0
    val height: Int = displayMetrics?.heightPixels ?: 0
    return width to height
}

fun Context.asComponentActivityOrNull(): ComponentActivity? {
    var context = this
    do {
        when (context) {
            is ComponentActivity -> {
                return context
            }

            is ContextThemeWrapper -> {
                context = context.baseContext
            }

            is android.view.ContextThemeWrapper -> {
                context = context.baseContext
            }

            else -> return null
        }
    } while (true)
}

suspend fun Context.queryUriFileName(uri: Uri): String? = withContext(Dispatchers.IO) {
    PurpleLogger.current.d(TAG, "queryUriFileName, uri:$uri")
    val projections: Array<String> = arrayOf(
        MediaStore.Files.FileColumns.DISPLAY_NAME
    )
    val cursor =
        contentResolver.query(uri, projections, null, null)
    cursor?.use {
        val moveToNext = it.moveToNext()
        if (!moveToNext) {
            return@withContext null
        }
        val index = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
        val name = it.getString(index)
        return@withContext name
    }
    return@withContext null
}