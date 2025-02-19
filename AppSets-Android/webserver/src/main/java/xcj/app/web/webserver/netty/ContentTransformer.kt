package xcj.app.web.webserver.netty

interface ContentTransformer {
    fun <T> fromString(jsonString: String, type: Class<T>): T
    fun toString(any: Any?): String
}