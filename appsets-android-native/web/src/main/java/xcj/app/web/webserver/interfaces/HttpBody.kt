package xcj.app.web.webserver.interfaces

/**
 * @param type
 * 0:FORM-DATA
 * 1:X-WWW-FORM-URLENCODED
 * 2:RAW.TEXT
 * 3.RAW.JAVASCRIPT
 * 4.RAW.JSON
 * 5.RAW.HTML
 * 6.RAW.XML
 * 7.BINARY
 * 8.GRAPHQL
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpBody(val required:Boolean = false, val type:Int=4)