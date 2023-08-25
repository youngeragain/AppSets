package xcj.app.userinfo.config

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import xcj.app.CoreLogger


//@EnableScheduling
@Component
class AutoScheduleByAnnotation {
    //@Scheduled(cron = "*/6 * * * * ?")
    private fun onTaskDoing() {
        CoreLogger.d("blue", "AutoScheduleByAnnotation::onTaskDoing")
    }
}

