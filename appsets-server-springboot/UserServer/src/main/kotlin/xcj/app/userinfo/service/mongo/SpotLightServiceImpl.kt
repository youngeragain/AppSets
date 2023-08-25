package xcj.app.userinfo.service.mongo

import com.google.gson.Gson
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import xcj.app.DesignResponse
import xcj.app.userinfo.dao.mongo.SearchSpotLightDao
import xcj.app.userinfo.model.table.mongo.SpotLight
import xcj.app.userinfo.qr.BaiduHotData
import xcj.app.userinfo.qr.MicrosoftBingWallpaperJson
import java.util.*

@Service
class SpotLightServiceImpl(
    private val spotLightDao: SearchSpotLightDao,
    private val redisTemplate: StringRedisTemplate,
):SpotLightService {

    override fun getWin11SpotLightInfo():DesignResponse<SpotLight>{
        val spotLightInfo = spotLightDao.getSpotLightInfo()
        if(!redisTemplate.hasKey("hot-data-from-baidu")&&
            !redisTemplate.hasKey("bing-wallpaper-data"))
            return DesignResponse(data = spotLightInfo)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)+1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val opsForHash = redisTemplate.opsForHash<String, String>()

        val dateOfBingWallpaper = "${year}/${month}/${dayOfMonth}"
        val gson = Gson()
        if(opsForHash.hasKey("bing-wallpaper-data", dateOfBingWallpaper)){
            val bingWallpaperJsonString = opsForHash.get("bing-wallpaper-data", dateOfBingWallpaper)
            val wallpaperJson = gson.fromJson<MicrosoftBingWallpaperJson>(
                bingWallpaperJsonString,
                MicrosoftBingWallpaperJson::class.java
            )
            spotLightInfo?.bing_wallpaper_json = wallpaperJson

        }


        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val dateOfBaiduHotData = "${year}/${month}/${dayOfMonth}/${hourOfDay}"
        if(opsForHash.hasKey("hot-data-from-baidu", dateOfBaiduHotData)){
            val baiduHotDataJson = opsForHash.get("hot-data-from-baidu", dateOfBaiduHotData)
            val baiduHotData = gson.fromJson<BaiduHotData>(baiduHotDataJson, BaiduHotData::class.java)
            spotLightInfo?.baidu_hot_data = baiduHotData
        }
        return DesignResponse(data = spotLightInfo)
    }

}