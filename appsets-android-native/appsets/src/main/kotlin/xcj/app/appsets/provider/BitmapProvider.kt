package xcj.app.appsets.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.db.room.dao.BitmapDao
import xcj.app.appsets.db.room.entity.BitmapDefinition
import xcj.app.core.android.ApplicationHelper
import xcj.app.purple_module.ModuleConstant
import java.io.File


class BitmapProvider:ContentProvider() {
    companion object{
        const val code = 200
    }
    private val uriMatcher: UriMatcher by lazy { UriMatcher(UriMatcher.NO_MATCH) }
    private val bitmapDao: BitmapDao by lazy {
        (ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_DATABASE_NAME))?.bitmapDao()
            ?: throw Exception("bitmapDao未初始化")
    }
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
            Log.e("BitmapProvider", "selectionArgs:$it")
        }
        if (uriMatcher.match(uri) == code) {
            if (!projection.isNullOrEmpty() && projection[0] == "id") {
                if (!selectionArgs.isNullOrEmpty()) {
                    return bitmapDao.queryBitmapById1(selectionArgs[0].toInt())
                }
            }
        }
        return null
    }

    override fun getType(uri: Uri): String? {
        return "application/bitmap_path"
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val path = values?.get("path") as? String
        val name = values?.get("name") as? String
        if(path.isNullOrEmpty()&&name.isNullOrEmpty())
            return null
        val tempBitmap = BitmapDefinition(path = path!!, name = name!!)
        val rowId = bitmapDao.insertBitmap(tempBitmap)
        return Uri.withAppendedPath(uri, rowId.toString())
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.e("BitmapProvider", "delete1")
        if (uriMatcher.match(uri) == code) {
            Log.e("BitmapProvider", "delete2")
            if (selection == "id=?" && selectionArgs?.size == 1) {
                val id = selectionArgs[0].toInt()
                Log.e("BitmapProvider", "delete:id:$id")
                val path = bitmapDao.queryBitmapById(id)?.path
                if (!path.isNullOrEmpty()) {
                    val bitmapFile = File(path)
                    if (bitmapFile.exists()) {
                        bitmapFile.delete()
                        val deleteCount = bitmapDao.deleteBitmapById(id)
                        if(deleteCount==1)
                            return 0
                    }
                }
            }
        }
        return -1
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
        Log.e(
            "BitmapProvider", """
            openFile: path:$path
        """.trimIndent()
        )
        if(path.isNullOrEmpty()){
            return null
        }
        return ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
    }
}