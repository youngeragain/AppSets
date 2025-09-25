package xcj.app.appsets.settings

import android.content.Context
import androidx.core.content.edit
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.notification.NotificationChannels
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.purple_module.configCoil
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.foundation.FinalProvider
import xcj.app.starter.foundation.Identifiable
import xcj.app.starter.foundation.Provider
import xcj.app.starter.test.LocalApplication
import xcj.app.starter.test.LocalPurpleCoroutineScope

interface ModuleSettings {

    fun init()

}

class AppSetsModuleSettings : ModuleSettings {

    companion object {

        const val WEBSITE_URL = "http://162.14.70.230/?route=download"

        const val KEY_im_bubble_alignment = "im_bubble_alignment"
        const val IM_BUBBLE_ALIGNMENT_ALL_START = "all_start"
        const val IM_BUBBLE_ALIGNMENT_ALL_END = "all_end"
        const val IM_BUBBLE_ALIGNMENT_START_END = "start_end"

        const val KEY_im_message_delivery_type = "im_message_delivery_type"
        const val IM_MESSAGE_DELIVERY_TYPE_DI = "send_directly"
        const val IM_MESSAGE_DELIVERY_TYPE_RT = "relay_transmission"

        const val KEY_is_im_message_show_date = "is_im_message_show_date"

        const val KEY_is_im_message_date_show_seconds = "is_im_message_date_show_seconds"


        fun get(): AppSetsModuleSettings {
            val moduleSettings =
                ModuleHelper.get<AppSetsModuleSettings>(
                    Identifiable.fromString(ModuleConstant.MODULE_NAME + "/settings")
                )
            if (moduleSettings != null) {
                return moduleSettings
            }

            val databaseProvider = object : Provider<String, AppDatabase> {
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

            ModuleHelper.addProvider(databaseProvider)

            val settingsProvider = FinalProvider(
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
        prepareSettingsConfig()
        prepareNotificationsChanelConfig()
        prepareCoilConfig()
    }

    private fun prepareSettingsConfig() {
        imMessageDeliveryType =
            appSettingSharedPreferences.getString(
                KEY_im_message_delivery_type,
                IM_MESSAGE_DELIVERY_TYPE_RT
            ) ?: IM_MESSAGE_DELIVERY_TYPE_RT
        imBubbleAlignment =
            appSettingSharedPreferences.getString(
                KEY_im_bubble_alignment,
                IM_BUBBLE_ALIGNMENT_START_END
            ) ?: IM_BUBBLE_ALIGNMENT_START_END

        isImMessageShowDate =
            appSettingSharedPreferences.getBoolean(
                KEY_is_im_message_show_date,
                true
            )
        isImMessageDateShowSeconds =
            appSettingSharedPreferences.getBoolean(
                KEY_is_im_message_date_show_seconds,
                false
            )
    }

    private fun prepareNotificationsChanelConfig() {
        val context = LocalApplication.current
        NotificationChannels.prepareToSystem(context)
    }

    private fun prepareCoilConfig() {
        val context = LocalApplication.current
        configCoil(context)
    }

    fun onIMBubbleAlignmentChanged(alignment: String) {
        imBubbleAlignment = alignment
        appSettingSharedPreferences.edit {
            putString(KEY_im_bubble_alignment, alignment)
        }
    }

    fun onIMMessageDeliveryTypeChanged(deliveryType: String) {
        imMessageDeliveryType = deliveryType
        appSettingSharedPreferences.edit {
            putString(KEY_im_message_delivery_type, deliveryType)
        }
    }

    fun onIsIMMessageShowDateChanged(show: Boolean) {
        isImMessageShowDate = show
        appSettingSharedPreferences.edit {
            putBoolean(KEY_is_im_message_show_date, show)
        }
    }

    fun onIsIMMessageDateShowSecondsChanged(show: Boolean) {
        isImMessageDateShowSeconds = show
        appSettingSharedPreferences.edit {
            putBoolean(KEY_is_im_message_date_show_seconds, show)
        }
    }
}