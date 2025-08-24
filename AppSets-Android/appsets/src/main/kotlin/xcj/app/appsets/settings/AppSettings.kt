package xcj.app.appsets.settings

import android.app.NotificationChannel
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import xcj.app.appsets.notification.NotificationChannels
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.purple_module.configCoil
import xcj.app.starter.test.LocalApplication

interface ModuleSettings {

    fun initConfig()

    fun name(): String

}

class AppSetsModuleSettings : ModuleSettings {

    companion object {

        const val WEBSITE_URL = "http://162.14.70.230/?route=download"

        const val IM_BUBBLE_ALIGNMENT_ALL_START = "all_start"
        const val IM_BUBBLE_ALIGNMENT_ALL_END = "all_end"
        const val IM_BUBBLE_ALIGNMENT_START_END = "start_end"

        const val IM_MESSAGE_DELIVERY_TYPE_DI = "send_directly"
        const val IM_MESSAGE_DELIVERY_TYPE_RT = "relay_transmission"


        fun get(): AppSetsModuleSettings {
            return AppSettings.getModuleSettings<AppSetsModuleSettings>(ModuleConstant.MODULE_NAME)!!
        }
    }

    private val appSettingSharedPreferences by lazy {
        LocalApplication.current.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    }

    var imMessageDeliveryType: String = IM_MESSAGE_DELIVERY_TYPE_RT
    var imBubbleAlignment: String = IM_BUBBLE_ALIGNMENT_START_END

    var isImMessageShowDate: Boolean = true
    var isImMessageDateShowSeconds: Boolean = false


    override fun initConfig() {
        prepareSettingsConfig()
        prepareNotificationsChanelConfig()
        prepareCoilConfig()
    }

    override fun name(): String {
        return ModuleConstant.MODULE_NAME
    }

    private fun prepareSettingsConfig() {
        imMessageDeliveryType =
            appSettingSharedPreferences.getString(
                "im_message_delivery_type",
                IM_MESSAGE_DELIVERY_TYPE_RT
            ) ?: IM_MESSAGE_DELIVERY_TYPE_RT
        imBubbleAlignment =
            appSettingSharedPreferences.getString(
                "im_bubble_alignment",
                IM_BUBBLE_ALIGNMENT_START_END
            ) ?: IM_BUBBLE_ALIGNMENT_START_END

        isImMessageShowDate =
            appSettingSharedPreferences.getBoolean(
                "is_im_message_show_date",
                true
            )
        isImMessageDateShowSeconds =
            appSettingSharedPreferences.getBoolean(
                "is_im_message_date_show_seconds",
                false
            )
    }

    private fun prepareNotificationsChanelConfig() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val context = LocalApplication.current
            // Register the channel with the system
            val notificationManager: NotificationManagerCompat =
                NotificationManagerCompat.from(context)
            NotificationChannels.provide(context).forEach { appsetsNotificationChannel ->
                val channel =
                    NotificationChannel(
                        appsetsNotificationChannel.id,
                        context.getString(appsetsNotificationChannel.name),
                        appsetsNotificationChannel.importance
                    )
                channel.description = context.getString(appsetsNotificationChannel.description)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    private fun prepareCoilConfig() {
        configCoil(LocalApplication.current)
    }

    fun onIMBubbleAlignmentChanged(alignment: String) {
        imBubbleAlignment = alignment
        appSettingSharedPreferences.edit {
            putString("im_bubble_alignment", alignment)
        }
    }

    fun onIMMessageDeliveryTypeChanged(deliveryType: String) {
        imMessageDeliveryType = deliveryType
        appSettingSharedPreferences.edit {
            putString("im_message_delivery_type", deliveryType)
        }
    }

    fun onIsIMMessageShowDateChanged(show: Boolean) {
        isImMessageShowDate = show
        appSettingSharedPreferences.edit {
            putBoolean("is_im_message_show_date", show)
        }
    }

    fun onIsIMMessageDateShowSecondsChanged(show: Boolean) {
        isImMessageDateShowSeconds = show
        appSettingSharedPreferences.edit {
            putBoolean("is_im_message_date_show_seconds", show)
        }
    }
}

object AppSettings {

    private val moduleSettings: MutableList<ModuleSettings> = mutableListOf(
        AppSetsModuleSettings()
    )

    fun collectAllModuleSettings(): List<ModuleSettings> {
        return moduleSettings
    }

    fun initAppConfig() {
        collectAllModuleSettings().forEach(ModuleSettings::initConfig)
    }

    fun <T : ModuleSettings> getModuleSettings(name: String): T? {
        return moduleSettings.firstOrNull { it.name() == name } as? T
    }
}