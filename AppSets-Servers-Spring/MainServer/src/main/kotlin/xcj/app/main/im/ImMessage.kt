package xcj.app.main.im

import xcj.app.main.util.ContentType
import java.util.*

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

        fun textImMessageMetadata(text: String): ImMessage.StringMessageMetadata {
            return ImMessage.StringMessageMetadata(
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

    override fun toString(): String {
        return ImMessageGenerator.gson.toJson(this)
    }


    abstract class MessageMetadata<D>(
        open val description: String,
        open val size: Int,//in bytes
        open val compressed: Boolean,
        open val encode: String,//url or base64
        open var data: D, // if encode is url, this will be https://xxxx, if encode is base64, decode this to bytes
        open val contentType: String,
    ) {
        @Transient
        var url: String? = null

        override fun toString(): String {
            return ImMessageGenerator.gson.toJson(this)
        }
    }

    open class StringMessageMetadata(
        description: String,
        size: Int,
        compressed: Boolean,
        encode: String,
        data: String,
        contentType: String,
    ) : MessageMetadata<String>(
        description, size, compressed, encode, data, contentType
    )

    data class HTML(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: StringMessageMetadata,
        override val messageType: String = ImMessageDesignType.TYPE_HTML
    ) : ImMessage()

    class TextMessageMetadata(
        description: String,
        size: Int,
        compressed: Boolean,
        encode: String,
        data: String,
        contentType: String,
        val textStyle: ImMessageTextStyle? = null
    ) : StringMessageMetadata(
        description, size, compressed, encode, data, contentType
    ) {
        data class ImMessageTextStyle(val textSize: Int)
    }

    data class Text(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: StringMessageMetadata,
        override val messageType: String = ImMessageDesignType.TYPE_TEXT
    ) : ImMessage()

    data class Image(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: StringMessageMetadata,
        override val messageType: String = ImMessageDesignType.TYPE_IMAGE
    ) : ImMessage()

    data class Voice(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: StringMessageMetadata,
        override val messageType: String = ImMessageDesignType.TYPE_VOICE
    ) : ImMessage()

    class VideoMessageMetadata(
        description: String,
        size: Int,
        compressed: Boolean,
        encode: String,
        contentType: String,
        data: String,
        var companionData: String
    ) : StringMessageMetadata(
        description, size, compressed, encode, data, contentType
    ) {
        @Transient
        var companionUrl: String? = null
    }

    data class Video(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: MessageMetadata<*>,
        override val messageType: String = ImMessageDesignType.TYPE_VIDEO
    ) : ImMessage()


    data class Music(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: StringMessageMetadata,
        override val messageType: String = ImMessageDesignType.TYPE_MUSIC
    ) : ImMessage()

    data class Ad(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: StringMessageMetadata,
        override val messageType: String = ImMessageDesignType.TYPE_AD
    ) : ImMessage()

    class LocationMessageMetadata(
        description: String,
        size: Int,
        compressed: Boolean,
        encode: String,
        contentType: String,
        data: ContentSelectionResults.LocationInfo,
    ) : MessageMetadata<ContentSelectionResults.LocationInfo>(
        description, size, compressed, encode, data, contentType
    )

    data class Location(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: MessageMetadata<*>,
        override val messageType: String = ImMessageDesignType.TYPE_LOCATION
    ) : ImMessage()

    data class File(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: StringMessageMetadata,
        override val messageType: String = ImMessageDesignType.TYPE_FILE
    ) : ImMessage()

    data class System(
        override val id: String = UUID.randomUUID().toString(),
        override val timestamp: Date,
        override val fromInfo: MessageFromInfo,
        override val toInfo: MessageToInfo,
        override val messageGroupTag: String?,
        override val metadata: StringMessageMetadata,
        override val messageType: String = ImMessageDesignType.TYPE_SYSTEM
    ) : ImMessage() {

        private var systemContentInterfaceCached: SystemContentInterface? = null

        val systemContentInterface: SystemContentInterface?
            get() {
                if (systemContentInterfaceCached != null) {
                    return systemContentInterfaceCached
                }
                runCatching {
                    val systemContentJson = ImMessageGenerator.gson.fromJson<SystemContentJson>(
                        metadata.data,
                        SystemContentJson::class.java
                    )
                    val contentInterface =
                        systemContentJson.getContentObject(ImMessageGenerator.gson)
                    systemContentInterfaceCached = contentInterface
                    return contentInterface
                }
                return null
            }
    }
}

data class ContentSelectionType(
    val name: String,
    val nameStringResource: Int,
)

sealed interface ContentSelectionResults {
    data class LocationInfo(
        val coordinate: String,
        val info: String? = null,
        val extras: String? = null
    )
}