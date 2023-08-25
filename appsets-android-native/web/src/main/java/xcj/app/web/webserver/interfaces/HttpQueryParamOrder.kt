package xcj.app.web.webserver.interfaces

/**
 * @sample https://localhost/example?name=hello&name=world
 * order=0 is hello
 * order=1 is world
 * @see HttpQueryParam
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpQueryParamOrder(val order:Int = 0)