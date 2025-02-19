package xcj.app.web.webserver.interfaces

/**
 * 请求信息中的信息
 */
@HttpBody(HttpBody.TYPE_RAW_JSON)
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequestInfo(val what: Int) {
    companion object {
        const val WHAT_REQUEST_NOTING = -1
        const val WHAT_REQUEST_HOST = 0
        const val WHAT_REQUEST_REMOTE_HOST = 1
        const val WHAT_REQUEST_O = 0
    }
}
