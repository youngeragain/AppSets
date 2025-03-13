package xcj.app.appsets.im.message

import android.content.Context
import xcj.app.appsets.im.ImMessageDesignType
import xcj.app.appsets.im.ImMessageGenerator
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.im.model.FriendRequestJson
import xcj.app.appsets.im.model.GroupRequestJson
import xcj.app.appsets.im.model.RequestFeedbackJson
import xcj.app.starter.util.ContentType
import java.util.Date

class MessageSendInfo {
    var progress: Float = 0f
    var isSent: Boolean = false
    var failureReason: String? = null
}


abstract class ImMessage {

    companion object {

        const val KEY_SESSION_ID = "session_id"
        const val KEY_IM_MESSAGE_ID = "im_message_id"
        const val KEY_IM_MESSAGE_NOTIFICATION_ID = "im_message_notification_id"

        const val TYPE_O2O = "one2one"
        const val TYPE_O2M = "one2many"

        const val HEADER_MESSAGE_MESSAGE_DELIVERY_TYPE = "a1"
        const val HEADER_MESSAGE_ID = "a2"
        const val HEADER_MESSAGE_UID = "a3"
        const val HEADER_MESSAGE_NAME = "a4"
        const val HEADER_MESSAGE_NAME_BASE64 = "a5"
        const val HEADER_MESSAGE_AVATAR_URL = "a6"
        const val HEADER_MESSAGE_ROLES = "a7"
        const val HEADER_MESSAGE_MESSAGE_GROUP_TAG = "a8"
        const val HEADER_MESSAGE_TO_ID = "a9"
        const val HEADER_MESSAGE_TO_NAME = "a10"
        const val HEADER_MESSAGE_TO_NAME_BASE64 = "a11"
        const val HEADER_MESSAGE_TO_TYPE = "a12"
        const val HEADER_MESSAGE_TO_ICON_URL = "a13"
        const val HEADER_MESSAGE_TO_ROLES = "a14"

        @JvmStatic
        fun readableContent(context: Context, imMessage: ImMessage?): String? {
            if (imMessage == null) {
                return null
            }
            when (imMessage) {

                is TextMessage -> {
                    if (imMessage.metadata.data == null) {
                        return null
                    }
                    return imMessage.metadata.data.toString()
                }

                is HTMLMessage -> {
                    return "(${context.getString(xcj.app.appsets.R.string.web_content)})"
                }

                is AdMessage -> {
                    return "(${context.getString(xcj.app.appsets.R.string.advertisement)})"
                }

                is LocationMessage -> {
                    return "(${context.getString(xcj.app.appsets.R.string.location)})"
                }

                is MusicMessage -> {
                    return "(${context.getString(xcj.app.appsets.R.string.music)})"
                }

                is VideoMessage -> {
                    return "(${context.getString(xcj.app.appsets.R.string.video)})"
                }

                is VoiceMessage -> {
                    return "(${context.getString(xcj.app.appsets.R.string.voice)})"
                }

                is FileMessage -> {
                    return "(${context.getString(xcj.app.appsets.R.string.file)})"
                }

                is ImageMessage -> {
                    return "(${context.getString(xcj.app.appsets.R.string.image)})"
                }

                is SystemMessage -> {
                    val systemContentInterface = imMessage.systemContentInterface
                    when (systemContentInterface) {
                        is FriendRequestJson -> {
                            return systemContentInterface.hello
                        }

                        is GroupRequestJson -> {
                            return systemContentInterface.hello
                        }

                        is RequestFeedbackJson -> {
                            return if (systemContentInterface.isAccept) {
                                context.getString(xcj.app.appsets.R.string.your_request_has_passed)
                            } else {
                                context.getString(xcj.app.appsets.R.string.your_request_has_not_passed)
                            }
                        }

                        else -> return "?"

                    }
                }

                else -> {
                    return ""
                }
            }
        }

        fun textImMessageMetadata(text: String): StringMessageMetadata {
            return StringMessageMetadata(
                "",
                0,
                false,
                "none",
                text,
                ContentType.APPLICATION_TEXT
            )
        }
    }

    abstract val id: String
    abstract val timestamp: Date
    abstract val fromInfo: MessageFromInfo
    abstract val toInfo: MessageToInfo
    abstract val messageGroupTag: String?
    abstract val metadata: MessageMetadata<*>

    /**
     * @see ImMessageDesignType
     */
    abstract val messageType: String

    var messageSendInfo: MessageSendInfo? = null

    val isSendMessage: Boolean
        get() = messageSendInfo != null

    val isReceivedMessage: Boolean
        get() = messageSendInfo == null

    val readableDate: String
        get() {
            return ImMessageGenerator.sdf.format(timestamp)
        }

    override fun toString(): String {
        return ImMessageGenerator.gson.toJson(this)
    }
}

/**
 * 解析消息To信息
 */
fun ImMessage.parseToImObj(): ImObj? {
    if (toInfo.toType == ImMessage.TYPE_O2M) {
        return ImObj.ImGroup(toInfo)
    }
    if (toInfo.toType == ImMessage.TYPE_O2O) {
        return ImObj.ImSingle(toInfo, toInfo.roles)
    }
    return null
}


/**
 * 解析消息from信息
 */
fun ImMessage.parseFromImObj(): ImObj? {
    if (toInfo.toType == ImMessage.TYPE_O2M) {
        return ImObj.ImGroup(toInfo)
    }
    if (toInfo.toType == ImMessage.TYPE_O2O) {
        return ImObj.ImSingle(fromInfo, fromInfo.roles)
    }
    return null
}