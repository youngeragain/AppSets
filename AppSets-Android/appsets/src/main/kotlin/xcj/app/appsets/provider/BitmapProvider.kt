package xcj.app.appsets.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import xcj.app.appsets.db.room.dao.BitmapDao
import xcj.app.appsets.db.room.entity.BitmapDefinition
import xcj.app.starter.android.util.PurpleLogger
import java.io.File

class BitmapProvider : ContentProvider() {
    companion object {
        private const val TAG = "BitmapProvider"
        private const val URI_MATCH_CODE = 200
    }

    private val uriMatcher: UriMatcher by lazy { UriMatcher(UriMatcher.NO_MATCH) }

    override fun onCreate(): Boolean {
        uriMatcher.addURI("xcj.appsets.provider", "bitmap", 200)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        selectionArgs?.forEach {
            PurpleLogger.current.d(TAG, "query, selectionArgs:$it")
        }
        val dao = BitmapDao.getInstance()
        if (
            dao == null ||
            selectionArgs.isNullOrEmpty() ||
            uriMatcher.match(uri) != URI_MATCH_CODE ||
            projection.isNullOrEmpty() ||
            projection[0] != "id"
        ) {
            return null
        }
        return dao.queryBitmapByIdForCursor(selectionArgs[0].toInt())
    }

    override fun getType(uri: Uri): String? {
        return "application/bitmap_path"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val path = values?.get("path") as? String ?: return null
        val name = values.get("name") as? String ?: return null
        val tempBitmap = BitmapDefinition(path = path, name = name)
        val rowId = BitmapDao.getInstance()?.insertBitmap(tempBitmap)
        return Uri.withAppendedPath(uri, rowId.toString())
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        PurpleLogger.current.d(TAG, "delete")
        val dao = BitmapDao.getInstance()
        if (
            dao == null ||
            selectionArgs.isNullOrEmpty() ||
            uriMatcher.match(uri) != URI_MATCH_CODE ||
            selection != "id=?" ||
            selectionArgs.size != 1
        ) {
            return -1
        }
        val id = selectionArgs[0].toInt()
        PurpleLogger.current.d(TAG, "delete:id:$id")
        val path = dao.queryBitmapById(id)?.path
        if (path.isNullOrEmpty()) {
            return -1
        }
        val bitmapFile = File(path)
        if (!bitmapFile.exists()) {
            return -1
        }
        bitmapFile.delete()
        val deleteCount = dao.deleteBitmapById(id)
        if (deleteCount != 1) {
            return -1
        }
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val path = uri.fragment
        PurpleLogger.current.d(TAG, "openFile, path:$path")
        if (path.isNullOrEmpty()) {
            return null
        }
        return ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
    }
}