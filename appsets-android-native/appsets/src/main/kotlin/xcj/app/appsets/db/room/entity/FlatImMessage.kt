package xcj.app.appsets.db.room.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date

/**
 * @param rawContent 文字类型为字面值，如果是文件，图片等，此值为file路径或者uri
 * @param content 文字类型为字面值，如果是文件，图片等，此值为url
 * @param messageType
 * @see RabbitMqBrokerPropertyDesignType
 */
@Entity
data class FlatImMessage(
    @PrimaryKey(autoGenerate = false)
    var id: String,
    var content: String,
    var uid: String,
    @Ignore
    var name: String?,
    @Ignore
    var avatarUrl: String?,
    var roles: String?,
    var timestamp: Date,
    var toId: String?,
    @Ignore
    var toName: String?,
    @Ignore
    var toIconUrl: String?,
    var toType: String?,
    var groupMessageTag: String?,
    var messageType: String
) {
    constructor() : this(
        "", "", "",
        null, null, null, Date(),
        null, null, null, null, null, ""
    )
}