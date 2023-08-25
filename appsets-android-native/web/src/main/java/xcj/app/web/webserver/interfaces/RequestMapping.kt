package xcj.app.web.webserver.interfaces

import org.eclipse.jetty.http.HttpMethod

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestMapping(val path:String, val httpMethod:Array<HttpMethod> = [])