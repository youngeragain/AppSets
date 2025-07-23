package xcj.app.web.webserver.interfaces

enum class HttpMethod() {
    HEAD, GET, POST, PUT, DELETE;

    fun readableName(): String {
        return when (this) {
            HEAD -> "HEAD"
            GET -> "GET"
            POST -> "POST"
            PUT -> "PUT"
            DELETE -> "DELETE"
        }
    }
}