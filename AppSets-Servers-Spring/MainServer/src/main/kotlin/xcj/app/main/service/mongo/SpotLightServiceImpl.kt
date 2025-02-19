package xcj.app.main.service.mongo

import com.google.gson.Gson
import org.bson.types.ObjectId
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.DesignResponse
import xcj.app.main.dao.mongo.SpotLightDao
import xcj.app.main.model.common.BaiduHotData
import xcj.app.main.model.common.MicrosoftBingWallpaper
import xcj.app.main.model.res.SpotLightRes
import xcj.app.main.model.table.mongo.SpotLight
import xcj.app.main.service.ExternalDataFetcher
import java.util.*

@Service
class SpotLightServiceImpl(
    private val spotLightDao: SpotLightDao,
    private val redisTemplate: StringRedisTemplate,
) : SpotLightService {
    companion object {
        private const val TAG = "SpotLightServiceImpl"
    }

    private val gson = Gson()

    override fun getSpotLightInfo(): DesignResponse<SpotLightRes> {
        val spotLight = spotLightDao.getSpotLight() ?: SpotLight(_id = ObjectId())
        val spotLightRes = SpotLightRes(
            holiday = spotLight.holiday,
            popularSearches = spotLight.popular_searches,
            todayInHistoryList = spotLight.today_in_history_list,
            wordOfTheDayList = spotLight.word_of_the_day_list,
            baiduHotData = null,
            microsoftBingWallpaperList = null
        )
        if (!redisTemplate.hasKey(ExternalDataFetcher.KEY_BING_WALLPAPER_DATA) &&
            !redisTemplate.hasKey(ExternalDataFetcher.KEY_BAIDU_HOT_DATA)
        ) {
            return DesignResponse(data = spotLightRes)
        }
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val opsForHash = redisTemplate.opsForHash<String, String>()
        val opsForValue = redisTemplate.opsForValue()

        val microsoftBingWallpaperList = mutableListOf<MicrosoftBingWallpaper>()

        for (day in 1..dayOfMonth) {
            val dateOfBingWallpaper = "${year}/${month}/${day}"
            if (!opsForHash.hasKey(ExternalDataFetcher.KEY_BING_WALLPAPER_DATA, dateOfBingWallpaper)) {
                continue
            }
            val bingWallpaperJsonString =
                opsForHash.get(ExternalDataFetcher.KEY_BING_WALLPAPER_DATA, dateOfBingWallpaper)
            //PurpleLogger.current.d(TAG, "hasKey ${ExternalDataFetcher.KEY_BING_WALLPAPER_DATA}, $bingWallpaperJsonString")
            if (bingWallpaperJsonString.isNullOrEmpty()) {
                continue
            }
            val wallpaperJson = gson.fromJson<MicrosoftBingWallpaper>(
                bingWallpaperJsonString,
                MicrosoftBingWallpaper::class.java
            )
            microsoftBingWallpaperList.add(wallpaperJson)
        }
        if (microsoftBingWallpaperList.isNotEmpty()) {
            spotLightRes.microsoftBingWallpaperList = microsoftBingWallpaperList
        }

        if (redisTemplate.hasKey(ExternalDataFetcher.KEY_BAIDU_HOT_DATA)) {
            val baiduHotDataJsonString = opsForValue.get(ExternalDataFetcher.KEY_BAIDU_HOT_DATA)
            //PurpleLogger.current.d(TAG, "hasKey ${ExternalDataFetcher.KEY_BAIDU_HOT_DATA}, $baiduHotDataJsonString")
            if (!baiduHotDataJsonString.isNullOrEmpty()) {
                val baiduHotData = gson.fromJson<BaiduHotData>(baiduHotDataJsonString, BaiduHotData::class.java)
                spotLightRes.baiduHotData = baiduHotData
            }
        }
        return DesignResponse(data = spotLightRes)
    }

}