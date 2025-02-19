package xcj.app.web.webserver.interfaces

/**
 * 专注JSON body
 */
@HttpBody(HttpBody.TYPE_RAW_JSON)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestBody
