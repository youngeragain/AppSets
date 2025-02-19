package xcj.app.appsets.db.room.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.db.room.entity.BitmapDefinition
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.util.PurpleLogger

@Dao
interface BitmapDao {

    @Insert
    fun insertBitmap(bitmap: BitmapDefinition): Long

    @Query("delete from BitmapDefinition where id=:id")
    fun deleteBitmapById(id: Int): Int

    @Query("select * from BitmapDefinition where id=:id")
    fun queryBitmapByIdForCursor(id: Int): Cursor

    @Query("select * from BitmapDefinition where id=:id")
    fun queryBitmapById(id: Int): BitmapDefinition?

    companion object {
        private const val TAG = "BitmapDao"

        fun getInstance(): BitmapDao? {
            val dataBase = ModuleHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            PurpleLogger.current.d(TAG, "getInstance, dataBase:${dataBase}")
            if (dataBase == null) {
                PurpleLogger.current.e(TAG, "getInstance, dataBase is null!!!")
                return null
            }
            return dataBase.bitmapDao()
        }
    }
}
