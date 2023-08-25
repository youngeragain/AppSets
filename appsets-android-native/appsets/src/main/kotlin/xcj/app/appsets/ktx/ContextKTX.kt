package xcj.app.appsets.ktx

import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import xcj.app.appsets.ui.nonecompose.base.UriHolder
import xcj.app.appsets.util.ByteUtil
import java.io.File
import java.io.FileOutputStream

/**
 * 扩展context, 用于获取Manifests.xml文件中定义的元数据meta-data
 * BroadcastReceiver获取方式由另外的方法
 */
inline fun <reified T> T?.getMetaData(key: String): Any? {
    this ?: return null
    return when (T::class.java) {
        Application::class.java -> {
            (this as Application).packageManager.getApplicationInfo(
                (this as Application).packageName,
                PackageManager.GET_META_DATA
            ).metaData.get(key)
        }
        AppCompatActivity::class.java -> {
            (this as AppCompatActivity).packageManager.getActivityInfo(
                (this as AppCompatActivity).componentName,
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

class MediaStoreDataUriWrapper : UriHolder {
    lateinit var uri: Uri
    lateinit var date: String
    lateinit var name: String
    var thumbnail: Bitmap? = null
    var size: Long = 0L
    var sizeReadable: String? = null

    override fun provideUri(): Uri? {
        if (!::uri.isInitialized)
            return null
        return uri
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
fun Context.getSystemFileUris(
    mediaType: Class<*>?,
    sortDirection: Int = 1,
    offset: Int = 0,
    limit: Int = 20
): List<MediaStoreDataUriWrapper>? {
    if (mediaType == null)
        return null
    Log.e("getSystemFileUris", "mediaType:${mediaType}, offset:${offset}, limit:${limit}")
    try {
        val uri: Uri = when (mediaType) {
            MediaStore.Images::class.java -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MediaStore.Video::class.java -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            MediaStore.Audio::class.java -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            MediaStore.Files::class.java -> MediaStore.Files.getContentUri("external")
            else -> throw UnsupportedOperationException()
        } ?: return null
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
        }else {
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
            if (it.count == 0)
                return null
            val fileUris = mutableListOf<MediaStoreDataUriWrapper>()
            while (it.moveToNext()) {
                val mediaStoreDataUriWrapper = MediaStoreDataUriWrapper()
                projections.forEach { projectionName ->
                    //Log.e("blue", "projectionName:${projectionName}")
                    val index = it.getColumnIndexOrThrow(projectionName)
                    when (projectionName) {
                        MediaStore.MediaColumns.DATA -> mediaStoreDataUriWrapper.uri =
                            Uri.fromFile(File(it.getString(index)))

                        MediaStore.MediaColumns.DISPLAY_NAME -> mediaStoreDataUriWrapper.name =
                            it.getString(index)

                        MediaStore.MediaColumns.SIZE -> mediaStoreDataUriWrapper.size =
                            it.getLong(index)

                        MediaStore.MediaColumns.DATE_ADDED -> mediaStoreDataUriWrapper.date =
                            it.getString(index)
                    }
                }
                mediaStoreDataUriWrapper.sizeReadable =
                    ByteUtil.getNetFileSizeDescription(mediaStoreDataUriWrapper.size)
                fileUris.add(mediaStoreDataUriWrapper)
            }
            return fileUris
        }
    }catch (e:Exception){
        e.printStackTrace()
    }
    return null
}

fun Context.getScreenWidthHeight(): Pair<Int, Int> {
    val displayMetrics = this.resources?.displayMetrics
    val width: Int = displayMetrics?.widthPixels?:0
    val height: Int = displayMetrics?.heightPixels?:0
    return width to height
}

fun Context.saveBitmap(bitmap: Bitmap, pathToSave: String? = null): Pair<String, String>? {
    val dir: File = (pathToSave?.let { File(it) } ?: getExternalFilesDir(null)) ?: return null
    //设置文件名称
    val name = "${System.currentTimeMillis()}-bitmap.jpg"
    val file1: File = File(dir, name)

    if (file1.exists()) {
        file1.delete()
    }
    file1.createNewFile()
    FileOutputStream(file1).use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    return file1.absolutePath to name
}


//Android 10+
fun ContentResolver.loadImagesUrisAfterQ(): List<Uri> {
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val images = mutableListOf<Uri>()
    val projection = arrayOf(MediaStore.Images.Media._ID)
    query(uri, projection, null, null, null)?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext()) {
            images += ContentUris.withAppendedId(uri, cursor.getLong(idColumn))
        }
    }
    return images
}

fun ContentResolver.loadImagesPathsBeforeQ(): List<String> {
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val images = mutableListOf<String>()
    val projection = arrayOf(MediaStore.MediaColumns.DATA)
    query(uri, projection, null, null, null)?.use { cursor ->
        val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
        while (cursor.moveToNext()) {
            images += cursor.getString(dataColumn)
        }
    }
    return images
}