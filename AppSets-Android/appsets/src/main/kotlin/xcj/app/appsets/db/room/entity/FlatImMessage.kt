package xcj.app.appsets.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import xcj.app.appsets.im.ImMessageDesignType
import xcj.app.appsets.im.ImMessageGenerator
import xcj.app.appsets.im.message.ImMessage
import xcj.app.starter.android.util.PurpleLogger
import java.util.Date
import java.util.UUID

/**
 * @param content 文字类型为字面值，如果是文件，图片等，此值为url
 * @param messageType
 */
@Entity
data class FlatImMessage(
    @PrimaryKey(autoGenerate = false)
    var id: String,
    var content: String,
    var uid: String,
    var roles: String?,
    var timestamp: Date,
    var toId: String?,
    var toType: String?,
    var messageGroupTag: String?,
    var messageType: String
) {
    constructor() : this(
        UUID.randomUUID().toString(),
        "", "", null, Date(),
        null, null, null,
        ImMessageDesignType.TYPE_TEXT
    )

    companion object {

        private const val TAG = "FlatImMessage"

        fun parseFromImMessage(imMessage: ImMessage): FlatImMessage {
            val content = ImMessageGenerator.makeMessageMetadataAsJsonString(imMessage)
            PurpleLogger.current.d(TAG, "parseFromImMessage, content:$content")
            return FlatImMessage(
                id = imMessage.id,
                content = content,
                uid = imMessage.fromInfo.uid,
                roles = imMessage.fromInfo.roles,
                timestamp = imMessage.timestamp,
                toId = imMessage.toInfo.id,
                toType = imMessage.toInfo.toType,
                messageGroupTag = imMessage.messageGroupTag,
                messageType = ImMessageDesignType.getType(imMessage)
            )
        }

    }
}