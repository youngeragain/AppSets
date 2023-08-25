package xcj.app.web.webserver.interfaces

/**
 * @sample https://localhost/example?name=hello
 * name is HttpQueryParam
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpQueryParam(val name:String, val required:Boolean = false)