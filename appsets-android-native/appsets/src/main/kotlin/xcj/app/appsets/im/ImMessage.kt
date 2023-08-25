package xcj.app.appsets.im

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.db.room.entity.FlatImMessage
import xcj.app.appsets.server.model.UserInfo
import java.util.Date

/**
 * @param groupMessageTag 当用户发送时选择了添加图片，然后又添加文字等一系列连串操作后，消息本身还是会进行串行发送，
 * 这一组有序列的消息的第一个消息将设置此字段为”(“， 序列最后的消息设置此字段为”)“.
 * @param dateStr 请根据规则提前设置此字段的值，尽量避免放到composition的时候做格式化
 */
sealed class ImMessage(
    val id: String,
    val content: String,
    val msgFromInfo: MessageFromInfo,
    val date: Date,
    val msgToInfo: MessageToInfo,
    val groupMessageTag: String?,
) {
    var notificationId: Int? = null
    var dateStr: String? = null
    val contentType: String = "application/*"
    fun contentIsLiteralType(): Boolean {
        return this is Text || this is HTML || this is Ad || this is Location
    }

    fun getFlatImMessage(): FlatImMessage {
        return FlatImMessage(
            id = id,
            content = content,
            uid = msgFromInfo.uid,
            name = msgFromInfo.name,
            avatarUrl = msgFromInfo.avatarUrl,
            roles = msgFromInfo.roles,
            timestamp = date,
            toId = msgToInfo.id,
            toName = msgToInfo.name,
            toIconUrl = msgToInfo.iconUrl,
            toType = msgToInfo.toType,
            groupMessageTag = groupMessageTag,
            messageType = RabbitMqBrokerPropertyDesignType.getTypeByImMessage(this)
        )
    }

    /**
     * 获取当前消息发送方是否是未关联的，发送方和自己有关联则返回null,没关联返回具体信息
     */
    fun getFromInfo(): UserInfo {
        return UserInfo.basicInfo(msgFromInfo.uid, msgFromInfo.name, msgFromInfo.avatarUrl)
    }

    fun contentByMyType(): String {
        return when (this) {
            is Text -> {
                content
            }

            is HTML -> {
                "${content} (website)"
            }

            is Ad -> {
                "${content} (advertisement)"
            }

            is Location -> {
                "${content} (location)"
            }

            is Music -> {
                "${musicJson.name} (music)"
            }

            is Video -> {
                "${videoJson.name} (video)"
            }

            is Voice -> {
                "${voiceJson.name} (voice)"
            }

            is File -> {
                "${fileJson.name} (file)"
            }

            is Image -> {
                "(image)"
            }

            is System -> {
                systemContentJson.content
            }
        }
    }

    data class HTML(
        val msgId: String, val url: String, val fromUserInfo: MessageFromInfo,
        val timestamp: Date, val toInfo: MessageToInfo, val msgGroupTag: String?
    ) : ImMessage(msgId, url, fromUserInfo, timestamp, toInfo, msgGroupTag)

    data class Text(
        val msgId: String, val text: String, val fromUserInfo: MessageFromInfo,
        val timestamp: Date, val toInfo: MessageToInfo, val msgGroupTag: String?
    ) : ImMessage(msgId, text, fromUserInfo, timestamp, toInfo, msgGroupTag)

    data class Image(
        val msgId: String,
        val imageJson: String,
        val imageRaw: String?,
        val fromUserInfo: MessageFromInfo,
        val timestamp: Date,
        val toInfo: MessageToInfo,
        val msgGroupTag: String?
    ) : ImMessage(msgId, imageJson, fromUserInfo, timestamp, toInfo, msgGroupTag)

    data class Voice(
        val msgId: String,
        val voiceJson: CommonURLJson.VoiceURLJson,
        val voiceRaw: String,
        val fromUserInfo: MessageFromInfo,
        val timestamp: Date,
        val toInfo: MessageToInfo,
        val msgGroupTag: String?
    ) : ImMessage(
        msgId,
        voiceRaw,
        fromUserInfo,
        timestamp,
        toInfo,
        msgGroupTag
    )

    data class Video(
        val msgId: String,
        val videoJson: CommonURLJson.VideoURLJson,
        val videoRaw: String,
        val fromUserInfo: MessageFromInfo,
        val timestamp: Date,
        val toInfo: MessageToInfo,
        val msgGroupTag: String?
    ) : ImMessage(
        msgId,
        videoRaw,
        fromUserInfo,
        timestamp,
        toInfo,
        msgGroupTag
    )


    data class Music(
        val msgId: String,
        val musicJson: CommonURLJson.MusicURLJson,
        val musicRaw: String,
        val fromUserInfo: MessageFromInfo,
        val timestamp: Date,
        val toInfo: MessageToInfo,
        val msgGroupTag: String?
    ) : ImMessage(
        msgId,
        musicRaw,
        fromUserInfo,
        timestamp,
        toInfo,
        msgGroupTag
    )

    data class Ad(
        val msgId: String,
        val adJson: String, val fromUserInfo: MessageFromInfo,
        val timestamp: Date, val toInfo: MessageToInfo, val msgGroupTag: String?
    ) : ImMessage(msgId, adJson, fromUserInfo, timestamp, toInfo, msgGroupTag)

    data class Location(
        val msgId: String,
        val geoJson: String, val fromUserInfo: MessageFromInfo,
        val timestamp: Date, val toInfo: MessageToInfo, val msgGroupTag: String?
    ) : ImMessage(msgId, geoJson, fromUserInfo, timestamp, toInfo, msgGroupTag)

    data class File(
        val msgId: String,
        val fileJson: CommonURLJson.FileURLJson,
        val fileRaw: String,
        val fromUserInfo: MessageFromInfo,
        val timestamp: Date,
        val toInfo: MessageToInfo,
        val contentType1: String,
        val msgGroupTag: String?,
    ) : ImMessage(
        msgId,
        fileRaw,
        fromUserInfo,
        timestamp,
        toInfo,
        msgGroupTag
    )


    data class System(
        val msgId: String,
        val systemContentJson: SystemContentJson,
        val fromUserInfo: MessageFromInfo,
        val timestamp: Date,
        val toInfo: MessageToInfo,
        val contentType1: String?,
        val msgGroupTag: String?
    ) : ImMessage(
        msgId,
        systemContentJson.content,
        fromUserInfo,
        timestamp,
        toInfo,
        msgGroupTag
    ) {
        val handling: MutableState<Boolean> = mutableStateOf(false)
    }
}

/**
 * 解析消息To信息
 */
fun ImMessage.parseToImObj(): ImObj? {
    if (msgToInfo.isImgGroupMessage) {
        return ImObj.ImGroup(msgToInfo.id, msgToInfo.name, msgToInfo.iconUrl)
    }
    if (msgToInfo.isImSingleMessage) {
        return ImObj.ImSingle(msgToInfo.id, msgToInfo.name, msgToInfo.iconUrl, msgToInfo.roles)
    }
    return null
}


/**
 * 解析消息from信息
 */
fun ImMessage.parseFromImObj(): ImObj? {
    if (msgToInfo.isImgGroupMessage) {
        return ImObj.ImGroup(msgToInfo.id, msgToInfo.name, msgToInfo.iconUrl)
    }
    if (msgToInfo.isImSingleMessage) {
        return ImObj.ImSingle(
            msgFromInfo.uid,
            msgFromInfo.name,
            msgFromInfo.avatarUrl,
            msgFromInfo.roles
        )
    }
    return null
}