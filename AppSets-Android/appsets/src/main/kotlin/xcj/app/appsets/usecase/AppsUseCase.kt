package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.starter.server.requestNotNull
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.AppsWithCategory
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.ui.model.TipsState
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.io.components.ObjectUploadOptions
import xcj.app.io.compress.ICompressor
import xcj.app.starter.android.util.PurpleLogger
import kotlin.math.abs

sealed interface AppCenterState : TipsState {
    val apps: List<AppsWithCategory>

    data class Loading(
        override val apps: MutableList<AppsWithCategory>,
        override val tips: Int? = null
    ) : AppCenterState

    data class LoadSuccess(
        override val apps: List<AppsWithCategory>,
        override val tips: Int? = null
    ) : AppCenterState
}



class AppsUseCase(
    private val coroutineScope: CoroutineScope,
    private val appSetsRepository: AppSetsRepository,
) : IComposeLifecycleAware {

    companion object {
        private const val TAG = "AppsUseCase"
        val appsContentObjectUploadOptions = object : ObjectUploadOptions {
            private val compressOptions = object : ICompressor.CompressOptions {
                override fun imageCompressQuality(): Int {
                    return 55
                }
            }

            override fun getInfixPath(): String {
                return "apps/"
            }

            override fun compressOptions(): ICompressor.CompressOptions {
                return compressOptions
            }
        }
        fun createMockApplications(count: Int): MutableList<AppsWithCategory> {
            val result = mutableListOf<AppsWithCategory>()
            val applications = mutableListOf<Application>()
            repeat(count) {
                val application = Application()
                applications.add(application)
            }
            val appsWithCategory = AppsWithCategory("", "", applications)
            result.add(appsWithCategory)
            return result
        }
    }

    val appCenterState: MutableState<AppCenterState> =
        mutableStateOf(AppCenterState.Loading(createMockApplications(32)))

    fun loadHomeApplications() {
        PurpleLogger.current.d(TAG, "loadHomeApplications")
        if (appCenterState.value is AppCenterState.LoadSuccess) {
            return
        }
        coroutineScope.launch {
            requestNotNull(
                action = {
                    appSetsRepository.getIndexApplications()
                },
                onSuccess = {
                    coroutineScope.launch {
                        val loadingState = appCenterState.value as AppCenterState.Loading
                        val appsWithCategories = loadingState.apps
                        val mockLoadingApplicationsCount =
                            appsWithCategories.sumOf { it.applications.size }
                        val realApplicationsCount = it.sumOf { it.applications.size }
                        val diff =
                            mockLoadingApplicationsCount - realApplicationsCount
                        if (abs(diff) != 0) {
                            repeat(abs(diff)) { i ->
                                if (diff < 0) {
                                    this@AppsUseCase.appCenterState.value =
                                        AppCenterState.Loading(
                                            createMockApplications(
                                                mockLoadingApplicationsCount + i + 1
                                            )
                                        )
                                } else {
                                    this@AppsUseCase.appCenterState.value =
                                        AppCenterState.Loading(
                                            createMockApplications(
                                                mockLoadingApplicationsCount - (i + 1)
                                            )
                                        )
                                }
                                delay(10)
                            }
                        }
                        this@AppsUseCase.appCenterState.value = AppCenterState.LoadSuccess(it)
                    }
                }
            )
        }
    }

    fun findApplicationByBioId(application: Application): Application? {
        PurpleLogger.current.d(TAG, "findApplicationByBioId, bioId:${application.id}")
        val centerState = appCenterState.value
        if (centerState !is AppCenterState.LoadSuccess) {
            return null
        }
        centerState.apps.forEach {
            for (app in it.applications) {
                if (app.id == application.id) {
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
        val centerState = appCenterState.value
        if (centerState !is AppCenterState.LoadSuccess) {
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