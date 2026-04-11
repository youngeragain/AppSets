package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.db.room.entity.UserRelation
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.ProjectConstants
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.Identifiable
import xcj.app.starter.foundation.http.DesignResponse
import java.lang.reflect.Proxy

@Dao
interface UserRelationDao {

    @Query("select * from userrelation")
    suspend fun getRelationList(): List<UserRelation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUserRelation(vararg userRelation: UserRelation)

    companion object {

        private const val TAG = "UserRelationDao"

        fun getInstance(): UserRelationDao {
            if (ProjectConstants.IS_IN_ANDROID_STUDIO_PREVIEW) {
                return Proxy.newProxyInstance(
                    UserRelationDao::class.java.classLoader,
                    arrayOf(UserRelationDao::class.java),

                    ) { proxy, method, args ->
                    PurpleLogger.current.d(
                        TAG,
                        "getInstance, proxy:$proxy, method:${method.name}"
                    )
                    DesignResponse.NO_DATA
                } as UserRelationDao
            }
            val dataBase =
                ModuleHelper.get<AppDatabase>(Identifiable.fromString(ModuleConstant.MODULE_NAME + "/database"))
            PurpleLogger.current.d(TAG, "getInstance, dataBase:${dataBase}")
            if (dataBase == null) {
                PurpleLogger.current.e(TAG, "getInstance, dataBase is null!")
                throw Exception()
            }
            return dataBase.userRelationDao()
        }
    }
}