package xcj.app.userinfo.controller

import com.google.gson.Gson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import xcj.app.DesignResponse
import xcj.app.userinfo.qr.BaiduHotData
import xcj.app.userinfo.qr.Hotsearch
import java.util.Calendar


@RestController
class HotWordsController {
    @Autowired
    lateinit var redisTemplate: StringRedisTemplate

    @GetMapping("/hotwords")
    fun getHotWords(@RequestParam(name = "from", required = false) from:String? = "baidu"): DesignResponse<BaiduHotData> {
        val opsForList = redisTemplate.opsForList()
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)+1
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
        val key = "hot-data-from-baidu?${year}/${month}/${dayOfMonth}/${hourOfDay}"
        val size = opsForList.size(key)
        if(size==null)
            return DesignResponse()
        val gson = Gson()
        val range = opsForList.range(key, 0, size - 1)?.map {
            gson.fromJson(it, Hotsearch::class.java)
        }
        if(range==null)
            return DesignResponse()
        return DesignResponse(data = BaiduHotData(range))
    }
}