package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.AppsWithCategory
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.ui.model.page_state.AppCenterPageUIState
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.io.components.ObjectUploadOptions
import xcj.app.io.compress.ICompressor
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.request
import java.util.UUID
import kotlin.math.abs

class AppsUseCase(
    private val appSetsRepository: AppSetsRepository,
) : ComposeLifecycleAware {

    companion object {
        private const val TAG = "AppsUseCase"
        val appsContentObjectUploadOptions: ObjectUploadOptions
            get() = object : ObjectUploadOptions {
                override fun getInfixPath(): String {
                    return "apps/"
                }

                override fun compressOptions(): ICompressor.CompressOptions {
                    return object : ICompressor.CompressOptions {
                        override fun imageCompressQuality(): Int {
                            return 55
                        }
                    }
                }
            }

        fun createMockApplications(count: Int): List<AppsWithCategory> {
            val result = mutableListOf<AppsWithCategory>()
            val applications = mutableListOf<Application>()
            repeat(count) {
                val application = Application(appId = UUID.randomUUID().toString())
                applications.add(application)
            }
            val appsWithCategory = AppsWithCategory("", "", applications)
            result.add(appsWithCategory)
            return result
        }
    }

    val appCenterPageUIState: MutableState<AppCenterPageUIState> =
        mutableStateOf(AppCenterPageUIState.Loading(createMockApplications(32)))

    suspend fun loadHomeApplications() {
        PurpleLogger.current.d(TAG, "loadHomeApplications")
        val centerPageState = appCenterPageUIState.value
        if (centerPageState is AppCenterPageUIState.LoadSuccess) {
            return
        }
        request {
            appSetsRepository.getIndexApplications()
        }.onSuccess { loadedCategoryApps ->
            val loadingState = centerPageState as AppCenterPageUIState.Loading
            val loadingCategoryApps = loadingState.apps
            val loadingAppsCount =
                loadingCategoryApps.sumOf { apps ->
                    apps.applications.size
                }
            val loadedAppsCount = loadedCategoryApps.sumOf { apps ->
                apps.applications.size
            }
            val diff =
                loadingAppsCount - loadedAppsCount
            if (abs(diff) != 0) {
                repeat(abs(diff)) { i ->
                    if (diff < 0) {
                        val mockApplications = createMockApplications(loadingAppsCount + i + 1)
                        this@AppsUseCase.appCenterPageUIState.value =
                            AppCenterPageUIState.Loading(mockApplications)
                    } else {
                        val mockApplications = createMockApplications(loadingAppsCount - (i + 1))
                        this@AppsUseCase.appCenterPageUIState.value =
                            AppCenterPageUIState.Loading(mockApplications)
                    }
                    delay(10)
                }
            }
            this@AppsUseCase.appCenterPageUIState.value =
                AppCenterPageUIState.LoadSuccess(loadedCategoryApps)
        }
    }

    fun findApplicationByBioId(application: Application): Application? {
        PurpleLogger.current.d(TAG, "findApplicationByBioId, bioId:${application.bioId}")
        val centerState = appCenterPageUIState.value
        if (centerState !is AppCenterPageUIState.LoadSuccess) {
            return null
        }
        centerState.apps.forEach {
            for (app in it.applications) {
                if (app.bioId == application.bioId) {
                    PurpleLogger.current.d(TAG, "findApplicationByBioId, found:$application")
                    return application
                }
            }
        }
        PurpleLogger.current.d(TAG, "findApplicationByBioId, not found")
        return null
    }

    fun findApplicationById(application: Application): Application? {
        PurpleLogger.current.d(TAG, "findApplicationById, applicationId:${application.appId}")
        val centerState = appCenterPageUIState.value
        if (centerState !is AppCenterPageUIState.LoadSuccess) {
            return null
        }
        centerState.apps.forEach {
            for (app in it.applications) {
                if (app.appId == application.appId) {
                    PurpleLogger.current.d(TAG, "findApplicationById, found:$application")
                    return application
                }
            }
        }
        PurpleLogger.current.d(TAG, "findApplicationById, not found")
        return null
    }

    override fun onComposeDispose(by: String?) {

    }
}