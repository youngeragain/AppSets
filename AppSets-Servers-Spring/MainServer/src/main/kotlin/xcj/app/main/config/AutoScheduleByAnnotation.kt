package xcj.app.main.config

import org.springframework.stereotype.Component
import xcj.app.util.PurpleLogger

//@EnableScheduling
@Component
class AutoScheduleByAnnotation {
    companion object{
        private const val TAG = "AutoScheduleByAnnotation"
    }
    //@Scheduled(cron = "*/6 * * * * ?")
    private fun onTaskDoing() {
        PurpleLogger.current.d(TAG, "onTaskDoing")
    }
}

