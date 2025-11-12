package xcj.app.appsets.settings

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.notification.NotificationChannels
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.purple_module.configCoil
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.foundation.FinalKeyedProvider
import xcj.app.starter.foundation.Identifiable
import xcj.app.starter.foundation.KeyedProvider
import xcj.app.starter.test.LocalApplication
import xcj.app.starter.test.LocalPurpleCoroutineScope

interface ModuleSettings {

    fun init()

}

class AppSetsModuleSettings : ModuleSettings {

    companion object {

        const val URI_SELF_DOWNLOAD_URL = "http://162.14.70.230/?route=download"
        const val KEY_IM_BUBBLE_ALIGNMENT = "im_bubble_alignment"
        const val IM_BUBBLE_ALIGNMENT_ALL_START = "all_start"
        const val IM_BUBBLE_ALIGNMENT_ALL_END = "all_end"
        const val IM_BUBBLE_ALIGNMENT_START_END = "start_end"
        const val KEY_IM_MESSAGE_DELIVERY_TYPE = "im_message_delivery_type"
        const val IM_MESSAGE_DELIVERY_TYPE_DI = "send_directly"
        const val IM_MESSAGE_DELIVERY_TYPE_RT = "relay_transmission"
        const val KEY_IS_IM_MESSAGE_SHOW_DATE = "is_im_message_show_date"
        const val KEY_IS_IM_MESSAGE_DATE_SHOW_SECONDS = "is_im_message_date_show_seconds"
        const val KEY_IS_IM_MESSAGE_RELIABILITY = "is_im_message_reliability"
        const val KEY_IS_APP_FIRST_LAUNCH = "is_app_first_launch"

        fun get(): AppSetsModuleSettings {
            val moduleSettings =
                ModuleHelper.get<AppSetsModuleSettings>(
                    Identifiable.fromString(ModuleConstant.MODULE_NAME + "/settings")
                )
            if (moduleSettings != null) {
                return moduleSettings
            }

            val databaseKeyedProvider = object : KeyedProvider<String, AppDatabase> {
                override fun key(): Identifiable<String> {
                    return Identifiable.fromString(ModuleConstant.MODULE_NAME + "/database")
                }

                override fun provide(): AppDatabase {
                    val moduleDatabase = AppDatabase.getRoomDatabase(
                        ModuleConstant.MODULE_DATABASE_NAME,
                        LocalApplication.current,
                        LocalPurpleCoroutineScope.current
                    )
                    return moduleDatabase
                }
            }

            ModuleHelper.addProvider(databaseKeyedProvider)

            val settingsProvider = FinalKeyedProvider(
                Identifiable.fromString(ModuleConstant.MODULE_NAME + "/settings"),
                AppSetsModuleSettings()
            )
            ModuleHelper.addProvider(settingsProvider)
            return settingsProvider.provide()
        }
    }

    private val appSettingSharedPreferences by lazy {
        LocalApplication.current.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    var imMessageDeliveryType: String = IM_MESSAGE_DELIVERY_TYPE_RT
    var imBubbleAlignment: String = IM_BUBBLE_ALIGNMENT_START_END

    var isImMessageShowDate: Boolean = true
    var isImMessageDateShowSeconds: Boolean = false

    var isBackgroundIMEnable: Boolean = true

    override fun init() {
        prepareNotificationsChanelConfig()
        prepareCoilConfig()
        LocalPurpleCoroutineScope.current.launch {
            prepareSettingsConfig()
        }
    }

    private suspend fun prepareSettingsConfig() {
        withContext(Dispatchers.IO) {
            isBackgroundIMEnable =
                appSettingSharedPreferences.getBoolean(
                    KEY_IS_IM_MESSAGE_RELIABILITY,
                    false
                )
            imMessageDeliveryType =
                appSettingSharedPreferences.getString(
                    KEY_IM_MESSAGE_DELIVERY_TYPE,
                    IM_MESSAGE_DELIVERY_TYPE_RT
                ) ?: IM_MESSAGE_DELIVERY_TYPE_RT
            imBubbleAlignment =
                appSettingSharedPreferences.getString(
                    KEY_IM_BUBBLE_ALIGNMENT,
                    IM_BUBBLE_ALIGNMENT_START_END
                ) ?: IM_BUBBLE_ALIGNMENT_START_END

            isImMessageShowDate =
                appSettingSharedPreferences.getBoolean(
                    KEY_IS_IM_MESSAGE_SHOW_DATE,
                    true
                )
            isImMessageDateShowSeconds =
                appSettingSharedPreferences.getBoolean(
                    KEY_IS_IM_MESSAGE_DATE_SHOW_SECONDS,
                    false
                )
        }
    }

    private fun prepareNotificationsChanelConfig() {
        val context = LocalApplication.current
        NotificationChannels.prepareToSystem(context)
    }

    private fun prepareCoilConfig() {
        val context = LocalApplication.current
        configCoil(context)
    }

    suspend fun onIMBubbleAlignmentChanged(alignment: String) {
        imBubbleAlignment = alignment
        appSettingSharedPreferences.edit {
            putString(KEY_IM_BUBBLE_ALIGNMENT, alignment)
        }
    }

    suspend fun onIMMessageDeliveryTypeChanged(deliveryType: String) {
        withContext(Dispatchers.IO) {
            imMessageDeliveryType = deliveryType
            appSettingSharedPreferences.edit {
                putString(KEY_IM_MESSAGE_DELIVERY_TYPE, deliveryType)
            }
        }
    }

    suspend fun onIsIMMessageShowDateChanged(show: Boolean) {
        withContext(Dispatchers.IO) {
            isImMessageShowDate = show
            appSettingSharedPreferences.edit {
                putBoolean(KEY_IS_IM_MESSAGE_SHOW_DATE, show)
            }
        }
    }

    suspend fun onIsIMMessageDateShowSecondsChanged(show: Boolean) {
        withContext(Dispatchers.IO) {
            isImMessageDateShowSeconds = show
            appSettingSharedPreferences.edit {
                putBoolean(KEY_IS_IM_MESSAGE_DATE_SHOW_SECONDS, show)
            }
        }
    }

    suspend fun onIsIMMessageReliabilityChanged(show: Boolean) {
        withContext(Dispatchers.IO) {
            isBackgroundIMEnable = show
            appSettingSharedPreferences.edit {
                putBoolean(KEY_IS_IM_MESSAGE_RELIABILITY, show)
            }
        }
    }

    fun isAppFirstLaunch(): Flow<Boolean> {
        return flow {
            val isFirstLaunch = withContext(Dispatchers.IO) {
                val isFirst = appSettingSharedPreferences.getBoolean(
                    KEY_IS_APP_FIRST_LAUNCH,
                    true
                )
                if (isFirst) {
                    appSettingSharedPreferences.edit {
                        putBoolean(KEY_IS_APP_FIRST_LAUNCH, false)
                    }
                }
                isFirst
            }
            emit(isFirstLaunch)
        }
    }
}