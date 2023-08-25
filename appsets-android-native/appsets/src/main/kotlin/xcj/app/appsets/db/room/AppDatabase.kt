package xcj.app.appsets.db.room

import android.app.Application
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import xcj.app.appsets.db.room.dao.DaoDefinitions
import xcj.app.appsets.db.room.entity.BitmapDefinition
import xcj.app.appsets.db.room.entity.FlatImMessage
import xcj.app.appsets.db.room.entity.PinnedApp
import xcj.app.appsets.db.room.entity.UserRelation
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import java.util.*


@TypeConverters(TimeStampDateConverter::class)
@Database(
    entities = [BitmapDefinition::class, PinnedApp::class,
        UserInfo::class, GroupInfo::class,
        FlatImMessage::class, UserRelation::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase(), DaoDefinitions {

    companion object {
        @Volatile
        private var db: AppDatabase? = null
        fun getRoomDatabase(
            databaseName: String,
            application: Application,
            coroutineScope: CoroutineScope
        ): AppDatabase {
            return db ?: synchronized(this) {
                val temp =
                    Room.databaseBuilder(
                        application,
                        AppDatabase::class.java,
                        databaseName
                    )
                        .fallbackToDestructiveMigration()
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