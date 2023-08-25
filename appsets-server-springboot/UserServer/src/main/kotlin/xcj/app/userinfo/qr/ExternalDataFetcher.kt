package xcj.app.userinfo.qr

import com.google.gson.Gson
import org.jsoup.Jsoup
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.net.URL
import java.net.URLConnection
import java.nio.charset.Charset
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Component
class ExternalDataFetcher(
	private val redisTemplate: StringRedisTemplate
){
	private val mExecutor:ExecutorService by lazy {
		Executors.newSingleThreadExecutor()
	}
	fun doFetch(type:String){
		mExecutor.execute {
			when(type){
				"baidu_hot_data"->fetchBaiduHotData()
				"bing_wallpaper_data"->fetchBingWallpaperData()
			}
		}
	}
	private fun fetchBingWallpaperData(){
		try {
			val calendar = Calendar.getInstance()
			val year = calendar.get(Calendar.YEAR)
			val month = calendar.get(Calendar.MONTH)+1
			val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
			val date = "${year}/${month}/${dayOfMonth}"
			val opsForHash = redisTemplate.opsForHash<String, String>()
			if(opsForHash.hasKey("bing-wallpaper-data", date))
				return
			val connection = openUrlConnection("https://www.bing.com/HPImageArchive.aspx?format=js&idx=0&n=1")
			val bingWallpaperJson = connection.getInputStream().use {
				it.readBytes().toString(Charset.defaultCharset())
			}
			opsForHash.putIfAbsent("bing-wallpaper-data", date, bingWallpaperJson)
		}catch (e:Exception){
			e.printStackTrace()
		}

	}
	private fun fetchBaiduHotData(){
		try {
			val calendar = Calendar.getInstance()
			val year = calendar.get(Calendar.YEAR)
			val month = calendar.get(Calendar.MONTH)+1
			val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
			val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
			val date = "${year}/${month}/${dayOfMonth}/${hourOfDay}"
			val opsForHash = redisTemplate.opsForHash<String, String>()

			if(opsForHash.hasKey("hot-data-from-baidu", date)){
				return
			}
			val connection = openUrlConnection("http://www.baidu.com/")
			val html = connection.getInputStream().use {
				it.readBytes().toString(Charset.defaultCharset())
			}
			val parse = Jsoup.parse(html)
			val hotDataJson = parse.getElementById("hotsearch_data")?.text()
			if(hotDataJson.isNullOrEmpty())
				return
			opsForHash.putIfAbsent("hot-data-from-baidu", date, hotDataJson)
		}catch (e:Exception){
			e.printStackTrace()
		}
	}
	private fun openUrlConnection(uri:String):URLConnection{
		val url = URL(uri)
		val connection = url.openConnection()
		connection.setRequestProperty("Cookies", "BAIDUID=AACE60F05F29C2184CEC0C3522FA5401:FG=1; BIDUPSID=AACE60F05F29C2187060314D0FFE23C2; H_PS_PSSID=36560_38643_38831_39027_39023_38942_39007_38958_38954_39009_39039_38918_38809_38636_26350_39042_39044; PSTM=1689068380; BDSVRTM=23; BD_HOME=1; BD_NOT_HTTPS=1")
		connection.setRequestProperty("User-Agent", "PostmanRuntime/7.32.3")
		connection.setRequestProperty("Accept", "*/*")
		connection.setRequestProperty("Accept", "*/*")
		//openConnection.setRequestProperty("Connection", "keep-alive")
		connection.setRequestProperty("Connection", "close")
		return connection
	}
}