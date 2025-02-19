package xcj.app.web.webserver.interfaces

/**
 * 标记参数需要android框架的context
 * 0->application context
 * 1->activity context
 * 2->service context
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AndroidContext(val type: Int = TYPE_ACTIVITY) {
    companion object {
        const val TYPE_APPLICATION = 0
        const val TYPE_ACTIVITY = 1
        const val TYPE_SERVICE = 2
        const val TYPE_PROVIDER = 3
    }
}