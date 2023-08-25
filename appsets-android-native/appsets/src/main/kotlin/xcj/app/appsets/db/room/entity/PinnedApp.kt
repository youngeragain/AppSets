package xcj.app.appsets.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PinnedApp")
data class PinnedApp(
    @PrimaryKey(autoGenerate = true)
    val id:Int?=null,
    val packageName:String)