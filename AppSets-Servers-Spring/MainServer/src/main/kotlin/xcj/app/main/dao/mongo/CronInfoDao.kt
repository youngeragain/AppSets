package xcj.app.main.dao.mongo

import xcj.app.main.model.table.mongo.CronInfo

interface CronInfoDao {

    fun getRedisTokenScanCronInfo(): CronInfo?

    fun getBaiduHotDataFetchCronInfo(): CronInfo?

    fun getBingWallpaperFetchCronInfo(): CronInfo?

}

