package xcj.app.launcher.ui.standard_home

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcj.app.starter.android.AppDefinition
import xcj.app.starter.android.util.PackageUtil
import xcj.app.starter.test.LocalApplication
import java.util.UUID

class StandardWindowHomeViewModel : ViewModel() {

    val apps: MutableState<List<AppDefinition>> = mutableStateOf(emptyList())

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

    private suspend fun makeMockApps(): List<AppDefinition> = withContext(Dispatchers.IO) {
        val result = mutableStateListOf<AppDefinition>()
        repeat(42) {
            result.add(AppDefinition(UUID.randomUUID().toString()))
        }
        return@withContext result
    }

    fun prepare(context: Context) {
        viewModelScope.launch {
            //apps.value = makeMockApps()
            val elements = PackageUtil.getLauncherIntentAppDefinitionList(context)
            apps.value = elements
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
}