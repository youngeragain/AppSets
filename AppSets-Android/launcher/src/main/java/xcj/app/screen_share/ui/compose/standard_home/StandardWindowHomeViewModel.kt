package xcj.app.screen_share.ui.compose.standard_home

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import xcj.app.screen_share.ui.model.AppStyle
import xcj.app.screen_share.ui.model.StyledAppDefinition
import xcj.app.starter.android.util.PackageUtil
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalApplication

class StandardWindowHomeViewModel : ViewModel() {

    val apps: MutableList<StyledAppDefinition> = mutableStateListOf()

    val settings: MutableState<AppsPageSettings> = mutableStateOf(AppsPageSettings())
    private val sharedPreferences = LocalApplication.current.getSharedPreferences(
        AppsPageSettings.SHARE_PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    init {
        restoreSettings()
    }

    private fun restoreSettings() {
        AppsPageSettings.restore(
            sharedPreferences,
            gson
        )?.let {
            settings.value = it
        }
    }

    fun prepare(context: Context) {
        val launcherIntentAppDefinitionListFlow =
            PackageUtil.getLauncherIntentAppDefinitionList(context)
        viewModelScope.launch {
            launcherIntentAppDefinitionListFlow.collect { appDefinitionList ->
                appDefinitionList.forEach { appDefinition ->
                    val styledAppDefinition =
                        StyledAppDefinition(appDefinition = appDefinition, style = AppStyle())
                    apps.add(styledAppDefinition)
                }
            }
        }
    }

    fun updateSettingsColor(color: Color?, whatColorToChange: Int) {
        val appsPageSettings = settings.value
        when (whatColorToChange) {
            AppsPageSettings.PAGE_BACKGROUND -> {
                settings.value =
                    appsPageSettings.copy(
                        pageBackgroundState = WindowHomeBackgroundState.Color(color = color?.toArgb())
                    )
            }

            AppsPageSettings.SEARCH_PAGE_APP_NAME_COLOR -> {
                if (color == null) {
                    return
                }
                settings.value =
                    appsPageSettings.copy(
                        searchPageAppNameColor = color.toArgb()
                    )
            }

            AppsPageSettings.APP_CARD_BACKGROUND -> {
                settings.value =
                    appsPageSettings.copy(
                        appCardBackgroundState = WindowHomeBackgroundState.Color(color = color?.toArgb())
                    )
            }

            AppsPageSettings.APP_CARD_APP_NAME_COLOR -> {
                if (color == null) {
                    return
                }
                settings.value =
                    appsPageSettings.copy(
                        appCardAppNameColor = color.toArgb()
                    )
            }
        }
        AppsPageSettings.save(sharedPreferences, gson, settings.value)
    }

    fun updateSettingsAppCountOnLine(count: Int) {
        settings.value = settings.value.copy(appCountOnLine = count)
        AppsPageSettings.save(sharedPreferences, gson, settings.value)
    }

    fun updateSettingsAppIconSize(size: Int) {
        settings.value = settings.value.copy(appIconSize = size)
        AppsPageSettings.save(sharedPreferences, gson, settings.value)
    }

    fun updateSettingsAppNameFontSize(size: Int) {
        settings.value = settings.value.copy(appNameFontSize = size)
        AppsPageSettings.save(sharedPreferences, gson, settings.value)
    }

    fun updateSettingsAppCardSpace(size: Int) {
        settings.value = settings.value.copy(appCardSpace = size)
        AppsPageSettings.save(sharedPreferences, gson, settings.value)
    }

    fun updateExistApp(styledApp: StyledAppDefinition) {
        val indexOfFirst = apps.indexOfFirst { it.appDefinition.id == styledApp.appDefinition.id }
        if (indexOfFirst == -1) {
            return
        }
        PurpleLogger.current.d("blue", "updateExistApp")
        apps.removeAt(indexOfFirst)
        apps.add(indexOfFirst, styledApp)
    }
}