package xcj.app.appsets.util.ktx

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.util.model.MediaStoreDataUri
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ByteUtil
import java.io.File

private const val TAG = "ContextKTX"

/**
 * 扩展context, 用于获取Manifests.xml文件中定义的元数据meta-data
 * BroadcastReceiver获取方式由另外的方法
 */
inline fun <reified T> T.getMetaData(key: String): Any? {
    return when (T::class.java) {
        Application::class.java -> {
            (this as Application).packageManager.getApplicationInfo(
                (this as Application).packageName,
                PackageManager.GET_META_DATA
            ).metaData.get(key)
        }

        AppCompatActivity::class.java -> {
            (this as Activity).packageManager.getActivityInfo(
                (this as Activity).componentName,
                PackageManager.GET_META_DATA
            ).metaData.get(key)
        }

        Service::class.java -> {
            (this as Service).packageManager.getServiceInfo(
                ComponentName.unflattenFromString(
                    T::class.simpleName ?: ""
                )!!, PackageManager.GET_META_DATA
            ).metaData.get(key)
        }

        BroadcastReceiver::class.java -> {
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
    sortDirection: Int = 1,
    offset: Int = 0,
    limit: Int = 20
): List<MediaStoreDataUri>? = withContext(Dispatchers.IO) {
    if (mediaType == null)
        return@withContext null
    PurpleLogger.current.d(
        "getSystemFileUris",
        "mediaType:${mediaType}, offset:${offset}, limit:${limit}"
    )
    val uri: Uri = when (mediaType) {
        MediaStore.Images::class.java -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        MediaStore.Video::class.java -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        MediaStore.Audio::class.java -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        MediaStore.Files::class.java -> MediaStore.Files.getContentUri("external")
        else -> throw UnsupportedOperationException()
    } ?: return@withContext null
    val projections: Array<String> = arrayOf(
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DATA,
        MediaStore.MediaColumns.DATE_ADDED
    )
    val sortColumns: Array<String> = arrayOf(MediaStore.MediaColumns.DATE_ADDED)
    val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        contentResolver.query(
            uri,
            projections,
            selectionArgs,
            null
        )
    } else {
        val sortDirectionStr = if (sortDirection == 1) {
            "desc"
        } else {
            "asc"
        }
        val sortOrderSql =
            "${sortColumns.joinToString(",")} $sortDirectionStr limit ${offset}, $limit"
        contentResolver.query(
            uri,
            projections,
            null,
            null,
            sortOrderSql
        )
    }
    cursor?.use {
        if (it.count == 0) {
            return@withContext null
        }
        val fileUris = mutableListOf<MediaStoreDataUri>()
        while (it.moveToNext()) {
            val mediaStoreDataUri = MediaStoreDataUri()
            projections.forEach { projection ->
                val index = it.getColumnIndex(projection)
                if (index != -1) {
                    when (projection) {
                        MediaStore.MediaColumns.DATA -> {
                            val path = it.getString(index)
                            if (!path.isNullOrEmpty()) {
                                mediaStoreDataUri.uri = Uri.fromFile(File(path))
                            }
                        }

                        MediaStore.MediaColumns.DISPLAY_NAME -> {
                            mediaStoreDataUri.displayName = it.getString(index)
                        }

                        MediaStore.MediaColumns.SIZE -> {
                            val size = it.getLong(index)
                            mediaStoreDataUri.size = size
                            mediaStoreDataUri.sizeReadable =
                                ByteUtil.getNetFileSizeDescription(size)
                        }

                        MediaStore.MediaColumns.DATE_ADDED -> {
                            mediaStoreDataUri.date = it.getString(index)
                        }
                    }
                }
            }

            fileUris.add(mediaStoreDataUri)
        }
        return@withContext fileUris
    }

    return@withContext null
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
        if (context is ComponentActivity) {
            return context
        }
        if (context is ContextThemeWrapper) {
            context = context.baseContext
        }
        if (context is android.view.ContextThemeWrapper) {
            context = context.baseContext
        }
    } while (true)

    return null
}

suspend fun Context.queryUriFileName(uri: Uri): String? = withContext(Dispatchers.IO) {
    PurpleLogger.current.d(TAG, "queryUriFileName, uri:$uri")
    val projections: Array<String> = arrayOf(
        MediaStore.Files.FileColumns.DISPLAY_NAME
    )
    var cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        contentResolver.query(uri, projections, null, null)
    } else {
        contentResolver.query(uri, projections, null, null, null)
    }
    cursor?.use {
        val moveToNext = it.moveToNext()
        if (!moveToNext) {
            return@withContext null
        }
        val projection = projections.first()
        val index = it.getColumnIndexOrThrow(projection)
        if (index != -1) {
            val name = it.getString(index)
            return@withContext name
        }
    }
    return@withContext null
}