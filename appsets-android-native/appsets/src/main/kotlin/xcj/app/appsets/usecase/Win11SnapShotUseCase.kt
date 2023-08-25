package xcj.app.appsets.usecase

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.db.room.repository.PinnedAppsRepository
import xcj.app.appsets.ui.compose.win11Snapshot.AppDefinition
import xcj.app.appsets.ui.compose.win11Snapshot.ItemDefinition
import xcj.app.appsets.ui.compose.win11Snapshot.SpotLightState
import xcj.app.appsets.util.PackageUtil
import xcj.app.core.android.ApplicationHelper
import xcj.app.core.foundation.usecase.NoConfigUseCase
import xcj.app.purple_module.ModuleConstant

class Win11SnapShotUseCase(
    coroutineScope: CoroutineScope
    ): NoConfigUseCase(), CoroutineScope by coroutineScope {
    private lateinit var pinnedAppsRepository: PinnedAppsRepository
    fun repository(): PinnedAppsRepository {
        if (!::pinnedAppsRepository.isInitialized) {
            val pinnedAppsDao =
                ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
                    ?.pinnedAppsDao() ?: throw Exception("pinnedAppsDao 未初始化")
            pinnedAppsRepository = PinnedAppsRepository(pinnedAppsDao)
        }
        return pinnedAppsRepository
    }

    val allApps: MutableList<AppDefinition> =
        PackageUtil.hasLauncherIntentAppDefinitionList(ApplicationHelper.application)
    val pinnedApps: MutableState<SpotLightState.PinnedApps> =
        mutableStateOf(SpotLightState.PinnedApps(emptyList()))

    val recommendItems: SpotLightState.RecommendedItems =
        SpotLightState.RecommendedItems(
            mutableStateListOf<ItemDefinition>().apply {
                if (allApps.size > 5) {
                    val elements = allApps.subList(0, 5).onEach {
                        it.description = "为你推荐"
                    }
                    addAll(elements)
                } else if (allApps.isNotEmpty()) {
                    val elements = allApps.onEach {
                        it.description = "为你推荐"
                    }
                addAll(elements)
            }
        }
    ).also {
            it.onClick = ::win11SnapShotPageStateClick
    }
    //观察此字段，改变后调用getPinnedApps更新pinnedApps
    val pinnedAppPackageNames:LiveData<List<String>> by lazy { repository().getAllPinnedAppsLiveData() }

    fun getPinnedApps(){
        val pinnedAppDefinitionList = mutableListOf<AppDefinition>()
        for(app in allApps){
            if(pinnedAppPackageNames.value?.find { pkName->pkName==app.packageName }!=null){
                app.isPinned = true
                pinnedAppDefinitionList.add(app)
            }
        }
        val pinnedApps1 = SpotLightState.PinnedApps(pinnedAppDefinitionList)
        pinnedApps1.onClick = ::win11SnapShotPageStateClick
        pinnedApps.value = pinnedApps1
    }

    fun addPinnedApp(pinnedAppPackageName: String?){
        launch(Dispatchers.IO) {
            repository().addPinnedApp(pinnedAppPackageName)
        }

    }

    fun unPinApp(appPackageName: String?) {
        if (appPackageName.isNullOrEmpty())
            return
        launch(Dispatchers.IO) {
            repository().unPinApp(appPackageName)
        }
    }


    fun win11SnapShotPageStateClick(
        spotLightState: SpotLightState,
        context: Context,
        payload: Any?
    ) {
        if (spotLightState is SpotLightState.HeaderTitle) {
            if (payload is Int) {
                when (payload) {
                    0 -> {}
                    2 -> {
                        Toast.makeText(context, "Are you ok?", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else if (spotLightState is SpotLightState.PinnedApps) {
            if (payload is AppDefinition) {
                val appDefinition = payload
                val launchIntentForPackage =
                    context.packageManager.getLaunchIntentForPackage(appDefinition.packageName)
                context.startActivity(launchIntentForPackage)
            }
        } else if (spotLightState is SpotLightState.RecommendedItems) {
            if (payload is ItemDefinition) {
                val itemDefinition = payload
                if (itemDefinition is AppDefinition) {
                    val launchIntentForPackage =
                        context.packageManager.getLaunchIntentForPackage(itemDefinition.packageName)
                    context.startActivity(launchIntentForPackage)
                }
            }
        }
    }
}