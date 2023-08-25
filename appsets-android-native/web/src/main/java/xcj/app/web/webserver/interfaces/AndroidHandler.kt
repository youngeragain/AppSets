package xcj.app.web.webserver.interfaces

/**
 * 标记参数需要android框架的handler
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class AndroidHandler