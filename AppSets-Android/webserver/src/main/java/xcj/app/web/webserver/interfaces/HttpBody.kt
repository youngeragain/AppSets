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
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpBody(val type: Int = TYPE_RAW_JSON, val required: Boolean = true) {
    companion object {
        const val TYPE_FORM_DATA = 0
        const val TYPE_X_WWW_FORM_URLENCODED = 1
        const val TYPE_RAW_TEXT = 2
        const val TYPE_RAW_JAVASCRIPT = 3
        const val TYPE_RAW_JSON = 4
        const val TYPE_RAW_HTML = 5
        const val TYPE_RAW_XML = 6
        const val TYPE_BINARY = 7
        const val TYPE_GRAPHQL = 8
        const val TYPE_FILE = 9
    }
}