package xcj.app.appsets.server.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.api.AppSetsApiProvider
import xcj.app.appsets.server.api.ApiProvider
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.AppsWithCategory
import xcj.app.appsets.server.model.MediaContent
import xcj.app.appsets.server.model.SpotLight
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.settings.AppConfig
import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.io.components.LocalFileIO
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import java.util.UUID

class AppSetsRepository(
    private val appSetsApi: AppSetsApiProvider
) {

    suspend fun getAppToken(): DesignResponse<String> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getAppToken, thread:${Thread.currentThread()}")
        val appTokenRes =
            appSetsApi.getAppToken(mapOf("appSetsAppId" to AppConfig.appConfiguration.appsetsAppId))
        return@withContext appTokenRes
    }

    suspend fun getIndexApplications(): DesignResponse<List<AppsWithCategory>> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getIndexApplications, thread:${Thread.currentThread()}")
            val indexApplications = appSetsApi.getIndexApplications()
            val appsWithCategories = indexApplications.data

            val applications = appsWithCategories?.flatMap {
                it.applications
            }
            PictureUrlMapper.mapPictureUrl(applications)
            return@withContext indexApplications
        }

    suspend fun getSpotLight(): DesignResponse<SpotLight> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getSpotLight, thread:${Thread.currentThread()}")
        return@withContext appSetsApi.getSpotLight()
    }

    suspend fun checkUpdate(versionCode: Int): DesignResponse<UpdateCheckResult> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "checkUpdate, thread:${Thread.currentThread()}")
            return@withContext appSetsApi.checkUpdate(versionCode, "android")
        }

    suspend fun getUpdateHistory(): DesignResponse<List<UpdateCheckResult>> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "checkUpdate, thread:${Thread.currentThread()}")
            return@withContext appSetsApi.getUpdateHistory(null, "android")
        }

    suspend fun createApplicationPreCheck(appName: String): DesignResponse<Boolean> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(
                TAG,
                "createApplicationPreCheck, thread:${Thread.currentThread()}"
            )
            return@withContext appSetsApi.createApplicationPreCheck(appName)
        }

    suspend fun createApplication(
        context: Context,
        applicationForCreate: ApplicationForCreate
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "createApplication, thread:${Thread.currentThread()}")
        val uid = LocalAccountManager.userInfo.uid
        val currentTimeMills = System.currentTimeMillis().toString()
        val app = hashMapOf<String, Any?>()
        val uploadUrlMarkerList = mutableListOf<String>()
        val uploadUriList = mutableListOf<Uri>()

        val appIconUrlMarker = UUID.randomUUID().toString()
        val appIconUri = applicationForCreate.iconUriHolder!!.provideUri()!!
        val appBannerUrlMarker = UUID.randomUUID().toString()
        val appBannerUri = applicationForCreate.bannerUriHolder!!.provideUri()!!
        uploadUrlMarkerList.add(appIconUrlMarker)
        uploadUriList.add(appIconUri)
        uploadUrlMarkerList.add(appBannerUrlMarker)
        uploadUriList.add(appBannerUri)
        with(app) {
            put("icon_url", appIconUrlMarker)
            put("banner_url", appBannerUrlMarker)
            put("name", applicationForCreate.name)
            put("category", applicationForCreate.category)
            put("website", applicationForCreate.website)
            put("developer_info", applicationForCreate.developerInfo)
            put("price", applicationForCreate.price)
            put("price_unit", applicationForCreate.priceUnit)
            put("create_uid", uid)
            put("update_uid", uid)
            put("create_time", currentTimeMills)
            put("update_time", currentTimeMills)
            put("app_id", applicationForCreate.appId)
        }
        if (applicationForCreate.platformForCreates.isNotEmpty()) {
            val platforms = mutableListOf<HashMap<String, Any?>>()
            app["platforms"] = platforms
            applicationForCreate.platformForCreates.forEach { platformForCreate ->
                val platform = hashMapOf<String, Any?>()
                platforms.add(platform)
                with(platform) {
                    put("id", platformForCreate.id)
                    put("name", platformForCreate.name)
                    put("package_name", platformForCreate.packageName)
                    put("introduction", platformForCreate.introduction)
                }
                if (platformForCreate.versionInfoForCreates.isNotEmpty()) {
                    val versionInfos = mutableListOf<HashMap<String, Any?>>()
                    platform["version_infos"] = versionInfos
                    platformForCreate.versionInfoForCreates.forEach { versionInfoForCreate ->
                        val appVersionIconUrlMarker = UUID.randomUUID().toString()
                        val appVersionIconUri =
                            versionInfoForCreate.versionIconUriHolder!!.provideUri()!!
                        uploadUrlMarkerList.add(appVersionIconUrlMarker)
                        uploadUriList.add(appVersionIconUri)

                        val appVersionBannerUrlMarker = UUID.randomUUID().toString()
                        val appVersionBannerUri =
                            versionInfoForCreate.versionBannerUriHolder!!.provideUri()!!
                        uploadUrlMarkerList.add(appVersionBannerUrlMarker)
                        uploadUriList.add(appVersionBannerUri)

                        val versionInfo = hashMapOf<String, Any?>()
                        versionInfos.add(versionInfo)
                        with(versionInfo) {
                            put("version_icon_url", appVersionIconUrlMarker)
                            put("version_banner_url", appVersionIconUrlMarker)
                            put("version", versionInfoForCreate.version)
                            put("version_code", versionInfoForCreate.versionCode)
                            put("changes", versionInfoForCreate.changes)
                            put("package_size", versionInfoForCreate.packageSize)
                            put("privacy_url", versionInfoForCreate.privacyPolicyUrl)
                            put("create_time", currentTimeMills)
                            put("update_time", currentTimeMills)
                            put("create_uid", uid)
                            put("id", versionInfoForCreate.id)
                        }
                        if (versionInfoForCreate.screenshotInfoForCreates.isNotEmpty()) {
                            val screenShotInfos = mutableListOf<HashMap<String, Any?>>()
                            versionInfo["screenshot_infos"] = screenShotInfos
                            versionInfoForCreate.screenshotInfoForCreates.forEach { screenshotInfoForCreate ->
                                val screenshotUrlMarker = UUID.randomUUID().toString()
                                val screenshotUri =
                                    screenshotInfoForCreate.uriHolder!!.provideUri()!!
                                uploadUrlMarkerList.add(screenshotUrlMarker)
                                uploadUriList.add(screenshotUri)

                                val screenShotInfo = hashMapOf<String, Any?>()
                                screenShotInfos.add(screenShotInfo)
                                with(screenShotInfo) {
                                    put("url", screenshotUrlMarker)
                                    put("type", "picture")
                                    put("content_type", "image/*")
                                    put("create_uid", uid)
                                    put("update_uid", uid)
                                    put("id", screenshotInfoForCreate.id)
                                    put("create_time", currentTimeMills)
                                    put("update_time", currentTimeMills)
                                }
                            }
                        }
                        if (versionInfoForCreate.downloadInfoForCreates.isNotEmpty()) {
                            val downloadInfos = mutableListOf<HashMap<String, Any?>>()
                            versionInfo["download_infos"] = downloadInfos
                            versionInfoForCreate.downloadInfoForCreates.forEach { downloadInfoForCreate ->
                                val downloadInfo = hashMapOf<String, Any?>()
                                downloadInfos.add(downloadInfo)
                                with(downloadInfo) {
                                    put("create_uid", uid)
                                    put("update_uid", uid)
                                    put("create_time", currentTimeMills)
                                    put("update_time", currentTimeMills)
                                    put("download_times", 0)
                                    put("url", downloadInfoForCreate.url)
                                    put("id", downloadInfoForCreate.id)
                                }
                            }
                        }
                    }
                }
            }
        }
        LocalFileIO.current.uploadWithMultiUri(context, uploadUriList, uploadUrlMarkerList)
        return@withContext appSetsApi.createApplication(app)
    }

    suspend fun getApplicationsByUser(uid: String): DesignResponse<List<Application>> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getApplicationsByUser, thread:${Thread.currentThread()}")
            val designResponse = appSetsApi.getUsersApplications(uid)
            PictureUrlMapper.mapPictureUrl(designResponse.data)
            return@withContext designResponse
        }

    suspend fun getMediaContent(
        contentType: String,
        page: Int,
        pageSize: Int
    ): DesignResponse<List<MediaContent>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getMediaContent, thread:${Thread.currentThread()}")
        val designResponse = appSetsApi.getMediaContent(contentType, page, pageSize)
        PictureUrlMapper.mapPictureUrl(designResponse.data)
        return@withContext designResponse
    }

    suspend fun getIMBrokerProperties(): DesignResponse<String> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getIMBrokerProperties, thread:${Thread.currentThread()}")
        return@withContext appSetsApi.getIMBrokerProperties()
    }

    companion object {

        private const val TAG = "AppSetsRepository"

        private var INSTANCE: AppSetsRepository? = null

        fun getInstance(): AppSetsRepository {
            if (INSTANCE == null) {
                val api = ApiProvider.provide(AppSetsApiProvider::class.java)
                val appSetsRepository = AppSetsRepository(api)
                INSTANCE = appSetsRepository
            }

            return INSTANCE!!
        }
    }
}