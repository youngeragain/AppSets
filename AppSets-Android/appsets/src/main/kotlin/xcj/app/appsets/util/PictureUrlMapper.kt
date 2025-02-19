package xcj.app.appsets.util

import xcj.app.appsets.im.ImMessageGenerator
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.im.message.AdMessage
import xcj.app.appsets.im.message.HTMLMessage
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.im.message.LocationMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.message.TextMessage
import xcj.app.appsets.im.message.VideoMessage
import xcj.app.appsets.im.message.VideoMessageMetadata
import xcj.app.appsets.im.model.CommonURLJson
import xcj.app.appsets.im.model.FriendRequestJson
import xcj.app.appsets.im.model.GroupRequestJson
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.MediaContent
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.server.model.UserInfo
import xcj.app.io.components.LocalFileIO
import xcj.app.io.components.SimpleFileIO

object PictureUrlMapper {

    private const val TAG = "PictureUrlMapper"

    @JvmStatic
    suspend fun <T> mapPictureUrl(
        anyList: List<T>?,
        simpleFileIO: SimpleFileIO = LocalFileIO.current
    ) {
        if (anyList.isNullOrEmpty()) {
            return
        }
        anyList.forEach { any ->
            mapSingleInternal(any, simpleFileIO)
        }
    }

    @JvmStatic
    suspend fun <T> mapPictureUrl(
        any: T?,
        simpleFileIO: SimpleFileIO = LocalFileIO.current,
    ) {
        if (any == null) {
            return
        }
        mapSingleInternal(any, simpleFileIO)
    }

    @JvmStatic
    private suspend fun <T> mapSingleInternal(any: T, simpleFileIO: SimpleFileIO) {
        when (any) {
            is UserInfo -> {
                mapForUserInfo(any, simpleFileIO)
            }

            is GroupInfo -> {
                mapForGroupInfo(any, simpleFileIO)
            }

            is ScreenInfo -> {
                mapForScreenInfo(any, simpleFileIO)
            }

            is ScreenReview -> {
                mapForScreenReview(any, simpleFileIO)
            }

            is Application -> {
                mapForApplication(any, simpleFileIO)
            }

            is ScreenMediaFileUrl -> {
                mapForMediaFileUrl(any, simpleFileIO)
            }

            is MediaContent -> {
                mapForMediaContent(any, simpleFileIO)
            }

            is MessageFromInfo -> {
                mapForMessageFromInfo(any, simpleFileIO)
            }

            is MessageToInfo -> {
                mapForMessageToInfo(any, simpleFileIO)
            }

            is CommonURLJson -> {
                mapForCommonURLJson(any, simpleFileIO)
            }

            is ImMessage -> {
                mapForImMessage(any, simpleFileIO)
            }
        }
    }

    private suspend fun mapForScreenReview(
        screenReviews: ScreenReview,
        fileIO: SimpleFileIO
    ) {
        screenReviews.userInfo?.let { userInfo ->
            mapForUserInfo(userInfo, fileIO)
        }
    }

    private suspend fun mapForImMessage(
        imMessage: ImMessage,
        fileIO: SimpleFileIO
    ) {
        when (imMessage) {
            is TextMessage, is AdMessage, is LocationMessage, is HTMLMessage -> {
                return
            }

            is SystemMessage -> {
                val systemContentInterface = imMessage.systemContentInterface
                if (systemContentInterface != null) {
                    if (systemContentInterface is FriendRequestJson) {
                        systemContentInterface.avatarUrl =
                            fileIO.generatePreSign(systemContentInterface.avatarUrl)
                    } else if (systemContentInterface is GroupRequestJson) {
                        systemContentInterface.avatarUrl =
                            fileIO.generatePreSign(systemContentInterface.avatarUrl)
                        systemContentInterface.groupIconUrl =
                            fileIO.generatePreSign(systemContentInterface.groupIconUrl)
                    }
                }
            }

            else -> {
                val contentUrlMarker = imMessage.metadata.data.toString()
                val signedUrl = fileIO.generatePreSign(
                    contentUrlMarker,
                    ImMessageGenerator.imContentObjectUploadOptions
                )
                imMessage.metadata.url = signedUrl
                if (imMessage is VideoMessage) {
                    val videoMessageMetadata = imMessage.metadata as VideoMessageMetadata
                    val contentUrlMarker = (videoMessageMetadata).companionData
                    val signedUrl = fileIO.generatePreSign(
                        contentUrlMarker,
                        ImMessageGenerator.imContentObjectUploadOptions
                    )
                    videoMessageMetadata.companionUrl = signedUrl
                }
            }
        }
    }

