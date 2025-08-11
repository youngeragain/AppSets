package xcj.app.appsets.db.room

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import xcj.app.appsets.db.room.dao.DaoDefinitions
import xcj.app.appsets.db.room.entity.BitmapDefinition
import xcj.app.appsets.db.room.entity.FlatImMessage
import xcj.app.appsets.db.room.entity.PinnedApp
import xcj.app.appsets.db.room.entity.UserRelation
import xcj.app.appsets.db.room.util.TimeStampDateConverter
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.starter.android.util.PurpleLogger


@TypeConverters(TimeStampDateConverter::class)
@Database(
    entities = [BitmapDefinition::class, PinnedApp::class,
        UserInfo::class, GroupInfo::class,
        FlatImMessage::class, UserRelation::class],
    version = 20250116,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase(), DaoDefinitions {

    companion object {
        private const val TAG = "AppDatabase"

        @Volatile
        private var db: AppDatabase? = null

        fun getRoomDatabase(
            databaseName: String,
            application: Application,
            coroutineScope: CoroutineScope
        ): AppDatabase {
            PurpleLogger.current.d(
                TAG,
                "getRoomDatabase, dataBaseName:$databaseName"
            )
            return db ?: synchronized(this) {
                val temp =
                    Room.databaseBuilder(
                        application,
                        AppDatabase::class.java,
                        databaseName
                    )
                        .fallbackToDestructiveMigration(false)
                        .addCallback(
                            DatabaseCallback(
                                coroutineScope
                            )
                        )
                        .build()
                db = temp
                temp
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        /**
         * Override the onCreate method to populate the database.
         */
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // If you want to keep the data through app restarts,
            // comment out the following line.
            //db.let {
            //    scope.launch(Dispatchers.IO) {
            //
            //    }
            //}
        }
    }
}