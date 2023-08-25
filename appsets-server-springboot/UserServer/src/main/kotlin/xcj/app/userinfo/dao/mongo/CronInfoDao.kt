package xcj.app.userinfo.dao.mongo

import xcj.app.userinfo.model.table.mongo.CronInfo

interface CronInfoDao {
    fun getRedisTokenScanCronInfo():CronInfo?
    fun getBaiduHotDataFetchCronInfo():CronInfo?
    fun getBingWallpaperFetchCronInfo():CronInfo?

}

