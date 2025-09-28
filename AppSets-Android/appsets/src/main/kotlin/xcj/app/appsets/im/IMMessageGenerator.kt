package xcj.app.appsets.im

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.rabbitmq.client.Delivery
import com.rabbitmq.client.LongString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.db.room.entity.FlatImMessage
import xcj.app.appsets.im.message.AdMessage
import xcj.app.appsets.im.message.FileMessage
import xcj.app.appsets.im.message.HTMLMessage
import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.im.message.ImageMessage
import xcj.app.appsets.im.message.LocationMessage
import xcj.app.appsets.im.message.LocationMessageMetadata
import xcj.app.appsets.im.message.MessageSending
import xcj.app.appsets.im.message.MusicMessage
import xcj.app.appsets.im.message.StringMessageMetadata
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.message.TextMessage
import xcj.app.appsets.im.message.VideoMessage
import xcj.app.appsets.im.message.VideoMessageMetadata
import xcj.app.appsets.im.message.VoiceMessage
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.appsets.util.VideoFileUtil
import xcj.app.appsets.util.ktx.queryUriFileName
import xcj.app.appsets.util.model.MediaStoreDataUri
import xcj.app.appsets.util.model.UriProvider
import xcj.app.io.components.FileIO
import xcj.app.io.components.LocalFileIO
import xcj.app.io.components.ObjectUploadOptions
import xcj.app.io.compress.ICompressor
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object IMMessageGenerator {

    private const val TAG = "ImMessageGenerator"


    val gson: Gson by lazy {
        Gson()
    }

    val sdf: SimpleDateFormat by lazy {
        SimpleDateFormat("MM/dd HH:mm", Locale.CHINA)
    }

    val imContentObjectUploadOptions: ObjectUploadOptions
        get() = object : ObjectUploadOptions {

            override fun getInfixPath(): String {
                return "im/"
            }

            override fun compressOptions(): ICompressor.CompressOptions {
                return object : ICompressor.CompressOptions {
                    override fun imageCompressQuality(): Int {
                        return 55
                    }
                }
            }
        }

    private suspend fun getContentName(context: Context, content: Any): String {
        when (content) {
            is MediaStoreDataUri -> {
                return content.displayName ?: ""
            }

            is UriProvider -> {
                val uri = content.provideUri()
                return context.queryUriFileName(uri) ?: ""
            }

            is Uri -> {
                return context.queryUriFileName(content) ?: ""
            }

            is File -> {
                return content.nameWithoutExtension
            }

            else -> {
                return ""
            }
        }
    }

    /**
     * 发送的内容为本地文件时，需要先上传到文件服务器获取其Url
     * @return 文件的UrlEndpoint
     *
     */
    private suspend fun uploadContent(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any,
        urlEndpoint: String
    ): Boolean {
        when (content) {
            is File -> {
                if (!content.exists()) {
                    return false
                }
                LocalFileIO.current.uploadWithFile(
                    context,
                    content,
                    urlEndpoint,
                    imContentObjectUploadOptions
                )
                return true
            }

            is Uri -> {
                LocalFileIO.current.uploadWithUri(
                    context,
                    content,
                    urlEndpoint,
                    imContentObjectUploadOptions
                )
                return true
            }

            is UriProvider -> {
                val uri = content.provideUri()
                LocalFileIO.current.uploadWithUri(
                    context,
                    uri,
                    urlEndpoint,
                    imContentObjectUploadOptions
                )
                return true
            }

            else -> return false
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getImMessageMetadataForBase64EncodedImage(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): StringMessageMetadata {
        val imageFileBase64Encoded =
            withContext(Dispatchers.IO) {
                context.contentResolver.openInputStream(content as Uri)?.buffered()?.use {
                    Base64.encode(it.readAllBytes())
                } ?: ""
            }
        val description = getContentName(context, content)
        return StringMessageMetadata(
            description,
            0,
            true,
            "base64",
            imageFileBase64Encoded,
            ContentType.IMAGE
        )
    }

    private suspend fun getImMessageMetadataForImage(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): StringMessageMetadata {
        val urlEndpoint = UUID.randomUUID().toString()

        LocalFileIO.current.progressObserver = object : FileIO.ProgressObserver {
            override fun id(): String {
                return urlEndpoint
            }

            override fun onProgress(id: String, total: Long, current: Long) {
                val progress = current.toFloat() / total.toFloat()
                /*val messageSendInfo = imageMessage.messageSending.sendInfoState.value
                if (messageSendInfo != null) {
                    val newMessageSendInfo = messageSendInfo.copy(progress = progress)
                    imageMessage.messageSending.updateSendInfo(newMessageSendInfo)
                }*/
            }
        }

        uploadContent(context, session, inputSelector, content, urlEndpoint)

        val description = getContentName(context, content)
        val messageMetadata = StringMessageMetadata(
            description,
            0,
            true,
            "url",
            urlEndpoint,
            ContentType.IMAGE
        )
        messageMetadata.localData = content
        return messageMetadata
    }

    private suspend fun getImMessageMetadataForVideo(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): VideoMessageMetadata {
        val urlEndpoint = UUID.randomUUID().toString()
        uploadContent(context, session, inputSelector, content, urlEndpoint)
        val mediaStoreDataUriWrapper = content as MediaStoreDataUri
        val uri = mediaStoreDataUriWrapper.provideUri()
        val companionUrlEndpoint = UUID.randomUUID().toString()
        val videoFrameUriProvider = VideoFileUtil.getVideoFirstFrameAsUriProvider(context, uri)
        if (videoFrameUriProvider != null) {
            uploadContent(
                context,
                session,
                inputSelector,
                videoFrameUriProvider,
                companionUrlEndpoint
            )
        }
        val description = getContentName(context, content)
        val messageMetadata = VideoMessageMetadata(
            description,
            0,
            true,
            "none",
            ContentType.VIDEO,
            data = urlEndpoint,
            companionData = companionUrlEndpoint
        )
        messageMetadata.localData = content to videoFrameUriProvider
        return messageMetadata
    }

    private suspend fun getImMessageMetadataForMusic(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): StringMessageMetadata {
        val urlEndpoint = UUID.randomUUID().toString()
        uploadContent(context, session, inputSelector, content, urlEndpoint)
        val description = getContentName(context, content)
        val messageMetadata = StringMessageMetadata(
            description,
            0,
            true,
            "none",
            urlEndpoint,
            ContentType.AUDIO_MUSIC
        )
        messageMetadata.localData = content
        return messageMetadata
    }

    private suspend fun getImMessageMetadataForVoice(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): StringMessageMetadata {
        val urlEndpoint = UUID.randomUUID().toString()
        uploadContent(context, session, inputSelector, content, urlEndpoint)
        val description = getContentName(context, content)
        val messageMetadata = StringMessageMetadata(
            description,
            0,
            true,
            "none",
            urlEndpoint,
            ContentType.AUDIO
        )
        messageMetadata.localData = content
        return messageMetadata
    }

    private suspend fun getImMessageMetadataForFile(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): StringMessageMetadata {
        val urlEndpoint = UUID.randomUUID().toString()
        uploadContent(context, session, inputSelector, content, urlEndpoint)
        val description = getContentName(context, content)
        val messageMetadata = StringMessageMetadata(
            description,
            0,
            true,
            "none",
            urlEndpoint,
            ContentType.APPLICATION_FILE
        )
        messageMetadata.localData = content
        return messageMetadata
    }

    private suspend fun getImMessageMetadataForLocation(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): LocationMessageMetadata {
        val locationInfo =
            content as ContentSelectionResult.LocationContentSelectionResult.LocationInfo
        val description = getContentName(context, content)
        val messageMetadata = LocationMessageMetadata(
            description,
            0,
            true,
            "none",
            ContentType.APPLICATION_GEO,
            locationInfo,
        )
        messageMetadata.localData = content
        return messageMetadata
    }

    private suspend fun getImMessageMetadataForHTML(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): StringMessageMetadata {
        val description = getContentName(context, content)
        val messageMetadata = StringMessageMetadata(
            description,
            0,
            true,
            "none",
            content.toString(),//"html url or html raw content",
            ContentType.APPLICATION_TEXT
        )
        messageMetadata.localData = content
        return messageMetadata
    }

    private suspend fun getImMessageMetadataForAD(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): StringMessageMetadata {
        val description = getContentName(context, content)
        val messageMetadata = StringMessageMetadata(
            description,
            0,
            true,
            "none",
            "[AD]$context", //"advertisement",
            ContentType.APPLICATION_TEXT
        )
        messageMetadata.localData = content
        return messageMetadata
    }

    private suspend fun getImMessageMetadataForText(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): StringMessageMetadata {
        val text = content.toString()
        val size = text.toByteArray().size
        val description = getContentName(context, content)
        val messageMetadata = StringMessageMetadata(
            description,
            size,
            false,
            "none",
            text,
            ContentType.APPLICATION_TEXT
        )
        messageMetadata.localData = content
        return messageMetadata
    }

    suspend fun generateBySend(
        context: Context,
        session: Session,
        inputSelector: Int,
        content: Any
    ): IMMessage {
        PurpleLogger.current.d(TAG, "generateBySend, start")
        val messageId = UUID.randomUUID().toString()
        val timestamp = Calendar.getInstance().time
        val fromUserInfo = LocalAccountManager.userInfo
        val messageFromInfo =
            MessageFromInfo(fromUserInfo.uid, fromUserInfo.bioName, fromUserInfo.avatarUrl)
        val messageToInfo = MessageToInfo.Companion.fromImObj(session.imObj)
        val messageSending = MessageSending()
        val imMessage = when (inputSelector) {
            InputSelector.IMAGE -> {
                val metadata =
                    getImMessageMetadataForImage(
                        context,
                        session,
                        inputSelector,
                        content
                    )
                ImageMessage(
                    messageId,
                    timestamp,
                    messageFromInfo,
                    messageToInfo,
                    null,
                    metadata
                )
            }

            InputSelector.VIDEO -> {
                val metadata =
                    getImMessageMetadataForVideo(context, session, inputSelector, content)
                VideoMessage(
                    messageId,
                    timestamp,
                    messageFromInfo,
                    messageToInfo,
                    null,
                    metadata
                )
            }

            InputSelector.MUSIC -> {
                val metadata =
                    getImMessageMetadataForMusic(context, session, inputSelector, content)
                MusicMessage(
                    messageId,
                    timestamp,
                    messageFromInfo,
                    messageToInfo,
                    null,
                    metadata
                )
            }

            InputSelector.FILE -> {
                val metadata = getImMessageMetadataForFile(context, session, inputSelector, content)
                FileMessage(
                    messageId,
                    timestamp,
                    messageFromInfo,
                    messageToInfo,
                    null,
                    metadata
                )
            }

            InputSelector.VOICE -> {
                val metadata =
                    getImMessageMetadataForVoice(context, session, inputSelector, content)
                VoiceMessage(
                    messageId,
                    timestamp,
                    messageFromInfo,
                    messageToInfo,
                    null,
                    metadata
                )
            }

            InputSelector.LOCATION -> {
                val metadata =
                    getImMessageMetadataForLocation(context, session, inputSelector, content)
                LocationMessage(
                    messageId,
                    timestamp,
                    messageFromInfo,
                    messageToInfo,
                    null,
                    metadata
                )
            }

            InputSelector.HTML -> {
                val metadata = getImMessageMetadataForHTML(context, session, inputSelector, content)
                HTMLMessage(messageId, timestamp, messageFromInfo, messageToInfo, null, metadata)
            }

            InputSelector.AD -> {
                val metadata = getImMessageMetadataForAD(context, session, inputSelector, content)
                AdMessage(messageId, timestamp, messageFromInfo, messageToInfo, null, metadata)
            }

            else -> {
                val metadata = getImMessageMetadataForText(context, session, inputSelector, content)
                TextMessage(messageId, timestamp, messageFromInfo, messageToInfo, null, metadata)
            }
        }
        imMessage.messageSending = messageSending
        PurpleLogger.current.d(TAG, "generateBySend, final imMessage:$imMessage")
        return imMessage
    }

    suspend fun generateByReceived(message: Delivery): IMMessage? {
        PurpleLogger.current.d(TAG, "generateByReceived, start")
        val headers = message.properties.headers
        val fromUid = if (headers.containsKey(IMMessage.HEADER_MESSAGE_UID)) {
            (headers[IMMessage.HEADER_MESSAGE_UID] as? LongString).toString()
        } else {
            return null
        }
        val messageId = if (headers.containsKey(IMMessage.HEADER_MESSAGE_ID)) {
            (headers[IMMessage.HEADER_MESSAGE_ID] as? LongString).toString()
        } else {
            return null
        }
        val fromName = if (headers.containsKey(IMMessage.HEADER_MESSAGE_NAME)) {
            (headers[IMMessage.HEADER_MESSAGE_NAME] as? LongString).toString()
        } else {
            null
        }
        val fromAvatarUrl = if (headers.containsKey(IMMessage.HEADER_MESSAGE_AVATAR_URL)) {
            (headers[IMMessage.HEADER_MESSAGE_AVATAR_URL] as? LongString).toString()
        } else {
            null
        }
        val fromRoles = if (headers.containsKey(IMMessage.HEADER_MESSAGE_ROLES)) {
            (headers[IMMessage.HEADER_MESSAGE_ROLES] as? LongString).toString()
        } else {
            null
        }
        val messageGroupTag = if (headers.containsKey(IMMessage.HEADER_MESSAGE_MESSAGE_GROUP_TAG)) {
            (headers[IMMessage.HEADER_MESSAGE_MESSAGE_GROUP_TAG] as? LongString).toString()
        } else {
            null
        }
        val toType = if (headers.containsKey(IMMessage.HEADER_MESSAGE_TO_TYPE)) {
            (headers[IMMessage.HEADER_MESSAGE_TO_TYPE] as? LongString).toString()
        } else {
            return null
        }
        val toId = if (headers.containsKey(IMMessage.HEADER_MESSAGE_TO_ID)) {
            (headers[IMMessage.HEADER_MESSAGE_TO_ID] as? LongString).toString()
        } else {
            return null
        }
        val toName = if (headers.containsKey(IMMessage.HEADER_MESSAGE_TO_NAME)) {
            (headers[IMMessage.HEADER_MESSAGE_TO_NAME] as? LongString).toString()
        } else {
            return null
        }
        val toRoles = if (headers.containsKey(IMMessage.HEADER_MESSAGE_TO_ROLES)) {
            (headers[IMMessage.HEADER_MESSAGE_TO_ROLES] as? LongString).toString()
        } else {
            null
        }
        val toIconUrl = if (headers.containsKey(IMMessage.HEADER_MESSAGE_TO_ICON_URL)) {
            (headers[IMMessage.HEADER_MESSAGE_TO_ICON_URL] as? LongString).toString()
        } else {
            null
        }

        val messageFromInfo = MessageFromInfo(fromUid, fromName, fromAvatarUrl, fromRoles)
        PictureUrlMapper.mapPictureUrl(messageFromInfo)

        val messageToInfo = MessageToInfo(toType, toIconUrl, toRoles, toId, toName)
        PictureUrlMapper.mapPictureUrl(messageToInfo)

        val messageMetadataString = String(message.body)

        val messageType = message.properties.type
        val timestamp = message.properties.timestamp

        val imMessage = generateImMessage(
            messageType,
            messageMetadataString,
            messageId,
            timestamp,
            messageFromInfo,
            messageToInfo,
            messageGroupTag
        )

        PurpleLogger.current.d(TAG, "generateByReceived, final imMessage:$imMessage")
        PictureUrlMapper.mapPictureUrl(imMessage)
        return imMessage
    }

    suspend fun generateByLocalDb(
        flatImMessage: FlatImMessage,
        messageFromInfo: MessageFromInfo,
        messageToInfo: MessageToInfo,
    ): IMMessage? {
        PurpleLogger.current.d(TAG, "generateByLocalDb, start")
        val messageMetadataString = flatImMessage.content
        val messageType = flatImMessage.messageType

        val imMessage = generateImMessage(
            messageType,
            messageMetadataString,
            flatImMessage.id,
            flatImMessage.timestamp,
            messageFromInfo,
            messageToInfo,
            flatImMessage.messageGroupTag
        )

        PurpleLogger.current.d(TAG, "generateByLocalDb, final imMessage:$imMessage")
        PictureUrlMapper.mapPictureUrl(imMessage)
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
    ): IMMessage? {
        return when (messageType) {
            IMMessageDesignType.TYPE_TEXT -> {
                val messageMetadata = gson.fromJson<StringMessageMetadata>(
                    messageMetadataString,
                    StringMessageMetadata::class.java
                )
                TextMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            IMMessageDesignType.TYPE_IMAGE -> {
                val messageMetadata = gson.fromJson<StringMessageMetadata>(
                    messageMetadataString,
                    StringMessageMetadata::class.java
                )
                ImageMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }


            IMMessageDesignType.TYPE_VOICE -> {
                val messageMetadata = gson.fromJson<StringMessageMetadata>(
                    messageMetadataString,
                    StringMessageMetadata::class.java
                )
                VoiceMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            IMMessageDesignType.TYPE_LOCATION -> {
                val messageMetadata = gson.fromJson<LocationMessageMetadata>(
                    messageMetadataString,
                    LocationMessageMetadata::class.java
                )
                LocationMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            IMMessageDesignType.TYPE_VIDEO -> {
                val messageMetadata = gson.fromJson<VideoMessageMetadata>(
                    messageMetadataString,
                    VideoMessageMetadata::class.java
                )
                VideoMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            IMMessageDesignType.TYPE_AD -> {
                val messageMetadata = gson.fromJson<StringMessageMetadata>(
                    messageMetadataString,
                    StringMessageMetadata::class.java
                )
                AdMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }


            IMMessageDesignType.TYPE_MUSIC -> {
                val messageMetadata = gson.fromJson<StringMessageMetadata>(
                    messageMetadataString,
                    StringMessageMetadata::class.java
                )
                MusicMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            IMMessageDesignType.TYPE_FILE -> {
                val messageMetadata = gson.fromJson<StringMessageMetadata>(
                    messageMetadataString,
                    StringMessageMetadata::class.java
                )
                FileMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            IMMessageDesignType.TYPE_HTML -> {
                val messageMetadata = gson.fromJson<StringMessageMetadata>(
                    messageMetadataString,
                    StringMessageMetadata::class.java
                )
                HTMLMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }


            IMMessageDesignType.TYPE_SYSTEM -> {
                val messageMetadata = gson.fromJson<StringMessageMetadata>(
                    messageMetadataString,
                    StringMessageMetadata::class.java
                )
                SystemMessage(
                    messageId, timestamp,
                    messageFromInfo, messageToInfo,
                    messageGroupTag, messageMetadata
                )
            }

            else -> null
        }
    }

    fun makeMessageMetadataAsJsonString(message: IMMessage): String {
        PurpleLogger.current.d(TAG, "makeMessageMetadataAsJsonString, for messageId:${message.id}")
        return gson.toJson(message.metadata)
    }

}