package xcj.app.web.webserver.interfaces

enum class HttpMethod() {
    HEAD, GET, POST, PUT, DELETE;

    fun readableName(): String {
        return when (this) {
            HEAD -> return "HEAD"
            GET -> return "GET"
            POST -> return "POST"
            PUT -> return "PUT"
            DELETE -> return "DELETE"
        }
    }
}