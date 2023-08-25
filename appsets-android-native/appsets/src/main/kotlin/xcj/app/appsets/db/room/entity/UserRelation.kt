package xcj.app.appsets.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param id, uid or groupId
 * @param type user or group
 * @param isRelate 1 related, 0 unRelated
 */
@Entity
data class UserRelation(
    @PrimaryKey(autoGenerate = false) val id: String,
    val type: String,
    val isRelate: Int
)