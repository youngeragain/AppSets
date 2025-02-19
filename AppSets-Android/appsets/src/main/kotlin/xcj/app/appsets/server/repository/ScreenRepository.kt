package xcj.app.appsets.server.repository

import android.content.Context
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.api.ApiProvider
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.AddUserScreenParams
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.ScreenReview
import xcj.app.appsets.ui.model.PostScreen
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.appsets.util.ktx.writeBitmap
import xcj.app.appsets.util.model.MediaStoreDataUri
import xcj.app.appsets.util.model.UriProvider
import xcj.app.io.components.LocalFileIO
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import xcj.app.starter.test.LocalAndroidContextFileDir
import xcj.app.starter.test.LocalApplication
import xcj.app.starter.util.ContentType
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class ScreenRepository(
    private val userApi: UserApi
) {
    companion object {

        private const val TAG = "ScreenRepository"

        private const val YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss"
        private const val MM_DD_HH_MM = "MM-dd HH:mm"
        private const val YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm"

        private var INSTANCE: ScreenRepository? = null

        fun getInstance(): ScreenRepository {
            if (INSTANCE == null) {
                val api = ApiProvider.provide(UserApi::class.java)
                val repository = ScreenRepository(api)
                INSTANCE = repository
            }
            return INSTANCE!!
        }

        suspend fun mapScreensIfNeeded(screens: List<ScreenInfo>?) {
            PurpleLogger.current.d(TAG, "mapScreensIfNeeded, thread:${Thread.currentThread()}")
            if (screens.isNullOrEmpty()) {
                return
            }
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val simpleDateFormat = SimpleDateFormat(YYYY_MM_DD_HH_MM_SS, Locale.CHINA)
            val simpleFileIO = LocalFileIO.current
            val textMine = LocalApplication.current.getString(xcj.app.appsets.R.string.i)
            screens.forEach { screenInfo ->
                simpleDateFormat.applyPattern(YYYY_MM_DD_HH_MM_SS)
                calendar.time = simpleDateFormat.parse(screenInfo.postTime)
                if (calendar.get(Calendar.YEAR) == currentYear) {
                    simpleDateFormat.applyPattern(MM_DD_HH_MM)
                    screenInfo.postTime = simpleDateFormat.format(calendar.time)
                } else {
                    simpleDateFormat.applyPattern(YYYY_MM_DD_HH_MM)
                    screenInfo.postTime = simpleDateFormat.format(calendar.time)
                }
                if (screenInfo.uid == LocalAccountManager.userInfo.uid) {
                    screenInfo.userInfo?.name = textMine
                } else {
                    screenInfo.userInfo?.name =
                        screenInfo.userInfo.name ?: screenInfo.userInfo.uid
                }
                PictureUrlMapper.mapPictureUrl(screenInfo, simpleFileIO)
            }
        }
    }

    suspend fun addScreen(
        context: Context,
        postScreen: PostScreen,
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "addScreen, thread:${Thread.currentThread()}")
        val mediaFilUrls =
            generateFileUrlMarkers(context, postScreen.pictures, postScreen.videos.firstOrNull())
        return@withContext userApi.addScreen(
            AddUserScreenParams(
                postScreen.content,
                postScreen.associateTopics,
                postScreen.associatePeoples,
                mediaFilUrls,
                postScreen.isPublic,
                "normal add from android app",
                postScreen.addToMediaFall
            )
        )
    }

    private suspend fun generateFileUrlMarkers(
        context: Context,
        pictures: List<UriProvider>,
        video: UriProvider?
    ): List<ScreenMediaFileUrl>? {
        if (pictures.isEmpty() && video == null) {
            PurpleLogger.current.d(TAG, "generateFileUrlMarkers, nothing need to do, return")
            return null
        }
        var mediaFilUrls: MutableList<ScreenMediaFileUrl>? = null
        val filesToUpload = mutableListOf<Uri>()
        val fileUrlMakers = mutableListOf<String>()
        if (pictures.isNotEmpty()) {
            mediaFilUrls = mutableListOf()
            for (pic in pictures) {
                val pictureUri = pic.provideUri() ?: continue
                filesToUpload.add(pictureUri)
                val contentUrlMarker = UUID.randomUUID().toString()
                fileUrlMakers.add(contentUrlMarker)
                mediaFilUrls.add(
                    ScreenMediaFileUrl(
                        contentUrlMarker,
                        null,
                        ContentType.IMAGE,
                        "picture",
                        0,
                    )
                )
            }
        }
        val video = video as? MediaStoreDataUri
        if (video != null) {
            if (video.size > 104_857_600) {
                context.getString(xcj.app.appsets.R.string.max_upload_video_size_tips)
                    .toastSuspend()
            } else {
                val videoUri = video.provideUri()
                if (videoUri != null) {
                    if (mediaFilUrls == null) {
                        mediaFilUrls = mutableListOf()
                    }
                    filesToUpload.add(videoUri)
                    val contentUrlMarker = UUID.randomUUID().toString()
                    fileUrlMakers.add(contentUrlMarker)
                    val mediaFileUrl = ScreenMediaFileUrl(
                        mediaFileUrl = contentUrlMarker,
                        mediaFileCompanionUrl = null,
                        mediaType = ContentType.VIDEO,
                        mediaDescription = video.displayName ?: "",
                        x18Content = 0
                    )
                    val mediaMetadataRetriever: MediaMetadataRetriever = MediaMetadataRetriever()
                    val thumbnailBitmap = video.getThumbnail(context, mediaMetadataRetriever)
                    if (thumbnailBitmap != null) {
                        val cacheDir = LocalAndroidContextFileDir.current.tempImagesCacheDir
                        if (!cacheDir.isNullOrEmpty()) {
                            val fileName = "${UUID.randomUUID()}.png"
                            val file = File(cacheDir, fileName)
                            file.createNewFile()
                            file.writeBitmap(thumbnailBitmap, Bitmap.CompressFormat.PNG, 85)
                            filesToUpload.add(file.toUri())
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
            PurpleLogger.current.d(TAG, "generateFileUrlMarkers, filesToUpload is not empty!")
            LocalFileIO.current.uploadWithMultiUri(context, filesToUpload, fileUrlMakers)
        }
        return mediaFilUrls
    }

    suspend fun getScreens(
        page: Int? = 1,
        pageSize: Int? = 20
    ): DesignResponse<List<ScreenInfo>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getScreens, thread:${Thread.currentThread()}")
        return@withContext userApi.getScreens(page, pageSize)
    }

    suspend fun getScreensByUid(
        uid: String,
        page: Int? = 1,
        pageSize: Int? = 20
    ): DesignResponse<List<ScreenInfo>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getScreensByUid, thread:${Thread.currentThread()}")
        val response = userApi.getScreensByUid(uid, page, pageSize)
        mapScreensIfNeeded(response.data)
        return@withContext response
    }


    @Throws
    suspend fun getIndexRecommendScreens(
        page: Int? = 1,
        pageSize: Int? = 20
    ): DesignResponse<List<ScreenInfo>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getIndexRecommendScreens, thread:${Thread.currentThread()}")
        val response = userApi.getIndexRecommendScreens(page, pageSize)
        mapScreensIfNeeded(response.data)
        return@withContext response
    }

    suspend fun getScreenReviews(
        screenId: String
    ): DesignResponse<List<ScreenReview>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getScreenReviews, thread:${Thread.currentThread()}")
        val screenReviews = userApi.getScreenReviews(screenId)
        PictureUrlMapper.mapPictureUrl(screenReviews.data)
        return@withContext screenReviews
    }

    suspend fun addScreenReview(
        screenId: String,
        content: String,
        isPublic: Boolean,
        screenReviewId: String? = null
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "addScreenReview, thread:${Thread.currentThread()}")
        return@withContext userApi.addScreenReview(
            hashMapOf(
                "screenId" to screenId,
                "content" to content,
                "isPublic" to isPublic,
                "screenReviewId" to screenReviewId
            )
        )
    }

    suspend fun screenViewedByUser(screenId: String): DesignResponse<Boolean> = withContext(
        Dispatchers.IO
    ) {
        PurpleLogger.current.d(TAG, "screenViewedByUser, thread:${Thread.currentThread()}")
        return@withContext userApi.screenViewedByUser(screenId)
    }

    suspend fun getScreenViewCount(screenId: String): DesignResponse<Int> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getScreenViewCount, thread:${Thread.currentThread()}")
            return@withContext userApi.getScreenViewCount(screenId)
        }

    suspend fun screenLikeItByUser(screenId: String, count: Int = 1): DesignResponse<Boolean> =
        withContext(
            Dispatchers.IO
        ) {
            PurpleLogger.current.d(TAG, "screenLikeItByUser, thread:${Thread.currentThread()}")
            return@withContext userApi.screenLikeItByUser(screenId, count)
        }

    suspend fun screenCollectByUser(screenId: String, category: String?): DesignResponse<Boolean> =
        withContext(
            Dispatchers.IO
        ) {
            PurpleLogger.current.d(TAG, "screenCollectByUser, thread:${Thread.currentThread()}")
            val overrideCategory = if (category.isNullOrEmpty()) {
                null
            } else {
                category
            }
            return@withContext userApi.screenCollectByUser(screenId, overrideCategory)
        }

    suspend fun removeCollectedScreen(screenId: String): DesignResponse<Boolean> = withContext(
        Dispatchers.IO
    ) {
        PurpleLogger.current.d(TAG, "removeCollectedScreen, thread:${Thread.currentThread()}")
        return@withContext userApi.removeCollectedScreen(screenId)
    }

    suspend fun changeScreenPublicState(
        screenId: String,
        isPublic: Boolean
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "changeScreenPublicState, thread:${Thread.currentThread()}")
        return@withContext userApi.changeScreenPublicState(screenId, isPublic)
    }

    suspend fun screenIsCollectByUser(screenId: String): DesignResponse<Boolean> = withContext(
        Dispatchers.IO
    ) {
        PurpleLogger.current.d(TAG, "screenIsCollectByUser, thread:${Thread.currentThread()}")
        return@withContext userApi.screenIsCollectByUser(screenId)
    }

    suspend fun getScreenLikedCount(screenId: String): DesignResponse<Int> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getScreenLikedCount, thread:${Thread.currentThread()}")
            return@withContext userApi.getScreenLikedCount(screenId)
        }
}