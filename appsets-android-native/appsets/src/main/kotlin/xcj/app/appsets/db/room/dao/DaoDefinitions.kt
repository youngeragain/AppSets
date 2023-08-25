package xcj.app.appsets.db.room.dao

interface DaoDefinitions {
    fun bitmapDao(): BitmapDao
    fun pinnedAppsDao(): PinnedAppsDao
    fun groupInfoDao(): GroupInfoDao
    fun userInfoDao(): UserInfoDao
    fun flatImMessageDao(): FlatImMessageDao

    fun userRelationDao(): UserRelationDao
}