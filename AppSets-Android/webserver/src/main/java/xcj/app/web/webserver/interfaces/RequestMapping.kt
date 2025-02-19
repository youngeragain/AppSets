package xcj.app.web.webserver.interfaces

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestMapping(val path: String, val method: Array<HttpMethod> = [])