package xcj.app.userinfo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@Component
class ApiDesignContext: ApplicationContextAware {
    lateinit var appContext: ApplicationContext
    private final val coroutineContext:CoroutineContext = EmptyCoroutineContext + Job()
    val coroutineScope: CoroutineScope = CoroutineScope(coroutineContext)

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        appContext = applicationContext
    }

}