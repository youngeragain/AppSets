package xcj.app.appsets.db.room.dao

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xcj.app.appsets.db.room.entity.BitmapDefinition

@Dao
interface BitmapDao{

    @Insert
    fun insertBitmap(bitmap: BitmapDefinition):Long

    @Query("delete from BitmapDefinition where id=:id")
    fun deleteBitmapById(id: Int):Int

    @Query("select * from BitmapDefinition where id=:id")
    fun queryBitmapById1(id: Int): Cursor?

    @Query("select * from BitmapDefinition where id=:id")
    fun queryBitmapById(id: Int): BitmapDefinition?
}
