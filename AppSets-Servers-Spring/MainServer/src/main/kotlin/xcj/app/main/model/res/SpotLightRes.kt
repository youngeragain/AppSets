package xcj.app.main.model.res

import xcj.app.main.model.common.BaiduHotData
import xcj.app.main.model.common.MicrosoftBingWallpaper
import xcj.app.main.model.table.mongo.Holiday
import xcj.app.main.model.table.mongo.PopularSearches
import xcj.app.main.model.table.mongo.TodayInHistory
import xcj.app.main.model.table.mongo.WordOfTheDay

data class SpotLightRes(
    val holiday: Holiday?,
    val popularSearches: PopularSearches?,
    val todayInHistoryList: List<TodayInHistory>?,
    val wordOfTheDayList: List<WordOfTheDay>?,
    var baiduHotData: BaiduHotData?,
    var microsoftBingWallpaperList: List<MicrosoftBingWallpaper>?
)