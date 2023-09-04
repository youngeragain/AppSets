package xcj.app.appsets.purple_module

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import xcj.app.core.android.ApplicationHelper
import xcj.app.core.android.ModuleMainEntry
import xcj.app.core.android.ModuleRouter

class ModuleRouter: ModuleRouter, ModuleMainEntry {

    fun initModule() {
        Log.e("ModuleRouter", "appsets initModule")
        //TODO 添加主线程未捕获异常的异常处理器,注意:当前方法调用处于子线程
        if (ApplicationHelper.isModuleInit(ModuleConstant.MODULE_NAME))
            return
        val appDatabase = xcj.app.appsets.db.room.AppDatabase.getRoomDatabase(
            ModuleConstant.MODULE_DATABASE_NAME,
            ApplicationHelper.application, ApplicationHelper.coroutineScope
        )
        ApplicationHelper.addDataBase(ModuleConstant.MODULE_NAME, appDatabase)
        ApplicationHelper.moduleInit(ModuleConstant.MODULE_NAME, this)
        initNotification(ApplicationHelper.application)

    }

    private fun initNotification(application: Application) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Conversion"
            val descriptionText = "Some people will send you a message"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("Conversion_Channel_1", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}