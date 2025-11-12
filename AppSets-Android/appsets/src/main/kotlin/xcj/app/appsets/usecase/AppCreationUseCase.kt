package xcj.app.appsets.usecase

import android.content.Context
import androidx.core.content.ContextCompat
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.ui.model.DownloadInfoForCreate
import xcj.app.appsets.ui.model.PlatformForCreate
import xcj.app.appsets.ui.model.ScreenshotInfoForCreate
import xcj.app.appsets.ui.model.VersionInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateApplicationPageUIState
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.SingleStateUpdater
import xcj.app.appsets.util.ktx.toast
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.android.ktx.startWithHttpSchema
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.requestRaw

class AppCreationUseCase(
    private val appSetsRepository: AppSetsRepository,
) : ComposeLifecycleAware {
    companion object {
        private const val TAG = "AppCreationUseCase"
    }

    fun inflateApplication(applicationForCreate: ApplicationForCreate, application: Application?) {
        if (application == null) {
            return
        }
        ApplicationForCreate.inflateFromApplication(applicationForCreate, application)
    }

    override fun onComposeDispose(by: String?) {

    }

    suspend fun finishCreateApp(
        context: Context,
        applicationForCreate: ApplicationForCreate,
        composeStateUpdater: ComposeStateUpdater<CreateApplicationPageUIState>
    ) {
        if (composeStateUpdater !is SingleStateUpdater) {
            return
        }
        val createApplicationState = composeStateUpdater.getStateValue()
        if (createApplicationState !is CreateApplicationPageUIState.CreateStart) {
            return
        }
        if (!checkAppIntegrity(context, applicationForCreate)) {
            return
        }
        composeStateUpdater.update(CreateApplicationPageUIState.Creating())
        requestRaw(
            action = {
                val createApplicationPreCheckRes =
                    appSetsRepository.createApplicationPreCheck(applicationForCreate.name.value)
                if (createApplicationPreCheckRes.data != true) {
                    composeStateUpdater.update(
                        300,
                        CreateApplicationPageUIState.CreateFailed(xcj.app.appsets.R.string.please_use_another_application_name),
                        CreateApplicationPageUIState.CreateStart()
                    )
                    return
                }
                val createApplicationRes =
                    appSetsRepository.createApplication(context, applicationForCreate)
                if (createApplicationRes.data != true) {
                    composeStateUpdater.update(
                        300,
                        CreateApplicationPageUIState.CreateFailed(xcj.app.appsets.R.string.create_application_failed),
                        CreateApplicationPageUIState.CreateStart()
                    )
                    return
                }
                composeStateUpdater.update(CreateApplicationPageUIState.CreateSuccess())
            }).onFailure {
            PurpleLogger.current.d(
                "CreateApplicationUseCase", "finishCreateApp failed:${it}"
            )
            composeStateUpdater.update(
                300,
                CreateApplicationPageUIState.CreateFailed(xcj.app.appsets.R.string.create_application_failed),
                CreateApplicationPageUIState.CreateStart()
            )
        }
    }

    private fun checkAppIntegrity(
        context: Context,
        applicationForCreate: ApplicationForCreate,
    ): Boolean {
        if (applicationForCreate.iconUriProvider.value == null) {
            String.format(
                ContextCompat.getString(context, xcj.app.appsets.R.string.please_input_a_template),
                ContextCompat.getString(context, xcj.app.appsets.R.string.icon)
            ).toast()
            return false
        }
        if (applicationForCreate.bannerUriProvider.value == null) {
            String.format(
                ContextCompat.getString(context, xcj.app.appsets.R.string.please_choose_a_template),
                "Banner"
            ).toast()
            return false
        }
        if (applicationForCreate.name.value.isEmpty()) {
            String.format(
                ContextCompat.getString(context, xcj.app.appsets.R.string.please_input_a_template),
                ContextCompat.getString(context, xcj.app.appsets.R.string.app_name)
            ).toast()
            return false
        }
        if (applicationForCreate.category.value.isEmpty()) {
            String.format(
                ContextCompat.getString(context, xcj.app.appsets.R.string.please_input_a_template),
                ContextCompat.getString(context, xcj.app.appsets.R.string.app_types)
            ).toast()
            return false
        }
        if (applicationForCreate.website.value.isNotEmpty()) {
            if (!applicationForCreate.website.value.startWithHttpSchema()) {
                String.format(
                    ContextCompat.getString(
                        context, xcj.app.appsets.R.string.please_input_a_template
                    ), ContextCompat.getString(context, xcj.app.appsets.R.string.website)
                ).toast()
                return false
            }
        }
        applicationForCreate.platformForCreates.forEach { platformForCreate ->
            if (platformForCreate.packageName.value.isEmpty()) {
                "请添加${platformForCreate.name}的应用包名".toast()
                return false
            }
            if (platformForCreate.introduction.value.isEmpty()) {
                "请添加${platformForCreate.name}的介绍".toast()
                return false
            }
            platformForCreate.versionInfoForCreates.forEach { versionInfoForCreate ->
                if (versionInfoForCreate.version.value.isEmpty()) {
                    "请输入${platformForCreate}平台的版本".toast()
                    return false
                }
                if (versionInfoForCreate.versionCode.value.isEmpty()) {
                    "请输入${platformForCreate}平台的版本Code".toast()
                    return false
                }
                if (versionInfoForCreate.changes.value.isEmpty()) {
                    "请输入${platformForCreate}平台${versionInfoForCreate.version}版本的日志".toast()
                    return false
                }
                if (versionInfoForCreate.privacyPolicyUrl.value.isEmpty()) {
                    "请输入${platformForCreate}平台${versionInfoForCreate.version}版本的隐私链接".toast()
                    return false
                }
                if (versionInfoForCreate.versionIconUriProvider.value == null) {
                    "请选择${platformForCreate}平台${versionInfoForCreate.version}版本的图标".toast()
                    return false
                }
                if (versionInfoForCreate.versionBannerUriProvider.value == null) {
                    "请选择${platformForCreate}平台${versionInfoForCreate.version}版本的Banner".toast()
                    return false
                }
                versionInfoForCreate.downloadInfoForCreates.forEach { downloadInfoForCreate ->
                    if (!downloadInfoForCreate.url.value.startWithHttpSchema()) {
                        "请输入正确的下载链接".toast()
                        return false
                    }
                }
            }
        }
        return true
    }

    fun deleteVersionInPlatform(
        platformForCreate: PlatformForCreate,
        versionInfoForCreate: VersionInfoForCreate,
    ) {
        platformForCreate.versionInfoForCreates.remove(versionInfoForCreate)
    }

    fun deleteDownloadInfoInVersion(
        versionInfoForCreate: VersionInfoForCreate,
        downloadInfoForCreate: DownloadInfoForCreate,
    ) {
        versionInfoForCreate.downloadInfoForCreates.remove(downloadInfoForCreate)
    }

    fun deleteScreenShotInfoInVersion(
        versionInfoForCreate: VersionInfoForCreate,
        screenshotInfoForCreate: ScreenshotInfoForCreate,
    ) {
        versionInfoForCreate.screenshotInfoForCreates.remove(screenshotInfoForCreate)
    }

    fun getPlatformForCreateByName(
        applicationForCreate: ApplicationForCreate,
        platformName: String?
    ): PlatformForCreate? {
        if (platformName.isNullOrEmpty()) {
            return getLastPlatformForCreateOrNull(applicationForCreate)
        }
        return getOrCreatePlatformIfNeeded(applicationForCreate, platformName)
    }

    private fun getOrCreatePlatformIfNeeded(
        applicationForCreate: ApplicationForCreate,
        platformName: String
    ): PlatformForCreate {

        val platformForCreates = applicationForCreate.platformForCreates
        var platformForCreate = platformForCreates.firstOrNull { it.name.value == platformName }
        if (platformForCreate != null) {
            PurpleLogger.current.d(TAG, "getOrCreatePlatformIfNeeded, exist:${platformForCreate}")
            return platformForCreate
        }

        platformForCreate = PlatformForCreate()
        platformForCreate.name.value = platformName
        platformForCreates.add(platformForCreate)
        PurpleLogger.current.d(TAG, "getOrCreatePlatformIfNeeded, new:${platformForCreate}")
        return platformForCreate
    }

    fun removePlatformForCreateByName(
        applicationForCreate: ApplicationForCreate, platformName: String
    ) {
        applicationForCreate.platformForCreates.apply {
            removeIf { it.name.value == platformName }
        }
    }

    fun getLastPlatformForCreateOrNull(
        applicationForCreate: ApplicationForCreate
    ): PlatformForCreate? {
        val platformForCreate = applicationForCreate.platformForCreates.lastOrNull()
        PurpleLogger.current.d(
            TAG, "getLastPlatformForCreateOrNull, platformForCreate:${platformForCreate}"
        )
        return platformForCreate
    }

    fun getPlatformForCreateById(
        applicationForCreate: ApplicationForCreate, platformId: String
    ): PlatformForCreate? {
        val platformForCreate =
            applicationForCreate.platformForCreates.firstOrNull { it.id == platformId }
        PurpleLogger.current.d(
            TAG, "getPlatformForCreateById:${platformForCreate}"
        )
        return platformForCreate
    }

    fun getVersionInfoForCreateById(
        platformForCreate: PlatformForCreate,
        versionInfoId: String,
    ): VersionInfoForCreate? {
        return platformForCreate.versionInfoForCreates.firstOrNull {
            it.id == versionInfoId
        }
    }

    fun addVersionInfoForCreate(platformForCreate: PlatformForCreate) {
        val versionInfoForCreate = VersionInfoForCreate()
        platformForCreate.versionInfoForCreates.add(versionInfoForCreate)
    }

    fun addScreenshotForCreate(
        versionInfoForCreate: VersionInfoForCreate,
    ): ScreenshotInfoForCreate {
        val screenshotInfoForCreate = ScreenshotInfoForCreate()
        versionInfoForCreate.screenshotInfoForCreates.add(screenshotInfoForCreate)
        return screenshotInfoForCreate
    }

    fun addDownloadInfoForCreate(
        versionInfoForCreate: VersionInfoForCreate,
    ) {
        val downloadInfoForCreate = DownloadInfoForCreate()
        versionInfoForCreate.downloadInfoForCreates.add(downloadInfoForCreate)
    }
}