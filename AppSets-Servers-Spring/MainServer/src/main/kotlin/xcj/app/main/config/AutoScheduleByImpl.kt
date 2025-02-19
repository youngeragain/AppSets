package xcj.app.main.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.CronTask
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.support.CronExpression
import xcj.app.main.dao.mongo.CronInfoDao
import xcj.app.main.external.ExternalDataFetcherService
import xcj.app.main.service.ExternalDataFetcher
import xcj.app.main.util.TokenHelper
import xcj.app.util.PurpleLogger


@Configuration
class AutoScheduleByImpl(
    private val cronInfoDao: CronInfoDao,
    private val tokenHelper: TokenHelper,
    private val externalDataFetcher: ExternalDataFetcher,
) : SchedulingConfigurer {

    companion object {
        private const val TAG = "AutoScheduleByImpl"
    }

    override fun configureTasks(scheduledTaskRegistrar: ScheduledTaskRegistrar) {
        addMonitorAutoTask(scheduledTaskRegistrar)
    }

    private fun addMonitorAutoTask(scheduledTaskRegistrar: ScheduledTaskRegistrar) {
        val threadPoolTaskScheduler = ThreadPoolTaskScheduler()
        threadPoolTaskScheduler.poolSize = 3
        threadPoolTaskScheduler.initialize()
        scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler)

        val redisTokenScanCronText = cronInfoDao.getRedisTokenScanCronInfo()?.cronText
        if (redisTokenScanCronText.isNullOrEmpty() || !CronExpression.isValidExpression(redisTokenScanCronText)) {
            PurpleLogger.current.d(
                TAG,
                "addMonitorAutoTask: redisTokenScanCronText is not correct! $redisTokenScanCronText"
            )
        } else {
            val redisTokenMonitorTask = CronTask(::onRedisTokenAutoTasking, redisTokenScanCronText)
            scheduledTaskRegistrar.addCronTask(redisTokenMonitorTask)
        }

        val baiduHotDataFetchCronText = cronInfoDao.getBaiduHotDataFetchCronInfo()?.cronText
        if (baiduHotDataFetchCronText.isNullOrEmpty() || !CronExpression.isValidExpression(baiduHotDataFetchCronText)) {
            PurpleLogger.current.d(
                TAG,
                "addMonitorAutoTask: baiduHotDataFetchCronText is not correct! $baiduHotDataFetchCronText"
            )
        } else {
            val baiduHotDataFetchTask = CronTask(::onFetchBaiduHotDataAutoTasking, baiduHotDataFetchCronText)
            scheduledTaskRegistrar.addCronTask(baiduHotDataFetchTask)
        }


        val bingWallpaperFetchCronText = cronInfoDao.getBingWallpaperFetchCronInfo()?.cronText
        if (bingWallpaperFetchCronText.isNullOrEmpty() || !CronExpression.isValidExpression(bingWallpaperFetchCronText)) {
            PurpleLogger.current.d(
                TAG,
                "addMonitorAutoTask: bingWallpaperFetchCronText is not correct! $bingWallpaperFetchCronText"
            )
            return
        } else {
            val bingWallpaperFetchTask = CronTask(::onFetchBingWallpaperAutoTasking, bingWallpaperFetchCronText)
            scheduledTaskRegistrar.addCronTask(bingWallpaperFetchTask)
        }

    }

    fun updateRedisTokenMonitorAutoTaskDirect(newCronStr: String) {
        if (!CronExpression.isValidExpression(newCronStr)) {
            PurpleLogger.current.d(
                TAG,
                "addRedisTokenMonitorAutoTask: redisTokenCronText is not correct!"
            )
            return
        }
    }

    private fun onRedisTokenAutoTasking() {
        PurpleLogger.current.d(
            TAG,
            "onRedisTokenAutoTasking:${System.currentTimeMillis()}"
        )
        tokenHelper.updateAllTokenInRedis()
    }

    private fun onFetchBaiduHotDataAutoTasking() {
        PurpleLogger.current.d(
            TAG,
            "onFetchBaiduHotDataAutoTasking:${System.currentTimeMillis()}"
        )
        externalDataFetcher.doFetch(ExternalDataFetcherService.FETCH_WHAT_BAIDU_HOT_DATA)
    }

    private fun onFetchBingWallpaperAutoTasking() {
        PurpleLogger.current.d(
            TAG,
            "onFetchBingWallpaperAutoTasking:${System.currentTimeMillis()}"
        )
        externalDataFetcher.doFetch(ExternalDataFetcherService.FETCH_WHAT_MS_BING_WALLPAPER)
    }
}