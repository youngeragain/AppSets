package xcj.app.screen_share.ui.compose.standard_home

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import android.graphics.Color as AndroidColor

data class AppsPageSettingsSaveState(
    val pageBackgroundColor: Int? = null,
    val appCardBackgroundColor: Int? = null,
    val appCardAppNameColor: Int = AppsPageSettings.APP_CARD_APP_NAME_DEFAULT_COLOR,
    val searchPageAppNameColor: Int = AppsPageSettings.SEARCH_PAGE_CONTENT_DEFAULT_COLOR,
    val appCardSpace: Int = 4,
    val appCountOnLine: Int = 4,
    val appIconSize: Int = 46,
    val appNameFontSize: Int = 10,
)

data class AppsPageSettings(
    val pageBackgroundState: WindowHomeBackgroundState = WindowHomeBackgroundState.Color(),
    val appCardBackgroundState: WindowHomeBackgroundState = WindowHomeBackgroundState.Color(
        APP_CARD_DEFAULT_COLOR
    ),
    val appCardAppNameColor: Int = APP_CARD_APP_NAME_DEFAULT_COLOR,
    val searchPageAppNameColor: Int = SEARCH_PAGE_CONTENT_DEFAULT_COLOR,
    val appCardSpace: Int = 4,
    val appCountOnLine: Int = 4,
    val appIconSize: Int = 46,
    val appNameFontSize: Int = 10
) {
    companion object {

        const val PAGE_BACKGROUND = 0
        const val APP_CARD_BACKGROUND = 1
        const val APP_CARD_APP_NAME_COLOR = 2
        const val SEARCH_PAGE_APP_NAME_COLOR = 3

        const val PAGE_DEFAULT_COLOR = AndroidColor.TRANSPARENT
        const val APP_CARD_DEFAULT_COLOR = AndroidColor.WHITE
        const val APP_CARD_APP_NAME_DEFAULT_COLOR = AndroidColor.BLACK
        const val SEARCH_PAGE_CONTENT_DEFAULT_COLOR = AndroidColor.WHITE
        const val SHARE_PREFERENCES_NAME = "appsets_launcher"
        const val KEY_SETTINGS_NAME = "launcher_settings"

        fun save(
            sharedPreferences: SharedPreferences,
            gson: Gson,
            settings: AppsPageSettings
        ) {
            runCatching {
                val pageBackgroundState =
                    settings.pageBackgroundState as? WindowHomeBackgroundState.Color
                val appCardBackgroundState =
                    settings.appCardBackgroundState as? WindowHomeBackgroundState.Color
                val saveState = AppsPageSettingsSaveState(
                    pageBackgroundState?.color,
                    appCardBackgroundState?.color,
                    settings.appCardAppNameColor,
                    settings.searchPageAppNameColor,
                    settings.appCardSpace,
                    settings.appCountOnLine,
                    settings.appIconSize,
                    settings.appNameFontSize,
                )
                val saveStateJson = gson.toJson(saveState)
                sharedPreferences.edit {
                    putString(KEY_SETTINGS_NAME, saveStateJson)
                    apply()
                }
            }

        }

        fun restore(
            sharedPreferences: SharedPreferences,
            gson: Gson,
        ): AppsPageSettings? {
            runCatching {
                val saveStateJson = sharedPreferences.getString(KEY_SETTINGS_NAME, null)
                if (saveStateJson.isNullOrEmpty()) {
                    return null
                }
                val saveState = gson.fromJson<AppsPageSettingsSaveState>(
                    saveStateJson,
                    AppsPageSettingsSaveState::class.java
                )
                val settings = AppsPageSettings(
                    WindowHomeBackgroundState.Color(saveState.pageBackgroundColor),
                    WindowHomeBackgroundState.Color(saveState.appCardBackgroundColor),
                    saveState.appCardAppNameColor,
                    saveState.searchPageAppNameColor,
                    saveState.appCardSpace,
                    saveState.appCountOnLine,
                    saveState.appIconSize,
                    saveState.appNameFontSize,
                )
                return settings
            }
            return null
        }
    }
}