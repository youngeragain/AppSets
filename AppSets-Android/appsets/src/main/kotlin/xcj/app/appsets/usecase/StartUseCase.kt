package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.appsets.db.room.repository.PinnedAppsRepository
import xcj.app.starter.server.requestNotNullRaw
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.ui.model.SpotLightState
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.android.AppDefinition
import xcj.app.starter.android.ItemDefinition
import xcj.app.starter.android.util.PackageUtil
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalApplication

class StartUseCase(
    private val coroutineScope: CoroutineScope,
    private val pinnedAppsRepository: PinnedAppsRepository,
    private val appSetsRepository: AppSetsRepository,
) : IComposeLifecycleAware {

    companion object {
        private const val TAG = "StartUseCase"
    }

    val spotLightsState: MutableList<SpotLightState> = mutableStateListOf()

    var allApps: MutableList<AppDefinition> = mutableStateListOf()

    val pinnedApp: MutableState<SpotLightState.PinnedApp> =
        mutableStateOf(SpotLightState.PinnedApp(emptyList()))

    val recommendItems: MutableState<SpotLightState.RecommendedItem> =
        mutableStateOf(SpotLightState.RecommendedItem(emptyList()))

    private val pinnedAppPackageNames: LiveData<List<String>> =
        pinnedAppsRepository.getAllPinnedAppsLiveData()

    init {
        pinnedAppPackageNames.observeForever {
            updatePinnedApps(allApps, it, pinnedApp)
        }
    }

    suspend fun doInitData(context: Context) {
        PurpleLogger.current.d(
            TAG,
            "doInitData, thread:${Thread.currentThread()}"
        )
        updateAllApps(allApps)
        updatePinnedApps(allApps, pinnedAppPackageNames.value, pinnedApp)
        updateRecommendItems(context, recommendItems)
        loadSpotLight(context)
    }

    private suspend fun updateAllApps(allApps: MutableList<AppDefinition>) {
        PackageUtil.getLauncherIntentAppDefinitionList(LocalApplication.current).collect{
            allApps.addAll(it)
        }
    }

    private fun updateRecommendItems(
        context: Context,
        recommendItems: MutableState<SpotLightState.RecommendedItem>
    ) {
        val appDefinitionMutableList = allApps
        val forYouText = context.getString(xcj.app.appsets.R.string.for_you)
        val mutableListOf = mutableListOf<ItemDefinition>().apply {
            val recommendItemsCount = (4..7).random()
            if (appDefinitionMutableList.size > recommendItemsCount) {
                while (size <= 5) {
                    val appDefinition = appDefinitionMutableList.random()
                    if (!contains(appDefinition)) {
                        appDefinition.description = forYouText
                        add(appDefinition)
                    }
                }
            } else if (appDefinitionMutableList.isNotEmpty()) {
                val elements = appDefinitionMutableList.onEach {
                    it.description = forYouText
                }
                addAll(elements)
            }
        }
        recommendItems.value = SpotLightState.RecommendedItem(mutableListOf)
    }

    private fun updatePinnedApps(
        allApps: MutableList<AppDefinition>,
        pinnedAppPackageNames: List<String>?,
        pinnedApp: MutableState<SpotLightState.PinnedApp>
    ) {
        val pinnedAppDefinitionList = mutableListOf<AppDefinition>()
        for (app in allApps) {
            if (pinnedAppPackageNames?.find { pkName -> pkName == app.applicationInfo?.packageName } != null) {
                app.isPinned = true
                pinnedAppDefinitionList.add(app)
            }
        }
        pinnedApp.value = SpotLightState.PinnedApp(pinnedAppDefinitionList)
    }

    fun addPinnedApp(pinnedAppPackageName: String?) {
        coroutineScope.launch(Dispatchers.IO) {
            pinnedAppsRepository.addPinnedApp(pinnedAppPackageName)
        }

    }

    fun unPinApp(appPackageName: String?) {
        if (appPackageName.isNullOrEmpty()) {
            return
        }
        coroutineScope.launch(Dispatchers.IO) {
            pinnedAppsRepository.unPinApp(appPackageName)
        }
    }


    fun onSnapShotPageStateClick(
        spotLightState: SpotLightState,
        context: Context,
        payload: Any?
    ) {
        when (spotLightState) {
            is SpotLightState.PinnedApp -> {
                if (payload !is AppDefinition) {
                    return
                }
                val launchIntentForPackage =
                    payload.applicationInfo?.packageName?.let {
                        context.packageManager.getLaunchIntentForPackage(it)
                    }
                if (launchIntentForPackage != null) {
                    runCatching {
                        context.startActivity(launchIntentForPackage)
                    }
                }
            }

            is SpotLightState.RecommendedItem -> {
                if (payload is ItemDefinition) {
                    if (payload !is AppDefinition) {
                        return
                    }
                    val launchIntentForPackage =
                        payload.applicationInfo?.packageName?.let {
                            context.packageManager.getLaunchIntentForPackage(it)
                        }
                    if (launchIntentForPackage != null) {
                        runCatching {
                            context.startActivity(launchIntentForPackage)
                        }
                    }
                }
            }

            else -> {}
        }
    }

    private fun loadSpotLight(context: Context) {
        if (spotLightsState.isNotEmpty()) {
            return
        }
        coroutineScope.launch {
            requestNotNullRaw(
                action = {
                    val response =
                        appSetsRepository.getSpotLight()
                    val spotLight = response.data ?: return@requestNotNullRaw

                    spotLight.microsoftBingWallpaperList?.let {
                        val bingWallpaper =
                            SpotLightState.BingWallpaper(it)
                        spotLightsState.add(bingWallpaper)
                    }

                    val wordOfDayItems: MutableList<Any> = mutableListOf()
                    val wordOfDay = SpotLightState.WordOfTheDay(wordOfDayItems)
                    spotLight.wordOfTheDayList?.let(wordOfDayItems::addAll)
                    spotLight.todayInHistoryList?.let(wordOfDayItems::addAll)
                    spotLightsState.add(wordOfDay)

                    val words: MutableList<Any> = mutableListOf()

                    val popularSearch = SpotLightState.PopularSearch(
                        xcj.app.compose_share.R.drawable.ic_call_missed_outgoing_24,
                        context.getString(xcj.app.appsets.R.string.hotspot),
                        words
                    )

                    spotLight.popularSearches?.keywords?.let {
                        words.addAll(it)
                    }

                    spotLight.baiduHotData?.hotsearch?.let {
                        words.addAll(it)
                    }

                    spotLightsState.add(popularSearch)

                },
                onFailed = {
                    PurpleLogger.current.d(TAG, "loadSpotLight, failed! ${it.info}")
                }
            )
        }
    }

    override fun onComposeDispose(by: String?) {

    }
}