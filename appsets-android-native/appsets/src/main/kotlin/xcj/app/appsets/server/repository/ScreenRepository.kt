package xcj.app.appsets.server.repository

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ktx.isHttpUrl
import xcj.app.appsets.ktx.saveBitmap
import xcj.app.appsets.ktx.toastSuspend
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.AddUserScreenParams
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.ui.nonecompose.base.UriHolder
import xcj.app.core.android.ApplicationHelper
import xcj.app.core.foundation.http.DesignResponse
import xcj.app.io.components.SimpleFileIO
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class ScreenRepository(private val userApi: UserApi) {

    companion object {
        fun mapScreensIfNeeded(screens: List<UserScreenInfo>?) {
            if (!screens.isNullOrEmpty()) {
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                screens.forEach { screenInfo ->
                    simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss")
                    calendar.time = simpleDateFormat.parse(screenInfo.postTime)
                    if (calendar.get(Calendar.YEAR) == currentYear) {
                        simpleDateFormat.applyPattern("MM-dd HH:mm")
                        screenInfo.postTime = simpleDateFormat.format(calendar.time)
                    } else {
                        simpleDateFormat.applyPattern("yyyy-MM-dd HH:mm")
                        screenInfo.postTime = simpleDateFormat.format(calendar.time)
                    }
                    if (screenInfo.uid == LocalAccountManager._userInfo.value.uid) {
                        screenInfo.userInfo?.name = "我"
                    } else {
                        screenInfo.userInfo?.name =
                            screenInfo.userInfo?.name ?: screenInfo.userInfo?.uid ?: ""
                    }
                    val pictureMediaFileUrls = mutableListOf<ScreenMediaFileUrl>()
                    val videoMediaFileUrls = mutableListOf<ScreenMediaFileUrl>()
                    screenInfo.mediaFileUrls?.forEach { mediaFile ->
                        if (mediaFile.mediaFileUrl.isNotEmpty() && !mediaFile.mediaFileUrl.isHttpUrl()) {
                            mediaFile.mediaFileUrl =
                                SimpleFileIO.getInstance().generatePreSign(mediaFile.mediaFileUrl)
                                    ?: mediaFile.mediaFileUrl
                        }
                        if (!mediaFile.mediaFileCompanionUrl.isNullOrEmpty() && !mediaFile.mediaFileCompanionUrl.isHttpUrl()) {
                            mediaFile.mediaFileCompanionUrl =
                                SimpleFileIO.getInstance()
                                    .generatePreSign(mediaFile.mediaFileCompanionUrl!!)
                                    ?: mediaFile.mediaFileCompanionUrl
                        }
                        if (mediaFile.mediaType == "image/*") {
                            pictureMediaFileUrls.add(mediaFile)
                        } else if (mediaFile.mediaType == "video/*") {
                            videoMediaFileUrls.add(mediaFile)
                        }
                    }

                    val avatarUrl = screenInfo.userInfo?.avatarUrl
                    if (!avatarUrl.isNullOrEmpty() && !avatarUrl.isHttpUrl()) {
                        screenInfo.userInfo.avatarUrl =
                            SimpleFileIO.getInstance().generatePreSign(avatarUrl) ?: avatarUrl
                    }

                    screenInfo.pictureMediaFileUrls = pictureMediaFileUrls
                    screenInfo.videoMediaFileUrls = videoMediaFileUrls
                    screenInfo.mediaFileUrls = null
                }
            }
        }
    }

    suspend fun addScreen(
        context: Context,
        isPublic: Boolean,
        content: String?,
        pictures: List<MediaStoreDataUriWrapper?>,
        video: MediaStoreDataUriWrapper?,
        associateTopics: String?,
        associatePeoples: String?
    ): DesignResponse<Boolean?> {
        var mediaFilUrls: MutableList<ScreenMediaFileUrl>? = null
        val filesToUpload = mutableListOf<Uri>()
        val fileUrlMakers = mutableListOf<String>()
        if (pictures.isNotEmpty()) {
            mediaFilUrls = mutableListOf()
            for (pic in pictures) {
                val pictureUri = (pic as UriHolder).provideUri() ?: continue
                filesToUpload.add(pictureUri)
                val contentUrlMarker = UUID.randomUUID().toString()
                fileUrlMakers.add(contentUrlMarker)
                mediaFilUrls.add(
                    ScreenMediaFileUrl(
                        contentUrlMarker,
                        null,
                        "image/*",
                        "picture",
                        0,
                    )
                )
            }
        }
        if (video != null) {
            val videoUri = video.provideUri()
            if (videoUri != null) {
                if (video.size > 104_857_600) {
                    "Max upload video size is 100Mb.".toastSuspend()
                } else {
                    if (mediaFilUrls == null)
                        mediaFilUrls = mutableListOf()

                    filesToUpload.add(videoUri)
                    val contentUrlMarker = UUID.randomUUID().toString()
                    fileUrlMakers.add(contentUrlMarker)
                    val mediaFileUrl = ScreenMediaFileUrl(
                        mediaFileUrl = contentUrlMarker,
                        mediaFileCompanionUrl = null,
                        mediaType = "video/*",
                        mediaDescription = video.name,
                        x18Content = 0
                    )

                    if (video.thumbnail != null) {
                        val filePathDir =
                            ApplicationHelper.getContextFileDir().tempFilesCacheDir
                        val fileAndFileName =
                            context.saveBitmap(video.thumbnail!!, filePathDir)
                        if (fileAndFileName != null) {
                            filesToUpload.add(File(fileAndFileName.first).toUri())
                            val contentCompanionUrlMarker = UUID.randomUUID().toString()
                            fileUrlMakers.add(contentCompanionUrlMarker)
                            mediaFileUrl.mediaFileCompanionUrl = contentCompanionUrlMarker
                        }
                    }
                    mediaFilUrls.add(mediaFileUrl)
                }

            }
        }
        if (filesToUpload.isNotEmpty()) {
            SimpleFileIO.getInstance().uploadWithMultiUri(context, filesToUpload, fileUrlMakers)
        }
        Log.e("ScreenPostUseCase", "post contentUrlMarker:contentUrlMarker:${fileUrlMakers}")
        return userApi.addScreen(
            AddUserScreenParams(
                content,
                associateTopics,
                associatePeoples,
                mediaFilUrls,
                isPublic,
                "normal add from android app"
            )
        )
    }

    suspend fun getScreens(
        page: Int? = 1,
        pageSize: Int? = 20
    ): DesignResponse<List<UserScreenInfo>> {
        return userApi.getScreens(page, pageSize)
    }

    suspend fun getScreensByUid(
        uid: String,
        page: Int? = 1,
        pageSize: Int? = 20
    ): DesignResponse<List<UserScreenInfo>> {
        val userScreenInfoListRes = userApi.getScreensByUid(uid, page, pageSize)
        mapScreensIfNeeded(userScreenInfoListRes.data)
        return userScreenInfoListRes
    }


    @Throws
    suspend fun getIndexRecommendScreens(
        page: Int? = 1,
        pageSize: Int? = 20
    ): DesignResponse<List<UserScreenInfo>> {
        AppSetsRepository.getInstance().provideAppToken() ?: throw Exception("appToken 异常!")
        val indexRecommendScreensRes = userApi.getIndexRecommendScreens(page, pageSize)
        mapScreensIfNeeded(indexRecommendScreensRes.data)
        return indexRecommendScreensRes
    }

    suspend fun getScreenReviews(
        screenId: String
    ): DesignResponse<List<ScreenReview>?> {
        return userApi.getScreenReviews(screenId)
    }

    suspend fun addScreenReview(
        screenId: String,
        content: String,
        isPublic: Boolean,
        screenReviewId: String? = null
    ): DesignResponse<Boolean?> {
        return userApi.addScreenReview(
            hashMapOf(
                "screenId" to screenId,
                "content" to content,
                "isPublic" to isPublic,
                "screenReviewId" to screenReviewId
            )
        )
    }

    suspend fun screenViewedByUser(screenId: String): DesignResponse<Boolean> {
        return userApi.screenViewedByUser(screenId)
    }

    suspend fun getScreenViewCount(screenId: String): DesignResponse<Int> {
        return userApi.getScreenViewCount(screenId)
    }

    suspend fun screenLikeItByUser(screenId: String, count: Int = 1): DesignResponse<Boolean> {
        return userApi.screenLikeItByUser(screenId, count)
    }

    suspend fun screenCollectByUser(screenId: String, category: String?): DesignResponse<Boolean> {
        return userApi.screenCollectByUser(screenId, category)
    }

    suspend fun removeCollectedScreen(screenId: String): DesignResponse<Boolean> {
        return userApi.removeCollectedScreen(screenId)
    }

    suspend fun changeScreenPublicState(
        screenId: String,
        isPublic: Boolean
    ): DesignResponse<Boolean> {
        return userApi.changeScreenPublicState(screenId, isPublic)
    }

    suspend fun screenIsCollectByUser(screenId: String): DesignResponse<Boolean> {
        return userApi.screenIsCollectByUser(screenId)
    }

    suspend fun getScreenLikedCount(screenId: String): DesignResponse<Int> {
        return userApi.getScreenLikedCount(screenId)
    }
}