    @JvmStatic
    private suspend fun mapForCommonURLJson(
        commonURLJson: CommonURLJson,
        fileIO: SimpleFileIO
    ) {
        commonURLJson.bioUrl = fileIO.generatePreSign(commonURLJson.url) ?: commonURLJson.url
    }

    @JvmStatic
    private suspend fun mapForMessageToInfo(
        messageToInfo: MessageToInfo,
        fileIO: SimpleFileIO
    ) {
        messageToInfo.bioUrl = fileIO.generatePreSign(messageToInfo.iconUrl)
    }

    @JvmStatic
    private suspend fun mapForMessageFromInfo(
        messageFromInfo: MessageFromInfo,
        fileIO: SimpleFileIO
    ) {
        messageFromInfo.bioUrl = fileIO.generatePreSign(messageFromInfo.avatarUrl)
    }

    @JvmStatic
    private suspend fun mapForUserInfo(userInfo: UserInfo, fileIO: SimpleFileIO) {
        userInfo.bioUrl = fileIO.generatePreSign(userInfo.avatarUrl)
    }

    @JvmStatic
    private suspend fun mapForGroupInfo(groupInfo: GroupInfo, fileIO: SimpleFileIO) {
        groupInfo.bioUrl = fileIO.generatePreSign(groupInfo.iconUrl)
        groupInfo.userInfoList?.forEach {
            mapForUserInfo(it, fileIO)
        }
    }

    private suspend fun mapForScreenInfo(screenInfo: ScreenInfo, fileIO: SimpleFileIO) {
        screenInfo.userInfo?.let {
            mapForUserInfo(it, fileIO)
        }
        screenInfo.mediaFileUrls?.forEach {
            mapForMediaFileUrl(it, fileIO)
        }
    }

    @JvmStatic
    private suspend fun mapForMediaContent(mediaContent: MediaContent, fileIO: SimpleFileIO) {
        mediaContent.uri = fileIO.generatePreSign(mediaContent.uri) ?: mediaContent.uri
        mediaContent.relateUser?.let {
            mapForUserInfo(it, fileIO)
        }
    }

    @JvmStatic
    private suspend fun mapForApplication(application: Application, fileIO: SimpleFileIO) {
        val iconRealUrl = fileIO.generatePreSign(application.iconUrl)
        application.bioUrl = iconRealUrl
        application.bannerUrl = fileIO.generatePreSign(application.bannerUrl)
        application.platforms?.forEach { platform ->
            platform.versionInfos?.forEach { versionInfo ->
                versionInfo.versionIconUrl = fileIO.generatePreSign(versionInfo.versionIconUrl)
                versionInfo.versionBannerUrl = fileIO.generatePreSign(versionInfo.versionBannerUrl)
                versionInfo.screenshotInfos?.forEach { screenshotInfo ->
                    screenshotInfo.url = fileIO.generatePreSign(screenshotInfo.url)
                }
            }
        }
    }

    @JvmStatic
    private suspend fun mapForMediaFileUrl(mediaFileUrl: ScreenMediaFileUrl, fileIO: SimpleFileIO) {
        mediaFileUrl.mediaFileUrl =
            fileIO.generatePreSign(mediaFileUrl.mediaFileUrl) ?: mediaFileUrl.mediaFileUrl
        mediaFileUrl.mediaFileCompanionUrl =
            fileIO.generatePreSign(mediaFileUrl.mediaFileCompanionUrl)
    }
}