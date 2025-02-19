package xcj.app.main.im

import com.google.gson.Gson
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.LongString
import xcj.app.main.model.res.UserInfoRes
import xcj.app.main.model.table.mysql.appSetsUserAdmin0
import xcj.app.main.util.ContentType
import xcj.app.util.PurpleLogger
import java.util.*

object ImMessageGenerator {

    private const val TAG = "ImMessageGenerator"

    val gson = Gson()

    private val messageFromInfo by lazy {
        val appSetsUserAdmin0 = appSetsUserAdmin0()
        MessageFromInfo(
            appSetsUserAdmin0.uid!!,
            appSetsUserAdmin0.name!!,
            appSetsUserAdmin0.avatarUrl!!,
            MessageToInfo.ROLE_ADMIN
        )
    }

    fun generateBySend(
        toUserInfo: UserInfoRes,
        content: Any
    ): ImMessage {
        PurpleLogger.current.d(TAG, "generateBySend")
        val messageToInfo =
            MessageToInfo(
                MessageToInfo.TO_TYPE_O2O,
                toUserInfo.uid,
                toUserInfo.name,
                toUserInfo.avatarUrl,
                null
            )
        val messageContent = gson.toJson(content)
        val metadata = ImMessage.StringMessageMetadata(
            "system_message",
            0,
            false,
            "none",
            messageContent,
            ContentType.TEXT_PLAIN
        )

        return ImMessage.System(
            UUID.randomUUID().toString(),
            Date(),
            messageFromInfo,
            messageToInfo,
            null,
            metadata,
        )
    }

    fun generateByReceived(properties: AMQP.BasicProperties?, body: ByteArray?): ImMessage? {
        val headers = properties?.headers ?: run {
            PurpleLogger.current.d(TAG, "generateByReceived, headers is null, return")
            return null
        }

        val fromUid = if (headers.containsKey(ImMessage.HEADER_MESSAGE_UID)) {
            (headers[ImMessage.HEADER_MESSAGE_UID] as? LongString).toString()
        } else {
            return null
        }
        val messageId = if (headers.containsKey(ImMessage.HEADER_MESSAGE_ID)) {
            (headers[ImMessage.HEADER_MESSAGE_ID] as? LongString).toString()
        } else {
            return null
        }
        val fromName = if (headers.containsKey(ImMessage.HEADER_MESSAGE_NAME)) {
            (headers[ImMessage.HEADER_MESSAGE_NAME] as? LongString).toString()
        } else {
            null
        }
        val fromAvatarUrl = if (headers.containsKey(ImMessage.HEADER_MESSAGE_AVATAR_URL)) {
            (headers[ImMessage.HEADER_MESSAGE_AVATAR_URL] as? LongString).toString()
        } else {
            null
        }
        val fromRoles = if (headers.containsKey(ImMessage.HEADER_MESSAGE_ROLES)) {
            (headers[ImMessage.HEADER_MESSAGE_ROLES] as? LongString).toString()
        } else {
            null
        }
        val messageGroupTag = if (headers.containsKey(ImMessage.HEADER_MESSAGE_MESSAGE_GROUP_TAG)) {
            (headers[ImMessage.HEADER_MESSAGE_MESSAGE_GROUP_TAG] as? LongString).toString()
        } else {
            null
        }
        val toType = if (headers.containsKey(ImMessage.HEADER_MESSAGE_TO_TYPE)) {
            (headers[ImMessage.HEADER_MESSAGE_TO_TYPE] as? LongString).toString()
        } else {
            return null
        }
        val toId = if (headers.containsKey(ImMessage.HEADER_MESSAGE_TO_ID)) {
            (headers[ImMessage.HEADER_MESSAGE_TO_ID] as? LongString).toString()
        } else {
            return null
        }
        val toName = if (headers.containsKey(ImMessage.HEADER_MESSAGE_TO_NAME)) {
            (headers[ImMessage.HEADER_MESSAGE_TO_NAME] as? LongString).toString()
        } else {
            return null
        }
        val toRoles = if (headers.containsKey(ImMessage.HEADER_MESSAGE_TO_ROLES)) {
            (headers[ImMessage.HEADER_MESSAGE_TO_ROLES] as? LongString).toString()
        } else {
            null
        }
        val toIconUrl = if (headers.containsKey(ImMessage.HEADER_MESSAGE_TO_ICON_URL)) {
            (headers[ImMessage.HEADER_MESSAGE_TO_ICON_URL] as? LongString).toString()
        } else {
            null
        }

        val messageFromInfo = MessageFromInfo(fromUid, fromName, fromAvatarUrl, fromRoles)

        val messageToInfo = MessageToInfo(toType, toId, toName, toIconUrl, toRoles)

        val messageType = properties.type

        val messageMetadataString = body?.decodeToString() ?: ""

        val timestamp = properties.timestamp
        val imMessage = generateImMessage(
            messageType,
            messageMetadataString,
            messageId,
            timestamp,
            messageFromInfo,
            messageToInfo,
            messageGroupTag
        )
        return imMessage
    }

