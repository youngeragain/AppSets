package xcj.app.appsets.server.repository

import android.content.Context
import android.net.Uri
import xcj.app.appsets.BuildConfig
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.ktx.isHttpUrl
import xcj.app.appsets.server.api.AppSetsApi
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.model.AppsWithCategory
import xcj.app.appsets.server.model.SpotLight
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.usecase.models.Application
import xcj.app.appsets.usecase.models.ApplicationForCreate
import xcj.app.core.foundation.http.DesignResponse
import xcj.app.core.test.AbsSingleInstance
import xcj.app.io.components.SimpleFileIO
import java.util.UUID

class AppSetsRepository private constructor(
    private val appSetsApi: AppSetsApi
) : AbsSingleInstance() {

    private lateinit var appToken: String
    fun provideAppToken(): String? {
        if (!::appToken.isInitialized)
            return null
        return appToken
    }

    suspend fun getAppToken(): DesignResponse<String?> {
        if (::appToken.isInitialized)
            return DesignResponse(data = appToken)
        if (BuildConfig.AppSetsAppId.isEmpty())
            throw Exception("BuildConfig.AppSetsAppId.isNullOrEmpty()")
        val appTokenRes = appSetsApi.getAppToken(mapOf("appSetsAppId" to BuildConfig.AppSetsAppId))
        if (appTokenRes.success && !appTokenRes.data.isNullOrEmpty())
            appToken = appTokenRes.data!!
        return appTokenRes
    }

    suspend fun getIndexApplications(): DesignResponse<List<AppsWithCategory>?> {
        if (!::appToken.isInitialized)
            throw Exception("appToken 异常!")
        return appSetsApi.getIndexApplications()
    }

    suspend fun getSpotLight(): DesignResponse<SpotLight?> {
        if (!::appToken.isInitialized)
            throw Exception("appToken 异常!")
        return appSetsApi.getSpotLight()
    }

    suspend fun checkUpdate(versionCode: Int): DesignResponse<UpdateCheckResult?> {
        return appSetsApi.checkUpdate(versionCode, "android")
    }

    suspend fun getUpdateHistory(): DesignResponse<List<UpdateCheckResult>?> {
        return appSetsApi.getUpdateHistory(null, "android")
    }

    suspend fun createApplicationPreCheck(appName: String): DesignResponse<Boolean> {
        return appSetsApi.createApplicationPreCheck(appName)
    }

    suspend fun createApplication(
        context: Context,
        applicationForCreate: ApplicationForCreate
    ): DesignResponse<Boolean> {
        val uid = LocalAccountManager._userInfo.value.uid
        val currentTimeMills = System.currentTimeMillis().toString()
        val app = hashMapOf<String, Any?>()
        val uploadUrlMarkerList = mutableListOf<String>()
        val uploadUriList = mutableListOf<Uri>()

        val appIconUrlMarker = UUID.randomUUID().toString()
        val appIconUri = applicationForCreate.iconUriHolderState.value!!.provideUri()!!
        val appBannerUrlMarker = UUID.randomUUID().toString()
        val appBannerUri = applicationForCreate.bannerUriHolderState.value!!.provideUri()!!
        uploadUrlMarkerList.add(appIconUrlMarker)
        uploadUriList.add(appIconUri)
        uploadUrlMarkerList.add(appBannerUrlMarker)
        uploadUriList.add(appBannerUri)
        with(app) {
            put("icon_url", appIconUrlMarker)
            put("banner_url", appBannerUrlMarker)
            put("name", applicationForCreate.name.value)
            put("category", applicationForCreate.category.value)
            put("website", applicationForCreate.website.value)
            put("developer_info", applicationForCreate.developerInfo.value)
            put("create_uid", uid)
            put("update_uid", uid)
            put("create_time", currentTimeMills)
            put("update_time", currentTimeMills)
            put("app_id", UUID.randomUUID().toString())
        }
        if (applicationForCreate.platformForCreates.isNotEmpty()) {
            val platforms = mutableListOf<HashMap<String, Any?>>()
            app.put("platforms", platforms)
            applicationForCreate.platformForCreates.forEach { platformForCreate ->
                val platform = hashMapOf<String, Any?>()
                platforms.add(platform)
                with(platform) {
                    put("id", UUID.randomUUID().toString())
                    put("name", platformForCreate.name)
                    put("package_name", platformForCreate.packageName.value)
                    put("introduction", platformForCreate.introduction.value)
                }
                if (platformForCreate.versionInfoForCreates.isNotEmpty()) {
                    val versionInfos = mutableListOf<HashMap<String, Any?>>()
                    platform.put("version_infos", versionInfos)
                    platformForCreate.versionInfoForCreates.forEach { versionInfoForCreate ->
                        val appVersionIconUrlMarker = UUID.randomUUID().toString()
                        val appVersionIconUri =
                            versionInfoForCreate.versionIconUriHolderState.value!!.provideUri()!!
                        uploadUrlMarkerList.add(appVersionIconUrlMarker)
                        uploadUriList.add(appVersionIconUri)

                        val appVersionBannerUrlMarker = UUID.randomUUID().toString()
                        val appVersionBannerUri =
                            versionInfoForCreate.versionBannerUriHolderState.value!!.provideUri()!!
                        uploadUrlMarkerList.add(appVersionBannerUrlMarker)
                        uploadUriList.add(appVersionBannerUri)

                        val versionInfo = hashMapOf<String, Any?>()
                        versionInfos.add(versionInfo)
                        with(versionInfo) {
                            put("version_icon_url", appVersionIconUrlMarker)
                            put("version_banner_url", appVersionIconUrlMarker)
                            put("version", versionInfoForCreate.version.value)
                            put("version_code", versionInfoForCreate.versionCode.value)
                            put("changes", versionInfoForCreate.changes.value)
                            put("package_size", versionInfoForCreate.packageSize.value)
                            put("privacy_url", versionInfoForCreate.privacyPolicyUrl.value)
                            put("create_time", currentTimeMills)
                            put("update_time", currentTimeMills)
                            put("create_uid", uid)
                            put("id", UUID.randomUUID().toString())
                        }
                        if (versionInfoForCreate.screenshotInfoForCreates.isNotEmpty()) {
                            val screenShotInfos = mutableListOf<HashMap<String, Any?>>()
                            versionInfo.put("screenshot_infos", screenShotInfos)
                            versionInfoForCreate.screenshotInfoForCreates.forEach { screenshotInfoForCreate ->
                                val screenshotUrlMarker = UUID.randomUUID().toString()
                                val screenshotUri =
                                    screenshotInfoForCreate.uriHolderState.value!!.provideUri()!!
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
                                    put("id", UUID.randomUUID().toString())
                                    put("create_time", currentTimeMills)
                                    put("update_time", currentTimeMills)
                                }
                            }
                        }
                        if (versionInfoForCreate.downloadInfoForCreates.isNotEmpty()) {
                            val downloadInfos = mutableListOf<HashMap<String, Any?>>()
                            versionInfo.put("download_infos", downloadInfos)
                            versionInfoForCreate.downloadInfoForCreates.forEach { downinfoForCreate ->
                                val downloadInfo = hashMapOf<String, Any?>()
                                downloadInfos.add(downloadInfo)
                                with(downloadInfo) {
                                    put("create_uid", uid)
                                    put("update_uid", uid)
                                    put("create_time", currentTimeMills)
                                    put("update_time", currentTimeMills)
                                    put("download_times", 0)
                                    put("url", downinfoForCreate.url.value)
                                    put("id", UUID.randomUUID().toString())
                                }
                            }
                        }
                    }
                }
            }
        }
        SimpleFileIO.getInstance().uploadWithMultiUri(context, uploadUriList, uploadUrlMarkerList)
        return appSetsApi.createApplication(app)
    }

    companion object {
        private var INSTANCE: AppSetsRepository? = null
        fun getInstance(): AppSetsRepository {
            return INSTANCE ?: synchronized(this) {
                val appSetsApi = URLApi.provide(AppSetsApi::class.java)
                val appSetsRepository = AppSetsRepository(appSetsApi)
                INSTANCE = appSetsRepository
                appSetsRepository
            }
        }

        fun mapIconUrl(applications: List<Application>?) {
            applications?.forEach { application ->
                if (!application.iconUrl.isNullOrEmpty() && !application.iconUrl.isHttpUrl()) {
                    application.iconUrl =
                        SimpleFileIO.getInstance().generatePreSign(application.iconUrl!!)
                            ?: application.iconUrl
                }
                if (!application.bannerUrl.isNullOrEmpty() && !application.bannerUrl.isHttpUrl()) {
                    application.bannerUrl =
                        SimpleFileIO.getInstance().generatePreSign(application.bannerUrl!!)
                            ?: application.bannerUrl
                }
                application.platforms?.forEach { platform ->
                    platform.versionInfos?.forEach { versionInfo ->
                        if (!versionInfo.versionIconUrl.isNullOrEmpty() && !versionInfo.versionIconUrl.isHttpUrl()) {
                            versionInfo.versionIconUrl =
                                SimpleFileIO.getInstance()
                                    .generatePreSign(versionInfo.versionIconUrl!!)
                                    ?: versionInfo.versionIconUrl
                        }
                        if (!versionInfo.versionBannerUrl.isNullOrEmpty() && !versionInfo.versionBannerUrl.isHttpUrl()) {
                            versionInfo.versionBannerUrl =
                                SimpleFileIO.getInstance()
                                    .generatePreSign(versionInfo.versionBannerUrl!!)
                                    ?: versionInfo.versionBannerUrl
                        }
                        versionInfo.screenshotInfos?.forEach { screenshotInfo ->
                            if (!screenshotInfo.url.isNullOrEmpty() && !screenshotInfo.url.isHttpUrl()) {
                                screenshotInfo.url =
                                    SimpleFileIO.getInstance()
                                        .generatePreSign(screenshotInfo.url!!) ?: screenshotInfo.url
                            }
                        }
                    }
                }
            }
        }
    }
}