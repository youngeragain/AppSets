package xcj.app.appsets.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BitmapDefinition(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val path: String,
    val name: String
)