    private fun generateImMessage(
        messageType: String,
        messageMetadataString: String,
        messageId: String,
        timestamp: Date,
        messageFromInfo: MessageFromInfo,
        messageToInfo: MessageToInfo,
        messageGroupTag: String?
    ): ImMessage? {
        return when (messageType) {
            ImMessageDesignType.TYPE_TEXT -> {
                val messageMetadata = gson.fromJson<ImMessage.StringMessageMetadata>(
                    messageMetadataString,
                    ImMessage.StringMessageMetadata::class.java
                )
                ImMessage.Text(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            ImMessageDesignType.TYPE_IMAGE -> {
                val messageMetadata = gson.fromJson<ImMessage.StringMessageMetadata>(
                    messageMetadataString,
                    ImMessage.StringMessageMetadata::class.java
                )
                ImMessage.Image(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }


            ImMessageDesignType.TYPE_VOICE -> {
                val messageMetadata = gson.fromJson<ImMessage.StringMessageMetadata>(
                    messageMetadataString,
                    ImMessage.StringMessageMetadata::class.java
                )
                ImMessage.Voice(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            ImMessageDesignType.TYPE_LOCATION -> {
                val messageMetadata = gson.fromJson<ImMessage.LocationMessageMetadata>(
                    messageMetadataString,
                    ImMessage.LocationMessageMetadata::class.java
                )
                ImMessage.Location(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            ImMessageDesignType.TYPE_VIDEO -> {
                val messageMetadata = gson.fromJson<ImMessage.VideoMessageMetadata>(
                    messageMetadataString,
                    ImMessage.VideoMessageMetadata::class.java
                )
                ImMessage.Video(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            ImMessageDesignType.TYPE_AD -> {
                val messageMetadata = gson.fromJson<ImMessage.StringMessageMetadata>(
                    messageMetadataString,
                    ImMessage.StringMessageMetadata::class.java
                )
                ImMessage.Ad(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }


            ImMessageDesignType.TYPE_MUSIC -> {
                val messageMetadata = gson.fromJson<ImMessage.StringMessageMetadata>(
                    messageMetadataString,
                    ImMessage.StringMessageMetadata::class.java
                )
                ImMessage.Music(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            ImMessageDesignType.TYPE_FILE -> {
                val messageMetadata = gson.fromJson<ImMessage.StringMessageMetadata>(
                    messageMetadataString,
                    ImMessage.StringMessageMetadata::class.java
                )
                ImMessage.File(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            ImMessageDesignType.TYPE_HTML -> {
                val messageMetadata = gson.fromJson<ImMessage.StringMessageMetadata>(
                    messageMetadataString,
                    ImMessage.StringMessageMetadata::class.java
                )
                ImMessage.HTML(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }


            ImMessageDesignType.TYPE_SYSTEM -> {
                val messageMetadata = gson.fromJson<ImMessage.StringMessageMetadata>(
                    messageMetadataString,
                    ImMessage.StringMessageMetadata::class.java
                )
                ImMessage.System(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            else -> null
        }
    }

    fun makeMessageMetadataAsJsonString(message: ImMessage): String {
        PurpleLogger.current.d(TAG, "makeMessageMetadataAsJsonString, for messageId:${message.id}")
        return gson.toJson(message.metadata)
    }
}