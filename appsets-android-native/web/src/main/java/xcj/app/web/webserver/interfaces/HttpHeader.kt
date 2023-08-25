package xcj.app.web.webserver.interfaces

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpHeader(val name:String, val required:Boolean = false)












