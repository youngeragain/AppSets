package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.starter.server.requestNotNullRaw
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.ui.model.CreateApplicationState
import xcj.app.appsets.ui.model.DownloadInfoForCreate
import xcj.app.appsets.ui.model.PlatformForCreate
import xcj.app.appsets.ui.model.ScreenshotInfoForCreate
import xcj.app.appsets.ui.model.VersionInfoForCreate
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.dynamic.IComposeDispose
import xcj.app.starter.android.ktx.startWithHttpSchema
import xcj.app.starter.android.util.PurpleLogger

class AppCreationUseCase(
    private val coroutineScope: CoroutineScope,
    private val appSetsRepository: AppSetsRepository
) : IComposeDispose {
    companion object {
        private const val TAG = "AppCreationUseCase"
    }

    private var chooseContentTempAny: Any? = null
    private var chooseContentTempFiledName: String? = null

    val createApplicationState: MutableState<CreateApplicationState> =
        mutableStateOf(CreateApplicationState.NewApplication())

    fun inflateApplication(application: Application?) {
        val state = if (application == null) {
            CreateApplicationState.NewApplication(ApplicationForCreate())
        } else {
            val applicationForCreate =
                ApplicationForCreate.inflateFromApplication(application, null)
            CreateApplicationState.NewApplication(applicationForCreate)
        }
        createApplicationState.value = state
    }

    override fun onComposeDispose(by: String?) {
        chooseContentTempAny = null
        chooseContentTempFiledName = null
        createApplicationState.value = CreateApplicationState.NewApplication()
    }

    fun finishCreateApp(context: Context) {
        val createApplicationState = this@AppCreationUseCase.createApplicationState.value
        if (createApplicationState is CreateApplicationState.Creating) {
            context.getString(xcj.app.appsets.R.string.creating_application_please_wait).toast()
            return
        }
        val newApplicationState =
            (this@AppCreationUseCase.createApplicationState.value as? CreateApplicationState.NewApplication)
                ?: return
        val applicationForCreate = newApplicationState.applicationForCreate
        if (!checkAppIntegrity(context, applicationForCreate)) {
            return
        }
        this@AppCreationUseCase.createApplicationState.value =
            CreateApplicationState.Creating(applicationForCreate)
        coroutineScope.launch {
            requestNotNullRaw(
                action = {
                    val createApplicationPreCheckRes =
                        appSetsRepository
                            .createApplicationPreCheck(applicationForCreate.name)
                    if (createApplicationPreCheckRes.data != true) {
                        this@AppCreationUseCase.createApplicationState.value =
                            CreateApplicationState.CreateFailed(
                                applicationForCreate,
                                xcj.app.appsets.R.string.please_use_another_application_name
                            )
                        delay(2000)
                        this@AppCreationUseCase.createApplicationState.value =
                            CreateApplicationState.NewApplication(applicationForCreate)
                        return@requestNotNullRaw
                    }
                    val createApplicationRes =
                        appSetsRepository.createApplication(context, applicationForCreate)
                    if (createApplicationRes.data != true) {
                        this@AppCreationUseCase.createApplicationState.value =
                            CreateApplicationState.CreateFailed(
                                applicationForCreate,
                                xcj.app.appsets.R.string.create_application_failed
                            )
                        delay(2000)
                        this@AppCreationUseCase.createApplicationState.value =
                            CreateApplicationState.NewApplication(applicationForCreate)
                        return@requestNotNullRaw
                    }
                    this@AppCreationUseCase.createApplicationState.value =
                        CreateApplicationState.CreateSuccess(applicationForCreate)
                },
                onFailed = {
                    PurpleLogger.current.d(
                        "CreateApplicationUseCase",
                        "finishCreateApp failed:${it}"
                    )
                    this@AppCreationUseCase.createApplicationState.value =
                        CreateApplicationState.CreateFailed(
                            applicationForCreate,
                            xcj.app.appsets.R.string.create_application_failed
                        )
                    delay(2000)
                    this@AppCreationUseCase.createApplicationState.value =
                        CreateApplicationState.NewApplication(applicationForCreate)
                })
        }
    }

    private fun checkAppIntegrity(
        context: Context,
        applicationForCreate: ApplicationForCreate
    ): Boolean {
        val tempApp = applicationForCreate
        if (tempApp.iconUriHolder == null) {
            String.format(
                context.getString(xcj.app.appsets.R.string.please_input_a_template),
                context.getString(xcj.app.appsets.R.string.icon)
            ).toast()
            return false
        }
        if (tempApp.bannerUriHolder == null) {
            String.format(
                context.getString(xcj.app.appsets.R.string.please_choose_a_template),
                "Banner"
            ).toast()
            return false
        }
        if (tempApp.name.isEmpty()) {
            String.format(
                context.getString(xcj.app.appsets.R.string.please_input_a_template),
                context.getString(xcj.app.appsets.R.string.app_name)
            ).toast()
            return false
        }
        if (tempApp.category.isEmpty()) {
            String.format(
                context.getString(xcj.app.appsets.R.string.please_input_a_template),
                context.getString(xcj.app.appsets.R.string.app_types)
            ).toast()
            return false
        }
        if (tempApp.website.isNotEmpty()) {
            if (!tempApp.website.startWithHttpSchema()) {
                String.format(
                    context.getString(xcj.app.appsets.R.string.please_input_a_template),
                    context.getString(xcj.app.appsets.R.string.website)
                ).toast()
                return false
            }
        }
        tempApp.platformForCreates.forEach { platformForCreate ->
            if (platformForCreate.packageName.isEmpty()) {
                "请添加${platformForCreate.name}的应用包名".toast()
                return false
            }
            if (platformForCreate.introduction.isEmpty()) {
                "请添加${platformForCreate.name}的介绍".toast()
                return false
            }
            platformForCreate.versionInfoForCreates.forEach { versionInfoForCreate ->
                if (versionInfoForCreate.version.isEmpty()) {
                    "请输入${platformForCreate}平台的版本".toast()
                    return false
                }
                if (versionInfoForCreate.versionCode.isEmpty()) {
                    "请输入${platformForCreate}平台的版本Code".toast()
                    return false
                }
                if (versionInfoForCreate.changes.isEmpty()) {
                    "请输入${platformForCreate}平台${versionInfoForCreate.version}版本的日志".toast()
                    return false
                }
                if (versionInfoForCreate.privacyPolicyUrl.isEmpty()) {
                    "请输入${platformForCreate}平台${versionInfoForCreate.version}版本的隐私链接".toast()
                    return false
                }
                if (versionInfoForCreate.versionIconUriHolder == null) {
                    "请选择${platformForCreate}平台${versionInfoForCreate.version}版本的图标".toast()
                    return false
                }
                if (versionInfoForCreate.versionBannerUriHolder == null) {
                    "请选择${platformForCreate}平台${versionInfoForCreate.version}版本的Banner".toast()
                    return false
                }
                versionInfoForCreate.downloadInfoForCreates.forEach { downloadInfoForCreate ->
                    if (!downloadInfoForCreate.url.startWithHttpSchema()) {
                        "请输入正确的下载链接".toast()
                        return false
                    }
                }
            }
        }
        return true
    }

    fun setChooseContentTempValues(
        any: Any,
        filedName: String
    ) {
        chooseContentTempAny = any
        chooseContentTempFiledName = filedName
    }

    fun updateSelectPicture(uri: UriProvider) {
        val tempAny = chooseContentTempAny
        val tempFiledName = chooseContentTempFiledName
        if (tempAny != null && tempFiledName != null) {
            onApplicationForCreateFiledChanged(tempAny, tempFiledName, uri)
            chooseContentTempAny = null
            chooseContentTempFiledName = null
        }
    }

    private fun copyApplicationForCreate(applicationForCreate: ApplicationForCreate) {
        PurpleLogger.current.d(
            TAG,
            "copyApplicationForCreate, applicationForCreate:$applicationForCreate"
        )
        createApplicationState.value = CreateApplicationState.NewApplication(applicationForCreate)
    }

    private fun copyPlatformForCreate(
        any: PlatformForCreate,
        platformForCreate: PlatformForCreate
    ) {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return
        PurpleLogger.current.d(TAG, "copyPlatformForCreate, platformForCreate:$platformForCreate")
        val applicationForCreate = newApplicationState.applicationForCreate
        var doCopy = false
        val platformForCreates = applicationForCreate.platformForCreates.toMutableList()
        val indexPlatform = platformForCreates.indexOfFirst {
            it.name == any.name
        }
        if (indexPlatform != -1) {
            platformForCreates.removeAt(indexPlatform)
            platformForCreates.add(indexPlatform, platformForCreate)
            doCopy = true
        }
        if (!doCopy) {
            PurpleLogger.current.d(
                TAG,
                "copyPlatformForCreate, not to copy return."
            )
            return
        }
        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = platformForCreates)
        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)
    }

    private fun copyVersionInfoForCreate(
        any: VersionInfoForCreate,
        versionInfoForCreate: VersionInfoForCreate
    ) {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return
        PurpleLogger.current.d(
            TAG,
            "copyVersionInfoForCreate, versionInfoForCreate:$versionInfoForCreate"
        )
        val applicationForCreate = newApplicationState.applicationForCreate
        var doCopy = false
        val platformForCreates = applicationForCreate.platformForCreates.toMutableList()
        var indexVersion = -1
        val indexPlatform = platformForCreates.indexOfFirst { platformForCreate ->
            val versionInfoForCreates = platformForCreate.versionInfoForCreates
            indexVersion = versionInfoForCreates.indexOfFirst { versionInfoForCreate ->
                versionInfoForCreate.id == any.id
            }
            indexVersion != -1
        }
        if (indexPlatform != -1 && indexVersion != -1) {
            val platformForCreate = platformForCreates[indexPlatform]
            val versionInfoForCreates =
                platformForCreate.versionInfoForCreates.toMutableList().apply {
                    removeAt(indexVersion)
                    add(indexVersion, versionInfoForCreate)
                }
            val newPlatformForCreate =
                platformForCreate.copy(versionInfoForCreates = versionInfoForCreates)
            platformForCreates.removeAt(indexPlatform)
            platformForCreates.add(indexPlatform, newPlatformForCreate)
            doCopy = true
        }
        if (!doCopy) {
            PurpleLogger.current.d(
                TAG,
                "copyVersionInfoForCreate, not to copy return."
            )
            return
        }
        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = platformForCreates)
        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)
    }

    private fun copyScreenshotInfoForCreate(
        any: ScreenshotInfoForCreate,
        screenshotInfoForCreate: ScreenshotInfoForCreate
    ) {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return
        PurpleLogger.current.d(
            TAG,
            "copyScreenshotInfoForCreate, screenshotInfoForCreate:$screenshotInfoForCreate"
        )
        val applicationForCreate = newApplicationState.applicationForCreate
        var doCopy = false
        val platformForCreates = applicationForCreate.platformForCreates.toMutableList()
        var indexScreenshot = -1
        var indexVersion = -1
        val indexPlatform = platformForCreates.indexOfFirst { platformForCreate ->
            val versionInfoForCreates = platformForCreate.versionInfoForCreates
            indexVersion = versionInfoForCreates.indexOfFirst { versionInfoForCreate ->
                val screenshotInfoForCreates =
                    versionInfoForCreate.screenshotInfoForCreates
                indexScreenshot =
                    screenshotInfoForCreates.indexOfFirst { screenshotInfoForCreate1 ->
                        screenshotInfoForCreate1.id == any.id
                    }
                indexScreenshot != -1
            }
            indexVersion != -1
        }
        if (indexPlatform != -1 && indexVersion != -1 && indexScreenshot != -1) {
            val platformForCreate = platformForCreates[indexPlatform]

            val versionInfoForCreates = platformForCreate.versionInfoForCreates.toMutableList()
            val versionInfoForCreate = versionInfoForCreates[indexVersion]
            val screenshotInfoForCreates =
                versionInfoForCreate.screenshotInfoForCreates.toMutableList()
            screenshotInfoForCreates.removeAt(indexScreenshot)
            screenshotInfoForCreates.add(indexScreenshot, screenshotInfoForCreate)

            val newVersionInfoForCreate =
                versionInfoForCreate.copy(screenshotInfoForCreates = screenshotInfoForCreates)

            versionInfoForCreates.removeAt(indexVersion)
            versionInfoForCreates.add(indexVersion, newVersionInfoForCreate)

            val newPlatformForCreate =
                platformForCreate.copy(versionInfoForCreates = versionInfoForCreates)
            platformForCreates.removeAt(indexPlatform)
            platformForCreates.add(indexPlatform, newPlatformForCreate)
            doCopy = true
        }
        if (!doCopy) {
            PurpleLogger.current.d(
                TAG,
                "copyScreenshotInfoForCreate, not to copy return."
            )
            return
        }
        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = platformForCreates)
        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)
    }

    private fun copyDownloadInfoForCreate(
        any: DownloadInfoForCreate,
        downloadInfoForCreate: DownloadInfoForCreate
    ) {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return
        PurpleLogger.current.d(
            TAG,
            "copyDownloadInfoForCreate, downloadInfoForCreate:$downloadInfoForCreate"
        )
        val applicationForCreate = newApplicationState.applicationForCreate
        var doCopy = false
        val platformForCreates = applicationForCreate.platformForCreates.toMutableList()
        var indexDownload = -1
        var indexVersion = -1
        val indexPlatform = platformForCreates.indexOfFirst { platformForCreate ->
            val versionInfoForCreates = platformForCreate.versionInfoForCreates
            indexVersion = versionInfoForCreates.indexOfFirst { versionInfoForCreate ->
                val downloadInfoForCreates = versionInfoForCreate.downloadInfoForCreates

                indexDownload =
                    downloadInfoForCreates.indexOfFirst { downloadInfoForCreate1 ->
                        downloadInfoForCreate1.id == any.id
                    }
                indexDownload != -1
            }
            indexVersion != -1
        }
        if (indexPlatform != -1 && indexVersion != -1 && indexDownload != -1) {
            val platformForCreate = platformForCreates[indexPlatform]

            val versionInfoForCreates = platformForCreate.versionInfoForCreates.toMutableList()
            val versionInfoForCreate = versionInfoForCreates[indexVersion]
            val downloadInfoForCreates = versionInfoForCreate.downloadInfoForCreates.toMutableList()
            downloadInfoForCreates.removeAt(indexDownload)
            downloadInfoForCreates.add(indexDownload, downloadInfoForCreate)

            val newVersionInfoForCreate =
                versionInfoForCreate.copy(downloadInfoForCreates = downloadInfoForCreates)

            versionInfoForCreates.removeAt(indexVersion)
            versionInfoForCreates.add(indexVersion, newVersionInfoForCreate)

            val newPlatformForCreate =
                platformForCreate.copy(versionInfoForCreates = versionInfoForCreates)
            platformForCreates.removeAt(indexPlatform)
            platformForCreates.add(indexPlatform, newPlatformForCreate)
            doCopy = true
        }
        if (!doCopy) {
            PurpleLogger.current.d(
                TAG,
                "copyDownloadInfoForCreate, not to copy return."
            )
            return
        }
        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = platformForCreates)
        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)
    }

    fun onApplicationForCreateFiledChanged(any: Any, filedName: String, filedValue: Any) {
        PurpleLogger.current.d(
            TAG,
            "onApplicationForCreateFiledChanged, any:${any}, filedName:$filedName, filedValue:${filedValue}"
        )
        when (any) {
            is ApplicationForCreate -> {
                when (filedName) {
                    ApplicationForCreate.FILED_NAME_ICON_URI_HOLDER -> {
                        copyApplicationForCreate(any.copy(iconUriHolder = filedValue as? UriProvider))
                    }

                    ApplicationForCreate.FILED_NAME_BANNER_URI_HOLDER -> {
                        copyApplicationForCreate(any.copy(bannerUriHolder = filedValue as? UriProvider))
                    }

                    ApplicationForCreate.FILED_NAME_NAME -> {
                        copyApplicationForCreate(any.copy(name = (filedValue as? String) ?: ""))
                    }

                    ApplicationForCreate.FILED_NAME_CATEGORY -> {
                        copyApplicationForCreate(any.copy(category = (filedValue as? String) ?: ""))
                    }

                    ApplicationForCreate.FILED_NAME_WEBSITE -> {
                        copyApplicationForCreate(any.copy(website = (filedValue as? String) ?: ""))
                    }

                    ApplicationForCreate.FILED_NAME_DEVELOPER_INFO -> {
                        copyApplicationForCreate(
                            any.copy(
                                developerInfo = (filedValue as? String) ?: ""
                            )
                        )
                    }

                    ApplicationForCreate.FILED_NAME_PRICE -> {
                        copyApplicationForCreate(any.copy(price = (filedValue as? String) ?: ""))
                    }

                    ApplicationForCreate.FILED_NAME_PRICE_UNIT -> {
                        copyApplicationForCreate(
                            any.copy(
                                priceUnit = (filedValue as? String) ?: ""
                            )
                        )
                    }
                }
            }

            is PlatformForCreate -> {
                when (filedName) {
                    PlatformForCreate.FILED_NAME_PACKAGE -> {
                        copyPlatformForCreate(
                            any,
                            any.copy(packageName = (filedValue as? String) ?: "")
                        )
                    }

                    PlatformForCreate.FILED_NAME_INTRODUCTION -> {
                        copyPlatformForCreate(
                            any,
                            any.copy(introduction = (filedValue as? String) ?: "")
                        )
                    }
                }
            }

            is VersionInfoForCreate -> {
                when (filedName) {
                    VersionInfoForCreate.FILED_NAME_VERSION -> {
                        copyVersionInfoForCreate(
                            any,
                            any.copy(version = (filedValue as? String) ?: "")
                        )
                    }

                    VersionInfoForCreate.FILED_NAME_VERSION_CODE -> {
                        copyVersionInfoForCreate(
                            any,
                            any.copy(versionCode = (filedValue as? String) ?: "")
                        )
                    }

                    VersionInfoForCreate.FILED_NAME_CHANGES -> {
                        copyVersionInfoForCreate(
                            any,
                            any.copy(changes = (filedValue as? String) ?: "")
                        )
                    }

                    VersionInfoForCreate.FILED_NAME_ICON_URI_HOLDER -> {
                        copyVersionInfoForCreate(
                            any,
                            any.copy(versionIconUriHolder = (filedValue as? UriProvider))
                        )
                    }

                    VersionInfoForCreate.FILED_NAME_BANNER_URI_HOLDER -> {
                        copyVersionInfoForCreate(
                            any,
                            any.copy(versionBannerUriHolder = (filedValue as? UriProvider))
                        )
                    }

                    VersionInfoForCreate.FILED_NAME_PACKAGE_SIZE -> {
                        copyVersionInfoForCreate(
                            any,
                            any.copy(packageSize = (filedValue as? String) ?: "")
                        )
                    }

                    VersionInfoForCreate.FILED_NAME_PRIVACY_POLICY_URL -> {
                        copyVersionInfoForCreate(
                            any,
                            any.copy(privacyPolicyUrl = (filedValue as? String) ?: "")
                        )
                    }
                }
            }

            is ScreenshotInfoForCreate -> {
                when (filedName) {
                    ScreenshotInfoForCreate.FILED_NAME_URI_HOLDER -> {
                        copyScreenshotInfoForCreate(
                            any,
                            any.copy(uriHolder = filedValue as? UriProvider)
                        )
                    }

                    ScreenshotInfoForCreate.FILED_NAME_TYPE -> {
                        copyScreenshotInfoForCreate(any, any.copy(type = filedValue as? String))
                    }
                }
            }

            is DownloadInfoForCreate -> {
                when (filedName) {
                    DownloadInfoForCreate.FILED_NAME_URL -> {
                        copyDownloadInfoForCreate(
                            any,
                            any.copy(url = (filedValue as? String) ?: "")
                        )
                    }
                }
            }
        }
    }

    fun deleteVersionInPlatform(
        platformForCreate: PlatformForCreate,
        versionInfoForCreate: VersionInfoForCreate
    ) {

        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return
        val applicationForCreate = newApplicationState.applicationForCreate

        val newVersionInfoForCreates =
            platformForCreate.versionInfoForCreates.toMutableList().apply {
                remove(versionInfoForCreate)
            }
        val newPlatformForCreate =
            platformForCreate.copy(versionInfoForCreates = newVersionInfoForCreates)
        val newPlatformForCreates = applicationForCreate.platformForCreates.toMutableList().apply {
            val indexPlatform = indexOfFirst { it.id == platformForCreate.id }
            if (indexPlatform != -1) {
                removeAt(indexPlatform)
                add(indexPlatform, newPlatformForCreate)
            } else {
                add(newPlatformForCreate)
            }
        }

        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = newPlatformForCreates)

        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)
    }

    fun deleteDownloadInfoInVersion(
        platformForCreate: PlatformForCreate,
        versionInfoForCreate: VersionInfoForCreate,
        downloadInfoForCreate: DownloadInfoForCreate
    ) {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return
        val applicationForCreate = newApplicationState.applicationForCreate

        val newDownloadInfoForCreates =
            versionInfoForCreate.downloadInfoForCreates.toMutableList().apply {
                remove(downloadInfoForCreate)
            }
        val newVersionInfoForCreate =
            versionInfoForCreate.copy(downloadInfoForCreates = newDownloadInfoForCreates)

        val newVersionInfoList = platformForCreate.versionInfoForCreates.toMutableList().apply {
            val indexVersion = indexOfFirst { it.id == versionInfoForCreate.id }
            if (indexVersion != -1) {
                removeAt(indexVersion)
                add(indexVersion, newVersionInfoForCreate)
            } else {
                add(newVersionInfoForCreate)
            }

        }
        val newPlatformForCreate =
            platformForCreate.copy(versionInfoForCreates = newVersionInfoList)
        val newPlatformForCreates = applicationForCreate.platformForCreates.toMutableList().apply {
            val indexPlatform = indexOfFirst { it.id == platformForCreate.id }
            if (indexPlatform != -1) {
                removeAt(indexPlatform)
                add(indexPlatform, newPlatformForCreate)
            } else {
                add(newPlatformForCreate)
            }
        }

        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = newPlatformForCreates)

        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)
    }

    fun deleteScreenInfoInVersion(
        platformForCreate: PlatformForCreate,
        versionInfoForCreate: VersionInfoForCreate,
        screenshotInfoForCreate: ScreenshotInfoForCreate
    ) {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return
        val applicationForCreate = newApplicationState.applicationForCreate

        val newScreenshotInfoForCreates =
            versionInfoForCreate.screenshotInfoForCreates.toMutableList().apply {
                remove(screenshotInfoForCreate)
            }
        val newVersionInfoForCreate =
            versionInfoForCreate.copy(screenshotInfoForCreates = newScreenshotInfoForCreates)

        val newVersionInfoList = platformForCreate.versionInfoForCreates.toMutableList().apply {
            val indexVersion = indexOfFirst { it.id == versionInfoForCreate.id }
            if (indexVersion != -1) {
                removeAt(indexVersion)
                add(indexVersion, newVersionInfoForCreate)
            } else {
                add(newVersionInfoForCreate)
            }
        }
        val newPlatformForCreate =
            platformForCreate.copy(versionInfoForCreates = newVersionInfoList)
        val newPlatformForCreates = applicationForCreate.platformForCreates.toMutableList().apply {
            val indexPlatform = indexOfFirst { it.id == platformForCreate.id }
            if (indexPlatform != -1) {
                removeAt(indexPlatform)
                add(indexPlatform, newPlatformForCreate)
            } else {
                add(newPlatformForCreate)
            }
        }

        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = newPlatformForCreates)

        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)
    }

    fun getPlatformForCreateByName(platformName: String?): PlatformForCreate? {
        if (platformName.isNullOrEmpty()) {
            return getLastPlatformForCreateOrNull()
        }
        return getOrCreatePlatformIfNeeded(platformName)
    }

    private fun getOrCreatePlatformIfNeeded(platformName: String): PlatformForCreate {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication)
                ?: throw IllegalStateException()

        val applicationForCreate = newApplicationState.applicationForCreate

        val platformForCreates = applicationForCreate.platformForCreates
        var platformForCreate = platformForCreates.firstOrNull { it.name == platformName }
        if (platformForCreate != null) {
            PurpleLogger.current.d(TAG, "getOrCreatePlatformIfNeeded, exist:${platformForCreate}")
            return platformForCreate
        }

        val newPlatformForCreates = platformForCreates.toMutableList()
        platformForCreate = PlatformForCreate(name = platformName)
        newPlatformForCreates.add(platformForCreate)

        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = newPlatformForCreates)
        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)
        PurpleLogger.current.d(TAG, "getOrCreatePlatformIfNeeded, new:${platformForCreate}")
        return platformForCreate
    }

    fun removePlatformForCreateByName(platformName: String) {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return

        val applicationForCreate = newApplicationState.applicationForCreate

        val newPlatformForCreates = applicationForCreate.platformForCreates.toMutableList().apply {
            removeIf { it.name == platformName }
        }

        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = newPlatformForCreates)
        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)

    }

    fun getLastPlatformForCreateOrNull(): PlatformForCreate? {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return null
        val applicationForCreate = newApplicationState.applicationForCreate
        val platformForCreate = applicationForCreate.platformForCreates.lastOrNull()
        PurpleLogger.current.d(
            TAG,
            "getLastPlatformForCreateOrNull, platformForCreate:${platformForCreate}"
        )
        return platformForCreate
    }

    fun getPlatformForCreateById(platformId: String): PlatformForCreate? {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return null
        val applicationForCreate = newApplicationState.applicationForCreate
        val platformForCreate =
            applicationForCreate.platformForCreates.firstOrNull { it.id == platformId }
        PurpleLogger.current.d(
            TAG,
            "getPlatformForCreateById:${platformForCreate}"
        )
        return platformForCreate
    }

    fun getVersionInfoForCreateById(
        platformForCreate: PlatformForCreate,
        versionInfoId: String
    ): VersionInfoForCreate? {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return null
        val applicationForCreate = newApplicationState.applicationForCreate
        return applicationForCreate.platformForCreates.flatMap {
            it.versionInfoForCreates
        }.firstOrNull {
            it.id == versionInfoId
        }
    }

    fun addVersionInfoForCreate(platformForCreate: PlatformForCreate) {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication) ?: return
        val applicationForCreate = newApplicationState.applicationForCreate
        val versionInfoForCreate = VersionInfoForCreate()
        val versionInfoForCreates = platformForCreate.versionInfoForCreates.toMutableList().apply {
            add(versionInfoForCreate)
        }
        val newPlatformForCreate =
            platformForCreate.copy(versionInfoForCreates = versionInfoForCreates)
        val newPlatformForCreates = applicationForCreate.platformForCreates.toMutableList().apply {
            val indexPlatform = indexOfFirst { it.id == platformForCreate.id }
            if (indexPlatform != -1) {
                removeAt(indexPlatform)
                add(indexPlatform, newPlatformForCreate)
            } else {
                add(newPlatformForCreate)
            }
        }
        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = newPlatformForCreates)
        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)
    }

    fun addScreenshotForCreate(
        platformForCreate: PlatformForCreate,
        versionInfoForCreate: VersionInfoForCreate
    ): ScreenshotInfoForCreate {
        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication)
                ?: throw IllegalStateException()
        val applicationForCreate = newApplicationState.applicationForCreate

        val screenshotInfoForCreate = ScreenshotInfoForCreate()

        val screenshotInfoForCreates =
            versionInfoForCreate.screenshotInfoForCreates.toMutableList().apply {
                add(screenshotInfoForCreate)
            }

        val newVersionInfoForCreate =
            versionInfoForCreate.copy(screenshotInfoForCreates = screenshotInfoForCreates)

        val versionInfoForCreates = platformForCreate.versionInfoForCreates.toMutableList().apply {
            val indexVersion = indexOfFirst { it.id == versionInfoForCreate.id }
            if (indexVersion != -1) {
                removeAt(indexVersion)
                add(indexVersion, newVersionInfoForCreate)
            } else {
                add(newVersionInfoForCreate)
            }
        }
        val newPlatformForCreate =
            platformForCreate.copy(versionInfoForCreates = versionInfoForCreates)
        val newPlatformForCreates = applicationForCreate.platformForCreates.toMutableList().apply {
            val indexPlatform = indexOfFirst { it.id == platformForCreate.id }
            if (indexPlatform != -1) {
                removeAt(indexPlatform)
                add(indexPlatform, newPlatformForCreate)
            } else {
                add(newPlatformForCreate)
            }
        }
        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = newPlatformForCreates)
        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)

        return screenshotInfoForCreate
    }

    fun addDownloadInfoForCreate(
        platformForCreate: PlatformForCreate,
        versionInfoForCreate: VersionInfoForCreate
    ): DownloadInfoForCreate {

        val newApplicationState =
            (createApplicationState.value as? CreateApplicationState.NewApplication)
                ?: throw IllegalStateException()
        val applicationForCreate = newApplicationState.applicationForCreate

        val downloadInfoForCreate = DownloadInfoForCreate()

        val downloadInfoForCreates =
            versionInfoForCreate.downloadInfoForCreates.toMutableList().apply {
                add(downloadInfoForCreate)
            }

        val newVersionInfoForCreate =
            versionInfoForCreate.copy(downloadInfoForCreates = downloadInfoForCreates)

        val versionInfoForCreates = platformForCreate.versionInfoForCreates.toMutableList().apply {
            val indexVersion = indexOfFirst { it.id == versionInfoForCreate.id }
            if (indexVersion != -1) {
                removeAt(indexVersion)
                add(indexVersion, newVersionInfoForCreate)
            } else {
                add(newVersionInfoForCreate)
            }
        }
        val newPlatformForCreate =
            platformForCreate.copy(versionInfoForCreates = versionInfoForCreates)
        val newPlatformForCreates = applicationForCreate.platformForCreates.toMutableList().apply {
            val indexPlatform = indexOfFirst { it.id == platformForCreate.id }
            if (indexPlatform != -1) {
                removeAt(indexPlatform)
                add(indexPlatform, newPlatformForCreate)
            } else {
                add(newPlatformForCreate)
            }
        }
        val newApplicationForCreate =
            applicationForCreate.copy(platformForCreates = newPlatformForCreates)
        createApplicationState.value =
            newApplicationState.copy(applicationForCreate = newApplicationForCreate)

        return downloadInfoForCreate
    }
}