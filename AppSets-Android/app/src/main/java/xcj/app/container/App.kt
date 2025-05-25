package xcj.app.container

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.os.ParcelFileDescriptor
import xcj.app.appsets.settings.AppConfig
import xcj.app.starter.android.DesignApplication
import xcj.app.starter.android.util.PurpleLogger

class App : DesignApplication() {

    override fun onCreate() {
        PurpleLogger.current.enable = AppConfig.isTest
        super.onCreate()
    }
}

class BackupAgent: BackupAgent(){
    override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput?,
        newState: ParcelFileDescriptor?
    ) {

    }

    override fun onRestore(
        data: BackupDataInput?,
        appVersionCode: Int,
        newState: ParcelFileDescriptor?
    ) {
        val mutableListOf = mutableListOf<String>()
    }
}