package xcj.app.userinfo.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.CronTask
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.support.CronExpression
import xcj.app.CoreLogger
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.dao.mongo.CronInfoDao
import xcj.app.userinfo.qr.ExternalDataFetcher


@Configuration
class AutoScheduleByImpl(
    private val cronInfoDao: CronInfoDao,
    private val tokenHelper: TokenHelper,
    private val externalDataFetcher: ExternalDataFetcher,
    ) : SchedulingConfigurer {

    override fun configureTasks(scheduledTaskRegistrar: ScheduledTaskRegistrar) {

        addMonitorAutoTask(scheduledTaskRegistrar)
    }
    private fun addMonitorAutoTask(scheduledTaskRegistrar: ScheduledTaskRegistrar) {
        val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
        threadPoolTaskScheduler.poolSize = 3
        threadPoolTaskScheduler.initialize()
        scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler)

        val redisTokenScanCronText = cronInfoDao.getRedisTokenScanCronInfo()?.cronText
        if(redisTokenScanCronText.isNullOrEmpty()||!CronExpression.isValidExpression(redisTokenScanCronText)){
            CoreLogger.d("AutoScheduleByImpl",
                "addMonitorAutoTask: redisTokenScanCronText is not correct! ${redisTokenScanCronText}")
        }else{
            val redisTokenMonitorTask = CronTask(::onRedisTokenAutoTasking, redisTokenScanCronText)
            scheduledTaskRegistrar.addCronTask(redisTokenMonitorTask)
        }

        val baiduHotDataFetchCronText = cronInfoDao.getBaiduHotDataFetchCronInfo()?.cronText
        if(baiduHotDataFetchCronText.isNullOrEmpty()||!CronExpression.isValidExpression(baiduHotDataFetchCronText)){
            CoreLogger.d("AutoScheduleByImpl",
                "addMonitorAutoTask: baiduHotDataFetchCronText is not correct! ${baiduHotDataFetchCronText}")
        }else{
            val baiduHotDataFetchTask = CronTask(::onFetchBaiduHotDataAutoTasking, baiduHotDataFetchCronText)
            scheduledTaskRegistrar.addCronTask(baiduHotDataFetchTask)
        }


        val bingWallpaperFetchCronText = cronInfoDao.getBingWallpaperFetchCronInfo()?.cronText
        if(bingWallpaperFetchCronText.isNullOrEmpty()||!CronExpression.isValidExpression(bingWallpaperFetchCronText)){
            CoreLogger.d("AutoScheduleByImpl",
                "addMonitorAutoTask: bingWallpaperFetchCronText is not correct! ${bingWallpaperFetchCronText}")
            return
        }else{
            val bingWallpaperFetchTask = CronTask(::onFetchBingWallpaperAutoTasking, bingWallpaperFetchCronText)
            scheduledTaskRegistrar.addCronTask(bingWallpaperFetchTask)
        }

    }

    fun updateRedisTokenMonitorAutoTaskDirect(newCronStr:String){
        if(!CronExpression.isValidExpression(newCronStr)){
            CoreLogger.d("AutoScheduleByImpl",
                "addRedisTokenMonitorAutoTask: redisTokenCronText is not correct!")
            return
        }
    }

    private fun onRedisTokenAutoTasking() {
        CoreLogger.d("AutoScheduleByImpl",
            "onRedisTokenAutoTasking:${System.currentTimeMillis()}")
        tokenHelper.updateAllTokenInRedis()
    }
    private fun onFetchBaiduHotDataAutoTasking(){
        CoreLogger.d("AutoScheduleByImpl",
            "onFetchBaiduHotDataAutoTasking:${System.currentTimeMillis()}")
        externalDataFetcher.doFetch("baidu_hot_data")
    }

    private fun onFetchBingWallpaperAutoTasking(){
        CoreLogger.d("AutoScheduleByImpl",
            "onFetchBingWallpaperAutoTasking:${System.currentTimeMillis()}")
        externalDataFetcher.doFetch("bing_wallpaper_data")
    }
}